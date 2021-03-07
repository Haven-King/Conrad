/*
 * Copyright 2021 Haven King
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.inkwell.conrad.impl;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.inkwell.conrad.api.EntryBuilderRegistry;
import dev.inkwell.oliver.api.ConfigDefinition;
import dev.inkwell.oliver.api.ConfigManager;
import dev.inkwell.oliver.api.data.DataType;
import dev.inkwell.oliver.api.data.SaveType;
import dev.inkwell.oliver.api.lang.Translator;
import dev.inkwell.oliver.api.util.ListView;
import dev.inkwell.oliver.api.value.ValueContainer;
import dev.inkwell.oliver.api.value.ValueContainerProvider;
import dev.inkwell.oliver.api.value.ValueKey;
import dev.inkwell.vivian.api.builders.CategoryBuilder;
import dev.inkwell.vivian.api.builders.ConfigScreenBuilderImpl;
import dev.inkwell.vivian.api.builders.SectionBuilder;
import dev.inkwell.vivian.api.builders.WidgetComponentFactory;
import dev.inkwell.vivian.api.screen.ConfigScreen;
import dev.inkwell.vivian.api.screen.ScreenStyle;
import dev.inkwell.vivian.api.util.Alignment;
import dev.inkwell.vivian.api.widgets.LabelComponent;
import dev.inkwell.vivian.api.widgets.TextButton;
import dev.inkwell.vivian.api.widgets.WidgetComponent;
import dev.inkwell.vivian.api.widgets.containers.RowContainer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class ConfigScreenProvider implements ModMenuApi {
    private static final Multimap<String, ListView<ValueKey<?>>> CONFIGS = LinkedHashMultimap.create();

    private final Map<String, ConfigScreenFactory<?>> factories = new HashMap<>();

    public ConfigScreenProvider() {
        for (String modId : CONFIGS.keySet()) {
            ConfigScreenBuilderImpl builder = new ConfigScreenBuilderImpl();
            ScreenStyle screenStyle = ScreenStyle.DEFAULT;
            this.factories.put(modId, parent -> new ConfigScreen(parent, builder));

            for (ListView<ValueKey<?>> values : CONFIGS.get(modId)) {
                ConfigDefinition<?> config = values.get(0).getConfig();

                for (ScreenStyle style : config.getData(DataType.SCREEN_STYLE)) {
                    if (style != ScreenStyle.DEFAULT && screenStyle == ScreenStyle.DEFAULT) {
                        screenStyle = style;
                    }
                }

                String name = config.toString();
                CategoryBuilder category = builder.startCategory(new TranslatableText(name));

                category.setCondition(() -> {
                    PlayerEntity player;
                    return config.getSaveType() == SaveType.USER
                            || ((player = MinecraftClient.getInstance().player) == null)
                            || player.hasPermissionLevel(4);
                });

                config.getData(DataType.COMMENT).forEach(string -> category.add(new LiteralText(string)));

                Deque<ValueKey<?>> deque = new ArrayDeque<>();
                values.forEach(deque::add);

                this.makeScreenBuilder(config, category, deque, 0, null, null);
            }

            builder.setStyle(screenStyle);
        }
    }

    public static void register(ListView<ValueKey<?>> values) {
        CONFIGS.put(values.get(0).getConfig().getNamespace(), values);
    }

    private static String parent(ValueKey<?> valueKey) {
        StringBuilder builder = new StringBuilder(valueKey.getConfig().toString());

        String[] path = valueKey.getPath();
        for (int i = 0; i < path.length - 1; ++i) {
            builder.append('/').append(path[i]);
        }

        return builder.toString();
    }

    private void makeScreenBuilder(ConfigDefinition<?> config, CategoryBuilder category, Deque<ValueKey<?>> values, int level, @Nullable SectionBuilder section, @Nullable String sectionName) {
        category.setSaveCallback(() -> Conrad.syncAndSave(config));


        String currentSectionName = sectionName;

        while (!values.isEmpty()) {
            ValueKey<?> value = values.pop();

            String parent = parent(value);

            if (value.getPath().length <= level || sectionName != null && !parent.startsWith(sectionName)) {
                values.push(value);
                return;
            } else if (section == null && value.getPath().length == level + 1) {
                section = category.addSection(new LiteralText(""));
                currentSectionName = parent;
            } else if (value.getPath().length == level + 2 && (currentSectionName == null || !currentSectionName.equals(parent))) {
                section = category.addSection(new TranslatableText(parent));
                @Nullable SectionBuilder finalSection = section;

                if (section != null) {
                    Translator.getComments(parent).forEach(string -> finalSection.add(new LiteralText(string)));
                }

                currentSectionName = parent;
            } else if (value.getPath().length > level + 2) {
                if (section == null) {
                    section = category.addSection(new LiteralText(""));
                    currentSectionName = parent;
                }

                ConfigScreenBuilderImpl builder = new ConfigScreenBuilderImpl();
                CategoryBuilder innerCategory = builder.startCategory(new TranslatableText(parent));

                SectionBuilder innerSection = innerCategory.addSection(new TranslatableText(" "));

                addEntry(innerSection, value);

                makeScreenBuilder(config, innerCategory, values, level + 2, innerSection, parent);

                section.add((screen, width, x, y, index) -> {
                    WidgetComponent label = new LabelComponent(screen, 0, 0, width / 2, (int) (30 * screen.getScale()), new TranslatableText(parent), true);
                    WidgetComponent widget = new TextButton(screen, 0, 0, width / 2, (int) (30 * screen.getScale()), 0, new LiteralText("▶"), Alignment.RIGHT, button -> {
                        MinecraftClient.getInstance().openScreen(new ConfigScreen(button.getParent(), builder));
                        return true;
                    });
                    WidgetComponent component = new RowContainer(screen, x, y, index, true, label, widget);
                    component.addTooltips(Translator.getComments(parent).stream().map(LiteralText::new).collect(Collectors.toList()));
                    return component;
                });

                continue;
            }

            if (section != null) {
                addEntry(section, value);
            }
        }

    }

    private <T> void addEntry(SectionBuilder section, ValueKey<T> configValue) {
        section.add((parent, width, x, y, index) -> {
            WidgetComponent label = new LabelComponent(parent, 0, 0, width / 2, (int) (30 * parent.getScale()), new TranslatableText(configValue.toString()), true);

            WidgetComponentFactory<T> factory = EntryBuilderRegistry.get(configValue);
            ValueContainer container = ValueContainerProvider.getInstance(configValue.getConfig().getSaveType()).getValueContainer();
            WidgetComponent widget = factory.build(parent, x, y, width / 2, (int) (30 * parent.getScale()),
                    configValue::getDefaultValue, t -> {
                    },
                    v -> configValue.setValue(v, container),
                    configValue.getValue(container));

            WidgetComponent component = new RowContainer(parent, x, y, index, true, label, widget);

            Collection<Text> comments = new ArrayList<>();
            ConfigManager.getComments(configValue).forEach(string -> comments.add(new LiteralText(string)));
            component.addTooltips(comments);

            return component;
        });
    }

    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        return factories;
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return factories.get("conrad");
    }
}
