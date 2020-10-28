package dev.hephaestus.clothy.impl.builders;

import dev.hephaestus.clothy.impl.gui.entries.EnumListEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

import java.util.Objects;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class EnumSelectorBuilder<T extends Enum<?>> extends FieldBuilder<T, EnumListEntry<T>> {
    private final Class<T> clazz;
    private Function<Enum<?>, Text> enumNameProvider = EnumListEntry.DEFAULT_NAME_PROVIDER;
    
    public EnumSelectorBuilder(Text resetButtonKey, Text fieldNameKey, Class<T> clazz) {
        super(resetButtonKey, fieldNameKey);
        Objects.requireNonNull(clazz);
        this.clazz = clazz;
    }

    public EnumSelectorBuilder<T> setEnumNameProvider(Function<Enum<?>, Text> enumNameProvider) {
        Objects.requireNonNull(enumNameProvider);
        this.enumNameProvider = enumNameProvider;
        return this;
    }

    @Override
    protected EnumListEntry<T> withValue(T value) {
        EnumListEntry<T> entry = new EnumListEntry<>(getFieldNameKey(), clazz, value, getResetButtonKey(), defaultValue, saveConsumer, enumNameProvider, this.tooltipSupplier, isRequireRestart());

        if (errorSupplier != null) {
            entry.setErrorSupplier(() -> errorSupplier.apply(entry.getValue()));
        }

        return entry;
    }
}
