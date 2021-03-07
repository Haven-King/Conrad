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

package dev.inkwell.vivid.api.screen;

import dev.inkwell.vivid.api.DrawableExtensions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;

public class CategoryButtonWidget extends ButtonWidget implements DrawableExtensions, TooltipAccess {
    private final ConfigScreen parent;
    private final List<Text> tooltips;

    public CategoryButtonWidget(ConfigScreen parent, int x, int y, int width, int height, Text message, PressAction onPress, List<Text> tooltips) {
        super(x, y, width, height, message, onPress, null);
        this.parent = parent;
        this.tooltips = tooltips;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        this.parent.setFocused(null);
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        TextRenderer textRenderer = minecraftClient.textRenderer;
        Style textStyle = this.parent.getStyle().categoryColor.color(!this.active, this.hovered);

        int color = textStyle.getColor() != null
                ? textStyle.getColor().getRgb()
                : 0xFFFFFFFF;

        MutableText text = this.getMessage().copy().styled(style -> textStyle);
        drawCenteredText(matrices, textRenderer, text, this.x + this.width / 2F, this.y + (this.height - 8) / 2F, color, parent.getScale());

        matrices.push();
        this.parent.getStyle().renderCategoryButtonDecorations(this, matrices, x, y, width, height);
        matrices.pop();

        this.parent.addTooltips(this);
    }

    @Override
    public void addTooltips(Consumer<Text> tooltipConsumer) {
        this.tooltips.forEach(tooltipConsumer);
    }
}
