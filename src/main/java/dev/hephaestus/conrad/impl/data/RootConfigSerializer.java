package dev.hephaestus.conrad.impl.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import net.fabricmc.loader.api.FabricLoader;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.ConradUtils;
import dev.hephaestus.conrad.impl.config.ConfigManager;

public class RootConfigSerializer implements ConfigSerializer {
	public static final RootConfigSerializer INSTANCE = new RootConfigSerializer();

	@Override
	public <T extends Config> void serialize(T config) {
		try {
			Class<? extends Config> configClass = config.getClass();
			Path configFolder = getRootSaveDirectory(configClass);
			File configFile = ConfigSerializer.getConfigFile(configFolder, configClass);

			ConfigSerializer.serialize(configFile, config);
		} catch (IllegalAccessException | InstantiationException | IOException e) {
			ConradUtils.LOG.warn("Failed to serialize config \"{}\": {}", config.getClass().getName(), e.getMessage());
		}
	}

	@Override
	public <T extends Config> T deserialize(Class<T> configClass) {
		try {
			Path configFolder = getRootSaveDirectory(configClass);
			File configFile = ConfigSerializer.getConfigFile(configFolder, configClass);

			return ConfigSerializer.deserialize(configFile, configClass);
		} catch (IllegalAccessException | InstantiationException | IOException e) {
			ConradUtils.LOG.warn("Failed to deserialize config \"{}\": {}", configClass.getName(), e.getMessage());
		}

		try {
			return configClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			ConradUtils.LOG.warn("Failed to create new instance of config \"{}\": {}", configClass.getName(), e.getMessage());
			return null;
		}
	}

	static <T extends Config> Path getRootSaveDirectory(Class<T> configClass) throws IOException {
		String key = ConfigManager.getKey(configClass);
		return Files.createDirectories(FabricLoader.getInstance().getConfigDir().normalize().resolve(key.substring(0, key.lastIndexOf("."))));
	}
}
