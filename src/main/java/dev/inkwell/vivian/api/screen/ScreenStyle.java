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

package dev.inkwell.vivian.api.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.inkwell.vivian.api.DrawableExtensions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;

import static dev.inkwell.vivian.impl.Vivian.BLUR;

@Environment(EnvType.CLIENT)
public class ScreenStyle extends DrawableHelper implements DrawableExtensions {
    public static final ScreenStyle DEFAULT = new ScreenStyle();
    public static final ScreenStyle TITLE;

    static {
        TITLE = new ScreenStyle() {
            private final RotatingCubeMapRenderer backgroundRenderer =
                    new RotatingCubeMapRenderer(TitleScreen.PANORAMA_CUBE_MAP);

            @Override
            protected void renderBackground(ConfigScreen screen, Screen parent, MatrixStack matrices, float tickDelta) {
                fill(matrices, 0, 0, 1000, 1000, 0xFFFFFFFF, 1F);
                this.backgroundRenderer.render(tickDelta, 1F);
            }
        };

        TITLE.blurAmount = 8F;
    }

    public ElementStyle<Style> categoryColor;
    public Style sectionColor;
    public ElementStyle<Style> labelColor;
    public ElementStyle<Integer> scrollbarColor = new ElementStyle<>(
            0xBB888888,
            0xBBAAAAAA,
            0xBBCCCCCC);

    public int accentColor;
    public int gradientColor;
    public float blurAmount = 0F;
    public Identifier backgroundTexture;
    public int backgroundColor = 0xFF404040;

    public ScreenStyle() {
        this.categoryColor = new ElementStyle<>(
                Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFFFF)).withBold(true).withUnderline(true),
                Style.EMPTY.withColor(TextColor.fromRgb(0xFFAAAAFF)).withBold(true),
                Style.EMPTY.withColor(TextColor.fromRgb(0xFFAAAAAA))
        );

        this.sectionColor = Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFFFF));
        this.labelColor = new ElementStyle<>(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFFFF)));
        this.accentColor = 0xFFFFFFFF;
        this.gradientColor = 0x00000000;
    }

    public void renderScrollbar(MatrixStack matrices, int x, int y, int width, int height, boolean active, boolean hovered) {
        fill(matrices, x, y, x + width, y + height, scrollbarColor.color(active, hovered), 1F);
    }

    public void renderCategoryButtonDecorations(CategoryButtonWidget button, MatrixStack matrices, int x, int y, int width, int height) {

    }

    public final void renderBackgroundFromPresets(ConfigScreen screen, Screen parent, MatrixStack matrices, float tickDelta) {
        this.renderBackground(screen, parent, matrices, tickDelta);

        if (blurAmount > 0F) {
            BLUR.setUniformValue("Start", 0F, 0F);
            BLUR.setUniformValue("End", 1F, 1F);
            BLUR.setUniformValue("Progress", 1F);
            BLUR.setUniformValue("Radius", blurAmount);
            BLUR.render(1F);
        }
    }

    protected void renderBackground(ConfigScreen screen, Screen parent, MatrixStack matrices, float tickDelta) {
        if (parent == null && backgroundTexture == null) {
            screen.renderBackground(matrices);
        } else if (backgroundTexture != null) {
            int a = this.backgroundColor >> 24;
            int r = (this.backgroundColor >> 16) & 0xFF;
            int g = (this.backgroundColor >> 8) & 0xFF;
            int b = (this.backgroundColor) & 0xFF;

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            MinecraftClient.getInstance().getTextureManager().bindTexture(this.backgroundTexture);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
            bufferBuilder.vertex(0.0D, screen.height, 0.0D).texture(0.0F, (float) screen.height / 32.0F).color(r, g, b, a).next();
            bufferBuilder.vertex(screen.width, screen.height, 0.0D).texture(screen.width / 32.0F, screen.height / 32.0F).color(r, g, b, a).next();
            bufferBuilder.vertex(screen.width, 0.0D, 0.0D).texture(screen.width / 32.0F, 0).color(r, g, b, a).next();
            bufferBuilder.vertex(0.0D, 0.0D, 0.0D).texture(0.0F, 0).color(r, g, b, a).next();
            tessellator.draw();
        } else {
            parent.renderBackground(matrices);
        }
    }

    public void renderDecorations(MatrixStack matrices, int mouseX, int mouseY, float delta, int screenWidth, int screenHeight, int headerHeight) {
        fillGradient(matrices, 0, 0, screenWidth, screenHeight / 8, 0x88000000 | (gradientColor & 0x00FFFFFF), (gradientColor & 0x00FFFFFF));
        fillGradient(matrices, 0, screenHeight - screenHeight / 8, screenWidth, screenHeight, (gradientColor & 0x00FFFFFF), 0x88000000 | (gradientColor & 0x00FFFFFF));

        line(matrices, 0, screenWidth, headerHeight / 2F, headerHeight / 2F, 0x88000000 | (accentColor & 0x00FFFFFF));
    }

    public interface FromOrdinal<T> {
        T fromOrdinal(int offset);
    }

    public static class ElementStyle<T> {
        public T active;
        public T hovered;
        public T base;

        public ElementStyle(T base) {
            this(base, base, base);
        }

        public ElementStyle(T active, T hovered, T base) {
            this.active = active;
            this.hovered = hovered;
            this.base = base;
        }

        public T color(boolean active, boolean hovered) {
            return active ? this.active : hovered ? this.hovered : this.base;
        }
    }
}
