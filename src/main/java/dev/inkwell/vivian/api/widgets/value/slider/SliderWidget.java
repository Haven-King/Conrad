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

import com.mojang.blaze3d.systems.RenderSystem;
import dev.inkwell.vivian.api.constraints.Bounded;
import dev.inkwell.vivian.api.screen.ConfigScreen;
import dev.inkwell.vivian.api.widgets.value.ValueWidgetComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
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
        MinecraftClient.getInstance().getTextureManager().bindTexture(AbstractButtonWidget.WIDGETS_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        int i = (this.isMouseOver(mouseX, mouseY) ? 2 : 1) * 20;

        DrawableHelper.drawTexture(matrixStack, this.x + (int) (this.getPercentage() * (double) (this.width - 8)), this.y, 0, 0, 46 + i, 4, 20, 256, 256);
        DrawableHelper.drawTexture(matrixStack, this.x + (int) (this.getPercentage() * (double) (this.width - 8)) + 4, this.y, 0, 196, 46 + i, 4, 20, 256, 256);

        String string = this.stringValue();
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        drawCenteredString(matrixStack, textRenderer, string, this.x + this.width / 2F, this.textYPos(), 0xFFFFFFFF);
    }

    @Override
    public void renderBackground(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        minecraftClient.getTextureManager().bindTexture(AbstractButtonWidget.WIDGETS_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        DrawableHelper.drawTexture(matrixStack, this.x, this.y, 0, 0, 46, this.width / 2, this.height, 256, 256);
        DrawableHelper.drawTexture(matrixStack, this.x + this.width / 2, this.y, 0, 200 - this.width / 2F, 46, this.width / 2, this.height, 256, 256);
    }

    protected abstract T subtract(T left, T right);

    protected abstract T add(T left, T right);

    protected abstract T multiply(T t, double d);

    protected abstract float getPercentage();

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.move(mouseX, mouseY, button)) {
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return this.move(mouseX, mouseY, button);
    }

    private boolean move(double mouseX, double mouseY, int button) {
        if (this.isMouseOver(mouseX, mouseY) && button == GLFW.GLFW_MOUSE_BUTTON_1) {
            float x1 = this.x + 4;
            float x2 = this.x + this.width - 8;

            if (mouseX < x1) {
                this.setValue(this.getMin());
                return true;
            }

            if (mouseX > x2) {
                this.setValue(this.getMax());
                return true;
            }

            float mark = (float) (mouseX - x1) / (x2 - x1);
            this.setValue(add((multiply(subtract(this.getMax(), this.getMin()), mark)), getMin()));

            return true;
        }

        return false;

    }

    protected abstract String stringValue();

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
