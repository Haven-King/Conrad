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

package dev.inkwell.oliver.api.value;

import dev.inkwell.oliver.api.ConfigDefinition;
import dev.inkwell.oliver.api.ConfigManager;
import dev.inkwell.oliver.api.data.SaveType;
import dev.inkwell.oliver.api.serialization.ConfigSerializer;
import dev.inkwell.oliver.impl.ConfigManagerImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class ValueContainer {
    private static final SaveType[] ROOT_SAVE_TYPES =
            FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT
                    ? new SaveType[]{SaveType.ROOT, SaveType.LEVEL, SaveType.USER}
                    : new SaveType[]{SaveType.ROOT, SaveType.LEVEL};

    public static final ValueContainer ROOT = new ValueContainer(FabricLoader.getInstance().getConfigDir().normalize(),
            ROOT_SAVE_TYPES
    );

    private final Path saveDirectory;
    private final Map<ValueKey<?>, Object> values = new ConcurrentHashMap<>();
    private final Map<ConfigDefinition<?>, Map<ValueKey<?>, Boolean>> modifications = new ConcurrentHashMap<>();
    private final Collection<SaveType> saveTypes = new HashSet<>();

    ValueContainer(Path saveDirectory, SaveType... saveTypes) {
        this.saveDirectory = saveDirectory;
        this.saveTypes.addAll(Arrays.asList(saveTypes));
    }

    public static ValueContainer of(Path saveDirectory, SaveType... saveTypes) {
        ValueContainer valueContainer = new ValueContainer(saveDirectory, saveTypes);

        if (saveDirectory != null) {
            for (ConfigDefinition<?> config : ConfigManager.getConfigKeys()) {
                if (valueContainer.contains(config.getSaveType())) {
                    ConfigManagerImpl.doSerialization(config, valueContainer);
                }
            }
        }

        return valueContainer;
    }

    /**
     * Puts the specified value into this value container.
     *
     * @param valueKey the key of the value to store
     * @param newValue the actual value to store
     * @param <T>      the type of the actual value
     * @return the value previously stored in the ValueContainer, or the default value
     */
    @ApiStatus.Internal
    public <T> T put(@NotNull ValueKey<T> valueKey, @NotNull T newValue) {
        SaveType saveType = valueKey.getConfig().getSaveType();

        if (!this.contains(saveType)) {
            ConfigManagerImpl.LOGGER.warn("Error putting value '{}' for '{}'.", newValue, valueKey);
            ConfigManagerImpl.LOGGER.warn("ValueContainer does not support save type {}", saveType);
            ConfigManagerImpl.LOGGER.warn("Valid save types are [{}]", this.saveTypes.stream().map(Object::toString).collect(Collectors.joining(", ")));
            return null;
        }

        //noinspection unchecked
        T result = (T) (this.values.containsKey(valueKey)
                ? this.values.get(valueKey)
                : valueKey.getDefaultValue());

        if (!newValue.equals(result)) {
            this.modifications.computeIfAbsent(valueKey.getConfig(), key -> new HashMap<>()).put(valueKey, true);
        }

        this.values.put(valueKey, newValue);

        return result;
    }

    /**
     * Gets the stored value of the specified config key stored in this container.
     *
     * @param valueKey the key of the value to fetch
     * @param <T>      the type of the actual value
     * @return the value stored in the ValueContainer, or the default value
     */
    @ApiStatus.Internal
    public <T> T get(ValueKey<T> valueKey) {
        if (!this.values.containsKey(valueKey)) {
            this.values.put(valueKey, valueKey.getDefaultValue());
        }

        //noinspection unchecked
        return (T) this.values.get(valueKey);
    }

    /**
     * Gets the number of values belonging to the specified config key that have unsaved modifications.
     *
     * @param configDefinition the config file in question
     * @return the number of unsaved modified config values
     */
    public int countUnsavedChanges(ConfigDefinition<?> configDefinition) {
        return this.modifications.getOrDefault(configDefinition, Collections.emptyMap()).size();
    }

    /**
     * Determines whether or not the specified config file has unsaved changes.
     *
     * @param configDefinition the config file in question
     * @return whether or not the config file has changes that need to be saved
     */
    public boolean hasUnsavedChanges(ConfigDefinition<?> configDefinition) {
        return this.countUnsavedChanges(configDefinition) > 0;
    }

    /**
     * Saves the specified config file to disk.
     *
     * @param configDefinition the config file in question
     */
    public <R> void save(ConfigDefinition<R> configDefinition) {
        if (this.saveDirectory == null) {
            ConfigManagerImpl.LOGGER.warn("Attempted to save ValueContainer with null save directory.");
            return;
        }

        ConfigSerializer<R> serializer = configDefinition.getSerializer();

        try {
            serializer.serialize(configDefinition, this);
        } catch (IOException e) {
            ConfigManagerImpl.LOGGER.error("Failed to save '{}' to disk", configDefinition);
        }

        this.modifications.remove(configDefinition);
    }

    /**
     * @param saveType the save type to check
     * @return whether or not this container contains configs of the specified type
     */
    public boolean contains(SaveType saveType) {
        return this.saveTypes.contains(saveType);
    }

    /**
     * @return the directory this value container saves configs to
     */
    public Path getSaveDirectory() {
        return this.saveDirectory;
    }
}
