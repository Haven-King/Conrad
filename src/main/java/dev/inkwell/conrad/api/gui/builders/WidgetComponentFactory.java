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

package dev.inkwell.conrad.api.gui.builders;

import dev.inkwell.conrad.api.gui.screen.ConfigScreen;
import dev.inkwell.conrad.api.gui.widgets.WidgetComponent;
import dev.inkwell.conrad.api.value.ConfigDefinition;
import dev.inkwell.conrad.api.value.data.Constraint;
import dev.inkwell.conrad.api.value.data.DataType;
import dev.inkwell.conrad.api.value.util.ListView;
import dev.inkwell.conrad.impl.data.DataObject;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

@FunctionalInterface
public interface WidgetComponentFactory<T> {
    DataType<WidgetComponentFactory<?>> DATA_TYPE = new DataType<>("widget_builder");

    WidgetComponent build(Text name, ConfigDefinition<?> config, ListView<Constraint<T>> constraints, DataObject data, ConfigScreen parent, int x, int y, int width, int height, Supplier<@NotNull T> defaultValueSupplier, Consumer<T> changedListener, Consumer<T> saveConsumer, @NotNull T value);
}
