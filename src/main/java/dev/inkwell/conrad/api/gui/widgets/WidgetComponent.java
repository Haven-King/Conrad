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

import dev.inkwell.conrad.api.gui.DrawableExtensions;
import dev.inkwell.conrad.api.gui.screen.ConfigScreen;
import dev.inkwell.conrad.api.gui.screen.TooltipAccess;
import dev.inkwell.conrad.impl.gui.ConradGuiConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.TickableElement;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public abstract class WidgetComponent implements Element, DrawableExtensions, TickableElement, TooltipAccess {
    protected final ConfigScreen parent;
    private final List<Text> tooltips = new ArrayList<>();
    protected int x, y, width, height;
    protected float hoverOpacity = 0F;

    protected float lastMouseX;
    protected float lastMouseY;
    private boolean focused;

    public WidgetComponent(ConfigScreen parent, int x, int y, int width, int height) {
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta, boolean shouldRenderHighlight) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;

        this.renderBackground(matrixStack, mouseX, mouseY, delta);

        if (shouldRenderHighlight) {
            this.renderHighlight(matrixStack, mouseX, mouseY, delta);
        }

        this.renderContents(matrixStack, mouseX, mouseY, delta);

        parent.addTooltips(this);
    }

    public abstract void renderBackground(MatrixStack matrixStack, int mouseX, int mouseY, float delta);

    public void renderHighlight(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        fill(matrixStack, this.x, this.y, this.x + this.width, this.y + this.height, this.highlightColor(), this.hoverOpacity * 0.75F);
    }

    public abstract void renderContents(MatrixStack matrixStack, int mouseX, int mouseY, float delta);

    protected int highlightColor() {
        return 0xFFFFFFFF;
    }

    public void scroll(int amount) {
        this.y += amount;
    }

    // The tooltip collection should be ordered.
    public final void addTooltips(Collection<Text> tooltips) {
        this.tooltips.addAll(tooltips);
    }

    @Override
    public void addTooltips(Consumer<Text> tooltipConsumer) {
        this.tooltips.forEach(tooltipConsumer);
    }

    public void addTooltipsToList(List<Text> tooltips) {
        tooltips.addAll(this.tooltips);
    }

    public boolean holdsFocus() {
        return false;
    }

    public final boolean isFocused() {
        return this.focused;
    }

    public void setFocused(boolean focused) {
        this.focused = this.holdsFocus() && focused;
    }

    @Override
    public void tick() {
        if (ConradGuiConfig.Animations.ENABLED.getValue()) {
            if (isMouseOver(this.lastMouseX, this.lastMouseY)) {
                this.hoverOpacity = Math.min(1F, ConradGuiConfig.Animations.SPEED.getValue() + this.hoverOpacity);
            } else {
                this.hoverOpacity = Math.max(0F, this.hoverOpacity - ConradGuiConfig.Animations.SPEED.getValue());
            }
        } else {
            this.hoverOpacity = this.isMouseOver(this.lastMouseX, this.lastMouseY) ? 1F : 0F;
        }
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
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public ConfigScreen getParent() {
        return this.parent;
    }

    protected float textYPos() {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        return this.y + this.height / 2F - (textRenderer.fontHeight * parent.getScale() + 4 * parent.getScale()) / 2F;
    }

    public Element getFocusElement(double mouseX, double mouseY) {
        return this;
    }
}
