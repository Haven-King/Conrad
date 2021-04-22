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

package dev.inkwell.conrad.impl.gui;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import dev.inkwell.conrad.api.gui.EntryBuilderRegistry;
import dev.inkwell.conrad.api.gui.ValueWidgetFactory;
import dev.inkwell.conrad.api.value.*;
import dev.inkwell.conrad.api.value.data.DataType;
import dev.inkwell.conrad.api.value.data.SaveType;
import dev.inkwell.conrad.api.value.lang.Translator;
import dev.inkwell.conrad.api.value.util.ListView;
import dev.inkwell.conrad.impl.Conrad;
import dev.inkwell.vivian.api.builders.CategoryBuilder;
import dev.inkwell.vivian.api.builders.ConfigScreenBuilderImpl;
import dev.inkwell.vivian.api.screen.ScreenStyle;
import dev.inkwell.vivian.api.util.Alignment;
import dev.inkwell.vivian.api.widgets.LabelComponent;
import dev.inkwell.vivian.api.widgets.TextButton;
import dev.inkwell.vivian.api.widgets.WidgetComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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

            FACTORIES.put(modId, builder::build);

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

                makeScreenBuilder(config, category, deque, 0, null);
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

    private static void makeScreenBuilder(ConfigDefinition<?> config, CategoryBuilder category, Deque<ValueKey<?>> values, int level, @Nullable String sectionName) {
        category.setSaveCallback(() -> Conrad.syncAndSave(config));

        String currentSectionName = sectionName;

        while (!values.isEmpty()) {
            ValueKey<?> value = values.pop();

            String parent = parent(value);

            if (value.getPath().length <= level || sectionName != null && !parent.startsWith(sectionName)) {
                values.push(value);
                return;
            } else if (currentSectionName == null && value.getPath().length == level + 1) {
                currentSectionName = parent;
            } else if (value.getPath().length == level + 2 && (currentSectionName == null || !currentSectionName.equals(parent))) {
                category.addSection(new TranslatableText(parent),
                        Translator.getComments(parent).stream().map(LiteralText::new).collect(Collectors.toList())
                );

                currentSectionName = parent;
            } else if (value.getPath().length > level + 2) {
                if (currentSectionName == null) {
                    currentSectionName = parent;
                }

                ConfigScreenBuilderImpl builder = new ConfigScreenBuilderImpl();
                CategoryBuilder innerCategory = builder.startCategory(new TranslatableText(parent));

                addEntry(category, value);

                makeScreenBuilder(config, innerCategory, values, level + 2, parent);

                builder.setStyle(builder.getStyle());

                List<Text> tooltips = Translator.getComments(parent).stream().map(LiteralText::new).collect(Collectors.toList());

                category.add((screen, x, y, width, consumer) -> {
                    consumer.accept(new LabelComponent(screen, x, y, width - 20, 20, new TranslatableText(parent)).withTooltips(tooltips));
                    consumer.accept(new TextButton(screen, x + width - 20, y, 20, 20, 0, new LiteralText("▶"), Alignment.CENTER, button -> {
                        MinecraftClient.getInstance().openScreen(builder.build(button.parent));
                        return true;
                    }).withTooltips(tooltips));

                    return 20;
                });

                continue;
            }

            addEntry(category, value);
        }

    }

    private static <T> void addEntry(CategoryBuilder category, ValueKey<T> configValue) {
        ConfigDefinition<?> configDefinition = configValue.getConfig();

        List<Text> comments = new ArrayList<>();
        ConfigManager.getComments(configValue).forEach(string -> comments.add(string.equals("") ? LiteralText.EMPTY : new LiteralText(string)));

        category.add((parent, x, y, width, consumer) -> {
            int componentWidth = width / 2;

            ValueWidgetFactory<T> factory = EntryBuilderRegistry.get(configValue);
            ValueContainer container = ValueContainerProvider.getInstance(configDefinition.getSaveType()).getValueContainer(configDefinition.getSaveType());

            WidgetComponent widget = factory.build(parent, x + componentWidth, y, componentWidth, new TranslatableText(configValue.toString()), configValue.getConfig(), configValue.getConstraints(), configValue,
                    configValue::getDefaultValue, t -> {
                    },
                    v -> configValue.setValue(v, container),
                    configValue.getValue(container));

            widget.setX(x + width - widget.getWidth());

            widget.setTooltipRegion(x, y, x + width, y + widget.getHeight());

            widget.addTooltips(comments);

            WidgetComponent label = new LabelComponent(parent, x, y, componentWidth, 20, new TranslatableText(configValue.toString()));

            consumer.accept(widget);
            consumer.accept(label);

            return widget.getHeight();
        });
    }

    public static void forEach(BiConsumer<String, Function<Screen, ? extends Screen>> consumer) {
        FACTORIES.forEach(consumer);
    }

    public static Screen get(String modId, Screen parent) {
        return FACTORIES.get(modId).apply(parent);
    }

    public static Iterator<Map.Entry<String, Function<Screen, ? extends Screen>>> getFactories() {
        return FACTORIES.entrySet().iterator();
    }
}
