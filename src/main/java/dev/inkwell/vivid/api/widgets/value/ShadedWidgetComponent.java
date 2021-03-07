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

package dev.inkwell.vivid.api.widgets.value;

import dev.inkwell.vivid.api.screen.ConfigScreen;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static dev.inkwell.vivid.impl.Vivid.BLUR;

public abstract class ShadedWidgetComponent<T> extends ValueWidgetComponent<T> {
    protected boolean isShadeDrawn = false;

    public ShadedWidgetComponent(ConfigScreen parent, int x, int y, int width, int height, Supplier<@NotNull T> defaultValueSupplier, Consumer<T> changedListener, Consumer<T> saveConsumer, @NotNull T value) {
        super(parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value);
    }

    @Override
    public void renderBackground(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        if (this.isShadeDrawn) {
            fill(matrixStack, 0, 0, 10000, 10000, 0, 0.5F);
            BLUR.setUniformValue("Start", 0F, 0F);
            BLUR.setUniformValue("End", 1F, 1F);
            BLUR.setUniformValue("Progress", 1F);
            BLUR.setUniformValue("Radius", 5F);
            BLUR.render(delta);
        }
    }

    @Override
    public boolean holdsFocus() {
        return true;
    }
}
