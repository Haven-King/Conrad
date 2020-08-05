package dev.hephaestus.conrad.impl.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.config.ConfigManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public interface ConfigSerializer {
	<T extends Config> void serialize(T config);
	<T extends Config> T deserialize(Class<T> configClass);

	static <T extends Config> void serialize(File configFile, T config) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(configFile));
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));

		mapper.writeValue(writer, config);
	}

	static <T extends Config> T deserialize(File configFile, Class<T> configClass) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(configFile));
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		T config = mapper.readValue(configFile, configClass);
		reader.close();

		return config;
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
		Path configFile = configFolder.resolve(key.substring(key.lastIndexOf(".") + 1) + ".yaml");

		return configFile.toFile();
	}

	static ConfigSerializer getInstance(Config.SaveType.Type saveType) {
		EnvType envType = FabricLoader.getInstance().getEnvironmentType();

		if ((saveType == Config.SaveType.Type.CLIENT && envType == EnvType.CLIENT) ||
			(saveType == Config.SaveType.Type.LEVEL && envType == EnvType.SERVER)) {
			return RootConfigSerializer.INSTANCE;
		} else if (saveType == Config.SaveType.Type.LEVEL && envType == EnvType.CLIENT) {
			return ClientConfigDeterminer.getInstance();
		} else {
			return VoidConfigSerializer.INSTANCE;
		}
	}

	static void save(Config config) {
		getInstance(config.getClass().getAnnotation(Config.SaveType.class).value()).serialize(config);
		NetworkedConfigSerializer.INSTANCE.serialize(config);
	}
}
