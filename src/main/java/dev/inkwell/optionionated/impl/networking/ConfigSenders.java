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
import dev.inkwell.optionionated.api.data.DataType;
import dev.inkwell.optionionated.api.data.SaveType;
import dev.inkwell.optionionated.api.data.SyncType;
import dev.inkwell.optionionated.api.serialization.ConfigSerializer;
import dev.inkwell.optionionated.api.value.ValueContainer;
import dev.inkwell.optionionated.api.value.ValueKey;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import java.util.function.Predicate;

public class ConfigSenders {
    private static final Logger LOGGER = LogManager.getLogger();

    @Environment(EnvType.CLIENT)
    public static <R> void sendToServer(ConfigDefinition<R> configDefinition, ValueContainer valueContainer) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!client.isIntegratedServerRunning() && client.getCurrentServerEntry() == null) return;
        ClientPlayerEntity player = client.player;
        SaveType saveType = configDefinition.getSaveType();

        // If the player doesn't exist we can't check their permission level
        // and if their permission level isn't high enough, we don't want them sending config values
        if (player == null || (saveType == SaveType.LEVEL && !player.hasPermissionLevel(4))
                // Also don't try and sync save types other than Fabric's builtin save types.
                || saveType != SaveType.LEVEL && saveType != SaveType.USER) return;

        PacketByteBuf buf = toPacket(configDefinition, valueContainer);

        if (buf != null) {
            ClientPlayNetworking.send(ConfigNetworking.SYNC_CONFIG, buf);
        }
    }

    public static <R> void sendToPlayer(ConfigDefinition<R> configDefinition, PacketSender sender, ValueContainer valueContainer) {
        PacketByteBuf buf = toPacket(configDefinition, valueContainer);

        if (buf != null) {
            sender.sendPacket(ConfigNetworking.SYNC_CONFIG, buf);
        }
    }

    public static <R> void sendToPlayers(MinecraftServer server, ConfigDefinition<R> configDefinition, ValueContainer valueContainer) {
        PacketByteBuf buf = toPacket(configDefinition, valueContainer);

        if (buf != null) {
            PlayerLookup.all(server).forEach(player -> ServerPlayNetworking.send(player, ConfigNetworking.SYNC_CONFIG, buf));
        }
    }

    public static <R> @Nullable PacketByteBuf toPacket(ConfigDefinition<R> configDefinition, ValueContainer valueContainer) {
        SaveType saveType = configDefinition.getSaveType();

        Predicate<ValueKey<?>> predicate = key -> true;
        boolean forward = false;

        // We only want to construct and send the packet if it's actually gonna contain values
        if (saveType == SaveType.USER) {
            Collection<SyncType> syncTypes = new HashSet<>();

            // Checks each config key
            for (ValueKey<?> value : configDefinition) {
                for (SyncType syncType : value.getData(DataType.SYNC_TYPE)) {
                    if (syncType != SyncType.NONE) {
                        syncTypes.add(syncType);
                    }
                }
            }

            // Checks the config definition itself
            for (SyncType syncType : configDefinition.getData(DataType.SYNC_TYPE)) {
                if (syncType != SyncType.NONE) {
                    syncTypes.add(syncType);
                }
            }

            if (syncTypes.isEmpty()) return null;

            forward = true;
            predicate = key -> !key.getData(DataType.SYNC_TYPE).isEmpty();
        }

        ConfigSerializer<R> serializer = configDefinition.getSerializer();
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeString(configDefinition.toString());
        buf.writeString(configDefinition.getVersion().toString());
        buf.writeBoolean(forward);

        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            serializer.serialize(configDefinition, stream, valueContainer, predicate, true);
            byte[] bytes = stream.toByteArray();

            // If we didn't actually serialize anything, don't bother sending
            if (bytes.length > 0) {
                buf.writeByteArray(bytes);
                return buf;
            }
        } catch (IOException e) {
            LOGGER.error("Failed to sync config '{}': {}", configDefinition, e.getMessage());
        }

        return null;
    }

    public static <R> void sendConfigValues(MinecraftServer server, String configDefinition, UUID except, PacketByteBuf buf) {
        PacketByteBuf peerBuf = new PacketByteBuf(Unpooled.buffer());

        peerBuf.writeUuid(except);
        peerBuf.writeBytes(buf);

        ((ConfigValueSender) server).send(configDefinition, except, peerBuf);
    }
}
