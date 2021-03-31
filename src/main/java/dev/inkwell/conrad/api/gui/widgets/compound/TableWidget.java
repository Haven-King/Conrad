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
import dev.inkwell.conrad.api.gui.builders.ConfigScreenBuilder;
import dev.inkwell.conrad.api.gui.builders.WidgetComponentFactory;
import dev.inkwell.conrad.api.gui.screen.ConfigScreen;
import dev.inkwell.conrad.api.gui.screen.ScreenStyle;
import dev.inkwell.conrad.api.gui.util.Alignment;
import dev.inkwell.conrad.api.gui.util.Group;
import dev.inkwell.conrad.api.gui.util.KeySuggestionProvider;
import dev.inkwell.conrad.api.gui.util.SuggestionProvider;
import dev.inkwell.conrad.api.gui.widgets.TextButton;
import dev.inkwell.conrad.api.gui.widgets.WidgetComponent;
import dev.inkwell.conrad.api.gui.widgets.containers.RowContainer;
import dev.inkwell.conrad.api.gui.widgets.value.ValueWidgetComponent;
import dev.inkwell.conrad.api.gui.widgets.value.entry.StringEntryWidget;
import dev.inkwell.conrad.api.gui.widgets.value.entry.TextWidgetComponent;
import dev.inkwell.conrad.api.value.util.Table;
import dev.inkwell.conrad.impl.gui.widgets.Mutable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class TableWidget<T> extends ValueWidgetComponent<Table<T>> implements ConfigScreenBuilder {
    private final Text name;
    private final WidgetComponentFactory<T> builder;
    private final float scale;
    private final boolean mutable;

    private ConfigScreen screen;
    private boolean changed;

    private KeySuggestionProvider<T> keySuggestionProvider = (v, s) -> Collections.emptyList();
    private SuggestionProvider suggestionProvider = s -> Collections.emptyList();

    public TableWidget(ConfigScreen parent, int x, int y, int width, int height, Supplier<@NotNull Table<T>> defaultValueSupplier, Consumer<Table<T>> changedListener, Consumer<Table<T>> saveConsumer, @NotNull Table<T> value, Text name, WidgetComponentFactory<T> builder, boolean mutable) {
        super(parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, new Table<>(value));
        this.name = name;
        this.scale = this.height / parent.getScale();
        this.builder = builder;
        this.mutable = mutable;
    }

    public TableWidget(ConfigScreen parent, int x, int y, int width, int height, Supplier<@NotNull Table<T>> defaultValueSupplier, Consumer<Table<T>> changedListener, Consumer<Table<T>> saveConsumer, @NotNull Table<T> value, Text name, WidgetComponentFactory<T> builder) {
        this(parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value, name, builder, true);
    }

    public TableWidget<T> withKeySuggestions(KeySuggestionProvider suggestionProvider) {
        this.keySuggestionProvider = suggestionProvider;
        return this;
    }

    public TableWidget<T> withSuggestions(SuggestionProvider suggestionProvider) {
        this.suggestionProvider = suggestionProvider;
        return this;
    }

    @Override
    public ScreenStyle getStyle() {
        return this.parent.getStyle();
    }

    @Override
    public List<Category> build(ConfigScreen parent, int contentLeft, int contentWidth, int y) {
        Group<WidgetComponent> section = new Group<>();
        List<Category> categories = Collections.singletonList(new Category(this.name.copy()));
        categories.get(0).add(section);

        int i = 0;
        int dY = y;
        int height = (int) (this.scale * parent.getScale());
        for (Table.Entry<String, T> value : this.getValue()) {
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

            WidgetComponent keyWidget = new StringEntryWidget(
                    parent,
                    0,
                    dY,
                    (contentWidth - height * (this.mutable ? 2 : 0)) / 2,
                    height,
                    Alignment.LEFT,
                    () -> "",
                    v -> this.setValue(this.getValue().setKey(index, v)),
                    v -> this.changed = true,
                    value.getKey()
            ).withSuggestions(s -> {
                return keySuggestionProvider.getSuggestions(this.getValue(), s);
            });

            WidgetComponent valueWidget = this.builder.build(
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
    public boolean hasError() {
        return false;
    }

    @Override
    public void renderBackground(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isMouseOver(mouseX, mouseY)) {
            this.parent.tryLeave(() -> MinecraftClient.getInstance().openScreen((this.screen = new ConfigScreen(this.parent, this))));
        }

        return false;
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
    protected Text getDefaultValueAsText() {
        return new LiteralText(this.getDefaultValue().toString());
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
