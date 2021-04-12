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
import dev.inkwell.conrad.api.EntryBuilderRegistry;
import dev.inkwell.conrad.api.gui.builders.CategoryBuilder;
import dev.inkwell.conrad.api.gui.builders.ConfigScreenBuilderImpl;
import dev.inkwell.conrad.api.gui.builders.SectionBuilder;
import dev.inkwell.conrad.api.gui.builders.WidgetComponentFactory;
import dev.inkwell.conrad.api.gui.screen.ConfigScreen;
import dev.inkwell.conrad.api.gui.screen.ScreenStyle;
import dev.inkwell.conrad.api.gui.util.Alignment;
import dev.inkwell.conrad.api.gui.widgets.LabelComponent;
import dev.inkwell.conrad.api.gui.widgets.TextButton;
import dev.inkwell.conrad.api.gui.widgets.WidgetComponent;
import dev.inkwell.conrad.api.gui.widgets.containers.RowContainer;
import dev.inkwell.conrad.api.value.*;
import dev.inkwell.conrad.api.value.data.DataType;
import dev.inkwell.conrad.api.value.data.SaveType;
import dev.inkwell.conrad.api.value.lang.Translator;
import dev.inkwell.conrad.api.value.util.ListView;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public final class ConfigScreenProviderImpl {
    private static final Multimap<String, ListView<ValueKey<?>>> CONFIGS = LinkedHashMultimap.create();
    private static final Map<String, Function<Screen, ? extends Screen>> FACTORIES = new TreeMap<>();

    private ConfigScreenProviderImpl() {

    }

    public static void init() {
        for (String modId : CONFIGS.keySet()) {
            ConfigScreenBuilderImpl builder = new ConfigScreenBuilderImpl();
            Text title = FabricLoader.getInstance().getModContainer(modId)
                    .map(container -> (Text) new LiteralText(container.getMetadata().getName()))
                    .orElse(new TranslatableText("conrad.title." + modId));

            FACTORIES.put(modId, parent -> new ConfigScreen(parent, builder, title));

            for (ListView<ValueKey<?>> values : CONFIGS.get(modId)) {
                ConfigDefinition<?> config = values.get(0).getConfig();

                for (ScreenStyle style : config.getData(DataType.SCREEN_STYLE)) {
                    if (style != ScreenStyle.DEFAULT && builder.getStyle() == ScreenStyle.DEFAULT) {
                        builder.setStyle(style);
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

                if (!category.getTooltips().isEmpty()) {
                    category.addTooltip(LiteralText.EMPTY);
                }

                category.addTooltip(new TranslatableText("conrad.tooltip.save_type", config.getSaveType()));

                Deque<ValueKey<?>> deque = new ArrayDeque<>();
                values.forEach(deque::add);

                makeScreenBuilder(config, category, deque, 0, null, null, builder.getStyle());
            }
        }
    }

    public static void register(String modId, ListView<ValueKey<?>> values) {
        CONFIGS.put(modId, values);
    }

    private static String parent(ValueKey<?> valueKey) {
        StringBuilder builder = new StringBuilder(valueKey.getConfig().toString());

        String[] path = valueKey.getPath();
        for (int i = 0; i < path.length - 1; ++i) {
            builder.append('/').append(path[i]);
        }

        return builder.toString();
    }

    private static void makeScreenBuilder(ConfigDefinition<?> config, CategoryBuilder category, Deque<ValueKey<?>> values, int level, @Nullable SectionBuilder section, @Nullable String sectionName, ScreenStyle style) {
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

                makeScreenBuilder(config, innerCategory, values, level + 2, innerSection, parent, builder.getStyle());

                SectionBuilder finalSection = section;

                builder.setStyle(builder.getStyle());

                section.add((screen, width, x, y, index) -> {
                    WidgetComponent label = new LabelComponent(screen, 0, 0, width - 20, 20, new TranslatableText(parent));
                    WidgetComponent widget = new TextButton(screen, 0, 0, 20, 20, 0, new LiteralText("▶"), Alignment.CENTER, button -> {
                        MinecraftClient.getInstance().openScreen(new ConfigScreen(button.getParent(), builder, finalSection.getName()));
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

    private static <T> void addEntry(SectionBuilder section, ValueKey<T> configValue) {
        ConfigDefinition<?> configDefinition = configValue.getConfig();

        section.add((parent, width, x, y, index) -> {
            int componentWidth = width / 2;

            WidgetComponentFactory<T> factory = EntryBuilderRegistry.get(configValue);
            ValueContainer container = ValueContainerProvider.getInstance(configDefinition.getSaveType()).getValueContainer(configDefinition.getSaveType());
            WidgetComponent widget = factory.build(new TranslatableText(configValue.toString()), configValue.getConfig(), configValue.getConstraints(), configValue, parent, x, y, componentWidth, 20,
                    configValue::getDefaultValue, t -> {
                    },
                    v -> configValue.setValue(v, container),
                    configValue.getValue(container));

            WidgetComponent label = new LabelComponent(parent, 0, 0, componentWidth + (widget.isFixedSize() ? componentWidth - widget.getWidth() : 0), 20, new TranslatableText(configValue.toString()));

            WidgetComponent component = new RowContainer(parent, x, y, index, true, label, widget);

            List<Text> comments = new ArrayList<>();
            ConfigManager.getComments(configValue).forEach(string -> comments.add(new LiteralText(string)));
            component.addTooltips(comments);

            return component;
        });
    }

    public static void forEach(BiConsumer<String, Function<Screen, ? extends Screen>> consumer) {
        FACTORIES.forEach(consumer);
    }

    public static Screen get(String modId, Screen parent) {
        return FACTORIES.get(modId).apply(parent);
    }

    public static Iterator<Map.Entry<String, Function<Screen,? extends Screen>>> getFactories() {
        return FACTORIES.entrySet().iterator();
    }
}
