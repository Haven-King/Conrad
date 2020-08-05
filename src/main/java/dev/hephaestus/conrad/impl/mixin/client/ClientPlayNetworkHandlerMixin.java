package dev.hephaestus.conrad.impl.mixin.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.config.RootConfigManager;
import dev.hephaestus.conrad.impl.network.NetworkingException;
import dev.hephaestus.conrad.impl.network.packets.c2s.ConfigInfoC2SPacket;
import dev.hephaestus.conrad.impl.network.packets.c2s.PingC2SPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void sendConradPing(GameJoinS2CPacket packet, CallbackInfo ci) {
        new PingC2SPacket().send();

        for (Config config : RootConfigManager.INSTANCE) {
            if (config.getClass().getAnnotation(Config.SaveType.class).value() == Config.SaveType.Type.CLIENT) {
                new ConfigInfoC2SPacket(config).send();
            }
        }
    }
}
