package dev.hephaestus.clothy.impl.builders;

import dev.hephaestus.clothy.impl.gui.entries.SelectionListEntry;
import dev.hephaestus.conrad.impl.common.config.ValueContainer;
import dev.hephaestus.conrad.impl.common.keys.ValueKey;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class SelectorBuilder<T> extends FieldBuilder<T, SelectionListEntry<T>> {
    
    private Consumer<T> saveConsumer = null;
    private Function<T, Optional<List<Text>>> tooltipSupplier = e -> Optional.empty();
    private final T[] valuesArray;
    private Function<T, Text> nameProvider = null;
    
    public SelectorBuilder(Text resetButtonKey, Text fieldNameKey, T[] valuesArray) {
        super(resetButtonKey, fieldNameKey);
        this.valuesArray = valuesArray;
    }

    public SelectorBuilder<T> setNameProvider(Function<T, Text> enumNameProvider) {
        this.nameProvider = enumNameProvider;
        return this;
    }

    @Override
    protected SelectionListEntry<T> withValue(T value) {
        SelectionListEntry<T> entry = new SelectionListEntry<>(getFieldNameKey(), this.valuesArray, value, getResetButtonKey(), this.defaultValue, this.saveConsumer, this.nameProvider, this.tooltipSupplier, isRequireRestart());

        if (errorSupplier != null) {
            entry.setErrorSupplier(() -> errorSupplier.apply(entry.getValue()));
        }

        return entry;
    }
}
