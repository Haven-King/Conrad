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

import dev.inkwell.conrad.api.value.data.Constraint;
import dev.inkwell.conrad.api.value.util.ListView;
import dev.inkwell.conrad.impl.gui.widgets.Mutable;
import dev.inkwell.vivian.api.screen.ConfigScreen;
import dev.inkwell.vivian.api.widgets.WidgetComponent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class ValueWidgetComponent<T> extends WidgetComponent implements Mutable {
    private final Supplier<T> defaultValueSupplier;
    private final T defaultValue;
    private final Consumer<T> changedListener;
    private final Consumer<T> saveConsumer;
    private final Text defaultValueText;
    private final Collection<Constraint<T>> constraints = new HashSet<>();

    private T initialValue;
    private T value;

    public ValueWidgetComponent(ConfigScreen parent, int x, int y, int width, int height, Supplier<T> defaultValueSupplier, Consumer<T> changedListener, Consumer<T> saveConsumer, T value) {
        super(parent, x, y, width, height);
        this.defaultValueSupplier = defaultValueSupplier;
        this.defaultValue = defaultValueSupplier.get();
        this.changedListener = changedListener;
        this.saveConsumer = saveConsumer;
        this.initialValue = value;
        this.value = value;
        this.defaultValueText = this.getDefaultValueAsText();
    }

    @Override
    public final void save() {
        if (!this.hasError()) {
            this.saveConsumer.accept(this.value);
            this.initialValue = this.value;
        }
    }

    public void addConstraints(Collection<Constraint<T>> constraints) {
        this.constraints.addAll(constraints);
    }

    public void addConstraints(Iterable<Constraint<T>> constraints) {
        constraints.forEach(this.constraints::add);
    }

    public ListView<Constraint<T>> getConstraints() {
        return new ListView<>(this.constraints);
    }

    @Override
    public final boolean hasError() {
        return this.passes(this.value);
    }

    @Override
    public final void reset() {
        this.value = this.defaultValueSupplier.get();
    }

    @Override
    public boolean hasChanged() {
        return !this.initialValue.equals(this.value);
    }

    public final T getValue() {
        return this.value;
    }

    protected final void setValue(T value) {
        if (this.value == null || !this.value.equals(value)) {
            T oldValue = this.value;
            this.value = value;

            if (this.hasError()) {
                this.value = oldValue;
            } else {
                this.changedListener.accept(value);
            }
        }
    }

    public final T getDefaultValue() {
        return this.defaultValue;
    }

    protected abstract @Nullable Text getDefaultValueAsText();

    @Override
    public void addTooltips() {
        super.addTooltips();

        if (this.constraints.size() > 0) {
            List<Text> lines = new ArrayList<>();

            this.constraints.forEach(c -> c.addLines(s -> lines.add(new LiteralText(s))));

            this.parent.addTooltips(lines);
        }

        Text defaultValue = this.getDefaultValueAsText();

        if (defaultValue != null) {
            this.parent.addTooltips(new TranslatableText("vivian.default", this.getDefaultValueAsText()));
        }
    }

    protected boolean passes(T s) {
        for (Constraint<T> constraint : this.constraints) {
            if (!constraint.passes(s)) return true;
        }

        return false;
    }
}
