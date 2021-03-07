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

package dev.inkwell.optionionated.api;

import dev.inkwell.optionionated.api.value.ValueContainer;
import dev.inkwell.optionionated.impl.networking.ConfigSenders;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.impl.networking.server.ServerPlayNetworkHandlerExtensions;
import net.minecraft.server.network.ServerPlayerEntity;

public class SyncConfigValues {
    @Environment(EnvType.CLIENT)
    public static void sendConfigValues(ConfigDefinition<?> configDefinition, ValueContainer valueContainer) {
        ConfigSenders.sendToServer(configDefinition, valueContainer);
    }

    private static <R> void sendConfigValues(ConfigDefinition<R> configDefinition, ServerPlayerEntity player, ValueContainer valueContainer) {
        ConfigSenders.sendToPlayer(configDefinition, ((ServerPlayNetworkHandlerExtensions) player.networkHandler).getAddon(), valueContainer);
    }
}
