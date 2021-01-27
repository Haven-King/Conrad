package dev.monarkhes.conrad.api;

import dev.monarkhes.conrad.impl.Conrad;
import dev.monarkhes.conrad.impl.entrypoints.ConfigScreenProvider;
import dev.monarkhes.conrad.test.Bounds;
import dev.monarkhes.vivid.builders.WidgetComponentBuilder;
import dev.monarkhes.vivid.screen.ConfigScreen;
import dev.monarkhes.vivid.util.Alignment;
import dev.monarkhes.vivid.widgets.WidgetComponent;
import dev.monarkhes.vivid.widgets.compound.ArrayWidget;
import dev.monarkhes.vivid.widgets.compound.TableWidget;
import dev.monarkhes.vivid.widgets.value.ToggleComponent;
import dev.monarkhes.vivid.widgets.value.ValueWidgetComponent;
import dev.monarkhes.vivid.widgets.value.entry.*;
import dev.monarkhes.vivid.widgets.value.slider.DoubleSliderWidget;
import dev.monarkhes.vivid.widgets.value.slider.FloatSliderWidget;
import dev.monarkhes.vivid.widgets.value.slider.IntegerSliderWidget;
import dev.monarkhes.vivid.widgets.value.slider.LongSliderWidget;
import net.fabricmc.loader.api.config.ConfigManager;
import net.fabricmc.loader.api.config.data.Constraint;
import net.fabricmc.loader.api.config.data.DataType;
import net.fabricmc.loader.api.config.data.KeyView;
import net.fabricmc.loader.api.config.exceptions.ConfigValueException;
import net.fabricmc.loader.api.config.serialization.ReflectionUtil;
import net.fabricmc.loader.api.config.util.Array;
import net.fabricmc.loader.api.config.util.ListView;
import net.fabricmc.loader.api.config.util.StronglyTypedImmutableCollection;
import net.fabricmc.loader.api.config.util.Table;
import net.fabricmc.loader.api.config.value.ValueKey;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EntryBuilderRegistry {
    private static final Map<Class<?>, WidgetBuilder<?, ?>> DEFAULT_BUILDERS = new HashMap<>();

    static {
        registerDefaults();
    }

    public static <T> void register(Class<T> clazz, WidgetBuilder.Default<T> builder) {
        for (Class<?> clazz2 : ReflectionUtil.getClasses(clazz)) {
            DEFAULT_BUILDERS.putIfAbsent(clazz2, builder);
        }
    }

    private static <T> void registerUnsafe(Class<?> clazz, WidgetBuilder.ValueDependent<T> builder) {
        DEFAULT_BUILDERS.putIfAbsent(clazz, builder);
    }

    private static <T extends Number> void registerBounded(Class<T> clazz, BoundedWidgetBuilder<T> bounded, UnboundedWidgetBuilder<T> unBounded) {
        register(clazz, (configValue, parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value) -> {
            Bounds<T> bounds = null;

            for (Constraint<T> constraint : configValue.getConstraints()) {
                if (constraint instanceof Bounds) {
                    bounds = (Bounds<T>) constraint;
                }
            }

            if (bounds == null) {
                return unBounded.build(parent, x, y, width, height, Alignment.RIGHT, defaultValueSupplier, changedListener, saveConsumer, value);
            } else {
                T min = bounds.getMin();
                T max = bounds.getMax();

                return bounded.build(parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value, min, max);
            }
        });
    }

    public static <T> void override(Class<T> clazz, WidgetBuilder<T, ?> builder) {
        DEFAULT_BUILDERS.put(clazz, builder);
    }

    @SuppressWarnings("unchecked")
    public static <T> WidgetComponentBuilder<T> get(ValueKey<T> configValue) throws ConfigValueException {
        ListView<WidgetBuilder<?, ?>> widgetBuilders = configValue.getData(WidgetBuilder.DATA_TYPE);

        if (!widgetBuilders.isEmpty()) {
            return (parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value) ->
                    ((WidgetBuilder<T, KeyView<T>>) widgetBuilders.get(0))
                            .build(configValue, parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value);
        }

        Class<T> clazz = (Class<T>) configValue.getDefaultValue().getClass();
        if (DEFAULT_BUILDERS.containsKey(clazz)) {
            WidgetBuilder<T, ?> builder = (WidgetBuilder<T, ?>) DEFAULT_BUILDERS.get(clazz);

            if (builder instanceof WidgetBuilder.Default) {
                return (parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value) ->
                        ((WidgetBuilder.Default<T>) builder).build(configValue, parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value);
            } else if (builder instanceof WidgetBuilder.ValueDependent) {
                return (parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value) ->
                        ((WidgetBuilder.ValueDependent<T>) builder).build(configValue, parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value);
            }
        }

        throw new ConfigValueException("Widget builder not registered for class '" + clazz.getName() + "' or provided for value '" + configValue.toString() + "'");
    }

    @SuppressWarnings("unchecked")
    private static <T> void registerDefaults() {
        registerBounded(Integer.class, IntegerSliderWidget::new, IntegerEntryWidget::new);
        registerBounded(Long.class, LongSliderWidget::new, LongEntryWidget::new);
        registerBounded(Float.class, FloatSliderWidget::new, FloatEntryWidget::new);
        registerBounded(Double.class, DoubleSliderWidget::new, DoubleEntryWidget::new);

        register(String.class, ((configValue, parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value) -> {
            return new StringEntryWidget(parent, x, y, width, height, Alignment.RIGHT, defaultValueSupplier, changedListener, saveConsumer, value);
        }));

        register(Boolean.class, (configValue, parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value) -> {
            return new ToggleComponent(parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value);
        });

        registerUnsafe(Array.class, (WidgetBuilder.ValueDependent<Array<T>>) (configValue, parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value) -> {
                ListView<WidgetBuilder<?, ?>> widgetBuilders = configValue.getData(WidgetBuilder.DATA_TYPE);

                WidgetComponentBuilder<T> builder = WrappedBuilder.of(widgetBuilders.isEmpty()
                        ? (WidgetBuilder) DEFAULT_BUILDERS.get(configValue.getDefaultValue().getValueClass())
                        : (WidgetBuilder) widgetBuilders.get(0), of(configValue));

                Consumer<Array<T>> saveAndSave = t -> {
                    saveConsumer.accept(t);
                    Conrad.syncAndSave(configValue.getConfig());
                };

                return new ArrayWidget<T>(parent, x, y, width, height, defaultValueSupplier, changedListener, saveAndSave, value, new TranslatableText(configValue.toString()), builder);
            }
        );

        registerUnsafe(Table.class, (WidgetBuilder.ValueDependent<Table<T>>) (configValue, parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value) -> {
                    ListView<WidgetBuilder<?, ?>> widgetBuilders = configValue.getData(WidgetBuilder.DATA_TYPE);

                    WidgetComponentBuilder<T> builder = WrappedBuilder.of(widgetBuilders.isEmpty()
                            ? (WidgetBuilder) DEFAULT_BUILDERS.get(configValue.getDefaultValue().getValueClass())
                            : (WidgetBuilder) widgetBuilders.get(0), of(configValue));

                    Consumer<Table<T>> syncAndSave = t -> {
                        saveConsumer.accept(t);
                        Conrad.syncAndSave(configValue.getConfig());
                    };
                    
                    return new TableWidget<>(parent, x, y, width, height, defaultValueSupplier, changedListener, syncAndSave, value, new TranslatableText(configValue.toString()), builder);
                }
        );
    }

    public static class WrappedBuilder<T, V extends KeyView<T>> implements WidgetComponentBuilder<T> {
        private final WidgetBuilder<T, V> builder;
        private final V keyView;

        public WrappedBuilder(WidgetBuilder<T, V> builder, V keyView) {
            this.builder = builder;
            this.keyView = keyView;
        }

        @Override
        public WidgetComponent build(ConfigScreen parent, int x, int y, int width, int height, Supplier<@NotNull T> defaultValueSupplier, Consumer<T> changedListener, Consumer<T> saveConsumer, @NotNull T value) {
            return this.builder.build(this.keyView, parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value);
        }

        public static <T, V extends KeyView<T>> WrappedBuilder<T, V> of(WidgetBuilder<T, V> builder, V keyView) {
            if (builder instanceof WidgetBuilder.Default) {
                return new WrappedBuilder<>(builder, keyView);
            } else if (builder instanceof WidgetBuilder.ValueDependent) {
                return new WrappedBuilder<>(builder, keyView);
            }

            return null;
        }
    }

    static <T extends StronglyTypedImmutableCollection<?, V, ?>, V> KeyView<V> of(ValueKey<T> valueKey) {
        List<Constraint<V>> constraints = new ArrayList<>();

        for (Constraint<T> constraint :valueKey.getConstraints()) {
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
            public <D> ListView<D> getData(DataType<D> dataType) {
                return valueKey.getData(dataType);
            }
        };
    }

    @FunctionalInterface
    public interface BoundedWidgetBuilder<T> {
        ValueWidgetComponent<T> build(ConfigScreen parent, int x, int y, int width, int height, Supplier<@NotNull T> defaultValueSupplier, Consumer<T> changedListener, Consumer<T> saveConsumer, @NotNull T value, @NotNull T min, @NotNull T max);
    }

    @FunctionalInterface
    public interface UnboundedWidgetBuilder<T> {
        ValueWidgetComponent<T> build(ConfigScreen parent, int x, int y, int width, int height, Alignment alignment, Supplier<@NotNull T> defaultValueSupplier, Consumer<T> changedListener, Consumer<T> saveConsumer, @NotNull T value);
    }

}