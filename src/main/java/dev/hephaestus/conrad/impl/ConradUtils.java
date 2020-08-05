package dev.hephaestus.conrad.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.config.ConfigManager;
import dev.hephaestus.conrad.impl.config.RootConfigManager;
import dev.hephaestus.conrad.impl.config.server.LevelConfigManager;
import dev.hephaestus.conrad.impl.duck.ConfigManagerProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.HashMap;

public class ConradUtils {
	public static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
	public static final String MOD_ID = "conrad";
	public static final Logger LOG = LogManager.getLogger("Conrad");
	public static Identifier id(String... path) {
		return new Identifier(MOD_ID, String.join(".", path));
	}

	private static final HashMap<Class<? extends Config>, Config> DEFAULT_CONFIGS = new HashMap<>();

	public static Object getDefault(Class<? extends Config> configClass, Field field) {
		if (!DEFAULT_CONFIGS.containsKey(configClass)) {
			try {
				DEFAULT_CONFIGS.putIfAbsent(configClass, configClass.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		if (DEFAULT_CONFIGS.containsKey(configClass)) {
			try {
				return field.get(DEFAULT_CONFIGS.get(configClass));
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	@SuppressWarnings("MethodCallSideOnly")
	public static ConfigManager getConfigManager(Config.SaveType.Type saveType) {
		EnvType envType = FabricLoader.getInstance().getEnvironmentType();

		if ((saveType == Config.SaveType.Type.CLIENT && envType == EnvType.CLIENT) || (saveType == Config.SaveType.Type.LEVEL && envType == EnvType.SERVER)) {
			return RootConfigManager.INSTANCE;
		} else if (saveType == Config.SaveType.Type.LEVEL && envType == EnvType.CLIENT) {
			if (MinecraftClient.getInstance().isIntegratedServerRunning()) {
				return LevelConfigManager.getInstance();
			} else if (MinecraftClient.getInstance().getCurrentServerEntry() != null) {
				return ConfigManagerProvider.of(MinecraftClient.getInstance().getCurrentServerEntry()).getConfigManager();
			} else {
				return RootConfigManager.INSTANCE;
			}
		} else if (saveType == Config.SaveType.Type.CLIENT && envType == EnvType.SERVER) {
			throw new IllegalArgumentException("Cannot get client config on server without player instance.");
		}

		throw new IllegalStateException();
	}
}
