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

import dev.inkwell.conrad.api.gui.util.SuggestionProvider;
import dev.inkwell.conrad.api.gui.widgets.value.entry.TextWidgetComponent;
import dev.inkwell.conrad.api.value.ConfigDefinition;
import dev.inkwell.conrad.api.value.util.Array;
import dev.inkwell.conrad.api.gui.Category;
import dev.inkwell.conrad.api.gui.builders.ConfigScreenBuilder;
import dev.inkwell.conrad.api.gui.builders.WidgetComponentFactory;
import dev.inkwell.conrad.api.gui.screen.ConfigScreen;
import dev.inkwell.conrad.api.gui.screen.ScreenStyle;
import dev.inkwell.conrad.api.gui.util.Group;
import dev.inkwell.conrad.api.gui.widgets.TextButton;
import dev.inkwell.conrad.api.gui.widgets.WidgetComponent;
import dev.inkwell.conrad.api.gui.widgets.containers.RowContainer;
import dev.inkwell.conrad.api.gui.widgets.value.ValueWidgetComponent;
import dev.inkwell.conrad.api.value.util.ListView;
import dev.inkwell.conrad.impl.data.DataObject;
import dev.inkwell.conrad.impl.gui.widgets.Mutable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class ArrayWidget<T> extends ValueWidgetComponent<Array<T>> implements ConfigScreenBuilder {
    private final ConfigDefinition<?> config;
    private final Text name;
    private final WidgetComponentFactory<T> builder;
    private final float scale;

    private ConfigScreen screen;
    private boolean changed;

    private SuggestionProvider suggestionProvider = s -> Collections.emptyList();

    public ArrayWidget(ConfigDefinition<?> config, ConfigScreen parent, int x, int y, int width, int height, Supplier<@NotNull Array<T>> defaultValueSupplier, Consumer<Array<T>> changedListener, Consumer<Array<T>> saveConsumer, @NotNull Array<T> value, Text name, WidgetComponentFactory<T> builder) {
        super(parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, new Array<>(value));
        this.config = config;
        this.name = name;
        this.builder = builder;
        this.scale = this.height / parent.getScale();
    }

    public ArrayWidget<T> withSuggestions(SuggestionProvider suggestionProvider) {
        this.suggestionProvider = suggestionProvider;
        return this;
    }

    @Override
    public void renderContents(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        int width = textRenderer.getWidth("▶");

        drawCenteredString(
                matrixStack,
                textRenderer,
                "▶",
                this.x + this.width - 3 - width * this.parent.getScale(),
                (int) (this.y + (this.height - textRenderer.fontHeight * this.parent.getScale()) / 2F),
                0xFFFFFFFF,
                this.parent.getScale()
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isMouseOver(mouseX, mouseY)) {
            this.parent.tryLeave(() ->
                    MinecraftClient.getInstance().openScreen((this.screen = new ConfigScreen(this.parent, this))));
        }

        return false;
    }

    @Override
    public void renderBackground(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {

    }

    @Override
    protected Text getDefaultValueAsText() {
        return new LiteralText(this.getDefaultValue().toString());
    }

    @Override
    public ScreenStyle getStyle() {
        return this.parent.getStyle();
    }

    @Override
    public List<Category> build(ConfigScreen parent, int contentLeft, int contentWidth, int y) {
        Group<WidgetComponent> section = new Group<>();
        List<Category> categories = Collections.singletonList(new Category((MutableText) this.name));
        categories.get(0).add(section);

        int i = 0;
        int dY = y;
        int height = (int) (this.scale * parent.getScale());
        for (T value : this.getValue()) {
            int index = i++;

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

            @SuppressWarnings("SuspiciousNameCombination")
            WidgetComponent up = new TextButton(
                    parent, 0, 0, height, height, 0, new LiteralText("▲"), button ->
            {
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

                    this.screen.setProvider(this);
                    this.changed = true;
                    return true;
                } else {
                    return false;
                }
            }
            );

            @SuppressWarnings("SuspiciousNameCombination")
            WidgetComponent down = new TextButton(
                    parent, 0, 0, height, height, 0, new LiteralText("▼"), button ->
            {
                Array<T> array = this.getValue();
                if (index < array.size() - 1 && array.size() >= 2) {
                    T temp = array.get(index);

                    //noinspection unchecked
                    T[] a = (T[]) java.lang.reflect.Array.newInstance(array.getValueClass(), array.size());

                    for (int j = 0; j < a.length; ++j) {
                        a[j] = array.get(j);
                    }

                    a[index] = a[index];
                    a[index + 1] = temp;

                    this.setValue(new Array<>(array.getValueClass(), array.getDefaultValue(), a));

                    this.screen.setProvider(this);
                    this.changed = true;
                    return true;
                } else {
                    return false;
                }
            }
            );

            WidgetComponent widget = this.builder.build(
                    LiteralText.EMPTY,
                    this.config,
                    ListView.empty(),
                    DataObject.EMPTY,
                    parent,
                    0,
                    dY,
                    contentWidth - height * 3,
                    height,
                    this.getValue().getDefaultValue(),
                    v -> this.setValue(this.getValue().set(index, v)),
                    v -> {},
                    value
            );

            if (widget instanceof TextWidgetComponent) {
                ((TextWidgetComponent<?>) widget).withSuggestions(this.suggestionProvider);
            }


            section.add(new RowContainer(parent, contentLeft, dY, index, false, remove, widget, up, down).withMainComponent(widget));
            dY += widget.getHeight();
        }

        section.add(new AddButton(parent, contentLeft, dY, contentWidth, height, 0x40000000, new LiteralText("+"), button -> {
            this.setValue(this.getValue().addEntry());
            this.screen.setProvider(this);
            this.changed = true;
            return true;
        }));

        return categories;
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
