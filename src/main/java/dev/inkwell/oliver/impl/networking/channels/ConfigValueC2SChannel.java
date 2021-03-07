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

import dev.inkwell.oliver.api.ConfigDefinition;
import dev.inkwell.oliver.api.ConfigManager;
import dev.inkwell.oliver.api.data.SaveType;
import dev.inkwell.oliver.api.value.ValueContainer;
import dev.inkwell.oliver.api.value.ValueContainerProvider;
import dev.inkwell.oliver.impl.networking.*;
import dev.inkwell.oliver.impl.networking.util.ConfigValueCache;
import dev.inkwell.oliver.impl.networking.util.ConfigValueSender;
import dev.inkwell.oliver.impl.networking.util.Disconnector;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvironmentInterface;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

@EnvironmentInterface(value = EnvType.CLIENT, itf = ClientModInitializer.class)
@EnvironmentInterface(value = EnvType.CLIENT, itf = ClientPlayConnectionEvents.Join.class)
public class ConfigValueC2SChannel extends C2SChannel implements ServerPlayConnectionEvents.Disconnect {
    private static final Identifier ID = new Identifier("oliver", "channel/send_client_values");

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public void onInitialize() {
        super.onInitialize();

        ServerPlayConnectionEvents.DISCONNECT.register(this);
    }

    @Override
    public void onPlayDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server) {
        ((ConfigValueCache) server).drop(handler.player);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void onPlayReady(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
        ValueContainer valueContainer = ValueContainerProvider.getInstance(SaveType.USER).getValueContainer();

        for (ConfigDefinition<?> configDefinition : ConfigManager.getConfigKeys()) {
            if (configDefinition.getSaveType() == SaveType.USER) {
                sendToServer(configDefinition, valueContainer);
            }
        }
    }

    @Override
    public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        this.receive(server, player, handler, buf);
    }

    private <R> void receive(MinecraftServer server, ServerPlayerEntity sender, ServerPlayNetworkHandler handler, PacketByteBuf buf) {
        buf.markReaderIndex();

        ConfigNetworking.Result result = ConfigNetworking.read(buf, saveType -> {
            ValueContainerProvider provider = ValueContainerProvider.getInstance(saveType);
            return saveType == SaveType.USER
                    ? provider.getPlayerValueContainer(sender.getUuid())
                    : provider.getValueContainer();
        }, ((Disconnector) handler));

        ConfigDefinition<R> configDefinition = ConfigManager.getDefinition(result.configDefinitionString);

        if (configDefinition != null && configDefinition.getSaveType() == SaveType.LEVEL) {
            PacketByteBuf out = ConfigNetworking.toPacket(configDefinition, result.valueContainer);

            if (out != null) {
                PlayerLookup.all(server).forEach(player -> ServerPlayNetworking.send(player, ServerConfigS2CChannel.ID, out));
            }
        }

        if (result.forward) {
            buf.resetReaderIndex();
            PacketByteBuf peerBuf = new PacketByteBuf(Unpooled.buffer());

            peerBuf.writeUuid(sender.getUuid());
            peerBuf.writeBytes(buf);

            ((ConfigValueSender) server).send(result.configDefinitionString, sender.getUuid(), peerBuf);
        }
    }

    @Environment(EnvType.CLIENT)
    public static <R> void sendToServer(ConfigDefinition<R> configDefinition, ValueContainer valueContainer) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!client.isIntegratedServerRunning() && client.getCurrentServerEntry() == null) return;
        ClientPlayerEntity player = client.player;
        SaveType saveType = configDefinition.getSaveType();

        // If the player doesn't exist we can't check their permission level
        // and if their permission level isn't high enough, we don't want them sending config values
        if (player == null || (saveType == SaveType.LEVEL && !player.hasPermissionLevel(4))
                // Also don't try and sync save types other than Conrad's builtin save types.
                || saveType != SaveType.LEVEL && saveType != SaveType.USER) return;

        PacketByteBuf buf = ConfigNetworking.toPacket(configDefinition, valueContainer);

        if (buf != null) {
            ClientPlayNetworking.send(ID, buf);
        }
    }
}
