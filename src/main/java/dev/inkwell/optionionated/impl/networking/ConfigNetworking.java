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

package dev.inkwell.optionionated.impl.networking;

import dev.inkwell.optionionated.api.ConfigDefinition;
import dev.inkwell.optionionated.api.ConfigManager;
import dev.inkwell.optionionated.api.data.SaveType;
import dev.inkwell.optionionated.api.value.ValueContainer;
import dev.inkwell.optionionated.api.value.ValueContainerProvider;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class ConfigNetworking implements ModInitializer, ClientModInitializer {
    public static final Identifier SYNC_CONFIG = new Identifier("fabric", "packet/sync_values");
    public static final Identifier USER_CONFIG = new Identifier("fabric", "packet/sync_values/user");

    @Override
    @Environment(EnvType.CLIENT)
    public void onInitializeClient() {
        ClientLoginNetworking.registerGlobalReceiver(SYNC_CONFIG, ConfigReceivers::receiveLevelConfigValues);
        ClientLoginNetworking.registerGlobalReceiver(USER_CONFIG, ConfigReceivers::receiveUserConfigValues);

        ClientPlayConnectionEvents.INIT.register((handler, client) -> {
            ClientPlayNetworking.registerReceiver(SYNC_CONFIG, ConfigReceivers::receiveLevelConfigValues);
            ClientPlayNetworking.registerReceiver(USER_CONFIG, ConfigReceivers::receiveUserConfigValues);

            ValueContainer valueContainer = ValueContainerProvider.getInstance(SaveType.USER).getValueContainer();

            for (ConfigDefinition<?> configDefinition : ConfigManager.getConfigKeys()) {
                if (configDefinition.getSaveType() == SaveType.USER) {
                    ConfigSenders.sendToServer(configDefinition, valueContainer);
                }
            }
        });
    }

    @Override
    public void onInitialize() {
        ServerLoginNetworking.registerGlobalReceiver(USER_CONFIG, ConfigReceivers::handleUserSyncResponse);
        ServerLoginNetworking.registerGlobalReceiver(SYNC_CONFIG, ConfigReceivers::handleLevelSyncResponse);

        ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
            ValueContainer valueContainer = ValueContainerProvider.getInstance(SaveType.LEVEL).getValueContainer();

            for (ConfigDefinition<?> configDefinition : ConfigManager.getConfigKeys()) {
                SaveType saveType = configDefinition.getSaveType();

                if (saveType == SaveType.LEVEL) {
                    ConfigSenders.sendToPlayer(configDefinition, sender, valueContainer);
                } else if (saveType == SaveType.USER) {
                    PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

                    buf.writeString(configDefinition.toString());
                    buf.writeString(configDefinition.getVersion().toString());

                    sender.sendPacket(USER_CONFIG, buf);
                }
            }
        });

        ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) -> {
            ServerPlayNetworking.registerReceiver(handler, SYNC_CONFIG, ConfigReceivers::receiveConfigValues);
            ((ConfigValueSender) server).sendCached(handler.player);
        }));

        ServerPlayConnectionEvents.DISCONNECT.register(((handler, server) ->
                ((ConfigValueSender) server).drop(handler.player)));
    }
}
