package dev.hephaestus.clothy.impl.builders.compound;

import dev.hephaestus.clothy.impl.gui.entries.DoubleListCell;
import dev.hephaestus.clothy.impl.gui.entries.DoubleListListEntry;
import dev.hephaestus.conrad.api.StronglyTypedList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class DoubleListBuilder extends BoundedListBuilder<Double, DoubleListCell, DoubleListListEntry> {
    public DoubleListBuilder(Text resetButtonKey, Text fieldNameKey, @NotNull Function<DoubleListListEntry, DoubleListCell> createNewInstance) {
        super(resetButtonKey, fieldNameKey, createNewInstance);
    }

    @Override
    protected DoubleListListEntry baseWidget(StronglyTypedList<Double> value) {
        return new DoubleListListEntry(
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
