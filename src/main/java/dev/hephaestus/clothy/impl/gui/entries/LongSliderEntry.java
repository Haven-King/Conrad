package dev.hephaestus.clothy.impl.gui.entries;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class LongSliderEntry extends SliderEntry<Long> {
    
    protected Slider sliderWidget;
    protected ButtonWidget resetButton;
    protected AtomicLong value;
    protected final long orginial;
    private long minimum, maximum;
    private Function<Long, Text> textGetter = value -> new LiteralText(String.format("Value: %d", value));
    private List<Element> widgets;
    
    public LongSliderEntry(Text fieldName, long minimum, long maximum, long value, Consumer<Long> saveConsumer, Text resetButtonKey, Supplier<Long> defaultValue) {
        this(fieldName, minimum, maximum, value, saveConsumer, resetButtonKey, defaultValue, l -> Optional.empty());
    }
    
    public LongSliderEntry(Text fieldName, long minimum, long maximum, long value, Consumer<Long> saveConsumer, Text resetButtonKey, Supplier<Long> defaultValue, @NotNull Function<Long, Optional<List<Text>>> tooltipSupplier) {
        this(fieldName, minimum, maximum, value, saveConsumer, resetButtonKey, defaultValue, tooltipSupplier, false);
    }
    
    public LongSliderEntry(Text fieldName, long minimum, long maximum, long value, Consumer<Long> saveConsumer, Text resetButtonKey, Supplier<Long> defaultValue, @NotNull Function<Long, Optional<List<Text>>> tooltipSupplier, boolean requiresRestart) {
        super(fieldName, tooltipSupplier, requiresRestart, saveConsumer, defaultValue);
        this.orginial = value;
        this.value = new AtomicLong(value);
        this.maximum = maximum;
        this.minimum = minimum;
        this.sliderWidget = new Slider(0, 0, 152, 20, ((double) LongSliderEntry.this.value.get() - minimum) / Math.abs(maximum - minimum));
        this.resetButton = new ButtonWidget(0, 0, MinecraftClient.getInstance().textRenderer.getWidth(resetButtonKey) + 6, 20, resetButtonKey, widget -> {
            setValue(defaultValue.get());
        });
        this.sliderWidget.setMessage(textGetter.apply(LongSliderEntry.this.value.get()));
        this.widgets = Lists.newArrayList(sliderWidget, resetButton);
    }
    
    public Function<Long, Text> getTextGetter() {
        return textGetter;
    }
    
    public LongSliderEntry setTextGetter(Function<Long, Text> textGetter) {
        this.textGetter = textGetter;
        this.sliderWidget.setMessage(textGetter.apply(LongSliderEntry.this.value.get()));
        return this;
    }
    
    @Override
    public Long getValue() {
        return value.get();
    }
    
    @Deprecated
    public void setValue(long value) {
        sliderWidget.setValue((MathHelper.clamp(value, minimum, maximum) - minimum) / (double) Math.abs(maximum - minimum));
        this.value.set(Math.min(Math.max(value, minimum), maximum));
        sliderWidget.updateMessage();
    }

    @Override
    public List<? extends Element> children() {
        return widgets;
    }
    
    @Override
    public boolean isModified() {
        return super.isModified() || getValue() != orginial;
    }
    
    public LongSliderEntry setMaximum(long maximum) {
        this.maximum = maximum;
        return this;
    }
    
    public LongSliderEntry setMinimum(long minimum) {
        this.minimum = minimum;
        return this;
    }
    
    @Override
    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
        super.render(matrices, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);
        Window window = MinecraftClient.getInstance().getWindow();
        this.resetButton.active = isEditable() && getDefaultValue().isPresent() && this.getDefaultValue().get() != value.get();
        this.resetButton.y = y;
        this.sliderWidget.active = isEditable();
        this.sliderWidget.y = y;
        Text displayedFieldName = getDisplayedFieldName();
        if (MinecraftClient.getInstance().textRenderer.isRightToLeft()) {
            MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, displayedFieldName.asOrderedText(), window.getScaledWidth() - x - MinecraftClient.getInstance().textRenderer.getWidth(displayedFieldName), y + 6, getPreferredTextColor());
            this.resetButton.x = x;
            this.sliderWidget.x = x + resetButton.getWidth() + 1;
        } else {
            MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, displayedFieldName.asOrderedText(), x, y + 6, getPreferredTextColor());
            this.resetButton.x = x + entryWidth - resetButton.getWidth();
            this.sliderWidget.x = x + entryWidth - 150;
        }
        this.sliderWidget.setWidth(150 - resetButton.getWidth() - 2);
        resetButton.render(matrices, mouseX, mouseY, delta);
        sliderWidget.render(matrices, mouseX, mouseY, delta);
    }
    
    private class Slider extends SliderWidget {
        protected Slider(int int_1, int int_2, int int_3, int int_4, double double_1) {
            super(int_1, int_2, int_3, int_4, NarratorManager.EMPTY, double_1);
        }
        
        @Override
        public void updateMessage() {
            setMessage(textGetter.apply(LongSliderEntry.this.value.get()));
        }
        
        @Override
        protected void applyValue() {
            LongSliderEntry.this.value.set((long) (minimum + Math.abs(maximum - minimum) * value));
        }
        
        @Override
        public boolean keyPressed(int int_1, int int_2, int int_3) {
            if (!isEditable())
                return false;
            return super.keyPressed(int_1, int_2, int_3);
        }
        
        @Override
        public boolean mouseDragged(double double_1, double double_2, int int_1, double double_3, double double_4) {
            if (!isEditable())
                return false;
            return super.mouseDragged(double_1, double_2, int_1, double_3, double_4);
        }
        
        public double getValue() {
            return value;
        }
        
        public void setValue(double integer) {
            this.value = integer;
        }
    }
    
}
