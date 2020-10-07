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
public class FloatListListEntry extends BoundedListListEntry<Float, FloatListCell, FloatListListEntry> {
    public FloatListListEntry(Text fieldName, StronglyTypedList<Float> value, boolean defaultExpanded, @NotNull Function<StronglyTypedList<Float>, Optional<List<Text>>> tooltipSupplier, Consumer<StronglyTypedList<Float>> saveConsumer, Supplier<StronglyTypedList<Float>> defaultValue, Text resetButtonKey, boolean requiresRestart, boolean deleteButtonEnabled, boolean insertInFront) {
        super(fieldName, value, defaultExpanded, tooltipSupplier, saveConsumer, defaultValue, resetButtonKey, requiresRestart, deleteButtonEnabled, insertInFront, FloatListCell::new);
    }

    @Override
    public FloatListListEntry self() {
        return this;
    }
}
