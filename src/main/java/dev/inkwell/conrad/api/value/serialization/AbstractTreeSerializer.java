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

import com.google.common.collect.ImmutableCollection;
import dev.inkwell.conrad.api.value.ConfigDefinition;
import dev.inkwell.conrad.api.value.ConfigManager;
import dev.inkwell.conrad.api.value.data.DataType;
import dev.inkwell.conrad.api.value.ValueContainer;
import dev.inkwell.conrad.api.value.ValueKey;
import dev.inkwell.conrad.impl.util.ReflectionUtil;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Abstract serializer that can be used as a base for config serializers using GSON-like serialization libraries.
 *
 * @param <E> The most basic building block class of this tree
 * @param <O> The "object" or "map" equivalent for this tree
 */
public abstract class AbstractTreeSerializer<E, O extends E> implements ConfigSerializer<O> {
    private static final Map<Class<? extends AbstractTreeSerializer>, Map<Class<?>, ValueSerializer>> CLASS_DEFAULTS = new HashMap<>();

    @SuppressWarnings("rawtypes")
    private final Map<Class<?>, ValueSerializer> serializableTypes = new HashMap<>();
    private final Map<Class<?>, Function> serializersRequiringDefaults = new HashMap<>();
    private final Map<Class<?>, ValueSerializer> enumSerializerCache = new HashMap<>();
    @SuppressWarnings("rawtypes")
    private final Map<Class<?>, ValueSerializer> dataSerializeCache = new HashMap<>();

    protected AbstractTreeSerializer() {
        for (Map.Entry<Class<?>, ValueSerializer> entry : CLASS_DEFAULTS.computeIfAbsent(this.getClass(), c -> new HashMap<>()).entrySet()) {
            this.addSerializer(entry.getKey(), entry.getValue());
        }
    }

    /**
     * @param valueClass      the class to be (de)serialized by the specified value serializer
     * @param valueSerializer the serializer that handles (de)serialization
     */
    protected final <T> void addSerializer(Class<T> valueClass, ValueSerializer<E, ?, T> valueSerializer) {
        this.serializableTypes.putIfAbsent(valueClass, valueSerializer);

        //noinspection unchecked
        valueClass = (Class<T>) ReflectionUtil.getClass(valueClass);

        for (Class<?> clazz : ReflectionUtil.getClasses(valueClass)) {
            this.serializableTypes.putIfAbsent(clazz, valueSerializer);
        }
    }

    protected final <T> void addSerializer(Class<T> valueClass, Function<T, ValueSerializer<?, ?, T>> serializerBuilder) {
        this.serializersRequiringDefaults.putIfAbsent(valueClass, serializerBuilder);
    }

    @SuppressWarnings("unchecked")
    protected final <V> ValueSerializer<E, ?, V> getSerializer(ValueKey<V> valueKey) {
        V defaultValue = valueKey.getDefaultValue();

        return this.getSerializer((Class<V>) defaultValue.getClass(), defaultValue);
    }

    @SuppressWarnings("unchecked")
    protected final <V> ValueSerializer<E, ?, V> getSerializer(Class<V> valueClass, V defaultValue) {
        if (valueClass.isEnum()) {
            return this.enumSerializerCache.computeIfAbsent(valueClass, this::getEnumSerializer);
        }

        if (this.serializableTypes.containsKey(valueClass)) {
            return (ValueSerializer<E, ?, V>) serializableTypes.get(valueClass);
        }

        if (this.serializersRequiringDefaults.containsKey(valueClass)) {
            return (ValueSerializer<E, ?, V>) serializersRequiringDefaults.get(valueClass).apply(defaultValue);
        }

        if (this.isDataClass(valueClass)) {
            return this.dataSerializeCache.computeIfAbsent(valueClass, this::getDataSerializer);
        }

        throw new RuntimeException("Cannot get serializer for unregistered type '" + valueClass.getName() + "'");
    }

    protected abstract <V> ValueSerializer<E, ?, V> getDataSerializer(Class<V> clazz);

    protected abstract <V> ValueSerializer<E,?,V> getEnumSerializer(Class<V> valueClass);

    private boolean isDataClass(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            Class<?> fieldType = field.getType();

            if (this.serializableTypes.containsKey(clazz) && !this.isDataClass(fieldType) && !ImmutableCollection.class.isAssignableFrom(fieldType)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void serialize(ConfigDefinition<O> configDefinition, OutputStream outputStream, ValueContainer valueContainer, Predicate<ValueKey<?>> valuePredicate, boolean minimal) throws IOException {
        O root = this.start(configDefinition.getData(DataType.COMMENT));

        this.add(root, "version", this.getSerializer(String.class, "1.0.0").serializeValue(configDefinition.getVersion().toString()), Collections.emptyList());

        for (ValueKey<?> value : configDefinition) {
            if (valuePredicate.test(value)) {
                doNested(root, value, (object, key) -> {
                    Object v = valueContainer.get(value);
                    Collection<String> comments = minimal ? Collections.emptyList() : ConfigManager.getComments(value);
                    this.add(object, key, this.getSerializer(value).serializeValue(v), comments);
                });
            }
        }

        Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        this.write(root, writer, minimal);
        writer.flush();
        writer.close();
    }

    @Override
    public void deserialize(ConfigDefinition<O> configDefinition, InputStream inputStream, ValueContainer valueContainer) throws IOException {
        O root = this.getRepresentation(inputStream);

        for (ValueKey<?> value : configDefinition) {
            this.handle(root, valueContainer, value);
        }
    }

    private <T> void handle(O root, ValueContainer valueContainer, ValueKey<T> value) {
        doNested(root, value, (object, key) -> {
            ValueSerializer<E, ?, T> serializer = this.getSerializer(value);
            E representation = this.get(object, key);

            if (representation != null) {
                value.setValue(serializer.deserialize(representation), valueContainer);
            }
        });
    }

    private void doNested(O root, ValueKey<?> value, Consumer<O, String> consumer) {
        O object = root;
        String[] path = value.getPath();

        for (int i = 0; i < path.length; ++i) {
            if (i == path.length - 1) {
                consumer.consume(object, path[i]);
            } else {
                if (this.get(object, path[i]) == null) {
                    object = this.add(object, path[i], this.start(null), Collections.emptyList());
                } else {
                    object = this.get(object, path[i]);
                }
            }
        }
    }

    protected abstract O start(@Nullable Iterable<String> comments);

    protected abstract <R extends E> R add(O object, String key, R representation, Iterable<String> comments);

    protected abstract <V extends E> V get(O object, String s);

    protected abstract void write(O root, Writer writer, boolean minimal) throws IOException;

    private interface Consumer<T1, T2> {
        void consume(T1 t1, T2 t2);
    }

    public interface ValueSerializer<E, R extends E, V> {
        R serialize(V value);

        @SuppressWarnings("unchecked")
        default R serializeValue(Object value) {
            return this.serialize((V) value);
        }

        V deserialize(E representation);
    }
}
