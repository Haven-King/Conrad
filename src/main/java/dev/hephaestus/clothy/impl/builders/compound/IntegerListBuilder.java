package dev.hephaestus.clothy.impl.builders.compound;

import dev.hephaestus.clothy.impl.gui.entries.IntegerListCell;
import dev.hephaestus.clothy.impl.gui.entries.IntegerListListEntry;
import dev.hephaestus.conrad.api.StronglyTypedList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class IntegerListBuilder extends BoundedListBuilder<Integer, IntegerListCell, IntegerListListEntry> {
    public IntegerListBuilder(Text resetButtonKey, Text fieldNameKey, @NotNull Function<IntegerListListEntry, IntegerListCell> createNewInstance) {
        super(resetButtonKey, fieldNameKey, createNewInstance);
    }

    @Override
    protected IntegerListListEntry baseWidget(StronglyTypedList<Integer> value) {
        return new IntegerListListEntry(
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
