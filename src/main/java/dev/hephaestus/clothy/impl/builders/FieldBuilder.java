package dev.hephaestus.clothy.impl.builders;

import dev.hephaestus.clothy.api.AbstractConfigListEntry;
import dev.hephaestus.clothy.impl.builders.primitive.BoundedFieldBuilder;
import dev.hephaestus.clothy.impl.gui.entries.BoundedFieldEntry;
import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.common.config.ValueContainer;
import dev.hephaestus.conrad.impl.common.keys.KeyRing;
import dev.hephaestus.conrad.impl.common.keys.ValueKey;
import dev.hephaestus.conrad.impl.common.util.ConradUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public abstract class FieldBuilder<T, A extends AbstractConfigListEntry<?>> {
    @NotNull
	private final Text fieldNameKey;
    @NotNull
	private final Text resetButtonKey;
    protected boolean requireRestart = false;
    @Nullable protected Supplier<T> defaultValue = null;
    @Nullable protected Function<T, Optional<Text>> errorSupplier;
    @Nullable protected Consumer<T> saveConsumer = null;
    @NotNull protected Function<T, Optional<List<Text>>> tooltipSupplier = bool -> Optional.empty();

    protected FieldBuilder(Text resetButtonKey, Text fieldNameKey) {
        this.resetButtonKey = Objects.requireNonNull(resetButtonKey);
        this.fieldNameKey = Objects.requireNonNull(fieldNameKey);
    }
    
    @Nullable
    public final Supplier<T> getDefaultValue() {
        return defaultValue;
    }

    public final FieldBuilder<T, A> setErrorSupplier(Function<T, Optional<Text>> errorSupplier) {
        this.errorSupplier = errorSupplier;
        return this;
    }

    protected abstract A withValue(T value);

    @NotNull
    @SuppressWarnings("unchecked")
    public final A build(ValueContainer valueContainer, ValueKey valueKey) {
        if (valueContainer != null && valueKey != null) {
            this.setDefaultValue((T) ValueContainer.getDefault(valueKey));
            this.setSaveConsumer(newValue -> {
                try {
                    valueContainer.put(valueKey, newValue, true);
                } catch (IOException e) {
                    ConradUtil.LOG.warn("Exception while saving config value {}: {}", valueKey.getName(), e.getMessage());
                }
            });

            Method method = KeyRing.get(valueKey);

            List<Text> tooltips = new ArrayList<>();

            ConradUtil.getTooltips(method, tooltips::add);

            if (tooltips.size() > 0) {
                this.setTooltip(Optional.of(tooltips));
            }

            if (method.isAnnotationPresent(Config.Value.IntegerBounds.class)
                    || method.isAnnotationPresent(Config.Value.FloatingBounds.class)) {
                Number min;
                Number max;

                if (method.isAnnotationPresent(Config.Value.IntegerBounds.class)) {
                    Config.Value.IntegerBounds bounds = method.getAnnotation(Config.Value.IntegerBounds.class);
                    min = bounds.min() > Long.MIN_VALUE ? bounds.min() : null;
                    max = bounds.max() < Long.MAX_VALUE ? bounds.max() : null;

                } else {
                    Config.Value.FloatingBounds bounds = method.getAnnotation(Config.Value.FloatingBounds.class);
                    min = bounds.min() > Double.MIN_VALUE ? bounds.min() : null;
                    max = bounds.max() < Double.MAX_VALUE ? bounds.max() : null;
                }

                if (min != null) {
                    ((BoundedFieldEntry) this).setMin(min);
                }

                if (max != null) {
                    ((BoundedFieldEntry) this).setMax(max);
                }
            }
        }

        return this.withValue(valueContainer == null || valueKey == null ? null : valueContainer.get(valueKey));
    }
    
    @NotNull
    public final Text getFieldNameKey() {
        return fieldNameKey;
    }
    
    @NotNull
    public final Text getResetButtonKey() {
        return resetButtonKey;
    }
    
    public final boolean isRequireRestart() {
        return requireRestart;
    }
    
    public final void requireRestart(boolean requireRestart) {
        this.requireRestart = requireRestart;
    }

    public final FieldBuilder<T, A> requireRestart() {
        requireRestart(true);
        return this;
    }

    public final FieldBuilder<T, A> setSaveConsumer(Consumer<T> saveConsumer) {
        this.saveConsumer = saveConsumer;
        return this;
    }

    public final FieldBuilder<T, A> setDefaultValue(Supplier<T> defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public final FieldBuilder<T, A> setDefaultValue(T defaultValue) {
        this.defaultValue = () -> defaultValue;
        return this;
    }

    public final FieldBuilder<T, A> setTooltipSupplier(@NotNull Function<T, Optional<List<Text>>> tooltipSupplier) {
        this.tooltipSupplier = tooltipSupplier;
        return this;
    }

    public final FieldBuilder<T, A> setTooltipSupplier(@NotNull Supplier<Optional<List<Text>>> tooltipSupplier) {
        this.tooltipSupplier = bool -> tooltipSupplier.get();
        return this;
    }

    public final FieldBuilder<T, A> setTooltip(Optional<List<Text>> tooltip) {
        this.tooltipSupplier = bool -> tooltip;
        return this;
    }
}
