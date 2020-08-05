package dev.hephaestus.conrad.impl.config.server;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.config.ConfigManager;
import dev.hephaestus.conrad.impl.config.RootConfigManager;
import dev.hephaestus.conrad.impl.data.ConfigSerializer;
import dev.hephaestus.conrad.impl.data.LevelConfigSerializer;
import dev.hephaestus.conrad.impl.duck.ConfigManagerProvider;
import net.minecraft.client.MinecraftClient;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

public class LevelConfigManager extends ConfigManager {
	private final HashMap<String, Config> configs = new HashMap<>();

	public LevelConfigManager() {
		for (Map.Entry<Class<? extends Config>, String> entry : ConfigManager.KEYS.entrySet()) {
			if (entry.getKey().getAnnotation(Config.SaveType.class).value() == Config.SaveType.Type.LEVEL) {
				this.configs.put(entry.getValue(), LevelConfigSerializer.INSTANCE.deserialize(entry.getKey()));
			}
		}
	}

	@SuppressWarnings("MethodCallSideOnly")
	public static ConfigManager getInstance() {
		if (MinecraftClient.getInstance().isIntegratedServerRunning() && MinecraftClient.getInstance().getServer() != null) {
			return ConfigManagerProvider.of(MinecraftClient.getInstance().getServer()).getConfigManager();
		} else {
			return RootConfigManager.INSTANCE;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Config> T getConfig(Class<T> configClass) {
		return (T) this.configs.get(ConfigManager.KEYS.get(configClass));
	}

	@Override
	public void putConfig(Config config) {
		this.configs.put(ConfigManager.KEYS.get(config.getClass()), config);
		this.save(config);
	}

	@Override
	protected void save(Config config) {
		ConfigSerializer.getInstance(config.getClass().getAnnotation(Config.SaveType.class).value()).serialize(config);
	}

	@Override
	public Iterator<Config> iterator() {
		return this.configs.values().iterator();
	}

	@Override
	public void forEach(Consumer<? super Config> action) {
		this.configs.values().forEach(action);
	}
}
