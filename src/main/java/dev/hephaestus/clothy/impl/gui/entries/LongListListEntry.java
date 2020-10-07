package dev.hephaestus.clothy.impl.gui.entries;

import dev.hephaestus.conrad.api.StronglyTypedList;
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
public class LongListListEntry extends BoundedListListEntry<Long, LongListCell, LongListListEntry> {
    public LongListListEntry(Text fieldName, StronglyTypedList<Long> value, boolean defaultExpanded, @NotNull Function<StronglyTypedList<Long>, Optional<List<Text>>> tooltipSupplier, Consumer<StronglyTypedList<Long>> saveConsumer, Supplier<StronglyTypedList<Long>> defaultValue, Text resetButtonKey, boolean requiresRestart, boolean deleteButtonEnabled, boolean insertInFront) {
        super(fieldName, value, defaultExpanded, tooltipSupplier, saveConsumer, defaultValue, resetButtonKey, requiresRestart, deleteButtonEnabled, insertInFront, LongListCell::new);
    }
    
    @Override
    public LongListListEntry self() {
        return this;
    }

}
