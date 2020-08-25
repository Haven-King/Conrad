package dev.hephaestus.clothy.gui.entries;

import dev.hephaestus.conrad.annotations.ApiStatus;
import dev.hephaestus.clothy.gui.widget.ColorDisplayWidget;
import dev.hephaestus.math.impl.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.apache.commons.lang3.StringUtils;
import dev.hephaestus.conrad.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ColorEntry extends TextFieldListEntry<Color> {
    
    private ColorDisplayWidget colorDisplayWidget;
    private boolean alpha;
    
    @ApiStatus.Internal
    @Deprecated
    public ColorEntry(Text fieldName, Color value, Text resetButtonKey, Supplier<Color> defaultValue, Consumer<Color> saveConsumer, @Nullable Supplier<Optional<List<Text>>> tooltipSupplier, boolean requiresRestart) {
        super(fieldName, Color.BLACK, resetButtonKey, defaultValue, saveConsumer, tooltipSupplier, requiresRestart);
        this.alpha = true;
        ColorValue colorValue = getColorValue(String.valueOf(value));
        if (colorValue.hasError())
            throw new IllegalArgumentException("Invalid Color: " + colorValue.getError().name());
        this.alpha = false;
        this.original = value;
        this.textFieldWidget.setText(getHexColorString(value.getColor()));
        this.colorDisplayWidget = new ColorDisplayWidget(textFieldWidget, 0, 0, 20, getColorValueColor(textFieldWidget.getText()));
        this.resetButton = new ButtonWidget(0, 0, MinecraftClient.getInstance().textRenderer.getWidth(resetButtonKey) + 6, 20, resetButtonKey, widget -> {
            ColorEntry.this.textFieldWidget.setText(getHexColorString(defaultValue.get().getColor()));
        });
    }
    
    @Override
    protected boolean isChanged(Color original, String s) {
        ColorValue colorValue = getColorValue(s);
        return colorValue.hasError() || this.original.getColor() != colorValue.color;
    }
    
    @Override
    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
        super.render(matrices, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);
        this.colorDisplayWidget.y = y;
        ColorValue value = getColorValue(textFieldWidget.getText());
        if (!value.hasError())
            colorDisplayWidget.setColor(alpha ? value.getColor() : 0xff000000 | value.getColor());
        if (MinecraftClient.getInstance().textRenderer.isRightToLeft()) {
            this.colorDisplayWidget.x = x + resetButton.getWidth() + textFieldWidget.getWidth();
        } else {
            this.colorDisplayWidget.x = textFieldWidget.x - 23;
        }
        colorDisplayWidget.render(matrices, mouseX, mouseY, delta);
    }
    
    @Override
    protected void textFieldPreRender(TextFieldWidget widget) {
        if (!getConfigError().isPresent()) {
            widget.setEditableColor(14737632);
        } else {
            widget.setEditableColor(16733525);
        }
    }
    
    @Override
    public void save() {
        if (saveConsumer != null)
            saveConsumer.accept(getValue());
    }
    
    @Override
    protected boolean isMatchDefault(String text) {
        if (!getDefaultValue().isPresent())
            return false;
        ColorValue colorValue = getColorValue(text);
        return !colorValue.hasError() && colorValue.color == getDefaultValue().get().getColor();
    }
    
    @Override
    public boolean isEdited() {
        ColorValue colorValue = getColorValue(textFieldWidget.getText());
        return colorValue.hasError() || colorValue.color != original.getColor();
    }
    
    @Override
    public Color getValue() {
        return Color.ofTransparent(getColorValueColor(textFieldWidget.getText()));
    }
    
    @Deprecated
    public void setValue(int color) {
        textFieldWidget.setText(getHexColorString(color));
    }
    
    @Override
    public Optional<Text> getError() {
        ColorValue colorValue = getColorValue(this.textFieldWidget.getText());
        if (colorValue.hasError())
            return Optional.of(new TranslatableText("text.cloth-config.error.color." + colorValue.getError().name().toLowerCase(Locale.ROOT)));
        return super.getError();
    }
    
    public void withAlpha() {
        if (!alpha) {
            this.alpha = true;
            textFieldWidget.setText(getHexColorString(original.getColor()));
        }
    }
    
    public void withoutAlpha() {
        if (alpha) {
            alpha = false;
            textFieldWidget.setText(getHexColorString(original.getColor()));
        }
    }
    
    protected String stripHexStarter(String hex) {
        if (hex.startsWith("#")) {
            return hex.substring(1);
        } else return hex;
    }
    
    protected boolean isValidColorString(String str) {
        return !getColorValue(str).hasError();
    }
    
    protected int getColorValueColor(String str) {
        return getColorValue(str).getColor();
    }
    
    protected ColorValue getColorValue(String str) {
        try {
            int color;
            if (str.startsWith("#")) {
                String stripHexStarter = stripHexStarter(str);
                if (stripHexStarter.length() > 8) return ColorError.INVALID_COLOR.toValue();
                if (!alpha && stripHexStarter.length() > 6) return ColorError.NO_ALPHA_ALLOWED.toValue();
                color = (int) Long.parseLong(stripHexStarter, 16);
            } else {
                color = (int) Long.parseLong(str);
            }
            int a = color >> 24 & 0xFF;
            if (!alpha && a > 0)
                return ColorError.NO_ALPHA_ALLOWED.toValue();
            if (a < 0 || a > 255)
                return ColorError.INVALID_ALPHA.toValue();
            int r = color >> 16 & 0xFF;
            if (r < 0 || r > 255)
                return ColorError.INVALID_RED.toValue();
            int g = color >> 8 & 0xFF;
            if (g < 0 || g > 255)
                return ColorError.INVALID_GREEN.toValue();
            int b = color & 0xFF;
            if (b < 0 || b > 255)
                return ColorError.INVALID_BLUE.toValue();
            return new ColorValue(color);
        } catch (NumberFormatException e) {
            return ColorError.INVALID_COLOR.toValue();
        }
    }
    
    protected String getHexColorString(int color) {
        return "#" + StringUtils.leftPad(Integer.toHexString(color), alpha ? 8 : 6, '0');
    }
    
    protected enum ColorError {
        NO_ALPHA_ALLOWED,
        INVALID_ALPHA,
        INVALID_RED,
        INVALID_GREEN,
        INVALID_BLUE,
        INVALID_COLOR;
        
        private ColorValue value;
        
        ColorError() {
            this.value = new ColorValue(this);
        }
        
        public ColorValue toValue() {
            return value;
        }
    }
    
    protected static class ColorValue {
        private int color = -1;
        @Nullable
        private ColorError error = null;
        
        public ColorValue(int color) {
            this.color = color;
        }
        
        public ColorValue(ColorError error) {
            this.error = error;
        }
        
        public int getColor() {
            return color;
        }
        
        @Nullable
        public ColorError getError() {
            return error;
        }
        
        public boolean hasError() {
            return getError() != null;
        }
    }
}