package dev.hephaestus.clothy.impl.builders;

import dev.hephaestus.clothy.impl.gui.entries.TextListEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class TextDescriptionBuilder extends FieldBuilder<Text, TextListEntry> {
    private int color = -1;

    public TextDescriptionBuilder(Text resetButtonKey, Text fieldNameKey) {
        super(resetButtonKey, fieldNameKey);
    }

    public TextDescriptionBuilder setColor(int color) {
        this.color = color;
        return this;
    }

    @Override
    protected TextListEntry withValue(Text value) {
        return new TextListEntry(getFieldNameKey(), value, color, this.tooltipSupplier);
    }
}
