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
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class IntegerEntryWidget extends NumberEntryWidget<Integer> implements Bounded<Integer> {
    public IntegerEntryWidget(ConfigScreen parent, int x, int y, int width, int height, Alignment alignment, Supplier<@NotNull Integer> defaultValueSupplier, Consumer<Integer> changedListener, Consumer<Integer> saveConsumer, @NotNull Integer value) {
        super(parent, x, y, width, height, alignment, defaultValueSupplier, changedListener, saveConsumer, value);
        this.setTextPredicate(string -> {
            try {
                long l = Long.parseLong(string);
                return l <= Integer.MAX_VALUE && l >= Integer.MIN_VALUE;
            } catch (NumberFormatException ignored) {
                return false;
            }
        });
    }

    @Override
    protected String valueOf(Integer value) {
        return String.valueOf(value);
    }

    @Override
    protected Integer emptyValue() {
        return 0;
    }

    @Override
    protected Optional<Integer> parse(String value) {
        try {
            return Optional.of(Integer.valueOf(value));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public boolean isWithinBounds(Integer value) {
        return (min == null || value >= min) && (max == null || value <= max);
    }
}
