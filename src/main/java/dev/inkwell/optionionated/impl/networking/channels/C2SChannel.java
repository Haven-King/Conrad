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

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvironmentInterface;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

@EnvironmentInterface(value = EnvType.CLIENT, itf = ClientModInitializer.class)
@EnvironmentInterface(value = EnvType.CLIENT, itf = ClientPlayConnectionEvents.class)
public abstract class C2SChannel implements Channel, ClientPlayConnectionEvents.Join, ServerPlayNetworking.PlayChannelHandler {
    @Override
    @Environment(EnvType.CLIENT)
    public void onInitializeClient() {
        ClientPlayConnectionEvents.JOIN.register(this);
    }

    @Override
    public void onInitialize() {
        ServerPlayConnectionEvents.INIT.register(((handler, server) ->
                ServerPlayNetworking.registerReceiver(handler, this.getId(), this)
        ));
    }
}
