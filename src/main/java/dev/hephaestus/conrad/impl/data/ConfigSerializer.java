package dev.hephaestus.conrad.impl.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.ConradUtils;
import dev.hephaestus.conrad.impl.config.ConfigManager;

public interface ConfigSerializer {
	<T extends Config> void serialize(T config);
	<T extends Config> T deserialize(Class<T> configClass);

	static <T extends Config> void serialize(File configFile, T config) throws IOException {
		config.getSerializer().save(configFile, config);
	}

	static <T extends Config> T deserialize(File configFile, Class<T> configClass) throws IOException {
		return ConradUtils.getDefault(configClass).getSerializer().load(configFile, configClass);
	}

	static File getConfigFile(Path configFolder, Class<? extends Config> configClass) throws IOException, IllegalAccessException, InstantiationException {
		File configFile = getConfigFileLocation(configFolder, configClass);

		if (!configFile.exists()) {
			Files.createDirectories(configFolder);

			if (configFile.createNewFile()) {
				serialize(configFile, configClass.newInstance());
			}
		}

		return configFile;
	}

	static File getConfigFileLocation(Path configFolder, Class<? extends Config> configClass) {
		String key = ConfigManager.getKey(configClass);
		Path configFile = configFolder.resolve(key.substring(key.lastIndexOf(".") + 1) + "." + ConradUtils.getDefault(configClass).getSerializer().fileType());

		return configFile.toFile();
	}

	static ConfigSerializer getInstance(Config.SaveType.Type saveType) {
		EnvType envType = FabricLoader.getInstance().getEnvironmentType();

		if ((saveType == Config.SaveType.Type.CLIENT && envType == EnvType.CLIENT)
				|| (saveType == Config.SaveType.Type.LEVEL && envType == EnvType.SERVER)) {
			return RootConfigSerializer.INSTANCE;
		} else if (saveType == Config.SaveType.Type.LEVEL && envType == EnvType.CLIENT) {
			return ClientConfigDeterminer.getInstance();
		} else {
			// If SaveType is CLIENT and EnvType is SERVER, we don't save the client configs
			return VoidConfigSerializer.INSTANCE;
		}
	}
}
