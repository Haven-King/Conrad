package dev.hephaestus.clothy.impl.builders.primitive;

import dev.hephaestus.clothy.impl.gui.entries.ColorEntry;
import dev.hephaestus.clothy.impl.builders.FieldBuilder;
import dev.hephaestus.math.impl.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class ColorFieldBuilder extends FieldBuilder<Color, ColorEntry> {
    private boolean alpha = false;
    
    public ColorFieldBuilder(Text resetButtonKey, Text fieldNameKey) {
        super(resetButtonKey, fieldNameKey);
    }

    public ColorFieldBuilder setAlphaMode(boolean withAlpha) {
        this.alpha = withAlpha;
        return this;
    }

    @Override
    protected ColorEntry withValue(Color value) {
        ColorEntry entry = new ColorEntry(getFieldNameKey(), value, getResetButtonKey(), defaultValue, saveConsumer, this.tooltipSupplier, isRequireRestart());

        if (this.alpha) {
            entry.withAlpha();
        } else {
            entry.withoutAlpha();
        }

        if (errorSupplier != null) {
            entry.setErrorSupplier(() -> errorSupplier.apply(entry.getValue()));
        }

        return entry;
    }
}
