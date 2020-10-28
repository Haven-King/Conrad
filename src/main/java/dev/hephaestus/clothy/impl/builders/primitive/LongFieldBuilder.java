package dev.hephaestus.clothy.impl.builders.primitive;

import dev.hephaestus.clothy.impl.gui.entries.LongListEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class LongFieldBuilder extends BoundedFieldBuilder<Long, LongListEntry> {
    public LongFieldBuilder(Text resetButtonKey, Text fieldNameKey) {
        super(resetButtonKey, fieldNameKey);
    }

    @Override
    protected LongListEntry baseWidget(Long value) {
        return new LongListEntry(getFieldNameKey(), value, getResetButtonKey(), defaultValue, saveConsumer, this.tooltipSupplier, isRequireRestart());
    }
}
