package dev.hephaestus.clothy.impl.gui.entries;

import dev.hephaestus.conrad.api.StronglyTypedList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @param <T>    the configuration object type
 * @param <C>    the cell type
 * @param <SELF> the "curiously recurring template pattern" type parameter
 * @see BaseListEntry
 */
@Environment(EnvType.CLIENT)
public abstract class AbstractListListEntry<T, C extends AbstractListCell<T, C, SELF>, SELF extends AbstractListListEntry<T, C, SELF>> extends BaseListEntry<T, C, SELF> {
    
    protected final BiFunction<T, SELF, C> createNewCell;
    protected Function<T, Optional<Text>> cellErrorSupplier;
    
    public AbstractListListEntry(Text fieldName, StronglyTypedList<T> value, boolean defaultExpanded, @NotNull Function<StronglyTypedList<T>, Optional<List<Text>>> tooltipSupplier, Consumer<StronglyTypedList<T>> saveConsumer, Supplier<StronglyTypedList<T>> defaultValue, Text resetButtonKey, boolean requiresRestart, boolean deleteButtonEnabled, boolean insertInFront, BiFunction<T, SELF, C> createNewCell) {
        super(fieldName, tooltipSupplier, defaultValue, abstractListListEntry -> createNewCell.apply(null, abstractListListEntry), saveConsumer, resetButtonKey, requiresRestart, deleteButtonEnabled, insertInFront);
        this.createNewCell = createNewCell;
        for (T f : value)
            cells.add(createNewCell.apply(f, this.self()));
        this.widgets.addAll(cells);
        setExpanded(defaultExpanded);
    }

    public Function<T, Optional<Text>> getCellErrorSupplier() {
        return cellErrorSupplier;
    }

    public void setCellErrorSupplier(Function<T, Optional<Text>> cellErrorSupplier) {
        this.cellErrorSupplier = cellErrorSupplier;
    }

    @Override
    public StronglyTypedList<T> getValue() {
        StronglyTypedList<T> value = new StronglyTypedList<T>(this.getDefaultValue().get().valueClass);
        value.addAll(cells.stream().map(C::getValue).collect(Collectors.toList()));
        return value;
    }
    
    @Override
    protected C getFromValue(T value) {
        return createNewCell.apply(value, this.self());
    }

}
