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

package dev.inkwell.oliver.impl.mixin;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import dev.inkwell.oliver.api.data.SaveType;
import dev.inkwell.oliver.api.value.ValueContainer;
import dev.inkwell.oliver.api.value.ValueContainerProvider;
import dev.inkwell.oliver.impl.networking.channels.ForwardUserConfigsS2CChannel;
import dev.inkwell.oliver.impl.networking.util.ConfigValueCache;
import dev.inkwell.oliver.impl.networking.util.ConfigValueSender;
import dev.inkwell.oliver.impl.util.ClientUtil;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.UserCache;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer implements ValueContainerProvider, ConfigValueCache, ConfigValueSender {
    @Unique
    private Map<UUID, ValueContainer> playerValueContainers;
    @Unique
    private ValueContainer valueContainer;
    @Unique
    private Map<UUID, Map<String, PacketByteBuf>> cachedConfigPackets;

    @Shadow
    public abstract Path getSavePath(WorldSavePath worldSavePath);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(Thread thread, DynamicRegistryManager.Impl impl, LevelStorage.Session session, SaveProperties saveProperties, ResourcePackManager resourcePackManager, Proxy proxy, DataFixer dataFixer, ServerResourceManager serverResourceManager, MinecraftSessionService minecraftSessionService, GameProfileRepository gameProfileRepository, UserCache userCache, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory, CallbackInfo ci) {
        this.playerValueContainers = new HashMap<>();
        this.valueContainer = ValueContainer.of(this.getSavePath(WorldSavePath.ROOT).normalize().resolve("config"), SaveType.LEVEL);
        this.cachedConfigPackets = new HashMap<>();
    }

    @Override
    public ValueContainer getValueContainer() {
        return this.valueContainer;
    }

    @Override
    public ValueContainer getPlayerValueContainer(UUID playerId) {
        if (ClientUtil.isLocalPlayer(playerId)) {
            return ValueContainer.ROOT;
        }

        return this.playerValueContainers.computeIfAbsent(playerId, id -> ValueContainer.of(null, SaveType.USER, SaveType.LEVEL));
    }

    @NotNull
    @Override
    public Iterator<Map.Entry<UUID, ValueContainer>> iterator() {
        return playerValueContainers.entrySet().iterator();
    }

    @Override
    public <R> void send(String configDefinition, UUID except, PacketByteBuf peerBuf) {
        cachedConfigPackets.compute(except, (k, v) -> new HashMap<>()).put(configDefinition, peerBuf);

        PlayerLookup.all(((MinecraftServer) (Object) this)).forEach(player -> {
            if (!player.getUuid().equals(except)) {
                ServerPlayNetworking.send(player, ForwardUserConfigsS2CChannel.ID, peerBuf);
            }
        });
    }

    @Override
    public void drop(ServerPlayerEntity player) {
        cachedConfigPackets.remove(player.getUuid());
    }

    @Override
    public Iterable<Map.Entry<UUID, Map<String, PacketByteBuf>>> cached() {
        return this.cachedConfigPackets.entrySet();
    }

    //    @Override
//    public void sendCached(ServerPlayerEntity player) {
//        for (Map.Entry<UUID, Map<String, PacketByteBuf>> entry : cachedConfigPackets.entrySet()) {
//            if (!entry.getKey().equals(player.getUuid())) {
//                entry.getValue().values().forEach(buf -> {
//                    ServerPlayNetworking.send(player, ConfigNetworking.USER_CONFIG, buf);
//                });
//            }
//        }
//    }
}