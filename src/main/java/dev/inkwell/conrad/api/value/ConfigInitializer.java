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

package dev.inkwell.conrad.api.value;

import dev.inkwell.conrad.api.value.data.SaveType;
import dev.inkwell.conrad.api.value.serialization.ConfigSerializer;
import dev.inkwell.conrad.api.value.util.ConfigUpgradeHandler;
import dev.inkwell.conrad.api.value.util.ConfigValueCollector;
import dev.inkwell.conrad.api.value.util.DataCollector;
import dev.inkwell.conrad.api.value.util.Version;
import net.fabricmc.loader.api.SemanticVersion;
import org.jetbrains.annotations.NotNull;

/**
 * Represents one config file.
 * <p>
 * See {@link ConfigProvider} for providing multiple config files with one entrypoint.
 *
 * @param <R> The representation of a config serializer
 */
public interface ConfigInitializer<R> extends ConfigUpgradeHandler<R> {
    /**
     * @return the concrete serializer instance associated with this config file.
     */
    @NotNull ConfigSerializer<R> getSerializer();

    @NotNull SaveType getSaveType();

    /**
     * Adds all config values belonging to this file to the supplied {@link ConfigValueCollector}.
     *
     * @param builder the builder to add to
     */
    void addConfigValues(@NotNull ConfigValueCollector builder);

    /**
     * Used to add data (such as comments) to a config file.
     *
     * @param collector collects data to be added to the config file
     */
    default void addConfigData(@NotNull DataCollector collector) {

    }

    /**
     * @return the name of the file (minus extension) that this config file will be saved as
     */
    default @NotNull String getName() {
        return "config";
    }

    default @NotNull Version getVersion() {
        return new Version(1, 0, 0);
    }

    /**
     * @return an array of folder names that point where this config file will be saved, relative to 'config/namespace'
     */
    default @NotNull String[] getSavePath() {
        return new String[0];
    }

    /**
     * Upgrades on older config file to the version represented by this initializer.
     * <p>
     * Called after post initializers but before the old version is deserialized.
     * <p>
     * All ValueKey's should be fully operational by this point.
     *
     * @param from           the version represented in the representation
     * @param representation the intermediate representation of the existing config file
     * @return whether or not to try to deserialize the existing config file after upgrading
     */
    default boolean upgrade(SemanticVersion from, R representation) {
        return true;
    }
}
