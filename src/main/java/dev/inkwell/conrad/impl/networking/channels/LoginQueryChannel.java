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

package dev.inkwell.conrad.impl.networking.channels;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvironmentInterface;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@EnvironmentInterface(value = EnvType.CLIENT, itf = ClientLoginNetworking.LoginQueryRequestHandler.class)
public abstract class LoginQueryChannel implements Channel, ServerLoginConnectionEvents.QueryStart, ClientLoginNetworking.LoginQueryRequestHandler, ServerLoginNetworking.LoginQueryResponseHandler {
    protected abstract void sendQuery(ServerLoginNetworkHandler handler, MinecraftServer server, PacketSender packetSender, ServerLoginNetworking.LoginSynchronizer synchronizer);

    protected abstract CompletableFuture<PacketByteBuf> handleQuery(MinecraftClient client, ClientLoginNetworkHandler handler, PacketByteBuf buf, Consumer<GenericFutureListener<? extends Future<? super Void>>> listenerAdder);

    protected abstract void handleQueryResponse(MinecraftServer server, ServerLoginNetworkHandler handler, boolean understood, PacketByteBuf buf, ServerLoginNetworking.LoginSynchronizer synchronizer, PacketSender responseSender);

    @Override
    public final void onLoginStart(ServerLoginNetworkHandler handler, MinecraftServer server, PacketSender sender, ServerLoginNetworking.LoginSynchronizer synchronizer) {
        this.sendQuery(handler, server, sender, synchronizer);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public final CompletableFuture<PacketByteBuf> receive(MinecraftClient client, ClientLoginNetworkHandler handler, PacketByteBuf buf, Consumer<GenericFutureListener<? extends Future<? super Void>>> listenerAdder) {
        return this.handleQuery(client, handler, buf, listenerAdder);
    }


    @Override
    public final void receive(MinecraftServer server, ServerLoginNetworkHandler handler, boolean understood, PacketByteBuf buf, ServerLoginNetworking.LoginSynchronizer synchronizer, PacketSender responseSender) {
        this.handleQueryResponse(server, handler, understood, buf, synchronizer, responseSender);
    }

    @Override
    public final void onInitialize() {
        ServerLoginConnectionEvents.QUERY_START.register(this);
        ServerLoginNetworking.registerGlobalReceiver(this.getId(), this);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public final void onInitializeClient() {
        ClientLoginNetworking.registerGlobalReceiver(this.getId(), this);
    }
}
