package dev.inkwell.conrad.api;

import dev.inkwell.conrad.impl.ConradException;
import dev.inkwell.vivid.builders.WidgetComponentBuilder;
import dev.inkwell.vivid.util.StronglyTypedCollection;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ParentConfigValue<T extends StronglyTypedCollection<?, V, ?>, V> extends ConfigValue<T> {
    private WidgetComponentBuilder<V> childBuilder;
    private V childMin;
    private V childMax;
    private boolean mutable = true;

    ParentConfigValue(@NotNull Supplier<@NotNull T> defaultValue) {
        super(defaultValue);
    }

    @Override
    public ParentConfigValue<T, V> sync() {
        super.sync();
        return this;
    }

    public ParentConfigValue<T, V> childWidget(WidgetComponentBuilder<V> builder) {
        this.childBuilder = builder;
        return this;
    }

    public ParentConfigValue<T, V> setChildMin(@Nullable V min) {
        this.childMin = min;
        return this;
    }

    public ParentConfigValue<T, V> setChildMax(@Nullable V max) {
        this.childMax = max;
        return this;
    }

    public ParentConfigValue<T, V> setChildBounds(@Nullable V min, @Nullable V max) {
        this.setChildMin(min);
        return this.setChildMax(max);
    }

    /**
     * Specifies whether or not elements should be able to be added to and removed from this element.
     * Note: only used for tables by default.
     * @param mutable whether or not users can add and remove elements to this collection
     * @return this collection
     */
    public ParentConfigValue<T, V> mutable(boolean mutable) {
        this.mutable = mutable;
        return this;
    }

    @ApiStatus.Internal
    public @Nullable WidgetComponentBuilder<V> getChildBuilder() {
        return this.childBuilder;
    }

    @ApiStatus.Internal
    public ConfigValue<V> getChildValue() {
        ConfigValue<V> child = new ConfigValue<>(this.getDefaultValue().getDefaultValue(), null);
        if (this.isSynced()) child.sync();
        child.setBounds(this.childMin, this.childMax);
        child.widget(this.childBuilder);

        return child;
    }

    @ApiStatus.Internal
    public boolean isMutable() {
        return this.mutable;
    }

    @Override
    public ConfigValue<T> setMin(@Nullable T min) {
        throw new ConradException("Cannot set parent min for parent config value");
    }

    @Override
    public ConfigValue<T> setMax(@Nullable T max) {
        throw new ConradException("Cannot set parent max for parent config value");
    }

    @Override
    public ConfigValue<T> setBounds(@Nullable T min, @Nullable T max) {
        throw new ConradException("Cannot set parent bounds for parent config value");
    }

    @Override
    public ParentConfigValue<T, V> widget(WidgetComponentBuilder<T> builder) {
        throw new ConradException("Cannot set parent widget builder for parent config value");
    }
}
