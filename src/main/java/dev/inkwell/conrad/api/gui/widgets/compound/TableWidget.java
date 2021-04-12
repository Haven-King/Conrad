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

package dev.inkwell.conrad.api.gui.widgets.compound;

import dev.inkwell.conrad.api.gui.Category;
import dev.inkwell.conrad.api.gui.builders.WidgetComponentFactory;
import dev.inkwell.conrad.api.gui.screen.ConfigScreen;
import dev.inkwell.conrad.api.gui.util.Alignment;
import dev.inkwell.conrad.api.gui.util.Group;
import dev.inkwell.conrad.api.gui.util.KeySuggestionProvider;
import dev.inkwell.conrad.api.gui.util.SuggestionProvider;
import dev.inkwell.conrad.api.gui.widgets.SpacerComponent;
import dev.inkwell.conrad.api.gui.widgets.TextButton;
import dev.inkwell.conrad.api.gui.widgets.WidgetComponent;
import dev.inkwell.conrad.api.gui.widgets.containers.RowContainer;
import dev.inkwell.conrad.api.gui.widgets.value.entry.StringEntryWidget;
import dev.inkwell.conrad.api.gui.widgets.value.entry.TextWidgetComponent;
import dev.inkwell.conrad.api.value.ConfigDefinition;
import dev.inkwell.conrad.api.value.data.Constraint;
import dev.inkwell.conrad.api.value.util.ListView;
import dev.inkwell.conrad.api.value.util.Table;
import dev.inkwell.conrad.impl.data.DataObject;
import dev.inkwell.conrad.impl.gui.widgets.Mutable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class TableWidget<T> extends SubScreenWidget<Table<T>> {
    private final WidgetComponentFactory<T> builder;
    private final boolean mutable;
    private final List<Constraint<String>> keyConstraints = new ArrayList<>();

    private boolean changed;

    private KeySuggestionProvider<T> keySuggestionProvider = (v, s) -> Collections.emptyList();
    private SuggestionProvider suggestionProvider = s -> Collections.emptyList();

    public TableWidget(ConfigDefinition<?> config, ConfigScreen parent, int x, int y, Supplier<@NotNull Table<T>> defaultValueSupplier, Consumer<Table<T>> changedListener, Consumer<Table<T>> saveConsumer, @NotNull Table<T> value, Text name, WidgetComponentFactory<T> builder, boolean mutable) {
        super(config, parent, x, y, defaultValueSupplier, changedListener, saveConsumer, new Table<>(value), name);
        this.builder = builder;
        this.mutable = mutable;
    }

    public TableWidget(ConfigDefinition<?> config, ConfigScreen parent, int x, int y, Supplier<@NotNull Table<T>> defaultValueSupplier, Consumer<Table<T>> changedListener, Consumer<Table<T>> saveConsumer, @NotNull Table<T> value, Text name, WidgetComponentFactory<T> builder) {
        this(config, parent, x, y, defaultValueSupplier, changedListener, saveConsumer, value, name, builder, true);
    }

    public TableWidget<T> withKeySuggestions(KeySuggestionProvider suggestionProvider) {
        this.keySuggestionProvider = suggestionProvider;
        return this;
    }

    public TableWidget<T> withSuggestions(SuggestionProvider suggestionProvider) {
        this.suggestionProvider = suggestionProvider;
        return this;
    }

    public void addKeyConstraint(Constraint<String> constraint) {
        this.keyConstraints.add(constraint);
    }

    @Override
    public List<Category> build(ConfigScreen parent, int contentLeft, int contentWidth, int y) {
        Group<WidgetComponent> section = new Group<>();
        List<Category> categories = Collections.singletonList(new Category(this.name.copy()));
        categories.get(0).add(section);

        int i = 0;
        int dY = y;
        for (Iterator<Table.Entry<String, T>> iterator = this.getValue().iterator(); iterator.hasNext(); ) {
            Table.Entry<String, T> value = iterator.next();
            int index = i++;

            WidgetComponent valueWidget = this.builder.build(
                    new LiteralText(value.getKey()),
                    this.config,
                    ListView.empty(),
                    DataObject.EMPTY,
                    parent,
                    0,
                    dY,
                    (contentWidth) / 2,
                    height,
                    this.getValue().getDefaultValue(),
                    v -> this.setValue(this.getValue().set(index, v)),
                    v -> this.changed = true,
                    value.getValue()
            );

            int height = valueWidget.getHeight();

            @SuppressWarnings("SuspiciousNameCombination")
            WidgetComponent remove = new TextButton(
                    parent, 0, 0, height, height, 0, new LiteralText("✕"), button ->
            {
                this.setValue(this.getValue().remove(index));
                this.screen.setProvider(this);
                this.changed = true;
                return true;
            }
            ) {
                @Override
                protected int highlightColor() {
                    return 0x80FF0000;
                }
            };

            TextWidgetComponent<String> keyWidget = new StringEntryWidget(
                    parent,
                    0,
                    dY,
                    ((contentWidth - height * (this.mutable ? 2 : 0)) / 2) + (valueWidget.isFixedSize() ? (contentWidth / 2 - valueWidget.getWidth()) : 0),
                    height,
                    Alignment.LEFT,
                    () -> "",
                    v -> this.setValue(this.getValue().setKey(index, v)),
                    v -> this.changed = true,
                    value.getKey()
            ).withSuggestions(s -> {
                return keySuggestionProvider.getSuggestions(this.getValue(), s);
            });

            keyWidget.addConstraints(this.keyConstraints);

            if (valueWidget instanceof TextWidgetComponent) {
                ((TextWidgetComponent<?>) valueWidget).withSuggestions(this.suggestionProvider);
            }

            RowContainer row;

            if (this.mutable) {
                row = new RowContainer(parent, contentLeft, dY, index, false, remove, keyWidget, valueWidget);
            } else {
                row = new RowContainer(parent, contentLeft, dY, index, false, keyWidget, valueWidget);
            }

            section.add(row.withMainComponent((mouseX, mouseY) -> mouseX >= this.x + this.width / 2D ? valueWidget : keyWidget));
            dY += valueWidget.getHeight();

            if (iterator.hasNext()) {
                section.add(new SpacerComponent(parent, contentLeft, dY, contentWidth, 7));
                dY += 7;
            }
        }

        if (this.mutable) {
            section.add(new TextButton(parent, contentLeft, dY, contentWidth, height, 0x40000000, new LiteralText("+"), button -> {
                this.setValue(this.getValue().addEntry());
                this.screen.setProvider(this);
                this.changed = true;
                return true;
            }));
        }

        section.add(new Dummy());

        return categories;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isMouseOver(mouseX, mouseY)) {
            this.parent.tryLeave(() -> MinecraftClient.getInstance().openScreen((this.screen = new ConfigScreen(this.parent, this, LiteralText.EMPTY))));
        }

        return false;
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
