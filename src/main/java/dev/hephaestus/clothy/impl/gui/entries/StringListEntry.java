package dev.hephaestus.clothy.impl.gui.entries;

import org.jetbrains.annotations.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class StringListEntry extends TextFieldListEntry<String> {
    public StringListEntry(Text fieldName, String value, Text resetButtonKey, Supplier<String> defaultValue, Consumer<String> saveConsumer, @Nullable Supplier<Optional<List<Text>>> tooltipSupplier, boolean requiresRestart) {
        super(fieldName, value, resetButtonKey, defaultValue, saveConsumer, tooltipSupplier, requiresRestart);
    }
    
    @Override
    public String getValue() {
        return textFieldWidget.getText();
    }
    
    @Override
    public void save() {
        if (saveConsumer != null)
            saveConsumer.accept(getValue());
    }
    
    @Override
    protected boolean isMatchDefault(String text) {
        return getDefaultValue().isPresent() && text.equals(getDefaultValue().get());
    }
}
