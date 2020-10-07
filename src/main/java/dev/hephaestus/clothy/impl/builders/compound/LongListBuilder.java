package dev.hephaestus.clothy.impl.builders.compound;

import dev.hephaestus.clothy.impl.gui.entries.LongListCell;
import dev.hephaestus.clothy.impl.gui.entries.LongListListEntry;
import dev.hephaestus.conrad.api.StronglyTypedList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class LongListBuilder extends BoundedListBuilder<Long, LongListCell, LongListListEntry> {
    public LongListBuilder(Text resetButtonKey, Text fieldNameKey, @NotNull Function<LongListListEntry, LongListCell> createNewInstance) {
        super(resetButtonKey, fieldNameKey, createNewInstance);
    }

    @Override
    protected LongListListEntry baseWidget(StronglyTypedList<Long> value) {
        return new LongListListEntry(
                this.getFieldNameKey(),
                value,
                this.expanded,
                this.tooltipSupplier,
                this.saveConsumer,
                this.defaultValue,
                this.getResetButtonKey(),
                this.isRequireRestart(),
                this.deleteButtonEnabled,
                this.insertInFront
        );
    }
}
