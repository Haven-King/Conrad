package dev.hephaestus.clothy.impl.builders.primitive;

import dev.hephaestus.clothy.impl.gui.entries.StringListEntry;
import dev.hephaestus.clothy.impl.builders.FieldBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class StringFieldBuilder extends FieldBuilder<String, StringListEntry> {
    private final String value;

    public StringFieldBuilder(Text resetButtonKey, Text fieldNameKey, String value) {
        super(resetButtonKey, fieldNameKey);
        Objects.requireNonNull(value);
        this.value =value;
    }

    @NotNull
    @Override
    public StringListEntry build() {
        StringListEntry entry = new StringListEntry(getFieldNameKey(), this.value, getResetButtonKey(), this.defaultValue, this.saveConsumer, null, isRequireRestart());
        entry.setTooltipSupplier(() -> this.tooltipSupplier.apply(entry.getValue()));
        if (this.errorSupplier != null)
            entry.setErrorSupplier(() -> this.errorSupplier.apply(entry.getValue()));
        return entry;
    }
}
