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
import dev.inkwell.vivian.api.util.Translatable;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class EnumDropdownWidget<T extends Enum<T>> extends DropdownWidgetComponent<T> {
    public EnumDropdownWidget(ConfigScreen parent, int x, int y, int width, int height, Supplier<@NotNull T> defaultValueSupplier, Consumer<T> changedListener, Consumer<T> saveConsumer, @NotNull T value) {
        super(parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value, value.getDeclaringClass().getEnumConstants());
    }

    @Override
    protected MutableText fromValue(T value) {
        if (value instanceof Translatable) {
            return ((Translatable) value).getText();
        } else {
            return new LiteralText(value.name());
        }
    }

    @Override
    public @Nullable Text getDefaultValueAsText() {
        return this.fromValue(this.getDefaultValue());
    }
}
