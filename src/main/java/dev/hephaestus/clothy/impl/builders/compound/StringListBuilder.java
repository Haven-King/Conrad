package dev.hephaestus.clothy.impl.builders.compound;

import dev.hephaestus.clothy.impl.gui.entries.StringListListEntry;
import dev.hephaestus.conrad.api.StronglyTypedList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class StringListBuilder extends ListBuilder<String, StringListListEntry.StringListCell, StringListListEntry> {
    public StringListBuilder(Text resetButtonKey, Text fieldNameKey, @NotNull Function<StringListListEntry, StringListListEntry.StringListCell> createNewInstance) {
        super(resetButtonKey, fieldNameKey, createNewInstance);
    }

    @Override
    protected StringListListEntry baseWidget(StronglyTypedList<String> value) {
        return new StringListListEntry(getFieldNameKey(), value, expanded, this.tooltipSupplier, saveConsumer, defaultValue, getResetButtonKey(), isRequireRestart(), deleteButtonEnabled, insertInFront);
    }
}
