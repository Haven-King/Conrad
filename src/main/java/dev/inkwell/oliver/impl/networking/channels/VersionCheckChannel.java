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
import dev.inkwell.oliver.impl.networking.ConfigNetworking;
import io.netty.buffer.Unpooled;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvironmentInterface;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@EnvironmentInterface(value = EnvType.CLIENT, itf = ClientLoginNetworking.LoginQueryRequestHandler.class)
public class VersionCheckChannel extends LoginQueryChannel {
    private static final Identifier ID = new Identifier("oliver", "channel/versions");

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public void sendQuery(ServerLoginNetworkHandler handler, MinecraftServer server, PacketSender sender, ServerLoginNetworking.LoginSynchronizer synchronizer) {
        Collection<ConfigDefinition<?>> syncedLevelConfigs = new ArrayList<>();

        for (ConfigDefinition<?> configDefinition : ConfigManager.getConfigKeys()) {
            if (ConfigNetworking.isSynced(configDefinition)) {
                syncedLevelConfigs.add(configDefinition);
            }
        }

        PacketByteBuf query = new PacketByteBuf(Unpooled.buffer());
        query.writeVarInt(syncedLevelConfigs.size());

        for (ConfigDefinition<?> configDefinition : syncedLevelConfigs) {
            query.writeString(configDefinition.toString());
            query.writeVarInt(configDefinition.getVersion().getVersionComponent(0));
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public CompletableFuture<PacketByteBuf> handleQuery(MinecraftClient client, ClientLoginNetworkHandler handler, PacketByteBuf buf, Consumer<GenericFutureListener<? extends Future<? super Void>>> listenerAdder) {
        int n = buf.readVarInt();

        for (int i = 0; i < n; ++i) {
            String configDefinitionString = buf.readString();
            ConfigDefinition<?> configDefinition = ConfigManager.getDefinition(configDefinitionString);
            int serverMajorVersion = buf.readVarInt();

            if (configDefinition != null && serverMajorVersion != configDefinition.getVersion().getVersionComponent(0)) {
                PacketByteBuf response = new PacketByteBuf(Unpooled.buffer());
                response.writeEnumConstant(ResponseStatus.INCORRECT_VERSION);
                response.writeString(configDefinitionString);
                response.writeVarInt(serverMajorVersion);
                response.writeVarInt(configDefinition.getVersion().getVersionComponent(0));

                return CompletableFuture.completedFuture(response);
            }
        }

        return CompletableFuture.completedFuture(new PacketByteBuf(Unpooled.buffer()).writeEnumConstant(ResponseStatus.SUCCESS));
    }

    @Override
    public void handleQueryResponse(MinecraftServer server, ServerLoginNetworkHandler handler, boolean understood, PacketByteBuf buf, ServerLoginNetworking.LoginSynchronizer synchronizer, PacketSender responseSender) {
        if (!understood) return;

        ResponseStatus status = buf.readEnumConstant(ResponseStatus.class);
        String configDefinitionString;

        if (status == ResponseStatus.INCORRECT_VERSION) {
            configDefinitionString = buf.readString(32767);
            int serverMajorVersion = buf.readVarInt();
            int userMajorVersion = buf.readVarInt();

            handler.disconnect(new TranslatableText("oliver.invalid_version",
                    configDefinitionString,
                    userMajorVersion,
                    serverMajorVersion));
        }
    }

    enum ResponseStatus {
        INCORRECT_VERSION,
        SUCCESS
    }
}
