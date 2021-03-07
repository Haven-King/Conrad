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

package dev.inkwell.vivian.api.widgets.value.slider;

import dev.inkwell.vivian.api.constraints.Bounded;
import dev.inkwell.vivian.api.screen.ConfigScreen;
import dev.inkwell.vivian.api.widgets.value.ValueWidgetComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class SliderWidget<T extends Number> extends ValueWidgetComponent<T> implements Bounded<T> {
    protected final T min;
    protected final T max;

    public SliderWidget(ConfigScreen parent, int x, int y, int width, int height, Supplier<@NotNull T> defaultValueSupplier, Consumer<T> changedListener, Consumer<T> saveConsumer, @NotNull T value, @NotNull T min, @NotNull T max) {
        super(parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value);
        this.min = min;
        this.max = max;
    }

    @Override
    public void renderContents(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        String string = this.stringValue();
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int dX = (int) (this.width - 3 - textRenderer.getWidth(string) * parent.getScale());
        draw(matrixStack, textRenderer, string, this.x + dX, this.textYPos(), 0xFFFFFFFF, parent.getScale());

        int x1 = this.x + 3;
        int x2 = this.x + this.width - 2 - Math.max(textRenderer.getWidth(String.valueOf(getMin())), textRenderer.getWidth(String.valueOf(getMax())));
        int barWidth = x2 - x1;
        float y1 = this.y + this.height / 2F;

        line(matrixStack, x1, x2, y1, y1, 0xFFFFFFFF);

        float mark = x1 + getPercentage() * barWidth;

        matrixStack.push();
        matrixStack.translate(mark, this.y + this.height / 2F, 0);
        matrixStack.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(45));
        fill(matrixStack, -1.5F, -1.5F, 1.5F, 1.5F, 0xFFFFFFFF, 1F);
        matrixStack.pop();
    }

    @Override
    public void renderBackground(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {

    }

    protected abstract T subtract(T left, T right);

    protected abstract T add(T left, T right);

    protected abstract T multiply(T t, double d);

    protected abstract float getPercentage();

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isMouseOver(mouseX, mouseY) && button == GLFW.GLFW_MOUSE_BUTTON_1) {
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            int x1 = this.x + 3;
            int x2 = this.x + this.width - 2 - Math.max(textRenderer.getWidth(String.valueOf(getMin())), textRenderer.getWidth(String.valueOf(getMax())));

            if (mouseX < x1) {
                this.setValue(this.getMin());
                return true;
            }

            if (mouseX > x2) {
                this.setValue(this.getMax());
                return true;
            }

            float mark = (float) (mouseX - x1) / (float) (x2 - x1);
            this.setValue(add((multiply(subtract(this.getMax(), this.getMin()), mark)), getMin()));

            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return this.mouseClicked(mouseX, mouseY, button);
    }

    protected abstract String stringValue();

    @Override
    public boolean hasError() {
        return false;
    }

    @Override
    public @NotNull T getMin() {
        return this.min;
    }

    @Override
    public void setMin(@Nullable T min) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull T getMax() {
        return this.max;
    }

    @Override
    public void setMax(@Nullable T max) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean passes() {
        return isWithinBounds(this.getValue());
    }
}
