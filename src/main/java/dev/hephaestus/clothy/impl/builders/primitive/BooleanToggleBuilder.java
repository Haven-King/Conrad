package dev.hephaestus.clothy.impl.builders.primitive;

import dev.hephaestus.clothy.impl.gui.entries.BooleanListEntry;
import dev.hephaestus.clothy.impl.builders.FieldBuilder;
import dev.hephaestus.conrad.impl.common.config.ValueContainer;
import dev.hephaestus.conrad.impl.common.keys.ValueKey;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class BooleanToggleBuilder extends FieldBuilder<Boolean, BooleanListEntry> {
    
    @Nullable private Function<Boolean, Text> yesNoTextSupplier = null;
    
    public BooleanToggleBuilder(Text resetButtonKey, Text fieldNameKey) {
        super(resetButtonKey, fieldNameKey);
    }

    @Override
    protected BooleanListEntry withValue(Boolean value) {
        BooleanListEntry entry = new BooleanListEntry(getFieldNameKey(), value, getResetButtonKey(), defaultValue, saveConsumer, this.tooltipSupplier, isRequireRestart()) {
            @Override
            public Text getYesNoText(boolean bool) {
                if (yesNoTextSupplier == null)
                    return super.getYesNoText(bool);
                return yesNoTextSupplier.apply(bool);
            }
        };

        if (this.errorSupplier != null) {
            entry.setErrorSupplier(() -> errorSupplier.apply(entry.getValue()));
        }

        return entry;
    }

    @Nullable
    public Function<Boolean, Text> getYesNoTextSupplier() {
        return yesNoTextSupplier;
    }
    
    public BooleanToggleBuilder setYesNoTextSupplier(@Nullable Function<Boolean, Text> yesNoTextSupplier) {
        this.yesNoTextSupplier = yesNoTextSupplier;
        return this;
    }
}
