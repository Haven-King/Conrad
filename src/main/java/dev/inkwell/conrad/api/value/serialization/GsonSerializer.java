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

package dev.inkwell.conrad.api.value.serialization;

import com.google.gson.*;
import dev.inkwell.conrad.api.value.util.Array;
import dev.inkwell.conrad.api.value.util.Table;
import dev.inkwell.conrad.api.value.util.Version;
import net.fabricmc.loader.api.VersionParsingException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Map;

public class GsonSerializer extends AbstractTreeSerializer<JsonElement, JsonObject> {
    public static GsonSerializer DEFAULT = new GsonSerializer(new GsonBuilder().setPrettyPrinting().create());

    private final Gson gson;

    public GsonSerializer(Gson gson) {
        super();

        this.gson = gson;

        this.addSerializer(Boolean.class, BooleanSerializer.INSTANCE);
        this.addSerializer(Integer.class, IntSerializer.INSTANCE);
        this.addSerializer(Long.class, LongSerializer.INSTANCE);
        this.addSerializer(String.class, StringSerializer.INSTANCE);
        this.addSerializer(Float.class, FloatSerializer.INSTANCE);
        this.addSerializer(Double.class, DoubleSerializer.INSTANCE);

        this.addSerializer(Array.class, t -> new ArraySerializer<>(t));
        this.addSerializer(Table.class, t -> new TableSerializer<>(t));
    }

    @Override
    public @NotNull String getExtension() {
        return "json";
    }

    @Override
    public @Nullable Version getVersion(InputStream inputStream) throws IOException, VersionParsingException {
        JsonElement s = this.getRepresentation(inputStream).get("version");
        return s != null && s.isJsonPrimitive() && s.getAsJsonPrimitive().isString()
                ? Version.parse(s.getAsString())
                : null;
    }

    @Override
    public @NotNull JsonObject getRepresentation(InputStream inputStream) throws IOException {
        Reader reader = new InputStreamReader(inputStream);
        JsonObject object = new JsonParser().parse(reader).getAsJsonObject();
        reader.close();

        return object;
    }

    @Override
    protected <V> ValueSerializer<JsonElement, ?, V> getDataSerializer(Class<V> clazz) {
        return new DataClassSerializer<>(clazz);
    }

    @Override
    protected <V> ValueSerializer<JsonElement, ?, V> getEnumSerializer(Class<V> valueClass) {
        return new EnumSerializer<>(valueClass);
    }

    @Override
    protected JsonObject start(@Nullable Iterable<String> comments) {
        return new JsonObject();
    }

    @Override
    protected <R extends JsonElement> R add(JsonObject object, String key, R representation, Iterable<String> comments) {
        object.add(key, representation);
        return representation;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <V extends JsonElement> V get(JsonObject object, String s) {
        return (V) object.get(s);
    }

    @Override
    protected void write(JsonObject root, Writer writer, boolean minimal) {
        this.gson.toJson(root, writer);
    }

    interface GsonValueSerializer<R extends JsonElement, V> extends ValueSerializer<JsonElement, R, V> {
    }

    private static class BooleanSerializer implements GsonValueSerializer<JsonPrimitive, Boolean> {
        static final BooleanSerializer INSTANCE = new BooleanSerializer();

        @Override
        public JsonPrimitive serialize(Boolean value) {
            return new JsonPrimitive(value);
        }

        @Override
        public Boolean deserialize(JsonElement representation) {
            return representation.getAsBoolean();
        }
    }

    private static class IntSerializer implements GsonValueSerializer<JsonPrimitive, Integer> {
        static final IntSerializer INSTANCE = new IntSerializer();

        @Override
        public JsonPrimitive serialize(Integer value) {
            return new JsonPrimitive(value);
        }

        @Override
        public Integer deserialize(JsonElement representation) {
            return representation.getAsInt();
        }
    }

    private static class LongSerializer implements GsonValueSerializer<JsonPrimitive, Long> {
        static final LongSerializer INSTANCE = new LongSerializer();

        @Override
        public JsonPrimitive serialize(Long value) {
            return new JsonPrimitive(value);
        }

        @Override
        public Long deserialize(JsonElement representation) {
            return representation.getAsLong();
        }
    }

    private static class StringSerializer implements GsonValueSerializer<JsonPrimitive, String> {
        static final StringSerializer INSTANCE = new StringSerializer();

        @Override
        public JsonPrimitive serialize(String value) {
            return new JsonPrimitive(value);
        }

        @Override
        public String deserialize(JsonElement representation) {
            return representation.getAsString();
        }
    }

    private static class FloatSerializer implements GsonValueSerializer<JsonPrimitive, Float> {
        static final FloatSerializer INSTANCE = new FloatSerializer();

        @Override
        public JsonPrimitive serialize(Float value) {
            return new JsonPrimitive(value);
        }

        @Override
        public Float deserialize(JsonElement representation) {
            return representation.getAsFloat();
        }
    }

    private static class DoubleSerializer implements GsonValueSerializer<JsonPrimitive, Double> {
        public static final DoubleSerializer INSTANCE = new DoubleSerializer();

        @Override
        public JsonPrimitive serialize(Double value) {
            return new JsonPrimitive(value);
        }

        @Override
        public Double deserialize(JsonElement representation) {
            return representation.getAsDouble();
        }
    }

    private class ArraySerializer<T> implements GsonValueSerializer<JsonArray, Array<T>> {
        private final Array<T> defaultValue;

        private ArraySerializer(Array<T> defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public JsonArray serialize(Array<T> value) {
            JsonArray array = new JsonArray();
            ValueSerializer<JsonElement, ?, T> serializer = GsonSerializer.this.getSerializer(this.defaultValue.getValueClass(), this.defaultValue.getDefaultValue().get());

            for (T t : value) {
                array.add(serializer.serialize(t));
            }

            return array;
        }

        @Override
        public Array<T> deserialize(JsonElement representation) {
            ValueSerializer<JsonElement, ?, T> serializer = GsonSerializer.this.getSerializer(this.defaultValue.getValueClass(), this.defaultValue.getDefaultValue().get());

            JsonArray array = (JsonArray) representation;

            //noinspection unchecked
            T[] values = (T[]) java.lang.reflect.Array.newInstance(defaultValue.getValueClass(), array.size());

            int i = 0;

            for (JsonElement element : array) {
                values[i++] = serializer.deserialize(element);
            }

            return new Array<>(this.defaultValue.getValueClass(), this.defaultValue.getDefaultValue(), values);
        }
    }

    private class TableSerializer<T> implements GsonValueSerializer<JsonObject, Table<T>> {
        private final Table<T> defaultValue;

        private TableSerializer(Table<T> defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public JsonObject serialize(Table<T> table) {
            JsonObject object = new JsonObject();
            ValueSerializer<JsonElement, ?, T> serializer = GsonSerializer.this.getSerializer(this.defaultValue.getValueClass(), this.defaultValue.getDefaultValue().get());

            for (Table.Entry<String, T> t : table) {
                object.add(t.getKey(), serializer.serialize(t.getValue()));
            }

            return object;
        }

        @Override
        public Table<T> deserialize(JsonElement representation) {
            ValueSerializer<JsonElement, ?, T> serializer = GsonSerializer.this.getSerializer(this.defaultValue.getValueClass(), this.defaultValue.getDefaultValue().get());

            JsonObject object = (JsonObject) representation;

            //noinspection unchecked
            Table.Entry<String, T>[] values = (Table.Entry<String, T>[]) java.lang.reflect.Array.newInstance(Table.Entry.class, object.size());

            int i = 0;

            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                values[i++] = new Table.Entry<>(entry.getKey(), serializer.deserialize(entry.getValue()));
            }

            return new Table<>(this.defaultValue.getValueClass(), this.defaultValue.getDefaultValue(), values);
        }
    }

    private static class EnumSerializer<T> implements GsonValueSerializer<JsonPrimitive, T> {
        private final Class<T> enumClass;
        private final T[] values;

        @SuppressWarnings("unchecked")
        private EnumSerializer(Class<?> enumClass) {
            this.enumClass = (Class<T>) enumClass;
            this.values = (T[]) enumClass.getEnumConstants();
        }

        @Override
        public JsonPrimitive serialize(T value) {
            return new JsonPrimitive(((Enum<?>) value).name());
        }

        @Override
        public T deserialize(JsonElement representation) {
            for (T value : this.values) {
                if (((Enum<?>) value).name().equals(representation.getAsString())) {
                    return value;
                }
            }

            throw new UnsupportedOperationException("Invalid value '" + representation.getAsString() + "' for enum '" + enumClass.getSimpleName());
        }
    }

    private class DataClassSerializer<T> implements GsonValueSerializer<JsonObject, T> {
        private final Class<T> valueClass;

        private DataClassSerializer(Class<T> valueClass) {
            this.valueClass = valueClass;
        }

        @Override
        public JsonObject serialize(T value) {
            JsonObject object = new JsonObject();

            for (Field field : this.valueClass.getDeclaredFields()) {
                object.add(field.getName(), this.serialize(field, value, field.getType()));
            }

            return object;
        }

        @Override
        public T deserialize(JsonElement representation) {
            try {
                T value = this.valueClass.newInstance();

                for (Field field : this.valueClass.getDeclaredFields()) {
                    field.setAccessible(true);
                    field.set(value, this.deserialize(field, representation.getAsJsonObject(), (Class<T>) field.getType(), (T) field.get(value)));
                }

                return value;
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        @SuppressWarnings("unchecked")
        private <D> JsonElement serialize(Field field, T value, Class<D> clazz) {
            try {
                field.setAccessible(true);
                D d = (D) field.get(value);
                return GsonSerializer.this.getSerializer(clazz, d).serialize(d);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        private <D> D deserialize(Field field, JsonObject from, Class<D> clazz, D defaultValue) {
            return GsonSerializer.this.getSerializer(clazz, defaultValue).deserialize(from.get(field.getName()));
        }
    }
}
