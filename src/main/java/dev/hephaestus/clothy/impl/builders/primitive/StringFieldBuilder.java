package dev.hephaestus.clothy.impl.builders.primitive;

import dev.hephaestus.clothy.impl.gui.entries.StringListEntry;
import dev.hephaestus.clothy.impl.builders.FieldBuilder;
import dev.hephaestus.conrad.impl.common.config.ValueContainer;
import dev.hephaestus.conrad.impl.common.keys.ValueKey;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class StringFieldBuilder extends FieldBuilder<String, StringListEntry> {
    public StringFieldBuilder(Text resetButtonKey, Text fieldNameKey) {
        super(resetButtonKey, fieldNameKey);
    }

    @Override
    protected StringListEntry withValue(String value) {
        StringListEntry entry = new StringListEntry(getFieldNameKey(), value, getResetButtonKey(), this.defaultValue, this.saveConsumer, this.tooltipSupplier, isRequireRestart());

        if (this.errorSupplier != null) {
            entry.setErrorSupplier(() -> this.errorSupplier.apply(entry.getValue()));
        }

        return entry;
    }
}
