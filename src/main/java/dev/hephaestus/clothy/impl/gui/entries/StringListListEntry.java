package dev.hephaestus.clothy.impl.gui.entries;

import dev.hephaestus.conrad.api.StronglyTypedList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class StringListListEntry extends AbstractTextFieldListListEntry<String, StringListListEntry.StringListCell, StringListListEntry> {
    public StringListListEntry(Text fieldName, StronglyTypedList<String> value, boolean defaultExpanded, @NotNull Function<StronglyTypedList<String>, Optional<List<Text>>> tooltipSupplier, Consumer<StronglyTypedList<String>> saveConsumer, Supplier<StronglyTypedList<String>> defaultValue, Text resetButtonKey, boolean requiresRestart, boolean deleteButtonEnabled, boolean insertInFront) {
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
