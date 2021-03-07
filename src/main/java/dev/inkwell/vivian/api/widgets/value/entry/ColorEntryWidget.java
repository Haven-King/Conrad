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

package dev.inkwell.vivian.api.widgets.value.entry;

import dev.inkwell.vivian.api.screen.ConfigScreen;
import dev.inkwell.vivian.api.util.Alignment;
import dev.inkwell.vivian.api.util.Color;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ColorEntryWidget extends TextWidgetComponent<Color> {
    public ColorEntryWidget(ConfigScreen parent, int x, int y, int width, int height, Alignment alignment, Supplier<@NotNull Color> defaultValueSupplier, Consumer<Color> changedListener, Consumer<Color> saveConsumer, @NotNull Color value) {
        super(parent, x, y, width, height, alignment, defaultValueSupplier, changedListener, saveConsumer, value);
    }

    @Override
    protected String valueOf(Color value) {
        if (value.value == -1) {
            return "0xFFFFFFFF";
        } else {
            return "0x" + Integer.toUnsignedString(value.value, 16).toUpperCase(Locale.ROOT);
        }
    }

    @Override
    protected int getColor() {
        return this.hasError() ? super.getColor() : this.getValue().value;
    }

    @Override
    protected Color emptyValue() {
        return new Color(-1);
    }

    @Override
    protected Optional<Color> parse(String value) {
        try {
            if (value.equalsIgnoreCase("0xFFFFFFFF")) {
                return Optional.of(new Color(-1));
            } else {
                return Optional.of(new Color(Integer.parseUnsignedInt(value.startsWith("0x")
                        ? value.substring(2) : value, 16)));
            }
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
