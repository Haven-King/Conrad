package dev.hephaestus.conrad.impl.mixin.server;

import dev.hephaestus.conrad.impl.config.ConfigManager;
import dev.hephaestus.conrad.impl.config.RootConfigManager;
import dev.hephaestus.conrad.impl.config.server.PlayerConfigManager;
import dev.hephaestus.conrad.impl.config.server.WorldConfigManager;
import dev.hephaestus.conrad.impl.duck.ConfigManagerProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerConfig implements ConfigManagerProvider {
	@Unique private ConfigManager configManager;
	@Unique private PlayerConfigManager playerConfigManager;

	@Inject(method = "loadWorld", at = @At("TAIL"))
	private void initializeConfigManagers(CallbackInfo ci) {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			configManager = new WorldConfigManager();
		} else {
			configManager = RootConfigManager.INSTANCE;
		}

		playerConfigManager = new PlayerConfigManager();
	}

	@Override
	public ConfigManager getConfigManager() {
		return this.configManager;
	}

	@Override
	public PlayerConfigManager getPlayerConfigManager() {
		return this.playerConfigManager;
	}
}
