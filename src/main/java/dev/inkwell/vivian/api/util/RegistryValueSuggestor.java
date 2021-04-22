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

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public class RegistryValueSuggestor<T> implements SuggestionProvider {
    private final Registry<T> registry;
    private final BiPredicate<Registry<T>, Identifier> predicate;

    public RegistryValueSuggestor(Registry<T> registry, BiPredicate<Registry<T>, Identifier> predicate) {
        this.registry = registry;
        this.predicate = predicate;
    }

    public RegistryValueSuggestor(Registry<T> registry) {
        this(registry, (r, id) -> true);
    }

    @Override
    public List<String> getSuggestions(String currentValue) {
        List<String> suggestions = new ArrayList<>();

        for (Identifier id : this.registry.getIds()) {
            String string = id.toString();

            if (predicate.test(this.registry, id) && (!string.equals(currentValue) && string.startsWith(currentValue) || id.getPath().startsWith(currentValue))) {
                suggestions.add(string);
            }
        }

        return suggestions;
    }
}
