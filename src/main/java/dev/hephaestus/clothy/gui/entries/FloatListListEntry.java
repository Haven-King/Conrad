package dev.hephaestus.clothy.gui.entries;

import dev.hephaestus.conrad.annotations.ApiStatus;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import dev.hephaestus.conrad.annotations.NotNull;
import dev.hephaestus.conrad.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class FloatListListEntry extends AbstractTextFieldListListEntry<Float, FloatListListEntry.FloatListCell, FloatListListEntry> {
    
    private float minimum, maximum;
    
    @ApiStatus.Internal
    @Deprecated
    public FloatListListEntry(Text fieldName, List<Float> value, boolean defaultExpanded, @Nullable Supplier<Optional<List<Text>>> tooltipSupplier, Consumer<List<Float>> saveConsumer, Supplier<List<Float>> defaultValue, Text resetButtonKey) {
        this(fieldName, value, defaultExpanded, tooltipSupplier, saveConsumer, defaultValue, resetButtonKey, false);
    }
    
    @ApiStatus.Internal
    @Deprecated
    public FloatListListEntry(Text fieldName, List<Float> value, boolean defaultExpanded, @Nullable Supplier<Optional<List<Text>>> tooltipSupplier, Consumer<List<Float>> saveConsumer, Supplier<List<Float>> defaultValue, Text resetButtonKey, boolean requiresRestart) {
        this(fieldName, value, defaultExpanded, tooltipSupplier, saveConsumer, defaultValue, resetButtonKey, requiresRestart, true, true);
    }
    
    @ApiStatus.Internal
    @Deprecated
    public FloatListListEntry(Text fieldName, List<Float> value, boolean defaultExpanded, @Nullable Supplier<Optional<List<Text>>> tooltipSupplier, Consumer<List<Float>> saveConsumer, Supplier<List<Float>> defaultValue, Text resetButtonKey, boolean requiresRestart, boolean deleteButtonEnabled, boolean insertInFront) {
        super(fieldName, value, defaultExpanded, tooltipSupplier, saveConsumer, defaultValue, resetButtonKey, requiresRestart, deleteButtonEnabled, insertInFront, FloatListCell::new);
        this.minimum = Float.NEGATIVE_INFINITY;
        this.maximum = Float.POSITIVE_INFINITY;
    }
    
    public FloatListListEntry setMaximum(float maximum) {
        this.maximum = maximum;
        return this;
    }
    
    public FloatListListEntry setMinimum(float minimum) {
        this.minimum = minimum;
        return this;
    }
    
    @Override
    public FloatListListEntry self() {
        return this;
    }
    
    public static class FloatListCell extends AbstractTextFieldListCell<Float, FloatListCell, FloatListListEntry> {
        
        public FloatListCell(Float value, FloatListListEntry listListEntry) {
            super(value, listListEntry);
        }
        
        @Nullable
        @Override
        protected Float substituteDefault(@Nullable Float value) {
            if (value == null)
                return 0f;
            else
                return value;
        }
        
        @Override
        protected boolean isValidText(@NotNull String text) {
            return text.chars().allMatch(c -> Character.isDigit(c) || c == '-' || c == '.');
        }
        
        public Float getValue() {
            try {
                return Float.valueOf(widget.getText());
            } catch (NumberFormatException e) {
                return 0f;
            }
        }
        
        @Override
        public Optional<Text> getError() {
            try {
                float i = Float.parseFloat(widget.getText());
                if (i > listListEntry.maximum)
                    return Optional.of(new TranslatableText("text.cloth-config.error.too_large", listListEntry.maximum));
                else if (i < listListEntry.minimum)
                    return Optional.of(new TranslatableText("text.cloth-config.error.too_small", listListEntry.minimum));
            } catch (NumberFormatException ex) {
                return Optional.of(new TranslatableText("text.cloth-config.error.not_valid_number_float"));
            }
            return Optional.empty();
        }
    }
}