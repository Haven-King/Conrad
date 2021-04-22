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

package dev.inkwell.conrad.api.gui;

import dev.inkwell.conrad.api.value.ConfigDefinition;
import dev.inkwell.conrad.api.value.data.Constraint;
import dev.inkwell.conrad.api.value.util.ListView;
import dev.inkwell.conrad.impl.data.DataObject;
import dev.inkwell.vivian.api.screen.ConfigScreen;
import dev.inkwell.vivian.api.widgets.WidgetComponent;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

@FunctionalInterface
public interface ValueWidgetFactory<T> {
    WidgetComponent build(ConfigScreen screen, int x, int y, int width, Text name, ConfigDefinition<?> config, ListView<Constraint<T>> constraints, DataObject data, Supplier<@NotNull T> defaultValueSupplier, Consumer<T> changedListener, Consumer<T> saveConsumer, @NotNull T value);
}
