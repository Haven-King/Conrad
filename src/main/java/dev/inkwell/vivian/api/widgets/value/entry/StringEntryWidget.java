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
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class StringEntryWidget extends TextWidgetComponent<String> {
    public StringEntryWidget(ConfigScreen parent, int x, int y, int width, int height, Alignment alignment, Supplier<@NotNull String> defaultValueSupplier, Consumer<String> changedListener, Consumer<String> saveConsumer, @NotNull String value) {
        super(parent, x, y, width, height, alignment, defaultValueSupplier, changedListener, saveConsumer, value);
    }

    @Override
    protected String valueOf(String value) {
        return value;
    }

    @Override
    protected String emptyValue() {
        return "";
    }

    @Override
    protected Optional<String> parse(String value) {
        return Optional.of(value);
    }
}
