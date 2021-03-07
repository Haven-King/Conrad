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

import dev.inkwell.oliver.api.data.SaveType;
import dev.inkwell.oliver.impl.ConfigManagerImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Allows other libraries or mods to add config value containers
 */
public interface ValueContainerProvider {
    ValueContainerProvider ROOT = new ValueContainerProvider() {
        @Override
        public ValueContainer getValueContainer() {
            return ValueContainer.ROOT;
        }

        @Override
        public ValueContainer getPlayerValueContainer(UUID playerId) {
            ConfigManagerImpl.LOGGER.warn("Attempted to get player value container from root provider.");
            ConfigManagerImpl.LOGGER.warn("Returning root config value container.");

            return ValueContainer.ROOT;
        }

        @Override
        public @NotNull Iterator<Map.Entry<UUID, ValueContainer>> iterator() {
            return Collections.emptyIterator();
        }
    };

    static ValueContainerProvider getInstance(SaveType saveType) {
        EnvType envType = FabricLoader.getInstance().getEnvironmentType();

        if (saveType == SaveType.LEVEL && envType == EnvType.CLIENT) {
            MinecraftClient client = MinecraftClient.getInstance();

            if (client.isIntegratedServerRunning() && client.getServer() != null) {
                return ((ValueContainerProvider) client.getServer());
            } else if (client.getCurrentServerEntry() != null) {
                return ((ValueContainerProvider) client.getCurrentServerEntry());
            }
        } else if (saveType == SaveType.USER && envType == EnvType.SERVER) {
            //noinspection deprecation
            return ((ValueContainerProvider) FabricLoader.getInstance().getGameInstance());
        } else if (saveType == SaveType.USER && envType == EnvType.CLIENT) {
            ValueContainerProvider provider = ((ValueContainerProvider) MinecraftClient.getInstance().getCurrentServerEntry());

            if (provider != null) {
                return provider;
            }
        }

        return ROOT;
    }

    ValueContainer getValueContainer();

    ValueContainer getPlayerValueContainer(UUID playerId);

    @NotNull Iterator<Map.Entry<UUID, ValueContainer>> iterator();
}
