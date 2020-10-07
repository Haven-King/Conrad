package dev.hephaestus.clothy.impl.builders.primitive;

import dev.hephaestus.clothy.impl.gui.entries.LongListEntry;
import dev.hephaestus.clothy.impl.builders.FieldBuilder;
import dev.hephaestus.conrad.impl.common.config.ValueContainer;
import dev.hephaestus.conrad.impl.common.keys.ValueKey;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
