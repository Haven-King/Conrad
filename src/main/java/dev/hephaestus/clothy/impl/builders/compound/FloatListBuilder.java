package dev.hephaestus.clothy.impl.builders.compound;

import dev.hephaestus.clothy.impl.gui.entries.FloatListCell;
import dev.hephaestus.clothy.impl.gui.entries.FloatListListEntry;
import dev.hephaestus.conrad.api.StronglyTypedList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class FloatListBuilder extends BoundedListBuilder<Float, FloatListCell, FloatListListEntry> {
    public FloatListBuilder(Text resetButtonKey, Text fieldNameKey, @NotNull Function<FloatListListEntry, FloatListCell> createNewInstance) {
        super(resetButtonKey, fieldNameKey, createNewInstance);
    }

    @Override
    protected FloatListListEntry baseWidget(StronglyTypedList<Float> value) {
        return new FloatListListEntry(
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
