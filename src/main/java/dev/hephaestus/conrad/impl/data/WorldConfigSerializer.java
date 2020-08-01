package dev.hephaestus.conrad.impl.data;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.ConradUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.WorldSavePath;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class WorldConfigSerializer implements ConfigSerializer {
	public static final WorldConfigSerializer INSTANCE = new WorldConfigSerializer();

	@Override
	public <T extends Config> void serialize(T config) {
		try {
			Class<? extends Config> configClass = config.getClass();
			File configFile = getWorldSaveDirectory(configClass);

			ConfigSerializer.serialize(configFile, config);
		} catch (IllegalAccessException | InstantiationException | IOException e) {
			ConradUtils.LOG.warn("Failed to deserialize config \"{}\": {}", config.getClass().getName(), e.getMessage());
		}
	}

	@Override
	public <T extends Config> T deserialize(Class<T> configClass) {
		try {
			return ConfigSerializer.deserialize(getWorldSaveDirectory(configClass), configClass);
		} catch (IOException | IllegalAccessException | InstantiationException e) {
			ConradUtils.LOG.warn("Failed to deserialize config \"{}\": {}", configClass.getName(), e.getMessage());
		}

		try {
			return configClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			ConradUtils.LOG.warn("Failed to create new instance of config \"{}\": {}", configClass.getName(), e.getMessage());
			return null;
		}
	}

	@SuppressWarnings({"MethodCallSideOnly", "LocalVariableDeclarationSideOnly"})
	private File getWorldSaveDirectory(Class<? extends Config> configClass) throws IllegalAccessException, IOException, InstantiationException {
		MinecraftClient client = MinecraftClient.getInstance();
		Path configFolder = client.getServer().getSavePath(WorldSavePath.ROOT).normalize().resolve("config");
		return ConfigSerializer.getConfigFile(configFolder, configClass);
	}
}
