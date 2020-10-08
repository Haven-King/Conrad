package dev.hephaestus.clothy.impl.builders.primitive;

import dev.hephaestus.clothy.impl.gui.entries.LongSliderEntry;
import dev.hephaestus.clothy.impl.builders.FieldBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class LongSliderBuilder extends FieldBuilder<Long, LongSliderEntry> {
    private final long max;
    private final long min;
    private Function<Long, Text> textGetter = null;
    
    public LongSliderBuilder(Text resetButtonKey, Text fieldNameKey, long min, long max) {
        super(resetButtonKey, fieldNameKey);
        this.max = max;
        this.min = min;
    }
    
    public LongSliderBuilder setTextGetter(Function<Long, Text> textGetter) {
        this.textGetter = textGetter;
        return this;
    }

    @Override
    protected LongSliderEntry withValue(Long value) {
        LongSliderEntry entry = new LongSliderEntry(getFieldNameKey(), min, max, value, saveConsumer, getResetButtonKey(), defaultValue, this.tooltipSupplier, isRequireRestart());

        if (textGetter != null) {
            entry.setTextGetter(textGetter);
        }

        if (errorSupplier != null) {
            entry.setErrorSupplier(() -> errorSupplier.apply(entry.getValue()));
        }

        return entry;
    }
}
