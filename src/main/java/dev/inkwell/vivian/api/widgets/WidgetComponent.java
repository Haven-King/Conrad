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

package dev.inkwell.vivian.api.widgets;

import dev.inkwell.vivian.api.DrawableExtensions;
import dev.inkwell.vivian.api.screen.ConfigScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Environment(EnvType.CLIENT)
public abstract class WidgetComponent implements Element, Drawable, DrawableExtensions {
    public final ConfigScreen parent;

    protected final List<Text> tooltips = new ArrayList<>();
    protected int tooltipsX1, tooltipsX2, tooltipsY1, tooltipsY2;
    protected int x, y, width, height;

    protected double lastMouseX;
    protected double lastMouseY;

    public WidgetComponent(ConfigScreen parent, int x, int y, int suggestedWidth, int suggestedHeight) {
        this.parent = parent;
        this.setX(x);
        this.setY(y);
        this.setWidth(suggestedWidth);
        this.setHeight(suggestedHeight);
    }

    public void setTooltipRegion(int x1, int y1, int x2, int y2) {
        this.tooltipsX1 = x1;
        this.tooltipsY1 = y1;
        this.tooltipsX2 = x2;
        this.tooltipsY2 = y2;
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;

        this.renderBackground(matrixStack, mouseX, mouseY, delta);

        this.renderContents(matrixStack, mouseX, mouseY, delta);

        if (this.showTooltips(mouseX, mouseY) && (this.parent.getFocused() == null || this.parent.getFocused() == this)) {
            this.addTooltips();
        }
    }

    public boolean showTooltips(int mouseX, int mouseY) {
        return mouseX >= this.tooltipsX1 && mouseX < this.tooltipsX2 && mouseY >= this.tooltipsY1 && mouseY < this.tooltipsY2;
    }

    public void addTooltips() {
        this.parent.addTooltips(this.tooltips);
    }

    public abstract void renderBackground(MatrixStack matrixStack, int mouseX, int mouseY, float delta);

    public abstract void renderContents(MatrixStack matrixStack, int mouseX, int mouseY, float delta);

    protected int highlightColor() {
        return 0xFFFFFFFF;
    }

    public void scroll(int amount) {
        this.setY(this.y + amount);
    }

    // The tooltip collection should be ordered.
    public final void addTooltips(List<Text> tooltips) {
        this.tooltips.addAll(tooltips);
    }

    public final void addTooltips(Text... tooltips) {
        this.tooltips.addAll(Arrays.asList(tooltips));
    }

    public final WidgetComponent withTooltips(List<Text> tooltips) {
        this.tooltips.addAll(tooltips);
        return this;
    }

    public final WidgetComponent withTooltips(Text... tooltips) {
        this.tooltips.addAll(Arrays.asList(tooltips));
        return this;
    }

    public boolean isFocused() {
        return this.parent.getFocused() == this;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseX < this.x + this.width
                && mouseY >= this.y && mouseY < this.y + this.height;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.tooltipsX1 += x - this.x;
        this.tooltipsX2 += x - this.x;
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.tooltipsY1 += y - this.y;
        this.tooltipsY2 += y - this.y;
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.tooltipsX2 += width - this.width;
        this.width = width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(int height) {
        this.tooltipsY2 += height - this.height;
        this.height = height;
    }

    protected float textYPos() {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        return this.y + this.height / 2F - textRenderer.fontHeight / 2F;
    }
}
