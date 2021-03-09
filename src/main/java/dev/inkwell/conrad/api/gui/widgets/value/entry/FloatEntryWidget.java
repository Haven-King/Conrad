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

import dev.inkwell.conrad.api.gui.screen.ConfigScreen;
import dev.inkwell.conrad.api.gui.util.Alignment;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FloatEntryWidget extends NumberEntryWidget<Float> {
    public FloatEntryWidget(ConfigScreen parent, int x, int y, int width, int height, Alignment alignment, Supplier<@NotNull Float> defaultValueSupplier, Consumer<Float> changedListener, Consumer<Float> saveConsumer, @NotNull Float value) {
        super(parent, x, y, width, height, alignment, defaultValueSupplier, changedListener, saveConsumer, value);
        this.setTextPredicate(string -> {
            try {
                Double.parseDouble(string);
                return true;
            } catch (NumberFormatException ignored) {
                return false;
            }
        });
    }

    @Override
    protected String valueOf(Float value) {
        return String.valueOf(value);
    }

    @Override
    protected Float emptyValue() {
        return 0F;
    }

    @Override
    protected Optional<Float> parse(String value) {
        try {
            return Optional.of(Float.valueOf(value));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public boolean isWithinBounds(Float value) {
        return (min == null || value >= min) && (max == null || value <= max);
    }
}
