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

package dev.inkwell.conrad.impl;

import dev.inkwell.optionionated.api.ConfigDefinition;
import dev.inkwell.optionionated.api.ConfigManager;
import dev.inkwell.optionionated.api.ConfigPostInitializer;
import dev.inkwell.optionionated.api.SyncConfigValues;
import dev.inkwell.optionionated.api.value.ValueContainer;
import dev.inkwell.optionionated.api.value.ValueContainerProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public class Conrad implements ConfigPostInitializer {
    public static void syncAndSave(ConfigDefinition<?> config) {
        ValueContainer valueContainer = ValueContainerProvider.getInstance(config.getSaveType()).getValueContainer();
        SyncConfigValues.sendConfigValues(config, valueContainer);
        ConfigManager.save(config, valueContainer);
    }

    @Override
    public void onConfigsLoaded() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            for (ConfigDefinition<?> configDefinition : ConfigManager.getConfigKeys()) {
                ConfigScreenProvider.register(ConfigManager.getValues(configDefinition));
            }
        }
    }
}
