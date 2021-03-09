package dev.inkwell.conrad.api.value.serialization;

import dev.inkwell.conrad.api.value.ConfigDefinition;
import dev.inkwell.conrad.api.value.data.DataType;
import dev.inkwell.conrad.api.value.util.Array;
import dev.inkwell.conrad.api.value.util.Table;
import dev.inkwell.conrad.api.value.util.Version;
import dev.inkwell.conrad.api.value.ValueContainer;
import dev.inkwell.conrad.api.value.ValueKey;
import dev.inkwell.conrad.impl.util.ReflectionUtil;
import dev.inkwell.owen.Owen;
import dev.inkwell.owen.OwenElement;
import net.fabricmc.loader.api.VersionParsingException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FlatOwenSerializer implements ConfigSerializer<OwenElement> {
    public static final FlatOwenSerializer INSTANCE = new FlatOwenSerializer(new Owen.Builder());

    private final HashMap<Class<?>, ValueSerializer<?>> serializableTypes = new HashMap<>();
    private final HashMap<Class<?>, Function<ValueKey<?>, ValueSerializer<?>>> typeDependentSerializers = new HashMap<>();
    private final Owen owen;

    public FlatOwenSerializer(Owen.Builder builder) {
        this.owen = builder.build();

        this.addSerializer(Boolean.class, new SimpleSerializer<>(Object::toString, Boolean::parseBoolean));
        this.addSerializer(Integer.class, new SimpleSerializer<>(Object::toString, Integer::parseInt));
        this.addSerializer(Long.class, new SimpleSerializer<>(Object::toString, Long::parseLong));
        this.addSerializer(String.class, new SimpleSerializer<>(Object::toString, s -> s));
        this.addSerializer(Float.class, new SimpleSerializer<>(Object::toString, Float::parseFloat));
        this.addSerializer(Double.class, new SimpleSerializer<>(Object::toString, Double::parseDouble));

        this.addSerializer(Array.class, valueKey -> new ArraySerializer<>(valueKey.getDefaultValue()));
        this.addSerializer(Table.class, valueKey -> new TableSerializer<>(valueKey.getDefaultValue()));
    }

    public final <T> void addSerializer(Class<T> valueClass, ValueSerializer<T> valueSerializer) {
        this.serializableTypes.putIfAbsent(valueClass, valueSerializer);

        //noinspection unchecked
        valueClass = (Class<T>) ReflectionUtil.getClass(valueClass);

        for (Class<?> clazz : ReflectionUtil.getClasses(valueClass)) {
            this.serializableTypes.putIfAbsent(clazz, valueSerializer);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected final <T> void addSerializer(Class<T> valueClass, Function<ValueKey<T>, ValueSerializer<T>> serializerBuilder) {
        this.typeDependentSerializers.putIfAbsent(valueClass, (Function) serializerBuilder);
    }

    @SuppressWarnings("unchecked")
    protected final <V> ValueSerializer<V> getSerializer(Class<V> valueClass) {
        return (ValueSerializer<V>) serializableTypes.get(valueClass);
    }

    @SuppressWarnings("unchecked")
    protected final <V> ValueSerializer<V> getSerializer(ValueKey<V> valueKey) {
        V defaultValue = valueKey.getDefaultValue();

        if (typeDependentSerializers.containsKey(defaultValue.getClass())) {
            return (ValueSerializer<V>) typeDependentSerializers.get(defaultValue.getClass()).apply(valueKey);
        }

        return (ValueSerializer<V>) this.getSerializer(defaultValue.getClass());
    }

    @Override
    public void serialize(ConfigDefinition<OwenElement> configDefinition, OutputStream outputStream, ValueContainer valueContainer, Predicate<ValueKey<?>> valuePredicate, boolean minimal) throws IOException {
        OwenElement root = new OwenElement();

        if (!minimal) {
            configDefinition.getData(DataType.COMMENT).forEach(root::addComment);
        }

        root.put("version", configDefinition.getVersion().toString());

        for (ValueKey<?> valueKey : configDefinition) {
            if (valuePredicate.test(valueKey)) {
                OwenElement element = get(valueContainer, valueKey);

                if (!minimal) {
                    valueKey.getData(DataType.COMMENT).forEach(element::addComment);
                }

                root.put(valueKey.toString(), element);
            }
        }

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            writer.write(this.owen.toString(root));
        }
    }

    private <T> OwenElement get(ValueContainer valueContainer, ValueKey<T> valueKey) {
        ValueSerializer<T> serializer = this.getSerializer(valueKey);
        return serializer.serialize(valueContainer.get(valueKey));
    }

    @Override
    public void deserialize(ConfigDefinition<OwenElement> configDefinition, InputStream inputStream, ValueContainer valueContainer) {
        OwenElement root = this.getRepresentation(inputStream);

        for (ValueKey<?> valueKey : configDefinition) {
            put(valueKey, root, valueContainer);
        }
    }

    private <T> void put(ValueKey<T> valueKey, OwenElement root, ValueContainer valueContainer) {
        ValueSerializer<T> serializer = this.getSerializer(valueKey);
        OwenElement element = root.get(valueKey.toString());

        if (element != null) {
            valueContainer.put(valueKey, serializer.deserialize(element));
        }
    }

    @Override
    public @NotNull String getExtension() {
        return "owen";
    }

    @Override
    public @Nullable Version getVersion(InputStream inputStream) throws VersionParsingException {
        return Version.parse(this.getRepresentation(inputStream).get("version").asString());
    }

    @Override
    public @NotNull OwenElement getRepresentation(InputStream inputStream) {
        String text = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines()
                .collect(Collectors.joining("\n"));

        return this.owen.parse(text);
    }

    public interface ValueSerializer<V> {
        OwenElement serialize(V value);

        V deserialize(OwenElement representation);
    }

    public static class SimpleSerializer<V> implements ValueSerializer<V> {
        private final Function<V, String> serializer;
        private final Function<String, V> deserializer;

        public SimpleSerializer(Function<V, String> serializer, Function<String, V> deserializer) {
            this.serializer = serializer;
            this.deserializer = deserializer;
        }

        @Override
        public OwenElement serialize(V value) {
            return Owen.string(this.serializer.apply(value));
        }

        @Override
        public V deserialize(OwenElement representation) {
            return deserializer.apply(representation.asString());
        }
    }

        private class ArraySerializer<T> implements ValueSerializer<Array<T>> {
        private final Array<T> defaultValue;

        private ArraySerializer(Array<T> defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public OwenElement serialize(Array<T> value) {
            OwenElement array = new OwenElement();
            ValueSerializer<T> serializer = FlatOwenSerializer.this.getSerializer(value.getValueClass());

            for (T t : value) {
                array.add(serializer.serialize(t));
            }

            return array;
        }

        @Override
        public Array<T> deserialize(OwenElement representation) {
            ValueSerializer<T> serializer = FlatOwenSerializer.this.getSerializer(this.defaultValue.getValueClass());

            //noinspection unchecked
            T[] values = (T[]) java.lang.reflect.Array.newInstance(defaultValue.getValueClass(), representation.asList().size());

            int i = 0;

            for (OwenElement element : representation.asList()) {
                values[i++] = serializer.deserialize(element);
            }

            return new Array<>(this.defaultValue.getValueClass(), this.defaultValue.getDefaultValue(), values);
        }
    }

    private class TableSerializer<T> implements ValueSerializer<Table<T>> {
        private final Table<T> defaultValue;

        private TableSerializer(Table<T> defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public OwenElement serialize(Table<T> table) {
            OwenElement object = new OwenElement();
            ValueSerializer<T> serializer = FlatOwenSerializer.this.getSerializer(this.defaultValue.getValueClass());

            for (Table.Entry<String, T> t : table) {
                object.put(t.getKey(), serializer.serialize(t.getValue()));
            }

            return object;
        }

        @Override
        public Table<T> deserialize(OwenElement representation) {
            ValueSerializer<T> serializer = FlatOwenSerializer.this.getSerializer(this.defaultValue.getValueClass());

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
