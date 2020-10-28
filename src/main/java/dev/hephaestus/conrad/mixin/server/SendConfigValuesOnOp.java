package dev.hephaestus.conrad.mixin.server;

import com.mojang.authlib.GameProfile;
import dev.hephaestus.conrad.impl.common.util.ConradUtil;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(PlayerManager.class)
public abstract class SendConfigValuesOnOp {
	@Shadow @Nullable public abstract ServerPlayerEntity getPlayer(UUID uuid);

	@Inject(method = "addToOperators", at = @At("TAIL"))
	private void sendConfigValues(GameProfile profile, CallbackInfo ci) {
		ConradUtil.sendValues(this.getPlayer(profile.getId()));
	}
}
