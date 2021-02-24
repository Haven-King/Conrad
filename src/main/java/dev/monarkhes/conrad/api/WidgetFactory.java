package dev.monarkhes.conrad.api;

import dev.monarkhes.vivid.screen.ConfigScreen;
import dev.monarkhes.vivid.widgets.WidgetComponent;
import net.fabricmc.loader.api.config.data.DataType;
import net.fabricmc.loader.api.config.data.KeyView;
import net.fabricmc.loader.api.config.value.ValueKey;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

@FunctionalInterface
public interface WidgetFactory<T, V extends KeyView<T>> {
    DataType<WidgetFactory<?, ?>> DATA_TYPE = new DataType<>("conrad", "widget_builder");

    WidgetComponent build(V keyView, ConfigScreen parent, int x, int y, int width, int height, Supplier<@NotNull T> defaultValueSupplier, Consumer<T> changedListener, Consumer<T> saveConsumer, @NotNull T value);

    @FunctionalInterface
    interface Default<T> extends WidgetFactory<T, KeyView<T>> {
        WidgetComponent build(KeyView<T> keyView, ConfigScreen parent, int x, int y, int width, int height, Supplier<@NotNull T> defaultValueSupplier, Consumer<T> changedListener, Consumer<T> saveConsumer, @NotNull T value);
    }

    @FunctionalInterface
    interface ValueDependent<T> extends WidgetFactory<T, ValueKey<T>> {
        WidgetComponent build(ValueKey<T> valueKey, ConfigScreen parent, int x, int y, int width, int height, Supplier<@NotNull T> defaultValueSupplier, Consumer<T> changedListener, Consumer<T> saveConsumer, @NotNull T value);
    }
}
