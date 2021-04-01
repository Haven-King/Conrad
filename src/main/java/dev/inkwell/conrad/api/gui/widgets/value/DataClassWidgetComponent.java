package dev.inkwell.conrad.api.gui.widgets.value;

import dev.inkwell.conrad.api.EntryBuilderRegistry;
import dev.inkwell.conrad.api.gui.Category;
import dev.inkwell.conrad.api.gui.builders.CategoryBuilder;
import dev.inkwell.conrad.api.gui.builders.ConfigScreenBuilder;
import dev.inkwell.conrad.api.gui.builders.WidgetComponentFactory;
import dev.inkwell.conrad.api.gui.screen.ConfigScreen;
import dev.inkwell.conrad.api.gui.screen.ScreenStyle;
import dev.inkwell.conrad.api.gui.util.Group;
import dev.inkwell.conrad.api.gui.widgets.LabelComponent;
import dev.inkwell.conrad.api.gui.widgets.WidgetComponent;
import dev.inkwell.conrad.api.gui.widgets.containers.RowContainer;
import dev.inkwell.conrad.api.value.ConfigDefinition;
import dev.inkwell.conrad.api.value.util.ListView;
import dev.inkwell.conrad.impl.Conrad;
import dev.inkwell.conrad.impl.data.DataObject;
import dev.inkwell.conrad.impl.gui.widgets.Mutable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DataClassWidgetComponent<D> extends ValueWidgetComponent<D> implements ConfigScreenBuilder {
    private final ConfigDefinition<?> config;
    private final Class<D> dataClass;
    private final Text name;
    private ConfigScreen screen;

    public DataClassWidgetComponent(ConfigDefinition<?> config, ConfigScreen parent, int x, int y, int width, int height, Supplier<@NotNull D> defaultValueSupplier, Consumer<D> changedListener, Consumer<D> saveConsumer, @NotNull D value, Class<D> dataClass) {
        super(parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value);
        this.config = config;
        this.dataClass = dataClass;
        this.name = new TranslatableText(dataClass.getName());
    }

    @Override
    public ScreenStyle getStyle() {
        return this.parent.getStyle();
    }

    @Override
    public List<Category> build(ConfigScreen parent, int contentLeft, int contentWidth, int y) {
        Group<WidgetComponent> section = new Group<>();
        int dY = y;

        int i = 0;
        for (Field field : this.dataClass.getDeclaredFields()) {
            Text name = new TranslatableText(this.dataClass.getName() + "." + field.getName());
            WidgetComponent widget = this.build(name, parent, field.getType(), field, contentWidth / 2, dY);
            WidgetComponent label = new LabelComponent(parent, 0, dY, contentWidth / 2, widget.getHeight(), name, false);

            section.add(new RowContainer(parent, contentLeft, dY, i++, false, label, widget));
            dY += widget.getHeight();
        }

        section.add(new Dummy());

        Category category = new CategoryBuilder(this.name.copy()).setSaveCallback(this::save).build(parent, contentLeft, contentWidth, y);
        List<Category> categories = Collections.singletonList(category);
        category.add(section);

        return categories;
    }

    private <T> WidgetComponent build(Text name, ConfigScreen parent, Class<T> type, Field field, int contentWidth, int dY) {
        try {
            field.setAccessible(true);
            T value = (T) field.get(this.getValue());
            WidgetComponentFactory<T> factory = EntryBuilderRegistry.get(type, t -> {});

            if (factory instanceof EntryBuilderRegistry.DataClassWidgetComponentFactory) {
                ((EntryBuilderRegistry.DataClassWidgetComponentFactory<T>) factory).setOuterSaveConsumer(
                        t -> this.save()
                );
            }

            return factory.build(
                    name,
                    this.config,
                    ListView.empty(),
                    DataObject.EMPTY,
                    parent,
                    0,
                    dY,
                    contentWidth,
                    height,
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

    @Override
    public void renderBackground(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {

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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isMouseOver(mouseX, mouseY)) {
            this.parent.tryLeave(() -> MinecraftClient.getInstance().openScreen((this.screen = new ConfigScreen(this.parent, this))));
        }

        return false;
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
