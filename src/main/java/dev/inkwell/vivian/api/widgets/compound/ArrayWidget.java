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

package dev.inkwell.vivian.api.widgets.compound;

import dev.inkwell.conrad.api.gui.ValueWidgetFactory;
import dev.inkwell.conrad.api.value.ConfigDefinition;
import dev.inkwell.conrad.api.value.util.Array;
import dev.inkwell.conrad.api.value.util.ListView;
import dev.inkwell.conrad.impl.data.DataObject;
import dev.inkwell.conrad.impl.gui.widgets.Mutable;
import dev.inkwell.vivian.api.builders.CategoryBuilder;
import dev.inkwell.vivian.api.screen.ConfigScreen;
import dev.inkwell.vivian.api.util.SuggestionProvider;
import dev.inkwell.vivian.api.widgets.TextButton;
import dev.inkwell.vivian.api.widgets.WidgetComponent;
import dev.inkwell.vivian.api.widgets.value.entry.TextWidgetComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class ArrayWidget<T> extends SubScreenWidget<Array<T>> {
    private final ValueWidgetFactory<T> builder;
    private boolean changed;
    private SuggestionProvider suggestionProvider = t -> Collections.emptyList();

    public ArrayWidget(ConfigDefinition<?> config, ConfigScreen parent, int x, int y, Supplier<@NotNull Array<T>> defaultValueSupplier, Consumer<Array<T>> changedListener, Consumer<Array<T>> saveConsumer, @NotNull Array<T> value, Text name, ValueWidgetFactory<T> builder) {
        super(config, parent, x, y, defaultValueSupplier, changedListener, saveConsumer, new Array<>(value), name);
        this.builder = builder;
    }

    public void setSuggestions(SuggestionProvider suggestionProvider) {
        this.suggestionProvider = suggestionProvider;
    }

    @Override
    public ConfigScreen build(Screen screen) {
        CategoryBuilder category = new CategoryBuilder(this.name.copy());

        int i = 0;

        for (T value : this.getValue()) {
            int index = i;

            category.add((parent, x, y, width, consumer) -> {
                WidgetComponent valueWidget = this.builder.build(parent, x + 20, y, width - 60,
                        LiteralText.EMPTY,
                        this.config,
                        ListView.empty(),
                        DataObject.EMPTY,
                        this.getValue().getDefaultValue(),
                        v -> this.setValue(this.getValue().set(index, v)),
                        v -> {
                        },
                        value
                );

                if (valueWidget instanceof TextWidgetComponent) {
                    ((TextWidgetComponent<?>) valueWidget).withSuggestions(this.suggestionProvider);
                }

                consumer.accept(valueWidget);

                consumer.accept(new TextButton(parent, x, y, 20, 20, 0, new LiteralText("✕"), button -> {
                    ArrayWidget.this.setValue(ArrayWidget.this.getValue().remove(index));
                    ArrayWidget.this.changed = true;

                    ArrayWidget.this.refresh();

                    return true;
                }));

                consumer.accept(new TextButton(parent, x + width - 40, y, 20, 20, 0, new LiteralText("▲"), button -> {
                    Array<T> array = this.getValue();

                    if (index > 0 && array.size() >= 2) {
                        T temp = array.get(index);

                        //noinspection unchecked
                        T[] a = (T[]) java.lang.reflect.Array.newInstance(array.getValueClass(), array.size());

                        for (int j = 0; j < a.length; ++j) {
                            a[j] = array.get(j);
                        }

                        a[index] = a[index - 1];
                        a[index - 1] = temp;

                        this.setValue(new Array<>(array.getValueClass(), array.getDefaultValue(), a));

                        this.changed = true;

                        this.refresh();

                        return true;
                    } else {
                        return false;
                    }
                }));

                consumer.accept(new TextButton(parent, x + width - 20, y, 20, 20, 0, new LiteralText("▼"), button -> {
                    Array<T> array = this.getValue();
                    if (index < array.size() - 1 && array.size() >= 2) {
                        T temp = array.get(index);

                        //noinspection unchecked
                        T[] a = (T[]) java.lang.reflect.Array.newInstance(array.getValueClass(), array.size());

                        for (int j = 0; j < a.length; ++j) {
                            a[j] = array.get(j);
                        }

                        a[index] = a[index + 1];
                        a[index + 1] = temp;

                        this.setValue(new Array<>(array.getValueClass(), array.getDefaultValue(), a));

                        this.changed = true;

                        this.refresh();

                        return true;
                    } else {
                        return false;
                    }
                }));

                return valueWidget.getHeight();
            });

            ++i;
        }

        category.add((parent, x, y, width, consumer) -> {
            consumer.accept(new AddButton(parent, x, y, width, height, 0x40000000, new LiteralText("+"), button -> {
                this.setValue(this.getValue().addEntry());
                this.changed = true;
                this.refresh();
                return true;
            }));

            return 20;
        });

        return new ConfigScreen(screen, this.parent.style, 0, this.name, category);
    }

    class AddButton extends TextButton implements Mutable {
        public AddButton(ConfigScreen parent, int x, int y, int width, int height, int color, MutableText text, Action onClick) {
            super(parent, x, y, width, height, color, text, onClick);
        }

        @Override
        public void save() {
            ArrayWidget.this.save();
        }

        @Override
        public void reset() {

        }

        @Override
        public boolean hasChanged() {
            return ArrayWidget.this.changed;
        }

        @Override
        public boolean hasError() {
            return false;
        }
    }
}
