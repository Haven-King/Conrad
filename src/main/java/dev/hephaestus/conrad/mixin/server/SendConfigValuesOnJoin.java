package dev.hephaestus.conrad.mixin.server;

import dev.hephaestus.conrad.impl.common.util.ConradUtil;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class SendConfigValuesOnJoin {
	@Shadow public ServerPlayerEntity player;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void sendConfigValues(MinecraftServer server, ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
		ConradUtil.sendValues(player);
	}
}
