package dev.inkwell.conrad.impl;

import dev.inkwell.conrad.api.ConfigValue;
import dev.inkwell.conrad.api.ParentConfigValue;
import dev.inkwell.vivid.builders.WidgetComponentBuilder;
import dev.inkwell.vivid.screen.ConfigScreen;
import dev.inkwell.vivid.util.Alignment;
import dev.inkwell.vivid.util.Array;
import dev.inkwell.vivid.util.Table;
import dev.inkwell.vivid.widgets.WidgetComponent;
import dev.inkwell.vivid.widgets.compound.ArrayWidget;
import dev.inkwell.vivid.widgets.compound.TableWidget;
import dev.inkwell.vivid.widgets.value.ToggleComponent;
import dev.inkwell.vivid.widgets.value.ValueWidgetComponent;
import dev.inkwell.vivid.widgets.value.entry.*;
import dev.inkwell.vivid.widgets.value.slider.DoubleSliderWidget;
import dev.inkwell.vivid.widgets.value.slider.FloatSliderWidget;
import dev.inkwell.vivid.widgets.value.slider.IntegerSliderWidget;
import dev.inkwell.vivid.widgets.value.slider.LongSliderWidget;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EntryBuilderRegistry {
    private static final Map<Class<?>, Builder<?>> DEFAULT_BUILDERS = new HashMap<>();

    static {
        registerDefaults();
    }

    public static <T> void register(Class<T> clazz, Builder<T> builder) {
        for (Class<?> clazz2 : ReflectionUtil.getClasses(clazz)) {
            DEFAULT_BUILDERS.putIfAbsent(clazz2, builder);
        }
    }

    private static <T> void registerUnsafe(Class<?> clazz, Builder<T> builder) {
        DEFAULT_BUILDERS.putIfAbsent(clazz, builder);
    }

    private static <T> void registerBounded(Class<T> clazz, BoundedWidgetBuilder<T> bounded, UnboundedWidgetBuilder<T> unBounded) {
        register(clazz, (configValue, parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value) -> {
            T min = configValue.getMin();
            T max = configValue.getMax();

            if (min == null || max == null) {
                return unBounded.build(parent, x, y, width, height, Alignment.RIGHT, defaultValueSupplier, changedListener, saveConsumer, value);
            } else {
                return bounded.build(parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value, min, max);
            }
        });
    }

    public static <T> void override(Class<T> clazz, Builder<T> builder) {
        DEFAULT_BUILDERS.put(clazz, builder);
    }

    @SuppressWarnings("unchecked")
    public static <T> WidgetComponentBuilder<T> get(ConfigValue<T> configValue) throws ConradException {
        Class<T> clazz = (Class<T>) configValue.getDefaultValue().getClass();
        if (DEFAULT_BUILDERS.containsKey(clazz)) {
            return (parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value) ->
                    ((Builder<T>) DEFAULT_BUILDERS.get(clazz))
                            .build(configValue, parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value);
        } else {
            throw new ConradException("Default builder not registered for class '" + clazz.getName() + "'");
        }
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

        registerUnsafe(Array.class, (Builder<Array<T>>) (configValue, parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value) -> {
                    WidgetComponentBuilder<T> builder = ((ParentConfigValue<Array<T>, T>) configValue).getChildBuilder();
                    builder = builder != null ? builder :
                            new WrappedBuilder<>(
                                    (Builder<T>) DEFAULT_BUILDERS.get(defaultValueSupplier.get().getValueClass()),
                                    ((ParentConfigValue<Array<T>, T>) configValue).getChildValue());
                    return new ArrayWidget<T>(parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value, new TranslatableText(configValue.getKey().toString()), builder);
                }
        );

        registerUnsafe(Table.class, (Builder<Table<T>>) (configValue, parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value) -> {
                WidgetComponentBuilder<T> builder = ((ParentConfigValue<Table<T>, T>) configValue).getChildBuilder();
                builder = builder != null ? builder :
                        new WrappedBuilder<>(
                                (Builder<T>) DEFAULT_BUILDERS.get(defaultValueSupplier.get().getValueClass()),
                                ((ParentConfigValue<Table<T>, T>) configValue).getChildValue());
                return new TableWidget<T>(parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value, new TranslatableText(configValue.getKey().toString()), builder, ((ParentConfigValue<Table<T>, T>) configValue).isMutable());
        });
    }

    @FunctionalInterface
    public interface Builder<T> {
        WidgetComponent build(ConfigValue<T> configValue, ConfigScreen parent, int x, int y, int width, int height, Supplier<@NotNull T> defaultValueSupplier, Consumer<T> changedListener, Consumer<T> saveConsumer, @NotNull T value);
    }

    public static class WrappedBuilder<T> implements WidgetComponentBuilder<T> {
        private final Builder<T> builder;
        private final ConfigValue<T> value;

        public WrappedBuilder(Builder<T> builder, ConfigValue<T> value) {
            this.builder = builder;
            this.value = value;
        }

        @Override
        public WidgetComponent build(ConfigScreen parent, int x, int y, int width, int height, Supplier<@NotNull T> defaultValueSupplier, Consumer<T> changedListener, Consumer<T> saveConsumer, @NotNull T value) {
            return this.builder.build(this.value, parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value);
        }
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
