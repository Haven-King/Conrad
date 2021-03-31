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

package dev.inkwell.conrad.api;

import dev.inkwell.conrad.api.gui.widgets.value.*;
import dev.inkwell.conrad.impl.Conrad;
import dev.inkwell.conrad.api.value.data.Bounds;
import dev.inkwell.conrad.api.value.data.Constraint;
import dev.inkwell.conrad.api.value.data.DataType;
import dev.inkwell.conrad.api.value.data.Flag;
import dev.inkwell.conrad.api.value.util.Array;
import dev.inkwell.conrad.api.value.util.ListView;
import dev.inkwell.conrad.api.value.util.StronglyTypedImmutableCollection;
import dev.inkwell.conrad.api.value.util.Table;
import dev.inkwell.conrad.api.value.ValueKey;
import dev.inkwell.conrad.impl.data.KeyView;
import dev.inkwell.conrad.impl.exceptions.ConfigValueException;
import dev.inkwell.conrad.impl.util.ReflectionUtil;
import dev.inkwell.conrad.api.gui.builders.WidgetComponentFactory;
import dev.inkwell.conrad.api.gui.screen.ConfigScreen;
import dev.inkwell.conrad.api.gui.util.Alignment;
import dev.inkwell.conrad.api.gui.widgets.WidgetComponent;
import dev.inkwell.conrad.api.gui.widgets.compound.ArrayWidget;
import dev.inkwell.conrad.api.gui.widgets.compound.TableWidget;
import dev.inkwell.conrad.api.gui.widgets.value.entry.*;
import dev.inkwell.conrad.api.gui.widgets.value.slider.DoubleSliderWidget;
import dev.inkwell.conrad.api.gui.widgets.value.slider.FloatSliderWidget;
import dev.inkwell.conrad.api.gui.widgets.value.slider.IntegerSliderWidget;
import dev.inkwell.conrad.api.gui.widgets.value.slider.LongSliderWidget;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EntryBuilderRegistry {
    private static final Map<Class<?>, WidgetFactory<?, ?>> DEFAULT_FACTORIES = new HashMap<>();

    static {
        registerDefaults();
    }

    public static <T> void register(Class<T> clazz, WidgetFactory.Default<T> builder) {
        for (Class<?> clazz2 : ReflectionUtil.getClasses(clazz)) {
            DEFAULT_FACTORIES.putIfAbsent(clazz2, builder);
        }
    }

    private static <T> void registerUnsafe(Class<?> clazz, WidgetFactory.ValueDependent<T> builder) {
        DEFAULT_FACTORIES.putIfAbsent(clazz, builder);
    }

    private static <T extends Number & Comparable<T>> void registerBounded(Class<T> clazz, BoundedWidgetFactory<T> bounded, UnboundedWidgetFactory<T> unBounded) {
        register(clazz, (configValue, parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value) -> {
            Bounds<T> bounds = null;

            if (configValue != null) {
                for (Constraint<T> constraint : configValue.getConstraints()) {
                    if (constraint instanceof Bounds) {
                        bounds = (Bounds<T>) constraint;
                    }
                }
            }

            if (bounds == null || bounds.getMin().equals(bounds.getAbsoluteMin()) || bounds.getMax().equals(bounds.getAbsoluteMax())) {
                return unBounded.build(parent, x, y, width, height, Alignment.RIGHT, defaultValueSupplier, changedListener, saveConsumer, value);
            } else {
                T min = bounds.getMin();
                T max = bounds.getMax();

                return bounded.build(parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value, min, max);
            }
        });
    }

    public static <T> void override(Class<T> clazz, WidgetFactory<T, ?> builder) {
        DEFAULT_FACTORIES.put(clazz, builder);
    }

    @SuppressWarnings("unchecked")
    public static <T> WidgetComponentFactory<T> get(ValueKey<T> configValue) throws ConfigValueException {
        ListView<WidgetFactory<?, ?>> widgetBuilders = configValue.getData(WidgetFactory.DATA_TYPE);

        if (!widgetBuilders.isEmpty()) {
            return (parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value) ->
                    ((WidgetFactory<T, KeyView<T>>) widgetBuilders.get(0))
                            .build(configValue, parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value);
        }

        return (WidgetComponentFactory<T>) get(configValue.getDefaultValue().getClass(), t -> Conrad.syncAndSave(configValue.getConfig()));
    }

    public static <T> WidgetComponentFactory<T> get(Class<T> type, Consumer<T> outerSaveConsumer) {
        if (DEFAULT_FACTORIES.containsKey(type)) {
            WidgetFactory<T, ?> builder = (WidgetFactory<T, ?>) DEFAULT_FACTORIES.get(type);

            if (builder instanceof WidgetFactory.Default) {
                return (parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value) ->
                        builder.build(null, parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value);
            }
        }

        if (type.isEnum()) {
            return (WidgetComponentFactory<T>) enumFactory(type);
        }

        if (isSimpleCompoundData(type)) {
            return new DataClassWidgetComponentFactory<>(type, outerSaveConsumer);
        }

        throw new ConfigValueException("Widget builder not registered for class '" + type.getName() + "' or provided for class '" + type.getName() + "'");

    }

    private static boolean isSimpleCompoundData(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            Class<?> fieldType = field.getType();

            if (!fieldType.isEnum() && !isSimpleCompoundData(fieldType) && !DEFAULT_FACTORIES.containsKey(fieldType) && !(DEFAULT_FACTORIES.get(fieldType) instanceof WidgetFactory.ValueDependent)) {
                return false;
            }
        }

        return true;

    }

    private static <T extends Enum<T>> WidgetComponentFactory<T> enumFactory(Class<?> clazz) {
        T[] enums = (T[]) clazz.getEnumConstants();

        if (enums.length <= 3) {
            return (EnumSelectorComponent::new);
        } else {
            return (EnumDropdownWidget::new);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void registerDefaults() {
        registerBounded(Integer.class, IntegerSliderWidget::new, IntegerEntryWidget::new);
        registerBounded(Long.class, LongSliderWidget::new, LongEntryWidget::new);
        registerBounded(Float.class, FloatSliderWidget::new, FloatEntryWidget::new);
        registerBounded(Double.class, DoubleSliderWidget::new, DoubleEntryWidget::new);

        register(String.class, ((configValue, parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value) ->
                new StringEntryWidget(parent, x, y, width, height, Alignment.RIGHT, defaultValueSupplier, changedListener, saveConsumer, value)));

        register(Boolean.class, (configValue, parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value) ->
                new ToggleComponent(parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value));

        registerUnsafe(Array.class, (WidgetFactory.ValueDependent<Array<T>>) (configValue, parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value) -> {
                    ListView<WidgetFactory<?, ?>> factories = configValue.getData(WidgetFactory.DATA_TYPE);

                    WidgetComponentFactory<T> builder = WrappedFactory.of(factories.isEmpty()
                            ? (WidgetFactory) DEFAULT_FACTORIES.get(configValue.getDefaultValue().getValueClass())
                            : (WidgetFactory) factories.get(0), of(configValue));

                    Consumer<Array<T>> saveAndSave = t -> {
                        saveConsumer.accept(t);
                        Conrad.syncAndSave(configValue.getConfig());
                    };

                    ArrayWidget<T> array = new ArrayWidget<T>(parent, x, y, width, height, defaultValueSupplier, changedListener, saveAndSave, value, new TranslatableText(configValue.toString()), builder);

                    if (configValue.getDataTypes().contains(DataType.SUGGESTION_PROVIDER)) {
                        array.withSuggestions(configValue.getData(DataType.SUGGESTION_PROVIDER).get(0));
                    }

                    return array;
                }
        );

        registerUnsafe(Table.class, (WidgetFactory.ValueDependent<Table<T>>) (configValue, parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value) -> {
                    ListView<WidgetFactory<?, ?>> factories = configValue.getData(WidgetFactory.DATA_TYPE);

                    WidgetComponentFactory<T> factory = WrappedFactory.of(factories.isEmpty()
                            ? (WidgetFactory) DEFAULT_FACTORIES.get(configValue.getDefaultValue().getValueClass())
                            : (WidgetFactory) factories.get(0), of(configValue));

                    Consumer<Table<T>> syncAndSave = t -> {
                        saveConsumer.accept(t);
                        Conrad.syncAndSave(configValue.getConfig());
                    };

                    TableWidget<T> table = new TableWidget<>(parent, x, y, width, height, defaultValueSupplier, changedListener, syncAndSave, value, new TranslatableText(configValue.toString()), factory);

                    if (configValue.getDataTypes().contains(DataType.KEY_SUGGESTION_PROVIDER)) {
                        table.withKeySuggestions(configValue.getData(DataType.KEY_SUGGESTION_PROVIDER).get(0));
                    }

                    if (configValue.getDataTypes().contains(DataType.SUGGESTION_PROVIDER)) {
                        table.withSuggestions(configValue.getData(DataType.SUGGESTION_PROVIDER).get(0));
                    }

                    return table;
                }
        );
    }

    static <T extends StronglyTypedImmutableCollection<?, V, ?>, V> KeyView<V> of(ValueKey<T> valueKey) {
        List<Constraint<V>> constraints = new ArrayList<>();

        for (Constraint<T> constraint : valueKey.getConstraints()) {
            if (constraint instanceof Constraint.Value) {
                for (Constraint<V> c : ((Constraint.Value<?, V>) constraint).getConstraints()) {
                    constraints.add(c);
                }
            }
        }

        return new KeyView<V>() {
            @Override
            public ListView<Constraint<V>> getConstraints() {
                return new ListView<>(constraints);
            }

            @Override
            public <D> @NotNull ListView<D> getData(DataType<D> dataType) {
                return valueKey.getData(dataType);
            }

            @Override
            public @NotNull ListView<DataType<?>> getDataTypes() {
                return valueKey.getDataTypes();
            }

            @Override
            public @NotNull ListView<Flag> getFlags() {
                return valueKey.getFlags();
            }
        };
    }

    @FunctionalInterface
    public interface BoundedWidgetFactory<T> {
        ValueWidgetComponent<T> build(ConfigScreen parent, int x, int y, int width, int height, Supplier<@NotNull T> defaultValueSupplier, Consumer<T> changedListener, Consumer<T> saveConsumer, @NotNull T value, @NotNull T min, @NotNull T max);
    }

    @FunctionalInterface
    public interface UnboundedWidgetFactory<T> {
        ValueWidgetComponent<T> build(ConfigScreen parent, int x, int y, int width, int height, Alignment alignment, Supplier<@NotNull T> defaultValueSupplier, Consumer<T> changedListener, Consumer<T> saveConsumer, @NotNull T value);
    }

    public static class WrappedFactory<T, V extends KeyView<T>> implements WidgetComponentFactory<T> {
        private final WidgetFactory<T, V> factory;
        private final V keyView;

        public WrappedFactory(WidgetFactory<T, V> factory, V keyView) {
            this.factory = factory;
            this.keyView = keyView;
        }

        public static <T, V extends KeyView<T>> WrappedFactory<T, V> of(WidgetFactory<T, V> builder, V keyView) {
            if (builder instanceof WidgetFactory.Default) {
                return new WrappedFactory<>(builder, keyView);
            } else if (builder instanceof WidgetFactory.ValueDependent) {
                return new WrappedFactory<>(builder, keyView);
            }

            return null;
        }

        @Override
        public WidgetComponent build(ConfigScreen parent, int x, int y, int width, int height, Supplier<@NotNull T> defaultValueSupplier, Consumer<T> changedListener, Consumer<T> saveConsumer, @NotNull T value) {
            return this.factory.build(this.keyView, parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value);
        }
    }

    public static class DataClassWidgetComponentFactory<T> implements WidgetComponentFactory<T> {
        private final Class<T> type;
        private Consumer<T> outerSaveConsumer;

        private DataClassWidgetComponentFactory(Class<T> type, Consumer<T> outerSaveConsumer) {
            this.type = type;
            this.outerSaveConsumer = outerSaveConsumer;
        }

        public void setOuterSaveConsumer(Consumer<T> saveConsumer) {
            this.outerSaveConsumer = saveConsumer;
        }

        @Override
        public WidgetComponent build(ConfigScreen parent, int x, int y, int width, int height, Supplier<@NotNull T> defaultValueSupplier, Consumer<T> changedListener, Consumer<T> saveConsumer, @NotNull T value) {
            return new DataClassWidgetComponent<>(parent, x, y, width, height, defaultValueSupplier, changedListener, t -> {
                saveConsumer.accept(t);
                outerSaveConsumer.accept(t);
            }, value, type);
        }
    }
}