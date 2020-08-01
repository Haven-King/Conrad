package dev.hephaestus.conrad.impl.mixin.client;

import dev.hephaestus.conrad.impl.config.ConfigManager;
import dev.hephaestus.conrad.impl.config.client.RemoteConfigManager;
import dev.hephaestus.conrad.impl.config.server.PlayerConfigManager;
import dev.hephaestus.conrad.impl.duck.ConfigManagerProvider;
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
public class ServerInfoConfig implements ConfigManagerProvider {
	@Unique private ConfigManager configManager;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void initializeConfigManager(String name, String address, boolean local, CallbackInfo ci) {
		this.configManager = new RemoteConfigManager();
	}

	@Override
	public ConfigManager getConfigManager() {
		return this.configManager;
	}

	@Override
	public PlayerConfigManager getPlayerConfigManager() {
		return null;
	}
}
