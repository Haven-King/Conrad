package dev.hephaestus.conrad.mixin.client;

import dev.hephaestus.conrad.impl.common.config.PlayerValueContainers;
import dev.hephaestus.conrad.impl.common.config.ValueContainer;
import dev.hephaestus.conrad.impl.common.config.ValueContainerProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ServerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ServerInfo.class)
public class AddRemoteValueContainer implements ValueContainerProvider {
	@Unique private ValueContainer valueContainer;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void initializeValueContainer(String name, String address, boolean local, CallbackInfo ci) {
		this.valueContainer = new ValueContainer.Remote();
	}

	@Override
	public ValueContainer getValueContainer() {
		return this.valueContainer;
	}

	@Override
	public PlayerValueContainers getPlayerValueContainers() {
		return null;
	}
}
