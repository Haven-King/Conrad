package dev.inkwell.conrad.api.value.serialization;

import dev.inkwell.conrad.api.value.util.Array;
import dev.inkwell.conrad.api.value.util.Table;
import dev.inkwell.conrad.api.value.util.Version;
import dev.inkwell.owen.Owen;
import dev.inkwell.owen.OwenElement;
import net.fabricmc.loader.api.VersionParsingException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class OwenTreeSerializer extends AbstractTreeSerializer<OwenElement, OwenElement> {
    public static final OwenTreeSerializer INSTANCE = new OwenTreeSerializer(new Owen.Builder());

    private final Owen owen;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public OwenTreeSerializer(Owen.Builder builder) {
        this.owen = builder.build();

        this.addSerializer(Boolean.class, BooleanSerializer.INSTANCE);
        this.addSerializer(Integer.class, IntSerializer.INSTANCE);
        this.addSerializer(Long.class, LongSerializer.INSTANCE);
        this.addSerializer(String.class, StringSerializer.INSTANCE);
        this.addSerializer(Float.class, FloatSerializer.INSTANCE);
        this.addSerializer(Double.class, DoubleSerializer.INSTANCE);

        this.addSerializer(Array.class, valueKey -> new ArraySerializer<>(valueKey.getDefaultValue()));
        this.addSerializer(Table.class, valueKey -> new TableSerializer<>(valueKey.getDefaultValue()));
    }

    @Override
    protected OwenElement start(@Nullable Iterable<String> comments) {
        return Owen.empty();
    }

    @Override
    protected <R extends OwenElement> R add(OwenElement object, String key, R representation, Iterable<String> comments) {
        object.put(key, representation);

        comments.forEach(representation::addComment);

        return representation;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <V extends OwenElement> V get(OwenElement object, String s) {
        return (V) object.get(s);
    }

    @Override
    protected void write(OwenElement root, Writer writer, boolean minimal) throws IOException {
        writer.write(this.owen.toString(root));
    }

    @Override
    public @NotNull String getExtension() {
        return "owen";
    }

    @Override
    public @Nullable Version getVersion(InputStream inputStream) throws VersionParsingException, IOException {
        return Version.parse(this.getRepresentation(inputStream).get("version").asString());
    }

    @Override
    public @NotNull OwenElement getRepresentation(InputStream inputStream) throws IOException {
        String text = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines()
                .collect(Collectors.joining("\n"));

        try {
            return Owen.parse(text);
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    interface OwenValueSerializer<V> extends ValueSerializer<OwenElement, OwenElement, V> {
    }

    private static class BooleanSerializer implements OwenValueSerializer<Boolean> {
        static final BooleanSerializer INSTANCE = new BooleanSerializer();

        @Override
        public OwenElement serialize(Boolean value) {
            return Owen.literal(value.toString());
        }

        @Override
        public Boolean deserialize(OwenElement representation) {
            return Boolean.parseBoolean(representation.asString());
        }
    }

    private static class IntSerializer implements OwenValueSerializer<Integer> {
        static final IntSerializer INSTANCE = new IntSerializer();

        @Override
        public OwenElement serialize(Integer value) {
            return Owen.literal(value.toString());
        }

        @Override
        public Integer deserialize(OwenElement representation) {
            return Integer.parseInt(representation.asString());
        }
    }

    private static class LongSerializer implements OwenValueSerializer<Long> {
        static final LongSerializer INSTANCE = new LongSerializer();

        @Override
        public OwenElement serialize(Long value) {
            return Owen.literal(value.toString());
        }

        @Override
        public Long deserialize(OwenElement representation) {
            return Long.parseLong(representation.asString());
        }
    }

    private static class StringSerializer implements OwenValueSerializer<String> {
        static final StringSerializer INSTANCE = new StringSerializer();

        @Override
        public OwenElement serialize(String value) {
            return Owen.literal(value);
        }

        @Override
        public String deserialize(OwenElement representation) {
            return representation.asString();
        }
    }

    private static class FloatSerializer implements OwenValueSerializer<Float> {
        static final FloatSerializer INSTANCE = new FloatSerializer();

        @Override
        public OwenElement serialize(Float value) {
            return Owen.literal(value.toString());
        }

        @Override
        public Float deserialize(OwenElement representation) {
            return Float.parseFloat(representation.asString());
        }
    }

    private static class DoubleSerializer implements OwenValueSerializer<Double> {
        static final DoubleSerializer INSTANCE = new DoubleSerializer();

        @Override
        public OwenElement serialize(Double value) {
            return Owen.literal(value.toString());
        }

        @Override
        public Double deserialize(OwenElement representation) {
            return Double.parseDouble(representation.asString());
        }
    }

    private class ArraySerializer<T> implements OwenValueSerializer<Array<T>> {
        private final Array<T> defaultValue;

        private ArraySerializer(Array<T> defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public OwenElement serialize(Array<T> value) {
            OwenElement array = Owen.empty();
            ValueSerializer<? extends OwenElement, ?, T> serializer = OwenTreeSerializer.this.getSerializer(value.getValueClass());

            for (T t : value) {
                array.add(serializer.serialize(t));
            }

            return array;
        }

        @Override
        public Array<T> deserialize(OwenElement representation) {
            ValueSerializer<OwenElement, ?, T> serializer = OwenTreeSerializer.this.getSerializer(this.defaultValue.getValueClass());

            //noinspection unchecked
            T[] values = (T[]) java.lang.reflect.Array.newInstance(defaultValue.getValueClass(), representation.asList().size());

            int i = 0;

            for (OwenElement element : representation.asList()) {
                values[i++] = serializer.deserialize(element);
            }

            return new Array<>(this.defaultValue.getValueClass(), this.defaultValue.getDefaultValue(), values);
        }
    }

    private class TableSerializer<T> implements OwenValueSerializer<Table<T>> {
        private final Table<T> defaultValue;

        private TableSerializer(Table<T> defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public OwenElement serialize(Table<T> table) {
            OwenElement object = Owen.empty();
            ValueSerializer<? extends OwenElement, ?, T> serializer = OwenTreeSerializer.this.getSerializer(this.defaultValue.getValueClass());

            for (Table.Entry<String, T> t : table) {
                object.put(t.getKey(), serializer.serialize(t.getValue()));
            }

            return object;
        }

        @Override
        public Table<T> deserialize(OwenElement representation) {
            ValueSerializer<OwenElement, ?, T> serializer = OwenTreeSerializer.this.getSerializer(this.defaultValue.getValueClass());

            //noinspection unchecked
            Table.Entry<String, T>[] values = (Table.Entry<String, T>[]) java.lang.reflect.Array.newInstance(Table.Entry.class, representation.asMap().size());

            int i = 0;

            for (Map.Entry<String, OwenElement> entry : representation.asMap().entrySet()) {
                values[i++] = new Table.Entry<>(entry.getKey(), serializer.deserialize(entry.getValue()));
            }

            return new Table<>(this.defaultValue.getValueClass(), this.defaultValue.getDefaultValue(), values);
        }
    }
}
