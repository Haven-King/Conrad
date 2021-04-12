package dev.inkwell.conrad.api.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.inkwell.conrad.api.gui.screen.ConfigScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class ImageWidget extends WidgetComponent {
    private final Identifier texture;

    public ImageWidget(ConfigScreen parent, int x, int y, int width, int height, Identifier textureId) {
        super(parent, x, y, width, height);
        this.texture = textureId;
    }

    @Override
    public void renderBackground(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {

    }

    @Override
    public void renderContents(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();

        client.getTextureManager().bindTexture(this.texture);

        RenderSystem.enableBlend();
        DrawableHelper.drawTexture(matrixStack, this.x, this.y, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
        RenderSystem.disableBlend();
    }
}
