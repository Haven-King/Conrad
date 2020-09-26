package dev.hephaestus.clothy.impl.builders;

import dev.hephaestus.clothy.impl.gui.entries.TextListEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class TextDescriptionBuilder extends FieldBuilder<Text, TextListEntry> {
    
    private int color = -1;
    private final Text value;
    
    public TextDescriptionBuilder(Text resetButtonKey, Text fieldNameKey, Text value) {
        super(resetButtonKey, fieldNameKey);
        this.value = value;
    }

    public TextDescriptionBuilder setColor(int color) {
        this.color = color;
        return this;
    }
    
    @NotNull
    @Override
    public TextListEntry build() {
        return new TextListEntry(getFieldNameKey(), value, color, () -> tooltipSupplier.apply(this.value));
    }
    
}
