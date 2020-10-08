package dev.hephaestus.clothy.impl.builders.primitive;

import dev.hephaestus.clothy.impl.gui.entries.IntegerSliderEntry;
import dev.hephaestus.clothy.impl.builders.FieldBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class IntSliderBuilder extends FieldBuilder<Integer, IntegerSliderEntry> {
    private int max;
    private int min;
    private Function<Integer, Text> textGetter = null;
    
    public IntSliderBuilder(Text resetButtonKey, Text fieldNameKey, int min, int max) {
        super(resetButtonKey, fieldNameKey);
        this.max = max;
        this.min = min;
    }

    public IntSliderBuilder setTextGetter(Function<Integer, Text> textGetter) {
        this.textGetter = textGetter;
        return this;
    }

    public IntSliderBuilder setMax(int max) {
        this.max = max;
        return this;
    }
    
    public IntSliderBuilder setMin(int min) {
        this.min = min;
        return this;
    }

    @Override
    protected IntegerSliderEntry withValue(Integer value) {
        IntegerSliderEntry entry = new IntegerSliderEntry(getFieldNameKey(), min, max, value, getResetButtonKey(), defaultValue, saveConsumer, this.tooltipSupplier, isRequireRestart());

        if (textGetter != null) {
            entry.setTextGetter(textGetter);
        }

        if (errorSupplier != null) {
            entry.setErrorSupplier(() -> errorSupplier.apply(entry.getValue()));
        }

        return entry;
    }
}
