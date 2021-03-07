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

package dev.inkwell.vivid.api.widgets.value.slider;

import dev.inkwell.vivid.api.screen.ConfigScreen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class IntegerSliderWidget extends SliderWidget<Integer> {
    public IntegerSliderWidget(ConfigScreen parent, int x, int y, int width, int height, Supplier<@NotNull Integer> defaultValueSupplier, Consumer<Integer> changedListener, Consumer<Integer> saveConsumer, @NotNull Integer value, @NotNull Integer min, @NotNull Integer max) {
        super(parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value, min, max);
    }

    @Override
    protected Integer subtract(Integer left, Integer right) {
        return left - right;
    }

    @Override
    protected Integer add(Integer left, Integer right) {
        return left + right;
    }

    @Override
    protected Integer multiply(Integer integer, double d) {
        return (int) (integer * d);
    }

    @Override
    public boolean isWithinBounds(Integer value) {
        return value >= this.min && value <= this.max;
    }

    @Override
    public Text getDefaultValueAsText() {
        return new LiteralText(this.getDefaultValue().toString());
    }

    @Override
    protected float getPercentage() {
        return ((this.getValue() - getMin()) / (float) (getMax() - getMin()));
    }

    @Override
    protected String stringValue() {
        return this.getValue().toString();
    }
}
