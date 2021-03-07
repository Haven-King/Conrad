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

package dev.inkwell.optionionated.api.value;

import dev.inkwell.optionionated.api.util.Version;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ConfigUpgrade<R> {
    /**
     * @param from           the version represented in the representation
     * @param representation the intermediate representation of the existing config file
     * @return whether or not to try to deserialize the existing config file after upgrading
     */
    boolean upgrade(@Nullable Version from, R representation);
}
