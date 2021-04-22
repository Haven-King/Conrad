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

import dev.inkwell.conrad.api.gui.EntryBuilderRegistry;
import dev.inkwell.conrad.api.gui.ValueWidgetFactory;
import dev.inkwell.conrad.api.value.ConfigDefinition;
import dev.inkwell.conrad.api.value.util.ListView;
import dev.inkwell.conrad.impl.data.DataObject;
import dev.inkwell.conrad.impl.gui.widgets.Mutable;
import dev.inkwell.vivian.api.builders.CategoryBuilder;
import dev.inkwell.vivian.api.builders.ConfigScreenBuilder;
import dev.inkwell.vivian.api.screen.ConfigScreen;
import dev.inkwell.vivian.api.widgets.LabelComponent;
import dev.inkwell.vivian.api.widgets.WidgetComponent;
import dev.inkwell.vivian.api.widgets.compound.SubScreenWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DataClassWidgetComponent<D> extends SubScreenWidget<D> implements ConfigScreenBuilder {
    private final Class<D> dataClass;

    public DataClassWidgetComponent(ConfigScreen parent, int width, int height, ConfigDefinition<?> config, Supplier<@NotNull D> defaultValueSupplier, Consumer<D> changedListener, Consumer<D> saveConsumer, @NotNull D value, Class<D> dataClass) {
        super(config, parent, width, height, defaultValueSupplier, changedListener, saveConsumer, value, new TranslatableText(dataClass.getName()));
        this.dataClass = dataClass;
    }

    @Override
    public void addTooltips() {
        this.parent.addTooltips(this.tooltips);
    }

    @Override
    public ConfigScreen build(Screen screen) {
        CategoryBuilder category = new CategoryBuilder(this.name.copy()).setSaveCallback(this::save);

        Field[] declaredFields = this.dataClass.getDeclaredFields();
        for (int j = 0; j < declaredFields.length; j++) {
            int finalJ = j;

            category.add((parent, x, y, width, consumer) -> {
                Field field = declaredFields[finalJ];
                Text name = new TranslatableText(this.dataClass.getName() + "." + field.getName());
                WidgetComponent widget = this.build(parent, x, y, width, name, field.getType(), field);

                consumer.accept(widget);
                consumer.accept(new LabelComponent(parent, x, y, width / 2, 20, name));
                consumer.accept(new Dummy());

                widget.setX(x + width - widget.getWidth());

                widget.setTooltipRegion(x, y, x + width, y + widget.getHeight());

                return widget.getHeight();
            });
        }

        return new ConfigScreen(screen, this.parent.style, 0, this.name, category);
    }

    @SuppressWarnings("unchecked")
    private <T> WidgetComponent build(ConfigScreen parent, int x, int y, int width, Text name, Class<T> type, Field field) {
        try {
            field.setAccessible(true);
            T value = (T) field.get(this.getValue());
            ValueWidgetFactory<T> factory = EntryBuilderRegistry.get(type, t -> {
            });

            if (factory instanceof EntryBuilderRegistry.DataClassValueWidgetFactory) {
                ((EntryBuilderRegistry.DataClassValueWidgetFactory<T>) factory).setOuterSaveConsumer(
                        t -> this.save()
                );
            }

            return factory.build(
                    parent,
                    x + width / 2,
                    y,
                    width / 2,
                    name,
                    this.config,
                    ListView.empty(),
                    DataObject.EMPTY,
                    () -> {
                        try {
                            return (T) field.get(this.dataClass.newInstance());
                        } catch (IllegalAccessException | InstantiationException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    v -> {
                    },
                    v -> this.setValue(field, v),
                    value
            );
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    private void setValue(Field field, Object value) {
        try {
            D d = this.getValue();
            D newValue = this.dataClass.newInstance();
            field.setAccessible(true);

            for (Field field1 : this.dataClass.getDeclaredFields()) {
                field1.setAccessible(true);
                field1.set(newValue, field1.equals(field) ? value : field1.get(d));
            }

            this.setValue(newValue);
            this.save();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    class Dummy extends WidgetComponent implements Mutable {
        public Dummy() {
            super(DataClassWidgetComponent.this.parent, 0, 0, 0, 0);
        }

        @Override
        public void save() {
            DataClassWidgetComponent.this.save();
        }

        @Override
        public void reset() {

        }

        @Override
        public boolean hasChanged() {
            return false;
        }

        @Override
        public boolean hasError() {
            return false;
        }

        @Override
        public void renderBackground(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {

        }

        @Override
        public void renderContents(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {

        }
    }
}
