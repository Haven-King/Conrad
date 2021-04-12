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

package dev.inkwell.conrad.api;

import dev.inkwell.conrad.api.gui.screen.ScreenStyle;
import dev.inkwell.conrad.api.value.ConfigInitializer;
import dev.inkwell.conrad.api.value.data.DataType;
import dev.inkwell.conrad.api.value.util.Array;
import dev.inkwell.conrad.api.value.util.Builders;
import dev.inkwell.conrad.api.value.util.ConfigValueCollector;
import dev.inkwell.conrad.api.value.util.ListView;
import dev.inkwell.conrad.api.value.ValueKey;
import dev.inkwell.conrad.impl.exceptions.ConfigValueException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * Helper class that makes creating configs easier.
 * <p>
 * Config values are registered from the class' fields. Any non-ValueKey fields will be ignored. If any of the ValueKey
 * fields are not public, static, and final an exception will be thrown.
 *
 * @param <R> The representation of a config serializer
 */
public abstract class Config<R> implements ConfigInitializer<R> {
    private final List<ValueKey<?>> valueKeys = new ArrayList<>();

    protected static <T> ValueKey<T> value(Supplier<T> defaultValue) {
        return new ValueKey.Builder<>(defaultValue).build();
    }

    protected static ValueKey<Integer> value(int defaultValue) {
        return new ValueKey.Builder<>(() -> defaultValue).build();
    }

    protected static ValueKey<Long> value(long defaultValue) {
        return new ValueKey.Builder<>(() -> defaultValue).build();
    }

    protected static ValueKey<Float> value(float defaultValue) {
        return new ValueKey.Builder<>(() -> defaultValue).build();
    }

    protected static ValueKey<Double> value(double defaultValue) {
        return new ValueKey.Builder<>(() -> defaultValue).build();
    }

    protected static ValueKey<Boolean> value(boolean defaultValue) {
        return new ValueKey.Builder<>(() -> defaultValue).build();
    }

    protected static ValueKey<Byte> value(byte defaultValue) {
        return new ValueKey.Builder<>(() -> defaultValue).build();
    }

    protected static ValueKey<Short> value(short defaultValue) {
        return new ValueKey.Builder<>(() -> defaultValue).build();
    }

    protected static ValueKey<Character> value(char defaultValue) {
        return new ValueKey.Builder<>(() -> defaultValue).build();
    }

    protected static ValueKey<String> value(String defaultValue) {
        return new ValueKey.Builder<>(() -> defaultValue).with(DataType.COMMENT).build();
    }

    protected static Builders.Number<Integer> builder(int defaultValue) {
        return new Builders.Number<>(() -> defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    protected static Builders.Number<Long> builder(long defaultValue) {
        return new Builders.Number<>(() -> defaultValue, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    protected static Builders.Number<Float> builder(float defaultValue) {
        return new Builders.Number<>(() -> defaultValue, Float.MIN_VALUE, Float.MAX_VALUE);
    }

    protected static Builders.Number<Double> builder(double defaultValue) {
        return new Builders.Number<>(() -> defaultValue, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    protected static ValueKey.Builder<Boolean> builder(boolean defaultValue) {
        return new ValueKey.Builder<>(() -> defaultValue);
    }

    protected static ValueKey.Builder<Byte> builder(byte defaultValue) {
        return new ValueKey.Builder<>(() -> defaultValue);
    }

    protected static ValueKey.Builder<Short> builder(short defaultValue) {
        return new ValueKey.Builder<>(() -> defaultValue);
    }

    protected static ValueKey.Builder<Character> builder(char defaultValue) {
        return new ValueKey.Builder<>(() -> defaultValue);
    }

    protected static ValueKey.Builder<String> builder(String defaultValue) {
        return new ValueKey.Builder<>(() -> defaultValue);
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    protected static <T> ValueKey.CollectionBuilder<Array<T>, T> array(Supplier<T> defaultValue, T... values) {
        return new ValueKey.CollectionBuilder<>(() -> new Array<>((Class<T>) defaultValue.get().getClass(), defaultValue, values));
    }

    protected static <T> Builders.Table<T> table(@NotNull Supplier<@NotNull T> defaultValue) {
        return new Builders.Table<>(defaultValue);
    }

    private static String name(Class<?> clazz) {
        return name(clazz
                .getSimpleName());
    }

    private static String name(String string) {
        return string
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2")
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .toLowerCase(Locale.ROOT);
    }

    @ApiStatus.Internal
    public final void addConfigValues(@NotNull ConfigValueCollector builder) {
        this.process(builder, new String[0], this.getClass());
    }

    private void process(@NotNull ConfigValueCollector builder, @NotNull String[] parent, Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType() == ValueKey.class) {
                int modifier = field.getModifiers();

                if (!Modifier.isFinal(modifier)) {
                    throw new ConfigValueException("Field " + field.getName() + " is not final!");
                }

                if (!Modifier.isStatic(modifier)) {
                    throw new ConfigValueException("Field " + field.getName() + " is not static!");
                }

                if (!Modifier.isPublic(modifier)) {
                    throw new ConfigValueException("Field " + field.getName() + " is not public!");
                }

                try {
                    ValueKey<?> valueKey = (ValueKey<?>) field.get(null);

                    if (valueKey.isInitialized()) {
                        throw new ConfigValueException("ConfigKey " + valueKey.toString() + " already registered!");
                    }

                    String name = name(field.getName());

                    if (parent.length > 0) {
                        String[] paths = Arrays.copyOfRange(parent, 1, parent.length + 1);
                        paths[paths.length - 1] = name;

                        builder.addConfigValue(valueKey, parent[0], paths);
                    } else {
                        builder.addConfigValue(valueKey, name);
                    }

                    this.valueKeys.add(valueKey);
                } catch (IllegalAccessException e) {
                    throw new ConfigValueException("Error reading field " + field.getDeclaringClass().getName() + "." + field.getName());
                }
            }
        }

        Class<?>[] innerClasses = clazz.getDeclaredClasses();

        for (int i = innerClasses.length - 1; i >= 0; --i) {
            String[] nestedParent = Arrays.copyOf(parent, parent.length + 1);
            nestedParent[nestedParent.length - 1] = name(innerClasses[i]);
            process(builder, nestedParent, innerClasses[i]);
        }
    }

    protected ListView<ValueKey<?>> getValues() {
        return new ListView<>(this.valueKeys);
    }
}
