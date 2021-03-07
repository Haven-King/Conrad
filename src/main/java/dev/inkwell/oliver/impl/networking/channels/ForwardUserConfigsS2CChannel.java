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

package dev.inkwell.oliver.impl.networking.channels;

import dev.inkwell.oliver.api.data.SaveType;
import dev.inkwell.oliver.api.value.ValueContainer;
import dev.inkwell.oliver.api.value.ValueContainerProvider;
import dev.inkwell.oliver.impl.networking.ConfigNetworking;
import dev.inkwell.oliver.impl.networking.util.ConfigValueCache;
import dev.inkwell.oliver.impl.networking.util.Disconnector;
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
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.UUID;

@EnvironmentInterface(value = EnvType.CLIENT, itf = ClientPlayNetworking.PlayChannelHandler.class)
public class ForwardUserConfigsS2CChannel extends S2CChannel {
    public static final Identifier ID = new Identifier("oliver", "channel/forward_user_values");

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public void onPlayReady(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        UUID receiverId = handler.player.getUuid();

        for (Map.Entry<UUID, Map<String, PacketByteBuf>> entry : ((ConfigValueCache) server).cached()) {
            if (!entry.getKey().equals(receiverId)) {
                entry.getValue().values().forEach(buf -> ServerPlayNetworking.send(handler.player, this.getId(), buf));
            }
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        UUID user = buf.readUuid();
        ValueContainer container = ValueContainerProvider.getInstance(SaveType.USER).getPlayerValueContainer(user);

        ConfigNetworking.read(buf, s -> container, (Disconnector) handler);
    }
}
