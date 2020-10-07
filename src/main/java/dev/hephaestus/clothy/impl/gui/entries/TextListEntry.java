package dev.hephaestus.clothy.impl.gui.entries;

import net.minecraft.text.LiteralText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class TextListEntry extends TooltipListEntry<Text> {
    
    private int savedWidth = -1;
    private final int color;
    private final Text text;

    public TextListEntry(Text fieldName, Text text, int color, @NotNull Function<Text, Optional<List<Text>>> tooltipSupplier) {
        super(fieldName, tooltipSupplier, t -> {}, () -> null);
        this.text = text;
        this.color = color;
    }
    
    @Override
    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
        super.render(matrices, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);
        this.savedWidth = entryWidth;
        int yy = y + 4;
        List<OrderedText> strings = MinecraftClient.getInstance().textRenderer.wrapLines(text, savedWidth);
        for (OrderedText string : strings) {
            MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, string, x, yy, color);
            yy += MinecraftClient.getInstance().textRenderer.fontHeight + 3;
        }
    }
    
    @Override
    public int getItemHeight() {
        if (savedWidth == -1)
            return 12;
        List<OrderedText> strings = MinecraftClient.getInstance().textRenderer.wrapLines(text, savedWidth);
        if (strings.isEmpty())
            return 0;
        return 15 + strings.size() * 12;
    }

    @Override
    public Text getValue() {
        return LiteralText.EMPTY;
    }

    @Override
    public List<? extends Element> children() {
        return Collections.emptyList();
    }
    
}
