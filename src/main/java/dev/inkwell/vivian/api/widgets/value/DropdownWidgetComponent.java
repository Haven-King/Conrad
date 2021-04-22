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

package dev.inkwell.vivian.api.widgets.value;

import dev.inkwell.vivian.api.screen.ConfigScreen;
import dev.inkwell.vivian.api.widgets.TextButton;
import dev.inkwell.vivian.api.widgets.WidgetComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class DropdownWidgetComponent<T> extends ShadedWidgetComponent<T> {
    private final TextButton button;
    private final TextButton[] buttons;

    private int buttonsHeight;

    public DropdownWidgetComponent(ConfigScreen parent, int x, int y, int width, int height, Supplier<@NotNull T> defaultValueSupplier, Consumer<T> changedListener, Consumer<T> saveConsumer, @NotNull T value, T[] possibleValues) {
        super(parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value);

        this.button = new TextButton(parent, x, y, width, height, 0, this.fromValue(value), button -> true);
        this.buttons = new TextButton[possibleValues.length];
        this.buttonsHeight = height * possibleValues.length;

        for (int i = 0; i < this.buttons.length; ++i) {
            int j = i;
            buttons[i] = new TextButton(parent, x, y + height * i - this.buttonsHeight / 2, width, height, i % 2 == 0 ? 0x30FFFFFF : 0x20FFFFFF, this.fromValue(possibleValues[i]), button -> {
                this.setValue(possibleValues[j]);
                this.button.setText(this.fromValue(possibleValues[j]));
                parent.setFocused(null);
                return true;
            });
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        super.render(matrixStack, mouseX, mouseY, delta);

        if (this.isFocused()) {
            for (TextButton button : this.buttons) {
                button.render(matrixStack, mouseX, mouseY, delta);
            }
        } else {
            this.button.render(matrixStack, mouseX, mouseY, delta);
        }
    }

    @Override
    public void renderContents(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean bl;

        if (this.isFocused()) {
            bl = false;

            for (TextButton btn : this.buttons) {
                bl |= btn.mouseClicked(mouseX, mouseY, button);
            }

            if (!bl) {
                this.parent.setFocused(null);
            }

        } else {
            bl = this.button.mouseClicked(mouseX, mouseY, button);

            if (bl) {
                this.parent.setFocused(this);
            }
        }

        return bl;
    }

    @Override
    public void scroll(int amount) {
        super.scroll(amount);
    }

    protected abstract MutableText fromValue(T value);

    @Override
    public void setX(int x) {
        super.setX(x);

        if (this.button != null) {
            this.button.setX(x);
            for (TextButton button : this.buttons) {
                button.setX(x);
            }
        }
    }

    @Override
    public void setY(int y) {
        super.setY(y);

        if (this.button != null) {
            this.button.setY(y);

            if (y - this.buttonsHeight / 2 < ConfigScreen.HEADER_SIZE) {
                y = ConfigScreen.HEADER_SIZE + this.buttonsHeight / 2;
            } else if (y + this.buttonsHeight / 2 > this.parent.height - ConfigScreen.FOOTER_SIZE) {
                y = this.parent.height - ConfigScreen.FOOTER_SIZE - this.buttonsHeight / 2;
            }

            for (int i = 0; i < this.buttons.length; ++i) {
                this.buttons[i].setY(y + i * this.height - this.buttonsHeight / 2);
            }
        }
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);

        if (this.button != null) {
            this.button.setWidth(width);

            for (TextButton button : this.buttons) {
                button.setWidth(width);
            }
        }
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);

        if (this.button != null) {
            this.button.setHeight(height);
            buttonsHeight = height * buttons.length;

            for (TextButton button : this.buttons) {
                button.setHeight(height);
            }
        }

        this.setY(this.y);
    }
}
