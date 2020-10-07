package dev.hephaestus.clothy.impl.builders.primitive;

import dev.hephaestus.clothy.impl.gui.entries.DoubleListEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class DoubleFieldBuilder extends BoundedFieldBuilder<Double, DoubleListEntry> {
    public DoubleFieldBuilder(Text resetButtonKey, Text fieldNameKey) {
        super(resetButtonKey, fieldNameKey);
    }

    @Override
    protected DoubleListEntry baseWidget(Double value) {
        return new DoubleListEntry(getFieldNameKey(), value, getResetButtonKey(), defaultValue, saveConsumer, this.tooltipSupplier, isRequireRestart());
    }
}
