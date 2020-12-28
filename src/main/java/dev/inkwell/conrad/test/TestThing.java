package dev.inkwell.conrad.test;

import dev.inkwell.conrad.impl.EntryBuilderRegistry;
import dev.inkwell.conrad.impl.JsonSerializer;
import dev.inkwell.conrad.json.JsonArray;
import dev.inkwell.conrad.json.JsonElement;
import dev.inkwell.conrad.json.JsonObject;
import dev.inkwell.conrad.json.JsonPrimitive;
import dev.inkwell.vivid.util.Alignment;
import dev.inkwell.vivid.util.Array;
import dev.inkwell.vivid.widgets.WidgetComponent;
import dev.inkwell.vivid.widgets.compound.ArrayWidget;
import dev.inkwell.vivid.widgets.containers.ComponentContainer;
import dev.inkwell.vivid.widgets.value.EnumDropdownWidget;
import dev.inkwell.vivid.widgets.value.entry.StringEntryWidget;
import net.minecraft.text.TranslatableText;

// So much ugly
public class TestThing<E extends Enum<E>> {
    private final Array<String> array;
    private E enumValue;

    public TestThing(E enumValue, Array<String> array) {
        this.enumValue = enumValue;
        this.array = array;
    }

    static {
        JsonSerializer.INSTANCE.addSerializer(TestThing.class, key ->
                new Serializer<>(key.getDefaultValue().enumValue.getClass()));

        registerBuilder();
    }

    private static <T extends Enum<T>> void registerBuilder() {
        EntryBuilderRegistry.register(TestThing.class, (configValue, parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value) -> { WidgetComponent selector = new EnumDropdownWidget(parent, 0, 0, width / 2, height, () -> (T) defaultValueSupplier.get().enumValue, e -> {
                value.enumValue = (Enum) e;
                changedListener.accept(value);
            }, e -> {
                value.enumValue = (Enum) e;
                saveConsumer.accept(value);
            }, value.enumValue);

            WidgetComponent array = new ArrayWidget<>(parent, width / 2, 0, width / 2, height, () -> (Array<String>) defaultValueSupplier.get().array, a -> {
                value.array.copy(a);
                changedListener.accept(value);
            }, a -> {
                value.array.copy(a);
                saveConsumer.accept(value);
            }, value.array, new TranslatableText("testthing"), (parent1, x1, y1, width1, height1, defaultValueSupplier1, changedListener1, saveConsumer1, value1) ->
                new StringEntryWidget(parent1, x1, y1, width1, height1, Alignment.RIGHT, defaultValueSupplier1, changedListener1, saveConsumer1, value1)
            );

            return new ComponentContainer(parent, x, y, 0, false, selector, array);
        });
    }

    public static class Serializer<T extends Enum<T>> implements JsonSerializer.JsonValueSerializer<JsonObject, TestThing<T>> {
        private final Class<T> enumClass;

        public Serializer(Class<T> enumClass) {
            this.enumClass = enumClass;
        }

        @Override
        public JsonObject serialize(TestThing<T> value) {
            JsonObject object = new JsonObject();
            object.put("enum", new JsonPrimitive(value.enumValue.name()));

            JsonArray array = new JsonArray();

            for (String string : value.array) {
                array.add(new JsonPrimitive(string));
            }

            object.put("array", array);

            return object;
        }

        @Override
        public TestThing<T> deserialize(JsonElement representation) {
            JsonObject object = (JsonObject) representation;

            Array<String> array = new Array<>(String.class, () -> "");

            int i = 0;
            for (JsonElement element : (JsonArray) object.get("array")) {
                array.addEntry();
                array.put(i++, ((JsonPrimitive) element).asString());
            }

            return new TestThing<>(Enum.valueOf(this.enumClass, object.getString("enum", null)), array);
        }
    }
}