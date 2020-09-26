package dev.hephaestus.clothy.impl.builders.compound;

import dev.hephaestus.clothy.impl.gui.entries.StringListListEntry;
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
public class StringListBuilder extends FieldBuilder<List<String>, StringListListEntry> {
    
    private Function<String, Optional<Text>> cellErrorSupplier;
    private Consumer<List<String>> saveConsumer = null;
    private Function<List<String>, Optional<List<Text>>> tooltipSupplier = list -> Optional.empty();
    private final List<String> value;
    private boolean expanded = false;
    private Function<StringListListEntry, StringListListEntry.StringListCell> createNewInstance;
    private Text addTooltip = new TranslatableText("text.cloth-config.list.add"), removeTooltip = new TranslatableText("text.cloth-config.list.remove");
    private boolean deleteButtonEnabled = true, insertInFront = true;
    
    public StringListBuilder(Text resetButtonKey, Text fieldNameKey, List<String> value) {
        super(resetButtonKey, fieldNameKey);
        this.value = value;
    }
    
    public Function<String, Optional<Text>> getCellErrorSupplier() {
        return cellErrorSupplier;
    }
    
    public StringListBuilder setCellErrorSupplier(Function<String, Optional<Text>> cellErrorSupplier) {
        this.cellErrorSupplier = cellErrorSupplier;
        return this;
    }
    
    public StringListBuilder setDeleteButtonEnabled(boolean deleteButtonEnabled) {
        this.deleteButtonEnabled = deleteButtonEnabled;
        return this;
    }
    
    public StringListBuilder setInsertInFront(boolean insertInFront) {
        this.insertInFront = insertInFront;
        return this;
    }
    
    public StringListBuilder setAddButtonTooltip(Text addTooltip) {
        this.addTooltip = addTooltip;
        return this;
    }
    
    public StringListBuilder setRemoveButtonTooltip(Text removeTooltip) {
        this.removeTooltip = removeTooltip;
        return this;
    }
    
    public StringListBuilder setCreateNewInstance(Function<StringListListEntry, StringListListEntry.StringListCell> createNewInstance) {
        this.createNewInstance = createNewInstance;
        return this;
    }
    
    public StringListBuilder setExpanded(boolean expanded) {
        this.expanded = expanded;
        return this;
    }

    @NotNull
    @Override
    public StringListListEntry build() {
        StringListListEntry entry = new StringListListEntry(getFieldNameKey(), value, expanded, null, saveConsumer, defaultValue, getResetButtonKey(), isRequireRestart(), deleteButtonEnabled, insertInFront);
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
