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

package dev.inkwell.conrad.impl.mixin;

import dev.inkwell.conrad.api.value.PlayerValueContainer;
import dev.inkwell.conrad.api.value.data.SaveType;
import dev.inkwell.conrad.api.value.ValueContainer;
import dev.inkwell.conrad.api.value.ValueContainerProvider;
import dev.inkwell.conrad.impl.ConfigManagerImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Environment(EnvType.CLIENT)
@Mixin(ServerInfo.class)
public class MixinServerInfo implements ValueContainerProvider {
    @Unique
    private Map<UUID, ValueContainer> playerValueContainers;
    @Unique
    private ValueContainer valueContainer;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(String name, String address, boolean local, CallbackInfo ci) {
        this.playerValueContainers = new HashMap<>();
        this.valueContainer = ValueContainer.of(null, SaveType.LEVEL, SaveType.ROOT);
    }

    @Override
    public ValueContainer getValueContainer(SaveType saveType) {
        if (saveType == SaveType.USER) {
            if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
                ConfigManagerImpl.LOGGER.warn("Attempted to get player value container from server provider.");
                ConfigManagerImpl.LOGGER.warn("Returning root config value container.");
            }

            return ValueContainer.ROOT;
        }

        return this.valueContainer;
    }

    @Override
    public ValueContainer getPlayerValueContainer(UUID playerId) {
        if (playerId.equals(MinecraftClient.getInstance().getSession().getProfile().getId())) {
            return ValueContainer.ROOT;
        }

        return this.playerValueContainers.computeIfAbsent(playerId, id -> PlayerValueContainer.of(id, SaveType.USER));
    }

    @NotNull
    @Override
    public Iterator<Map.Entry<UUID, ValueContainer>> iterator() {
        return playerValueContainers.entrySet().iterator();
    }
}
