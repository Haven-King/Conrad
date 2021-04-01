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

package dev.inkwell.conrad.api.gui.screen;

import dev.inkwell.conrad.api.gui.DrawableExtensions;
import dev.inkwell.conrad.impl.gui.ConradGuiConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class FancyButton extends ButtonWidget implements DrawableExtensions, TooltipAccess {
    private final List<Text> tooltips = new ArrayList<>();

    private int hoverColor = 0xFFFFFFFF;
    private int backgroundColor = 0x44FFFFFF;
    private final ConfigScreen parent;
    private float hoverOpacity = 0F;

    public FancyButton(ConfigScreen parent, int x, int y, int width, int height, Text message, PressAction onPress) {
        super(x, y, width, height, message, onPress);
        this.parent = parent;
    }

    public FancyButton withBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public FancyButton withHoverColor(int hoverColor) {
        this.hoverColor = hoverColor;
        return this;
    }

    public FancyButton withColors(int backgroundColor, int hoverColor) {
        this.backgroundColor = backgroundColor;
        this.hoverColor = hoverColor;
        return this;
    }

    public FancyButton withTooltips(Text... tooltips) {
        this.tooltips.addAll(Arrays.asList(tooltips));
        return this;
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        TextRenderer textRenderer = minecraftClient.textRenderer;
        DrawableHelper.fill(matrices, this.x, this.y, this.x + this.width, this.y + this.height, this.backgroundColor);

        if (ConradGuiConfig.Animations.ENABLED.getValue()) {
            if (isMouseOver(mouseX, mouseY) && (this.parent.getFocused() == null || this.parent.getFocused() == this)) {
                hoverOpacity = Math.min(1F, ConradGuiConfig.Animations.SPEED.getValue() + hoverOpacity);
            } else {
                hoverOpacity = Math.max(0F, hoverOpacity - ConradGuiConfig.Animations.SPEED.getValue());
            }
        } else {
            hoverOpacity = this.isMouseOver(mouseX, mouseY) ? 1F : 0F;
        }

        if (isMouseOver(mouseX, mouseY)) {
            this.parent.addTooltips(this);
        }

        fill(matrices, x, y, x + width, y + getHeight(), hoverColor, hoverOpacity * 0.75F);
        drawCenteredText(matrices, textRenderer, this.getMessage(), this.x + this.width / 2F, this.y + (this.height - 8) / 2F, 0xFFFFFFFF, 1.25F * parent.getScale());
    }

    @Override
    public void addTooltips(Consumer<Text> tooltipConsumer) {
        this.tooltips.forEach(tooltipConsumer);
    }
}
