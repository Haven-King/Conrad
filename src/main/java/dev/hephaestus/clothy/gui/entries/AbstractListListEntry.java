package dev.hephaestus.clothy.gui.entries;

import dev.hephaestus.conrad.annotations.ApiStatus;
import dev.hephaestus.conrad.annotations.Nullable;
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
    
    @ApiStatus.Internal
    public AbstractListListEntry(Text fieldName, List<T> value, boolean defaultExpanded, @Nullable Supplier<Optional<List<Text>>> tooltipSupplier, Consumer<List<T>> saveConsumer, Supplier<List<T>> defaultValue, Text resetButtonKey, boolean requiresRestart, boolean deleteButtonEnabled, boolean insertInFront, BiFunction<T, SELF, C> createNewCell) {
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
    public List<T> getValue() {
        return cells.stream().map(C::getValue).collect(Collectors.toList());
    }
    
    @Override
    protected C getFromValue(T value) {
        return createNewCell.apply(value, this.self());
    }

}
