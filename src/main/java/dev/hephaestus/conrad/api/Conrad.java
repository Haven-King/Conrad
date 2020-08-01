package dev.hephaestus.conrad.api;

import dev.hephaestus.conrad.api.annotation.SaveType;
import dev.hephaestus.conrad.impl.config.RootConfigManager;
import dev.hephaestus.conrad.impl.config.server.WorldConfigManager;
import dev.hephaestus.conrad.impl.duck.ConfigManagerProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;

public class Conrad {
	public static <T extends Config> T getConfig(Class<T> configClass) {
		SaveType.Type saveType = configClass.getAnnotation(SaveType.class).value();
		EnvType envType = FabricLoader.getInstance().getEnvironmentType();

		if ((saveType == SaveType.Type.CLIENT && envType == EnvType.CLIENT) || (saveType == SaveType.Type.SERVER && envType == EnvType.SERVER)) {
			return RootConfigManager.INSTANCE.getConfig(configClass);
		} else if (saveType == SaveType.Type.SERVER && envType == EnvType.CLIENT) {
			return WorldConfigManager.getInstance().getConfig(configClass);
		} else if (saveType == SaveType.Type.CLIENT && envType == EnvType.SERVER) {
			throw new IllegalArgumentException(String.format("Cannot get client config \"%s\" on server without player instance.", configClass));
		} else {
			throw new IllegalStateException();
		}
	}

	public static <T extends Config> T getConfig(Class<T> configClass, ServerPlayerEntity playerEntity) {
		SaveType.Type saveType = configClass.getAnnotation(SaveType.class).value();

		if (saveType == SaveType.Type.CLIENT && playerEntity.getServer() != null) {
			return ConfigManagerProvider.of(playerEntity.getServer()).getPlayerConfigManager().getConfig(playerEntity, configClass);
		} else if (saveType == SaveType.Type.SERVER) {
			throw new IllegalArgumentException(String.format("Cannot get server config \"%s\" from player instance.", configClass));
		} else {
			throw new IllegalStateException();
		}
	}
}
