package dev.inkwell.conrad.api.value.serialization;

import com.google.common.collect.ImmutableCollection;
import dev.inkwell.conrad.api.value.ConfigDefinition;
import dev.inkwell.conrad.api.value.ValueContainer;
import dev.inkwell.conrad.api.value.ValueKey;
import dev.inkwell.conrad.api.value.data.DataType;
import dev.inkwell.conrad.api.value.util.Array;
import dev.inkwell.conrad.api.value.util.Table;
import dev.inkwell.conrad.api.value.util.Version;
import dev.inkwell.conrad.impl.util.ReflectionUtil;
import dev.inkwell.owen.Owen;
import dev.inkwell.owen.OwenElement;
import net.fabricmc.loader.api.VersionParsingException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Serializes configs in a flat structure, versus {@link OwenTreeSerializer}'s JSON-like structure.
 */
public class FlatOwenSerializer implements ConfigSerializer<OwenElement> {
    public static final FlatOwenSerializer INSTANCE = new FlatOwenSerializer(new Owen.Builder());

    private final Map<Class<?>, ValueSerializer<?>> serializableTypes = new HashMap<>();
    private final Map<Class<?>, Function> serializersRequiringDefaults = new HashMap<>();
    private final Map<Class<?>, EnumSerializer<?>> enumSerializerCache = new HashMap<>();
    private final Map<Class<?>, DataClassSerializer<?>> dataClassSerializerCache = new HashMap<>();

    private final Owen owen;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public FlatOwenSerializer(Owen.Builder builder) {
        this.owen = builder.build();

        this.addSerializer(Boolean.class, new SimpleSerializer<>(Object::toString, Boolean::parseBoolean));
        this.addSerializer(Integer.class, new SimpleSerializer<>(Object::toString, Integer::parseInt));
        this.addSerializer(Long.class, new SimpleSerializer<>(Object::toString, Long::parseLong));
        this.addSerializer(String.class, new SimpleSerializer<>(Object::toString, s -> s));
        this.addSerializer(Float.class, new SimpleSerializer<>(Object::toString, Float::parseFloat));
        this.addSerializer(Double.class, new SimpleSerializer<>(Object::toString, Double::parseDouble));

        this.addSerializer(Array.class, t -> new ArraySerializer<>(t));
        this.addSerializer(Table.class, t -> new TableSerializer<>(t));
    }

    public final <T> void addSerializer(Class<T> valueClass, ValueSerializer<T> valueSerializer) {
        this.serializableTypes.putIfAbsent(valueClass, valueSerializer);

        //noinspection unchecked
        valueClass = (Class<T>) ReflectionUtil.getClass(valueClass);

        for (Class<?> clazz : ReflectionUtil.getClasses(valueClass)) {
            this.serializableTypes.putIfAbsent(clazz, valueSerializer);
        }
    }

    @SuppressWarnings({"rawtypes"})
    protected final <T> void addSerializer(Class<T> valueClass, Function<T, ValueSerializer<T>> serializerBuilder) {
        this.serializersRequiringDefaults.putIfAbsent(valueClass, (Function) serializerBuilder);
    }

    @SuppressWarnings("unchecked")
    protected final <V> ValueSerializer<V> getSerializer(Class<V> valueClass, V defaultValue) {
        if (valueClass.isEnum()) {
            return (ValueSerializer<V>) this.enumSerializerCache.computeIfAbsent(valueClass, EnumSerializer::new);
        }

        if (this.serializableTypes.containsKey(valueClass)) {
            return (ValueSerializer<V>) serializableTypes.get(valueClass);
        }

        if (this.serializersRequiringDefaults.containsKey(valueClass)) {
            return (ValueSerializer<V>) serializersRequiringDefaults.get(valueClass).apply(defaultValue);
        }

        if (this.isDataClass(valueClass)) {
            return (ValueSerializer<V>) this.dataClassSerializerCache.computeIfAbsent(valueClass, DataClassSerializer::new);
        }

        throw new RuntimeException("Cannot get serializer for unregistered type '" + valueClass.getName() + "'");
    }

    @SuppressWarnings("unchecked")
    protected final <V> ValueSerializer<V> getSerializer(ValueKey<V> valueKey) {
        V defaultValue = valueKey.getDefaultValue();

        return this.getSerializer((Class<V>) defaultValue.getClass(), valueKey.getDefaultValue());
    }

    private boolean isDataClass(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            Class<?> fieldType = field.getType();

            if (!this.serializersRequiringDefaults.containsKey(clazz) && !this.isDataClass(fieldType) && !ImmutableCollection.class.isAssignableFrom(fieldType)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void serialize(ConfigDefinition<OwenElement> configDefinition, OutputStream outputStream, ValueContainer valueContainer, Predicate<ValueKey<?>> valuePredicate, boolean minimal) throws IOException {
        OwenElement root = Owen.empty();

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
    public void deserialize(ConfigDefinition<OwenElement> configDefinition, InputStream inputStream, ValueContainer valueContainer) throws IOException {
        OwenElement root = this.getRepresentation(inputStream);

        for (ValueKey<?> valueKey : configDefinition) {
            put(valueKey, root, valueContainer);
        }
    }

    private <T> void put(ValueKey<T> valueKey, OwenElement root, ValueContainer valueContainer) {
        ValueSerializer<T> serializer = this.getSerializer(valueKey);
        OwenElement element = root.get(valueKey.toString());

        if (element != null) {
            valueKey.setValue(serializer.deserialize(element), valueContainer);
        }
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
            return Owen.literal(this.serializer.apply(value));
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
            OwenElement array = Owen.empty();
            ValueSerializer<T> serializer = FlatOwenSerializer.this.getSerializer(value.getValueClass(), this.defaultValue.getDefaultValue().get());

            for (T t : value) {
                array.add(serializer.serialize(t));
            }

            return array;
        }

        @Override
        public Array<T> deserialize(OwenElement representation) {
            ValueSerializer<T> serializer = FlatOwenSerializer.this.getSerializer(this.defaultValue.getValueClass(), this.defaultValue.getDefaultValue().get());

            //noinspection unchecked
            T[] values = (T[]) java.lang.reflect.Array.newInstance(defaultValue.getValueClass(), representation == null ? 0 : representation.asList().size());

            int i = 0;

            if (representation != null) {
                for (OwenElement element : representation.asList()) {
                    values[i++] = serializer.deserialize(element);
                }
            }

            return new Array<>(this.defaultValue.getValueClass(), this.defaultValue.getDefaultValue(), values);
        }
    }

    private static class EnumSerializer<T> implements ValueSerializer<T> {
        private final Class<T> enumClass;
        private final T[] values;

        @SuppressWarnings("unchecked")
        private EnumSerializer(Class<?> enumClass) {
            this.enumClass = (Class<T>) enumClass;
            this.values = (T[]) enumClass.getEnumConstants();
        }

        @Override
        public OwenElement serialize(T value) {
            return Owen.literal(((Enum<?>) value).name());
        }

        @Override
        public T deserialize(OwenElement representation) {
            for (T value : this.values) {
                if (((Enum<?>) value).name().equals(representation.asString())) {
                    return value;
                }
            }

            throw new UnsupportedOperationException("Invalid value '" + representation.asString() + "' for enum '" + enumClass.getSimpleName());
        }
    }

    private class TableSerializer<T> implements ValueSerializer<Table<T>> {
        private final Table<T> defaultValue;

        private TableSerializer(Table<T> defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public OwenElement serialize(Table<T> table) {
            OwenElement object = Owen.empty();
            ValueSerializer<T> serializer = FlatOwenSerializer.this.getSerializer(this.defaultValue.getValueClass(), this.defaultValue.getDefaultValue().get());

            for (Table.Entry<String, T> t : table) {
                object.put(t.getKey(), serializer.serialize(t.getValue()));
            }

            return object;
        }

        @Override
        public Table<T> deserialize(OwenElement representation) {
            ValueSerializer<T> serializer = FlatOwenSerializer.this.getSerializer(this.defaultValue.getValueClass(), this.defaultValue.getDefaultValue().get());

            //noinspection unchecked
            Table.Entry<String, T>[] values = (Table.Entry<String, T>[]) java.lang.reflect.Array.newInstance(Table.Entry.class, representation == null ? 0 : representation.asMap().size());

            int i = 0;

            if (representation != null) {
                for (Map.Entry<String, OwenElement> entry : representation.asMap().entrySet()) {
                    values[i++] = new Table.Entry<>(entry.getKey(), serializer.deserialize(entry.getValue()));
                }
            }

            return new Table<>(this.defaultValue.getValueClass(), this.defaultValue.getDefaultValue(), values);
        }
    }

    private class DataClassSerializer<T> implements ValueSerializer<T> {
        private final Class<T> valueClass;

        private DataClassSerializer(Class<T> valueClass) {
            this.valueClass = valueClass;
        }

        @Override
        public OwenElement serialize(T value) {
            OwenElement element = Owen.empty();

            for (Field field : this.valueClass.getDeclaredFields()) {
                element.put(field.getName(), this.serialize(field, value, field.getType()));
            }

            return element;
        }

        @Override
        public T deserialize(OwenElement representation) {
            try {
                T value = this.valueClass.newInstance();

                for (Field field : this.valueClass.getDeclaredFields()) {
                    field.setAccessible(true);
                    field.set(value, this.deserialize(field, representation, (Class<T>) field.getType(), (T) field.get(value)));
                }

                return value;
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        @SuppressWarnings("unchecked")
        private <D> OwenElement serialize(Field field, T value, Class<D> clazz) {
            try {
                field.setAccessible(true);
                D d = (D) field.get(value);
                return FlatOwenSerializer.this.getSerializer(clazz, d).serialize(d);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        private <D> D deserialize(Field field, OwenElement from, Class<D> clazz, D defaultValue) {
            return FlatOwenSerializer.this.getSerializer(clazz, defaultValue).deserialize(from.get(field.getName()));
        }
    }
}
