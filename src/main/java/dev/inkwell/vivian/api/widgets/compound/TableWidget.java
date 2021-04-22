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
import dev.inkwell.conrad.api.value.data.Constraint;
import dev.inkwell.conrad.api.value.util.ListView;
import dev.inkwell.conrad.api.value.util.Table;
import dev.inkwell.conrad.impl.data.DataObject;
import dev.inkwell.conrad.impl.gui.widgets.Mutable;
import dev.inkwell.vivian.api.builders.CategoryBuilder;
import dev.inkwell.vivian.api.screen.ConfigScreen;
import dev.inkwell.vivian.api.util.Alignment;
import dev.inkwell.vivian.api.util.KeySuggestionProvider;
import dev.inkwell.vivian.api.util.SuggestionProvider;
import dev.inkwell.vivian.api.widgets.TextButton;
import dev.inkwell.vivian.api.widgets.WidgetComponent;
import dev.inkwell.vivian.api.widgets.value.entry.StringEntryWidget;
import dev.inkwell.vivian.api.widgets.value.entry.TextWidgetComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class TableWidget<T> extends SubScreenWidget<Table<T>> {
    private final ValueWidgetFactory<T> builder;
    private final boolean mutable;
    private final List<Constraint<String>> keyConstraints = new ArrayList<>();

    private boolean changed;

    private KeySuggestionProvider<T> keySuggestionProvider = (v, s) -> Collections.emptyList();
    private SuggestionProvider suggestionProvider = s -> Collections.emptyList();

    public TableWidget(ConfigDefinition<?> config, ConfigScreen parent, int x, int y, Supplier<@NotNull Table<T>> defaultValueSupplier, Consumer<Table<T>> changedListener, Consumer<Table<T>> saveConsumer, @NotNull Table<T> value, Text name, ValueWidgetFactory<T> builder, boolean mutable) {
        super(config, parent, x, y, defaultValueSupplier, changedListener, saveConsumer, new Table<>(value), name);
        this.builder = builder;
        this.mutable = mutable;
    }

    public TableWidget(ConfigDefinition<?> config, ConfigScreen parent, int x, int y, Supplier<@NotNull Table<T>> defaultValueSupplier, Consumer<Table<T>> changedListener, Consumer<Table<T>> saveConsumer, @NotNull Table<T> value, Text name, ValueWidgetFactory<T> builder) {
        this(config, parent, x, y, defaultValueSupplier, changedListener, saveConsumer, value, name, builder, true);
    }

    public TableWidget<T> setKeySuggestions(KeySuggestionProvider<T> suggestionProvider) {
        this.keySuggestionProvider = suggestionProvider;
        return this;
    }

    public void setValueSuggestions(SuggestionProvider suggestionProvider) {
        this.suggestionProvider = suggestionProvider;
    }

    public void addKeyConstraint(Constraint<String> constraint) {
        this.keyConstraints.add(constraint);
    }

    @Override
    public ConfigScreen build(Screen screen) {
        CategoryBuilder category = new CategoryBuilder(this.name.copy());

        int i = 0;

        for (Table.Entry<String, T> value : this.getValue()) {
            int index = i;

            category.add((parent, x, y, width, consumer) -> {
                WidgetComponent valueWidget = this.builder.build(
                        parent,
                        x + width / 2,
                        y,
                        width / 2,
                        new LiteralText(value.getKey()),
                        this.config,
                        ListView.empty(),
                        DataObject.EMPTY,
                        this.getValue().getDefaultValue(),
                        v -> this.setValue(this.getValue().set(index, v)),
                        v -> this.changed = true,
                        value.getValue()
                );

                valueWidget.setX(x + width - valueWidget.getWidth());

                valueWidget.setTooltipRegion(x + (this.mutable ? 20 : 0), y, x + width, y + valueWidget.getHeight());

                if (valueWidget instanceof TextWidgetComponent) {
                    ((TextWidgetComponent<?>) valueWidget).withSuggestions(this.suggestionProvider);
                }

                consumer.accept(valueWidget);

                if (this.mutable) {
                    consumer.accept(
                            new TextButton(parent, x, y, 20, 20, 0, new LiteralText("✕"), button -> {
                                this.setValue(this.getValue().remove(index));
                                this.changed = true;
                                this.refresh();
                                return true;
                            })
                    );
                }

                TextWidgetComponent<String> keyWidget = new StringEntryWidget(
                        parent,
                        x + (this.mutable ? 20 : 0),
                        y,
                        width - 20 - valueWidget.getWidth(),
                        height,
                        Alignment.LEFT,
                        () -> "",
                        v -> this.setValue(this.getValue().setKey(index, v)),
                        v -> this.changed = true,
                        value.getKey()
                ).withSuggestions(s -> keySuggestionProvider.getSuggestions(this.getValue(), s));

                keyWidget.addConstraints(this.keyConstraints);

                consumer.accept(keyWidget);

                return valueWidget.getHeight();
            });

            ++i;
        }

        if (this.mutable) {
            category.add((parent, x, y, width, consumer) -> {
                consumer.accept(new TextButton(parent, x, y, width, 20, 0x40000000, new LiteralText("+"), button -> {
                    this.setValue(this.getValue().addEntry());
                    this.changed = true;
                    this.refresh();
                    return true;
                }));

                return 20;
            });
        }

        category.add((parent, x, y, width, consumer) -> {
            consumer.accept(new Dummy());
            return 0;
        });

        return new ConfigScreen(screen, this.parent.style, 0, this.name, category);
    }

    class Dummy extends WidgetComponent implements Mutable {
        public Dummy() {
            super(TableWidget.this.parent, 0, 0, 0, 0);
        }

        @Override
        public void save() {
            TableWidget.this.save();
        }

        @Override
        public void reset() {

        }

        @Override
        public boolean hasChanged() {
            return TableWidget.this.changed;
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
