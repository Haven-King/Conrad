package dev.hephaestus.conrad.api;

import dev.hephaestus.conrad.impl.ConradUtils;
import dev.hephaestus.conrad.impl.duck.ConfigManagerProvider;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

public class Conrad {
	/**
	 * Gets a config object of the given configClass.
	 * If a config file does not exist for the specified class, one will be created with the default values.
	 * @param configClass class of the config to get an instance of
	 * @return an instance of configClass
	 */
	public static <T extends Config> T getConfig(Class<T> configClass) {
		return ConradUtils.getConfigManager(configClass.getAnnotation(Config.SaveType.class).value()).getConfig(configClass);
	}

	/**
	 * Gets a player's client-side config values if they exist.
	 * @param configClass class of the config to get an instance of
	 * @param playerEntity the player whose config values should be retrieved
	 * @return an instance of configClass if it exists. Players
	 */
	public static <T extends Config> Optional<T> getConfig(Class<T> configClass, ServerPlayerEntity playerEntity) {
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
