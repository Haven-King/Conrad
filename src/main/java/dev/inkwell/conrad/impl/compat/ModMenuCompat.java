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

package dev.inkwell.conrad.impl.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.inkwell.conrad.impl.gui.ConfigScreenProviderImpl;

import java.util.HashMap;
import java.util.Map;

public class ModMenuCompat implements ModMenuApi {
    private final Map<String, ConfigScreenFactory<?>> factories = new HashMap<>();

    public ModMenuCompat() {
        ConfigScreenProviderImpl.forEach((string, screenFunction) -> factories.put(string, screenFunction::apply));
    }

    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        return factories;
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return factories.get("conrad");
    }
}
