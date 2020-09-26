package dev.hephaestus.clothy.impl.gui;

import org.jetbrains.annotations.Nullable;
import dev.hephaestus.clothy.api.Tooltip;
import dev.hephaestus.math.impl.Point;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AbstractPressableButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class ClothConfigTabButton extends AbstractPressableButtonWidget {
    
    private final int index;
    private final ClothConfigScreen screen;
    @Nullable private final Supplier<Optional<List<Text>>> tooltipSupplier;
    
    public ClothConfigTabButton(ClothConfigScreen screen, int index, int int_1, int int_2, int int_3, int int_4, Text string_1, Supplier<Optional<List<Text>>> tooltipSupplier) {
        super(int_1, int_2, int_3, int_4, string_1);
        this.index = index;
        this.screen = screen;
        this.tooltipSupplier = tooltipSupplier;
    }

    public ClothConfigTabButton(ClothConfigScreen screen, int index, int int_1, int int_2, int int_3, int int_4, Text string_1) {
        this(screen, index, int_1, int_2, int_3, int_4, string_1, null);
    }
    
    @Override
    public void onPress() {
        if (index != -1)
            screen.selectedCategoryIndex = index;
        screen.init(MinecraftClient.getInstance(), screen.width, screen.height);
    }
    
    @Override
    public void render(MatrixStack matrices, int int_1, int int_2, float float_1) {
        active = index != screen.selectedCategoryIndex;
        super.render(matrices, int_1, int_2, float_1);

        if (isMouseOver(int_1, int_2)) {
            Optional<List<Text>> tooltip = getTooltip();
            if (tooltip.isPresent() && tooltip.get().size() > 0)
                screen.addTooltip(Tooltip.of(new Point(int_1, int_2), tooltip.get()));
        }
    }
    
    @Override
    protected boolean clicked(double double_1, double double_2) {
        return visible && active && isMouseOver(double_1, double_2);
    }
    
    @Override
    public boolean isMouseOver(double double_1, double double_2) {
        return this.visible && double_1 >= this.x && double_2 >= this.y && double_1 < this.x + this.width && double_2 < this.y + this.height && double_1 >= 20 && double_1 < screen.width - 20;
    }

    public Optional<List<Text>> getTooltip() {
        if (tooltipSupplier != null)
            return tooltipSupplier.get();
        return Optional.empty();
    }

    public @Nullable Supplier<Optional<List<Text>>> getTooltipSupplier() {
        return tooltipSupplier;
    }
}
