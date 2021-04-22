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

import dev.inkwell.conrad.impl.networking.channels.ConfigValueC2SChannel;
import dev.inkwell.conrad.impl.networking.channels.ServerConfigS2CChannel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerPlayerEntity;

public class SyncConfigValues {
    @Environment(EnvType.CLIENT)
    public static void sendConfigValues(ConfigDefinition<?> configDefinition, ValueContainer valueContainer) {
        ConfigValueC2SChannel.sendToServer(configDefinition, valueContainer);
    }

    private static <R> void sendConfigValues(ConfigDefinition<R> configDefinition, ServerPlayerEntity player, ValueContainer valueContainer) {
        ServerConfigS2CChannel.send(configDefinition, valueContainer, player);
    }
}
