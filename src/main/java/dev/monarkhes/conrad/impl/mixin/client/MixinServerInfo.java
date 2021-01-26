package dev.monarkhes.conrad.impl.mixin.client;

import dev.monarkhes.conrad.impl.Conrad;
import dev.monarkhes.conrad.impl.value.Remote;
import dev.monarkhes.conrad.impl.value.ValueContainer;
import dev.monarkhes.conrad.impl.util.ValueContainerProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
    @Unique private Map<UUID, ValueContainer> playerValueContainers;
    @Unique private ValueContainer valueContainer;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(String name, String address, boolean local, CallbackInfo ci) {
        this.playerValueContainers = new HashMap<>();
        this.valueContainer = new Remote();
    }

    @Override
    public ValueContainer getValueContainer() {
        return this.valueContainer;
    }

    @Override
    public ValueContainer getPlayerValueContainer(UUID playerId) {
        if (playerId.equals(MinecraftClient.getInstance().getSession().getProfile().getId())) {
            return ValueContainer.ROOT;
        }

        return this.playerValueContainers.computeIfAbsent(playerId, id -> new Remote());
    }

    @Override
    public int playerCount() {
        return playerValueContainers.size();
    }

    @NotNull
    @Override
    public Iterator<Map.Entry<UUID, ValueContainer>> iterator() {
        return playerValueContainers.entrySet().iterator();
    }
}
