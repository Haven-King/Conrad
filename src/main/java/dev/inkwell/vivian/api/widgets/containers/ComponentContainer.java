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

package dev.inkwell.vivian.api.widgets.containers;

import dev.inkwell.vivian.api.screen.ConfigScreen;
import dev.inkwell.vivian.api.widgets.ListComponent;
import dev.inkwell.vivian.api.widgets.WidgetComponent;
import dev.inkwell.vivian.impl.widgets.Mutable;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ComponentContainer extends ListComponent implements Mutable {
    protected final List<WidgetComponent> children = new ArrayList<>();
    private final boolean shouldRenderHighlight;

    public ComponentContainer(ConfigScreen parent, int x, int y, int index, boolean shouldRenderHighlight, @NotNull WidgetComponent child, WidgetComponent... children) {
        super(parent, x, y, 0, 0, index);
        this.children.add(child);
        this.children.addAll(Arrays.asList(children));
        this.init();
        this.shouldRenderHighlight = shouldRenderHighlight;
    }

    protected void init() {
        WidgetComponent child = this.children.get(0);
        this.x = child.getX();
        this.y = child.getY();
        this.width = child.getWidth();
        this.height = child.getHeight();

        for (WidgetComponent thing : this.children) {
            int x1 = Math.min(this.x, thing.getX());
            int y1 = Math.min(this.y, thing.getY());
            int x2 = Math.max(this.x + this.width, thing.getX() + thing.getWidth());
            int y2 = Math.max(this.y + this.height, thing.getY() + thing.getHeight());

            this.x = x1;
            this.y = y1;
            this.width = x2 - x1;
            this.height = y2 - y1;
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta, boolean shouldRenderHighlight) {
        super.render(matrixStack, mouseX, mouseY, delta, this.shouldRenderHighlight);
        this.children.forEach(child -> child.render(matrixStack, mouseX, mouseY, delta, !this.shouldRenderHighlight));
    }

    @Override
    public void renderBackground(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {

    }

    @Override
    public void renderContents(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {

    }

    @Override
    public void renderHighlight(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        if (this.shouldRenderHighlight) {
            super.renderHighlight(matrixStack, mouseX, mouseY, delta);
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.children.forEach(WidgetComponent::tick);
    }

    @Override
    public void scroll(int amount) {
        super.scroll(amount);
        this.children.forEach(child -> child.scroll(amount));
    }

    @Override
    public boolean holdsFocus() {
        for (WidgetComponent child : this.children) {
            if (child.holdsFocus()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        for (WidgetComponent child : this.children) {
            if (child.isMouseOver(mouseX, mouseY)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        this.children.forEach(child -> child.mouseMoved(mouseX, mouseY));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean bl = false;

        for (WidgetComponent child : this.children) {
            bl |= child.mouseClicked(mouseX, mouseY, button);
        }

        return bl;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean bl = false;

        for (WidgetComponent child : this.children) {
            bl |= child.mouseReleased(mouseX, mouseY, button);
        }

        return bl;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        boolean bl = false;

        for (WidgetComponent child : this.children) {
            bl |= child.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }

        return bl;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        boolean bl = false;

        for (WidgetComponent child : this.children) {
            bl |= child.mouseScrolled(mouseX, mouseY, amount);
        }

        return bl;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean bl = false;

        for (WidgetComponent child : this.children) {
            bl |= child.keyPressed(keyCode, scanCode, modifiers);
        }

        return bl;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        boolean bl = false;

        for (WidgetComponent child : this.children) {
            bl |= child.keyReleased(keyCode, scanCode, modifiers);
        }

        return bl;
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        boolean bl = false;

        for (WidgetComponent child : this.children) {
            bl |= child.charTyped(chr, keyCode);
        }

        return bl;
    }

    @Override
    public boolean changeFocus(boolean lookForwards) {
        boolean bl = false;

        for (WidgetComponent child : this.children) {
            bl |= child.changeFocus(lookForwards);
        }

        return bl;
    }


    @Override
    public void save() {
        for (WidgetComponent child : this.children) {
            if (child instanceof Mutable) {
                ((Mutable) child).save();
            }
        }
    }

    @Override
    public void reset() {
        // TODO
    }

    @Override
    public boolean hasChanged() {
        for (WidgetComponent child : this.children) {
            if (child instanceof Mutable && ((Mutable) child).hasChanged()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean hasError() {
        for (WidgetComponent child : this.children) {
            if (child instanceof Mutable && ((Mutable) child).hasError()) {
                return true;
            }
        }

        return false;
    }

    public void setX(int x) {
        int dX = x - this.x;
        this.x = x;

        for (WidgetComponent child : this.children) {
            child.setX(child.getX() + dX);
        }
    }

    public void setY(int y) {
        int dY = y - this.y;

        for (WidgetComponent child : this.children) {
            child.setY(child.getY() + dY);
        }
    }
}
