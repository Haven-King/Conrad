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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;

public class ConradButtonWidget extends ButtonWidget implements DrawableExtensions, TooltipAccess {
    private final ConfigScreen parent;
    private final List<Text> tooltips;
    private final boolean renderBackground;

    public ConradButtonWidget(ConfigScreen parent, int x, int y, int width, int height, Text message, PressAction onPress, List<Text> tooltips) {
        this(parent, x, y, width, height, message, onPress, tooltips, true);
    }

    public ConradButtonWidget(ConfigScreen parent, int x, int y, int width, int height, Text message, PressAction onPress, List<Text> tooltips, boolean renderBackground) {
        super(x, y, width, height, message, onPress, ((button, matrices, mouseX, mouseY) -> {}));
        this.parent = parent;
        this.tooltips = tooltips;
        this.renderBackground = renderBackground;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        this.parent.setFocused(null);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.visible && mouseX >= (double)this.x && mouseY >= (double)this.y && mouseX < (double)(this.x + this.width) && mouseY < (double)(this.y + this.height);
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (this.renderBackground) {
            super.renderButton(matrices, mouseX, mouseY, delta);
        } else {
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            TextRenderer textRenderer = minecraftClient.textRenderer;
            drawCenteredText(matrices, textRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, 0xFFFFFFFF);
        }

        this.parent.addTooltips(this);
    }

    @Override
    public void addTooltips(Consumer<Text> tooltipConsumer) {
        this.tooltips.forEach(tooltipConsumer);
    }
}
