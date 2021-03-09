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

package dev.inkwell.conrad.api.gui.widgets.value.entry;

import dev.inkwell.conrad.api.gui.constraints.Bounded;
import dev.inkwell.conrad.api.gui.screen.ConfigScreen;
import dev.inkwell.conrad.api.gui.util.Alignment;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class NumberEntryWidget<T extends Number> extends TextWidgetComponent<T> implements Bounded<T> {
    protected T min = null;
    protected T max = null;

    public NumberEntryWidget(ConfigScreen parent, int x, int y, int width, int height, Alignment alignment, Supplier<@NotNull T> defaultValueSupplier, Consumer<T> changedListener, Consumer<T> saveConsumer, @NotNull T value) {
        super(parent, x, y, width, height, alignment, defaultValueSupplier, changedListener, saveConsumer, value);
    }

    @Override
    public boolean passes() {
        return super.passes() && this.isWithinBounds(this.getValue());
    }

    @Override
    public void addConstraintTooltips(List<Text> tooltips) {
        Bounded.super.addConstraintTooltips(tooltips);
    }

    @Override
    public @Nullable T getMin() {
        return this.min;
    }

    @Override
    public void setMin(@Nullable T min) {
        this.min = min;
    }

    @Override
    public @Nullable T getMax() {
        return this.max;
    }

    @Override
    public void setMax(@Nullable T max) {
        this.max = max;
    }

    @Override
    public void setBounds(T min, T max) {
        this.setMin(min);
        this.setMax(max);
    }
}
