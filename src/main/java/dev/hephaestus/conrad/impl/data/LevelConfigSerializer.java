package dev.hephaestus.conrad.impl.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.WorldSavePath;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.ConradUtils;

public class LevelConfigSerializer implements ConfigSerializer {
	public static final LevelConfigSerializer INSTANCE = new LevelConfigSerializer();

	@Override
	public <T extends Config> void serialize(T config) {
		try {
			Class<? extends Config> configClass = config.getClass();
			File configFile = getSaveFile(configClass);

			ConfigSerializer.serialize(configFile, config);
		} catch (IllegalAccessException | InstantiationException | IOException e) {
			ConradUtils.LOG.warn("Failed to serialize config \"{}\": {}", config.getClass().getName(), e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public <T extends Config> T deserialize(Class<T> configClass) {
		try {
			File saveFile = getSaveFileLocation(configClass);

			boolean bl = saveFile.exists();

			if (bl) {
				return ConfigSerializer.deserialize(saveFile, configClass);
			} else {
				return RootConfigSerializer.INSTANCE.deserialize(configClass);
			}
		} catch (IOException e) {
			ConradUtils.LOG.warn("Failed to deserialize config \"{}\": {}", configClass.getName(), e.getMessage());
			e.printStackTrace();
		}

		try {
			return configClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			ConradUtils.LOG.warn("Failed to create new instance of config \"{}\": {}", configClass.getName(), e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	@SuppressWarnings({"MethodCallSideOnly", "LocalVariableDeclarationSideOnly", "ConstantConditions"})
	private File getSaveFile(Class<? extends Config> configClass) throws IllegalAccessException, IOException, InstantiationException {
		MinecraftClient client = MinecraftClient.getInstance();
		Path configFolder = client.getServer().getSavePath(WorldSavePath.ROOT).normalize().resolve("config");
		return ConfigSerializer.getConfigFile(configFolder, configClass);
	}

	@SuppressWarnings({"MethodCallSideOnly", "LocalVariableDeclarationSideOnly", "ConstantConditions"})
	private File getSaveFileLocation(Class<? extends Config> configClass) {
		MinecraftClient client = MinecraftClient.getInstance();
		Path configFolder = client.getServer().getSavePath(WorldSavePath.ROOT).normalize().resolve("config");
		return ConfigSerializer.getConfigFileLocation(configFolder, configClass);
	}
}
