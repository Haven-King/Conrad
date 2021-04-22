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

package dev.inkwell.vivian.api.util;

import dev.inkwell.conrad.api.value.util.Table;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;

public final class RegistryKeySuggestor<T> implements KeySuggestionProvider<T> {
    private final Registry<?> registry;

    public RegistryKeySuggestor(Registry<?> registry) {
        this.registry = registry;
    }

    @Override
    public List<String> getSuggestions(Table<T> currentValue, String currentKey) {
        List<String> suggestions = new ArrayList<>();

        for (Identifier id : this.registry.getIds()) {
            String string = id.toString();

            if (!currentValue.containsKey(string) && (!string.equals(currentKey) && string.startsWith(currentKey) || id.getPath().startsWith(currentKey))) {
                suggestions.add(string);
            }
        }

        return suggestions;
    }
}
