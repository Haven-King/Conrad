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

package dev.inkwell.oliver.api.serialization;

import dev.inkwell.oliver.api.ConfigDefinition;
import dev.inkwell.oliver.api.ConfigManager;
import dev.inkwell.oliver.api.data.DataType;
import dev.inkwell.oliver.api.value.ValueContainer;
import dev.inkwell.oliver.api.value.ValueKey;
import dev.inkwell.oliver.impl.util.ReflectionUtil;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Abstract serializer that can be used as a base for config serializers using GSON-like serialization libraries.
 *
 * @param <E> The most basic building block class of this tree
 * @param <O> The "object" or "map" equivalent for this tree
 */
public abstract class AbstractTreeSerializer<E, O extends E> implements ConfigSerializer<O> {
    private static final Logger LOGGER = LogManager.getLogger();

    @SuppressWarnings("rawtypes")
    private final HashMap<Class<?>, ValueSerializer> serializableTypes = new HashMap<>();
    private final HashMap<Class<?>, Function<ValueKey<?>, ValueSerializer<E, ?, ?>>> typeDependentSerializers = new HashMap<>();

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

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected final <T> void addSerializer(Class<T> valueClass, Function<ValueKey<T>, ValueSerializer<E, ?, T>> serializerBuilder) {
        this.typeDependentSerializers.putIfAbsent(valueClass, (Function) serializerBuilder);
    }

    @SuppressWarnings("unchecked")
    protected final <V> ValueSerializer<E, ?, V> getSerializer(ValueKey<V> valueKey) {
        V defaultValue = valueKey.getDefaultValue();

        if (typeDependentSerializers.containsKey(defaultValue.getClass())) {
            return (ValueSerializer<E, ?, V>) typeDependentSerializers.get(defaultValue.getClass()).apply(valueKey);
        }

        return (ValueSerializer<E, ?, V>) this.getSerializer(defaultValue.getClass());
    }

    @SuppressWarnings("unchecked")
    protected final <V> ValueSerializer<E, ?, V> getSerializer(Class<V> valueClass) {
        return (ValueSerializer<E, ?, V>) serializableTypes.get(valueClass);
    }

    @Override
    public void serialize(ConfigDefinition<O> configDefinition, OutputStream outputStream, ValueContainer valueContainer, Predicate<ValueKey<?>> valuePredicate, boolean minimal) throws IOException {
        O root = this.start(configDefinition.getData(DataType.COMMENT));

        for (ValueKey<?> value : configDefinition) {
            if (valuePredicate.test(value)) {
                doNested(root, value, (object, key) -> {
                    Object v = valueContainer.get(value);
                    Collection<String> comments = minimal ? Collections.emptyList() : ConfigManager.getComments(value);
                    this.add(object, key, this.getSerializer(value).serializeValue(v), comments);
                });
            }
        }

        this.write(root, new BufferedWriter(new OutputStreamWriter(outputStream)), minimal);
    }

    @Override
    public void deserialize(ConfigDefinition<O> configDefinition, InputStream inputStream, ValueContainer valueContainer) throws IOException {
        O root = this.getRepresentation(inputStream);

        MutableBoolean backup = new MutableBoolean(false);

        for (ValueKey<?> value : configDefinition) {
            this.handle(root, valueContainer, value, backup);
        }

        backup.booleanValue();
    }

    private <T> void handle(O root, ValueContainer valueContainer, ValueKey<T> value, MutableBoolean backup) {
        doNested(root, value, (object, key) -> {
            ValueSerializer<E, ?, T> serializer = this.getSerializer(value);
            E representation = this.get(object, key);

            if (representation != null) {
                value.setValue(serializer.deserialize(representation), valueContainer);
            } else {
                backup.setTrue();
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

    protected abstract <V> V get(O object, String s);

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