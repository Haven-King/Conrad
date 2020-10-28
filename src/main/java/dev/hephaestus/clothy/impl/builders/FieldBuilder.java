package dev.hephaestus.clothy.impl.builders;

import dev.hephaestus.clothy.api.AbstractConfigListEntry;
import dev.hephaestus.conrad.impl.common.config.ValueContainer;
import dev.hephaestus.conrad.impl.common.config.ValueDefinition;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public final A build(ValueContainer valueContainer, ValueDefinition valueDefinition) {
        if (valueContainer != null && valueDefinition != null) {
            this.setDefaultValue((T) ValueContainer.getDefault(valueDefinition.getKey()));
            this.setSaveConsumer(newValue -> {
                valueContainer.put(valueDefinition.getKey(), newValue, true);
            });

            List<Text> tooltips = new ArrayList<>();

            valueDefinition.getTooltips(false, tooltips::add);

            if (tooltips.size() > 0) {
                this.setTooltip(Optional.of(tooltips));
            }

            this.handleProperties(valueDefinition);
        }

        return this.withValue(valueContainer == null || valueDefinition == null ? null : valueContainer.get(valueDefinition.getKey()));
    }

    protected void handleProperties(ValueDefinition valueDefinition) {

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
