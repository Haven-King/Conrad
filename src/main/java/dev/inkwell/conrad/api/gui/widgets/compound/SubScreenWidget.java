package dev.inkwell.conrad.api.gui.widgets.compound;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.inkwell.conrad.api.gui.builders.ConfigScreenBuilder;
import dev.inkwell.conrad.api.gui.screen.ConfigScreen;
import dev.inkwell.conrad.api.gui.screen.ScreenStyle;
import dev.inkwell.conrad.api.gui.widgets.value.ValueWidgetComponent;
import dev.inkwell.conrad.api.value.ConfigDefinition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class SubScreenWidget<T> extends ValueWidgetComponent<T> implements ConfigScreenBuilder {
    protected final ConfigDefinition<?> config;
    protected final Text name;
    protected ConfigScreen screen;

    public SubScreenWidget(ConfigDefinition<?> config, ConfigScreen parent, int x, int y, Supplier<@NotNull T> defaultValueSupplier, Consumer<T> changedListener, Consumer<T> saveConsumer, @NotNull T value, Text name) {
        super(parent, x, y, 20, 20, defaultValueSupplier, changedListener, saveConsumer, value);
        this.config = config;
        this.name = name;
    }

    @Override
    public void renderContents(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        drawCenteredString(
                matrixStack,
                textRenderer,
                "▶",
                this.x + this.width / 2F,
                this.textYPos(),
                0xFFFFFFFF
        );
    }

    @Override
    public boolean isFixedSize() {
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isMouseOver(mouseX, mouseY)) {
            this.parent.tryLeave(() ->
                    MinecraftClient.getInstance().openScreen((this.screen = new ConfigScreen(this.parent, this, LiteralText.EMPTY))));
        }

        return false;
    }

    @Override
    public void renderBackground(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        minecraftClient.getTextureManager().bindTexture(AbstractButtonWidget.WIDGETS_LOCATION);
        int textureOffset = this.isMouseOver(mouseX, mouseY) ? 2 : 1;
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        DrawableHelper.drawTexture(matrixStack, this.x + this.width - 20, this.y, 0, 0, 46 + textureOffset * 20, 10, this.height, 256, 256);
        DrawableHelper.drawTexture(matrixStack, this.x + this.width - 10, this.y, 0, 200 - 10, 46 + textureOffset * 20, 10, this.height, 256, 256);
    }

    @Override
    public ScreenStyle getStyle() {
        return this.parent.getStyle();
    }

    @Override
    protected Text getDefaultValueAsText() {
        return new LiteralText(this.getDefaultValue().toString());
    }
}
