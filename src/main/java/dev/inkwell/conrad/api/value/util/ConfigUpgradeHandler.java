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

package dev.inkwell.conrad.api.value.util;

import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public interface ConfigUpgradeHandler<R> {
    /**
     * Upgrades on older config file to the version represented by this initializer.
     * <p>Called after post initializers but before the old version is deserialized.<p>
     * <p>All ValueKey's should be fully operational by this point.</p>
     *
     * @param from           the version represented in the representation
     * @param representation the intermediate representation of the existing config file
     * @return whether or not to try to deserialize the existing config file after upgrading
     */
    default boolean upgrade(@Nullable Version from, R representation) {
        return false;
    }

    /**
     * @return a relative path to the config file this config replaces. See {@link #migrate}
     */
    default @Nullable Path getMigrationCandidate() {
        return null;
    }

    /**
     * Migrates a config file created using another, possibly absent system.
     * <p>Called after post initializers.</p>
     *
     * @param path the full path of the config file to migrate
     * @return whether or not the old config file should be deleted
     */
    default boolean migrate(Path path) {
        return false;
    }
}
