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

package dev.inkwell.conrad.api.gui;

import dev.inkwell.conrad.api.value.ConfigDefinition;
import dev.inkwell.conrad.api.value.ValueKey;
import dev.inkwell.conrad.api.value.data.Bounds;
import dev.inkwell.conrad.api.value.data.Constraint;
import dev.inkwell.conrad.api.value.data.DataType;
import dev.inkwell.conrad.api.value.lang.Translator;
import dev.inkwell.conrad.api.value.util.Array;
import dev.inkwell.conrad.api.value.util.ListView;
import dev.inkwell.conrad.api.value.util.Table;
import dev.inkwell.conrad.impl.Conrad;
import dev.inkwell.conrad.impl.data.DataObject;
import dev.inkwell.conrad.impl.exceptions.ConfigValueException;
import dev.inkwell.conrad.impl.util.ReflectionUtil;
import dev.inkwell.vivian.api.screen.ConfigScreen;
import dev.inkwell.vivian.api.util.Alignment;
import dev.inkwell.vivian.api.widgets.WidgetComponent;
import dev.inkwell.vivian.api.widgets.compound.ArrayWidget;
import dev.inkwell.vivian.api.widgets.compound.TableWidget;
import dev.inkwell.vivian.api.widgets.value.*;
import dev.inkwell.vivian.api.widgets.value.entry.*;
import dev.inkwell.vivian.api.widgets.value.slider.DoubleSliderWidget;
import dev.inkwell.vivian.api.widgets.value.slider.FloatSliderWidget;
import dev.inkwell.vivian.api.widgets.value.slider.IntegerSliderWidget;
import dev.inkwell.vivian.api.widgets.value.slider.LongSliderWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EntryBuilderRegistry {
    private static final Map<Class<?>, ValueWidgetFactory<?>> DEFAULT_FACTORIES = new HashMap<>();

    static {
        registerDefaults();
    }

    public static <T> void register(Class<T> clazz, ValueWidgetFactory<T> builder) {
        for (Class<?> clazz2 : ReflectionUtil.getClasses(clazz)) {
            DEFAULT_FACTORIES.putIfAbsent(clazz2, builder);
        }
    }

    private static <T extends Number & Comparable<T>> void registerBounded(Class<T> clazz, BoundedWidgetFactory<T> bounded, UnboundedWidgetFactory<T> unBounded) {
        register(clazz, (screen, x, y, width, name, config, constraints, data, defaultValueSupplier, changedListener, saveConsumer, value) -> {
            Bounds<T> bounds = null;

            for (Constraint<T> constraint : constraints) {
                if (constraint instanceof Bounds) {
                    bounds = (Bounds<T>) constraint;
                }
            }

            if (bounds == null || bounds.getMin().equals(bounds.getAbsoluteMin()) || bounds.getMax().equals(bounds.getAbsoluteMax())) {
                ValueWidgetComponent<T> widget = unBounded.build(screen, x, y, width, 20, Alignment.RIGHT, defaultValueSupplier, changedListener, saveConsumer, value);

                widget.addConstraints(constraints);

                return widget;
            } else {
                T min = bounds.getMin();
                T max = bounds.getMax();

                return bounded.build(screen, x, y, width, 20, defaultValueSupplier, changedListener, saveConsumer, value, min, max);
            }
        });
    }

    public static <T> void override(Class<T> clazz, ValueWidgetFactory<T> builder) {
        DEFAULT_FACTORIES.put(clazz, builder);
    }

    @SuppressWarnings("unchecked")
    public static <T> ValueWidgetFactory<T> get(ValueKey<T> configValue) throws ConfigValueException {
        ListView<ValueWidgetFactory<?>> widgetBuilders = configValue.getData(DataType.WIDGET);

        if (!widgetBuilders.isEmpty()) {
            return (ValueWidgetFactory<T>) widgetBuilders.get(0);
        }

        return (ValueWidgetFactory<T>) get(configValue.getDefaultValue().getClass(), t -> Conrad.syncAndSave(configValue.getConfig()));
    }

    @SuppressWarnings("unchecked")
    public static <T> ValueWidgetFactory<T> get(Class<T> type, Consumer<T> outerSaveConsumer) {
        if (DEFAULT_FACTORIES.containsKey(type)) {
            return (ValueWidgetFactory<T>) DEFAULT_FACTORIES.get(type);
        }

        if (type.isEnum()) {
            return (ValueWidgetFactory<T>) enumFactory(type);
        }

        if (isSimpleCompoundData(type)) {
            return new DataClassValueWidgetFactory<>(type, outerSaveConsumer);
        }

        throw new ConfigValueException("Widget builder not registered for class '" + type.getName() + "' or provided for class '" + type.getName() + "'");

    }

    private static boolean isSimpleCompoundData(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            Class<?> fieldType = field.getType();

            if (!fieldType.isEnum() && !isSimpleCompoundData(fieldType) && !DEFAULT_FACTORIES.containsKey(fieldType)) {
                return false;
            }
        }

        return true;

    }

    @SuppressWarnings("unchecked")
    private static <T extends Enum<T>> ValueWidgetFactory<T> enumFactory(Class<?> clazz) {
        T[] enums = (T[]) clazz.getEnumConstants();

        if (enums.length <= 3) {
            return ((screen, x, y, width, name, config, constraints, data, defaultValueSupplier, changedListener, saveConsumer, value) ->
                    new EnumSelectorComponent<>(screen, x, y, width, 20, defaultValueSupplier, changedListener, saveConsumer, value));
        } else {
            return ((screen, x, y, width, name, config, constraints, data, defaultValueSupplier, changedListener, saveConsumer, value) ->
                    new EnumDropdownWidget<>(screen, x, y, width, 20, defaultValueSupplier, changedListener, saveConsumer, value));
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void registerDefaults() {
        registerBounded(Integer.class, IntegerSliderWidget::new, IntegerEntryWidget::new);
        registerBounded(Long.class, LongSliderWidget::new, LongEntryWidget::new);
        registerBounded(Float.class, FloatSliderWidget::new, FloatEntryWidget::new);
        registerBounded(Double.class, DoubleSliderWidget::new, DoubleEntryWidget::new);

        register(String.class, (ConfigScreen screen, int x, int y, int width, Text name, ConfigDefinition<?> config, ListView<Constraint<String>> constraints, DataObject data, Supplier<@NotNull String> defaultValueSupplier, Consumer<String> changedListener, Consumer<String> saveConsumer, @NotNull String value) -> {
            StringEntryWidget component = new StringEntryWidget(screen, x, y, width, 20, Alignment.RIGHT, defaultValueSupplier, changedListener, saveConsumer, value);

            component.setTextPredicate(string -> {
                for (Constraint<String> constraint : constraints) {
                    if (!constraint.passes(value)) {
                        return false;
                    }
                }

                return true;
            });

            return component;
        });

        register(Boolean.class, (ConfigScreen screen, int x, int y, int width, Text name, ConfigDefinition<?> config, ListView<Constraint<Boolean>> constraints, DataObject data, Supplier<@NotNull Boolean> defaultValueSupplier, Consumer<Boolean> changedListener, Consumer<Boolean> saveConsumer, @NotNull Boolean value) ->
                new ToggleComponent(screen, x, y, width, 20, defaultValueSupplier, changedListener, saveConsumer, value, bl -> {
                    String translationKey = (name instanceof TranslatableText ? ((TranslatableText) name).getKey() : name.asString()) + ".value." + (bl ? "true" : "false");

                    if (Translator.translate(translationKey) == null) {
                        translationKey = config.toString() + ".value." + (bl ? "true" : "false");
                    }

                    if (Translator.translate(translationKey) == null) {
                        translationKey = bl ? "conrad.value.true" : "conrad.value.false";
                    }

                    return new TranslatableText(translationKey);
                })
        );

        register(Array.class, (screen, x, y, width, name, config, constraints, data, defaultValueSupplier, changedListener, saveConsumer, value) -> {
            ListView<ValueWidgetFactory<?>> widgetBuilders = data.getData(DataType.WIDGET);

            ValueWidgetFactory<T> factory = widgetBuilders.isEmpty()
                    ? get(value.getValueClass(), t -> {
            })
                    : (ValueWidgetFactory<T>) widgetBuilders.get(0);

            Consumer<Array<T>> saveAndSave = t -> {
                saveConsumer.accept(t);
                Conrad.syncAndSave(config);
            };

            ArrayWidget<T> array = new ArrayWidget<T>(config, screen, x, y, (Supplier<@NotNull Array<T>>) (Object) defaultValueSupplier, (Consumer<Array<T>>) (Object) changedListener, saveAndSave, value, name, factory);

            if (data.getDataTypes().contains(DataType.SUGGESTION_PROVIDER)) {
                array.setSuggestions(data.getData(DataType.SUGGESTION_PROVIDER).get(0));
            }

            array.addConstraints((ListView<Constraint<Array<T>>>) (Object) constraints);

            return array;

        });

        register(Table.class, (screen, x, y, width, name, config, constraints, data, defaultValueSupplier, changedListener, saveConsumer, value) -> {
            ListView<ValueWidgetFactory<?>> widgetBuilders = data.getData(DataType.WIDGET);

            ValueWidgetFactory<T> factory = widgetBuilders.isEmpty()
                    ? get(value.getValueClass(), t -> {
            })
                    : (ValueWidgetFactory<T>) widgetBuilders.get(0);

            Consumer<Table<T>> saveAndSave = t -> {
                saveConsumer.accept(t);
                Conrad.syncAndSave(config);
            };

            TableWidget<T> table = new TableWidget<T>(config, screen, x, y, (Supplier<@NotNull Table<T>>) (Object) defaultValueSupplier, (Consumer<Table<T>>) (Object) changedListener, saveAndSave, value, name, factory);

            if (data.getDataTypes().contains(DataType.SUGGESTION_PROVIDER)) {
                table.setValueSuggestions(data.getData(DataType.SUGGESTION_PROVIDER).get(0));
            }

            table.addConstraints((ListView<Constraint<Table<T>>>) (Object) constraints);

            if (config.getSerializer().getKeyConstraint() != null) {
                table.addKeyConstraint(config.getSerializer().getKeyConstraint());
            }

            return table;
        });
    }

    @FunctionalInterface
    public interface BoundedWidgetFactory<T> {
        ValueWidgetComponent<T> build(ConfigScreen parent, int x, int y, int width, int height, Supplier<@NotNull T> defaultValueSupplier, Consumer<T> changedListener, Consumer<T> saveConsumer, @NotNull T value, @NotNull T min, @NotNull T max);
    }

    @FunctionalInterface
    public interface UnboundedWidgetFactory<T> {
        ValueWidgetComponent<T> build(ConfigScreen parent, int x, int y, int width, int height, Alignment alignment, Supplier<@NotNull T> defaultValueSupplier, Consumer<T> changedListener, Consumer<T> saveConsumer, @NotNull T value);
    }

    public static class DataClassValueWidgetFactory<T> implements ValueWidgetFactory<T> {
        private final Class<T> type;
        private Consumer<T> outerSaveConsumer;

        private DataClassValueWidgetFactory(Class<T> type, Consumer<T> outerSaveConsumer) {
            this.type = type;
            this.outerSaveConsumer = outerSaveConsumer;
        }

        public void setOuterSaveConsumer(Consumer<T> saveConsumer) {
            this.outerSaveConsumer = saveConsumer;
        }

        @Override
        public WidgetComponent build(ConfigScreen screen, int x, int y, int width, Text name, ConfigDefinition<?> config, ListView<Constraint<T>> constraints, DataObject data, Supplier<@NotNull T> defaultValueSupplier, Consumer<T> changedListener, Consumer<T> saveConsumer, @NotNull T value) {
            return new DataClassWidgetComponent<>(screen, x, y, config, defaultValueSupplier, changedListener, t -> {
                saveConsumer.accept(t);
                outerSaveConsumer.accept(t);
            }, value, type);
        }
    }
}