package dev.hephaestus.clothy.gui.entries;

import dev.hephaestus.conrad.annotations.ApiStatus;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import dev.hephaestus.conrad.annotations.NotNull;
import dev.hephaestus.conrad.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class StringListListEntry extends AbstractTextFieldListListEntry<String, StringListListEntry.StringListCell, StringListListEntry> {
    
    @ApiStatus.Internal
    @Deprecated
    public StringListListEntry(Text fieldName, List<String> value, boolean defaultExpanded, @Nullable Supplier<Optional<List<Text>>> tooltipSupplier, Consumer<List<String>> saveConsumer, Supplier<List<String>> defaultValue, Text resetButtonKey) {
        this(fieldName, value, defaultExpanded, tooltipSupplier, saveConsumer, defaultValue, resetButtonKey, false);
    }
    
    @ApiStatus.Internal
    @Deprecated
    public StringListListEntry(Text fieldName, List<String> value, boolean defaultExpanded, @Nullable Supplier<Optional<List<Text>>> tooltipSupplier, Consumer<List<String>> saveConsumer, Supplier<List<String>> defaultValue, Text resetButtonKey, boolean requiresRestart) {
        this(fieldName, value, defaultExpanded, tooltipSupplier, saveConsumer, defaultValue, resetButtonKey, requiresRestart, true, true);
    }
    
    @ApiStatus.Internal
    @Deprecated
    public StringListListEntry(Text fieldName, List<String> value, boolean defaultExpanded, @Nullable Supplier<Optional<List<Text>>> tooltipSupplier, Consumer<List<String>> saveConsumer, Supplier<List<String>> defaultValue, Text resetButtonKey, boolean requiresRestart, boolean deleteButtonEnabled, boolean insertInFront) {
        super(fieldName, value, defaultExpanded, tooltipSupplier, saveConsumer, defaultValue, resetButtonKey, requiresRestart, deleteButtonEnabled, insertInFront, StringListCell::new);
    }
    
    @Override
    public StringListListEntry self() {
        return this;
    }
    
    public static class StringListCell extends AbstractTextFieldListCell<String, StringListCell, StringListListEntry> {
        
        public StringListCell(String value, StringListListEntry listListEntry) {
            super(value, listListEntry);
        }
        
        @Nullable
        @Override
        protected String substituteDefault(@Nullable String value) {
            if (value == null)
                return "";
            else
                return value;
        }
        
        @Override
        protected boolean isValidText(@NotNull String text) {
            return true;
        }
        
        @Override
        public String getValue() {
            return widget.getText();
        }
        
        @Override
        public Optional<Text> getError() {
            return Optional.empty();
        }
        
    }
    
}
