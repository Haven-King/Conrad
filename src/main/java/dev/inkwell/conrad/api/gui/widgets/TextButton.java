/*
 * Copyright 2021 Haven King
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.inkwell.conrad.api.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.inkwell.conrad.api.gui.screen.ConfigScreen;
import dev.inkwell.conrad.api.gui.util.Alignment;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class TextButton extends WidgetComponent {
    private final Action onClick;
    private final Alignment alignment;

    protected int color;
    private MutableText text;

    public TextButton(ConfigScreen parent, int x, int y, int width, int height, int color, MutableText text, Action onClick) {
        this(parent, x, y, width, height, color, text, Alignment.CENTER, onClick);
    }

    public TextButton(ConfigScreen parent, int x, int y, int width, int height, int color, MutableText text, Alignment alignment, Action onClick) {
        super(parent, x, y, width, height);
        this.color = color;
        this.alignment = alignment;
        this.text = text;
        this.onClick = onClick;
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
        DrawableHelper.drawTexture(matrixStack, this.x, this.y, 0, 0, 46 + textureOffset * 20, this.width / 2, this.height / 2, 256, 256);
        DrawableHelper.drawTexture(matrixStack, this.x + this.width / 2, this.y, 0, 200 - this.width / 2F, 46 + textureOffset * 20, this.width / 2, this.height / 2, 256, 256);
        DrawableHelper.drawTexture(matrixStack, this.x, this.y + this.height / 2, 0, 0, 46 + textureOffset * 20 + 20 - this.height / 2F, this.width / 2, this.height / 2, 256, 256);
        DrawableHelper.drawTexture(matrixStack, this.x + this.width / 2, this.y + this.height / 2, 0, 200 - this.width / 2F, 46 + textureOffset * 20 + 20 - this.height / 2F, this.width / 2, this.height / 2, 256, 256);
    }

    @Override
    public void renderContents(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        float x;

        float width = textRenderer.getWidth(this.text);

        switch (this.alignment) {
            case LEFT:
                x = this.x + 3;
                break;
            case CENTER:
                x = this.x + this.width / 2F - (width / 2F);
                break;
            case RIGHT:
                x = this.x + this.width - 3 - width;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + this.alignment);
        }

        textRenderer.drawWithShadow(
                matrixStack,
                this.text,
                x,
                this.textYPos(),
                0xFFFFFFFF
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isMouseOver(mouseX, mouseY) && button == GLFW.GLFW_MOUSE_BUTTON_1) {
            return this.onClick.onClick(this);
        }

        return false;
    }

    public void setText(MutableText text) {
        this.text = text;
    }

    @FunctionalInterface
    public interface Action {
        boolean onClick(TextButton button);
    }
}
