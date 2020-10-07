package dev.hephaestus.clothy.impl.gui.entries;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class IntegerListEntry extends BoundedTextFieldListEntry<Integer> {
    private static final Function<String, String> stripCharacters = s -> {
        StringBuilder builder = new StringBuilder();
        char[] var2 = s.toCharArray();

        for (char c : var2)
            if (Character.isDigit(c) || c == '-')
                builder.append(c);
        
        return builder.toString();
    };

    public IntegerListEntry(Text fieldName, Integer value, Text resetButtonKey, Supplier<Integer> defaultValue, Consumer<Integer> saveConsumer, Function<Integer, Optional<List<Text>>> tooltipSupplier, boolean requiresRestart) {
        super(fieldName, value, resetButtonKey, defaultValue, saveConsumer, tooltipSupplier, requiresRestart);
    }

    @Override
    protected String stripAddText(String s) {
        return stripCharacters.apply(s);
    }

    @Override
    protected boolean isMatchDefault(String text) {
        return getDefaultValue().isPresent() && text.equals(this.getDefaultValue().get().toString());
    }
    
    @Override
    public Integer getValue() {
        try {
            return Integer.valueOf(textFieldWidget.getText());
        } catch (Exception e) {
            return 0;
        }
    }
}
