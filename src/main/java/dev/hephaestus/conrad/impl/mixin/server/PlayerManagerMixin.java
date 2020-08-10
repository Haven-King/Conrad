package dev.hephaestus.conrad.impl.mixin.server;

import com.mojang.authlib.GameProfile;
import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.duck.ConfigManagerProvider;
import dev.hephaestus.conrad.impl.network.packets.all.ConfigInfoPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(method = "addToOperators", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void sendConfigs(GameProfile profile, CallbackInfo ci, ServerPlayerEntity playerEntity) {
        if (playerEntity.hasPermissionLevel(4)) {
            for (Config config : ConfigManagerProvider.of(playerEntity.server).getConfigManager()) {
                new ConfigInfoPacket(config).send(playerEntity);
            }
        }
    }
}
