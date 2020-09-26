package dev.hephaestus.clothy.impl.builders.compound;

import dev.hephaestus.clothy.impl.gui.entries.LongListListEntry;
import dev.hephaestus.clothy.impl.builders.FieldBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class LongListBuilder extends FieldBuilder<List<Long>, LongListListEntry> {
    
    protected Function<Long, Optional<Text>> cellErrorSupplier;
    private Consumer<List<Long>> saveConsumer = null;
    private Function<List<Long>, Optional<List<Text>>> tooltipSupplier = list -> Optional.empty();
    private final List<Long> value;
    private boolean expanded = false;
    private Long min = null, max = null;
    private Function<LongListListEntry, LongListListEntry.LongListCell> createNewInstance;
    private Text addTooltip = new TranslatableText("text.cloth-config.list.add"), removeTooltip = new TranslatableText("text.cloth-config.list.remove");
    private boolean deleteButtonEnabled = true, insertInFront = true;
    
    public LongListBuilder(Text resetButtonKey, Text fieldNameKey, List<Long> value) {
        super(resetButtonKey, fieldNameKey);
        this.value = value;
    }
    
    public Function<Long, Optional<Text>> getCellErrorSupplier() {
        return cellErrorSupplier;
    }
    
    public LongListBuilder setCellErrorSupplier(Function<Long, Optional<Text>> cellErrorSupplier) {
        this.cellErrorSupplier = cellErrorSupplier;
        return this;
    }
    
    public LongListBuilder setDeleteButtonEnabled(boolean deleteButtonEnabled) {
        this.deleteButtonEnabled = deleteButtonEnabled;
        return this;
    }
    
    public LongListBuilder setInsertInFront(boolean insertInFront) {
        this.insertInFront = insertInFront;
        return this;
    }
    
    public LongListBuilder setAddButtonTooltip(Text addTooltip) {
        this.addTooltip = addTooltip;
        return this;
    }
    
    public LongListBuilder setRemoveButtonTooltip(Text removeTooltip) {
        this.removeTooltip = removeTooltip;
        return this;
    }
    
    public LongListBuilder requireRestart() {
        requireRestart(true);
        return this;
    }
    
    public LongListBuilder setCreateNewInstance(Function<LongListListEntry, LongListListEntry.LongListCell> createNewInstance) {
        this.createNewInstance = createNewInstance;
        return this;
    }
    
    public LongListBuilder setExpanded(boolean expanded) {
        this.expanded = expanded;
        return this;
    }

    public LongListBuilder setMin(long min) {
        this.min = min;
        return this;
    }
    
    public LongListBuilder setMax(long max) {
        this.max = max;
        return this;
    }
    
    public LongListBuilder removeMin() {
        this.min = null;
        return this;
    }
    
    public LongListBuilder removeMax() {
        this.max = null;
        return this;
    }

    @NotNull
    @Override
    public LongListListEntry build() {
        LongListListEntry entry = new LongListListEntry(getFieldNameKey(), value, expanded, null, saveConsumer, defaultValue, getResetButtonKey(), isRequireRestart(), deleteButtonEnabled, insertInFront);
        if (min != null)
            entry.setMinimum(min);
        if (max != null)
            entry.setMaximum(max);
        if (createNewInstance != null)
            entry.setCreateNewInstance(createNewInstance);
        entry.setCellErrorSupplier(cellErrorSupplier);
        entry.setTooltipSupplier(() -> tooltipSupplier.apply(entry.getValue()));
        entry.setAddTooltip(addTooltip);
        entry.setRemoveTooltip(removeTooltip);
        if (errorSupplier != null)
            entry.setErrorSupplier(() -> errorSupplier.apply(entry.getValue()));
        return entry;
    }
    
}
