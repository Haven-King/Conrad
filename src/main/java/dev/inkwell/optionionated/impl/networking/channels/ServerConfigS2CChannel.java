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

package dev.inkwell.optionionated.impl.networking.channels;

import dev.inkwell.optionionated.api.ConfigDefinition;
import dev.inkwell.optionionated.api.ConfigManager;
import dev.inkwell.optionionated.api.data.SaveType;
import dev.inkwell.optionionated.api.value.ValueContainer;
import dev.inkwell.optionionated.api.value.ValueContainerProvider;
import dev.inkwell.optionionated.impl.networking.ConfigNetworking;
import dev.inkwell.optionionated.impl.networking.util.Disconnector;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvironmentInterface;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

@EnvironmentInterface(value = EnvType.CLIENT, itf = ClientPlayNetworking.PlayChannelHandler.class)
public class ServerConfigS2CChannel extends S2CChannel {
    private static final Identifier ID = new Identifier("optionionated", "channel/send_server_values");

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public void onPlayReady(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        ValueContainer valueContainer = ValueContainerProvider.getInstance(SaveType.LEVEL).getValueContainer();

        for (ConfigDefinition<?> configDefinition : ConfigManager.getConfigKeys()) {
            if (configDefinition.getSaveType() == SaveType.LEVEL) {
                send(configDefinition, valueContainer, handler.player);
            }
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        ValueContainer container = ValueContainerProvider.getInstance(SaveType.LEVEL).getValueContainer();
        ConfigNetworking.read(buf, s -> container, (Disconnector) handler);
    }

    public static void send(ConfigDefinition<?> configDefinition, ValueContainer valueContainer, ServerPlayerEntity player) {
        PacketByteBuf buf = ConfigNetworking.toPacket(configDefinition, valueContainer);

        if (buf != null) {
            ServerPlayNetworking.send(player, ID, buf);
        }
    }
}
