package dev.hephaestus.clothy.impl.builders;

import dev.hephaestus.clothy.api.AbstractConfigListEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    @NotNull
	protected Function<T, Optional<List<Text>>> tooltipSupplier = bool -> Optional.empty();

    protected FieldBuilder(Text resetButtonKey, Text fieldNameKey) {
        this.resetButtonKey = Objects.requireNonNull(resetButtonKey);
        this.fieldNameKey = Objects.requireNonNull(fieldNameKey);
    }
    
    @Nullable
    public final Supplier<T> getDefaultValue() {
        return defaultValue;
    }
    
    @SuppressWarnings("rawtypes")
    @Deprecated
    public final AbstractConfigListEntry buildEntry() {
        return build();
    }

    public FieldBuilder<T, A> setErrorSupplier(Function<T, Optional<Text>> errorSupplier) {
        this.errorSupplier = errorSupplier;
        return this;
    }

    @NotNull
    public abstract A build();
    
    @NotNull
    public final Text getFieldNameKey() {
        return fieldNameKey;
    }
    
    @NotNull
    public final Text getResetButtonKey() {
        return resetButtonKey;
    }
    
    public boolean isRequireRestart() {
        return requireRestart;
    }
    
    public void requireRestart(boolean requireRestart) {
        this.requireRestart = requireRestart;
    }

    public FieldBuilder<T, A> requireRestart() {
        requireRestart(true);
        return this;
    }

    public FieldBuilder<T, A> setSaveConsumer(Consumer<T> saveConsumer) {
        this.saveConsumer = saveConsumer;
        return this;
    }

    public FieldBuilder<T, A> setDefaultValue(Supplier<T> defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public FieldBuilder<T, A> setDefaultValue(T defaultValue) {
        this.defaultValue = () -> defaultValue;
        return this;
    }

    public FieldBuilder<T, A> setTooltipSupplier(@NotNull Function<T, Optional<List<Text>>> tooltipSupplier) {
        this.tooltipSupplier = tooltipSupplier;
        return this;
    }

    public FieldBuilder<T, A> setTooltipSupplier(@NotNull Supplier<Optional<List<Text>>> tooltipSupplier) {
        this.tooltipSupplier = bool -> tooltipSupplier.get();
        return this;
    }

    public FieldBuilder<T, A> setTooltip(Optional<List<Text>> tooltip) {
        this.tooltipSupplier = bool -> tooltip;
        return this;
    }
}
