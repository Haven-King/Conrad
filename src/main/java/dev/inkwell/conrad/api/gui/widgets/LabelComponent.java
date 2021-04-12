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

import dev.inkwell.conrad.api.gui.screen.ConfigScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class LabelComponent extends WidgetComponent {
    protected final Text label;
    private int color = 0xFFFFFFFF;

    public LabelComponent(ConfigScreen parent, int x, int y, int width, int height, Text label) {
        super(parent, x, y, width, height);
        this.label = label;
    }

    @Override
    public void renderBackground(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {

    }

    @Override
    public void renderContents(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        textRenderer.drawWithShadow(
                matrixStack,
                this.label,
                this.x + 3,
                this.textYPos(),
                0xFFFFFFFF
        );
    }

    public LabelComponent withColor(Style sectionColor) {
        this.color = sectionColor.getColor() == null ? this.color : sectionColor.getColor().getRgb();
        return this;
    }
}
