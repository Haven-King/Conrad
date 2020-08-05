package dev.hephaestus.conrad.api;

import dev.hephaestus.conrad.impl.ConradUtils;
import dev.hephaestus.conrad.impl.config.RootConfigManager;
import dev.hephaestus.conrad.impl.config.server.LevelConfigManager;
import dev.hephaestus.conrad.impl.duck.ConfigManagerProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.network.ServerPlayerEntity;

public class Conrad {
	public static <T extends Config> T getConfig(Class<T> configClass) {
		return ConradUtils.getConfigManager(configClass.getAnnotation(Config.SaveType.class).value()).getConfig(configClass);
	}

	public static <T extends Config> T getConfig(Class<T> configClass, ServerPlayerEntity playerEntity) {
		Config.SaveType.Type saveType = configClass.getAnnotation(Config.SaveType.class).value();

		if (saveType == Config.SaveType.Type.CLIENT && playerEntity.getServer() != null) {
			return ConfigManagerProvider.of(playerEntity.getServer()).getPlayerConfigManager().getConfig(playerEntity, configClass);
		} else if (saveType == Config.SaveType.Type.LEVEL) {
			throw new IllegalArgumentException(String.format("Cannot get server config \"%s\" from player instance.", configClass));
		} else {
			throw new IllegalStateException();
		}
	}
}
