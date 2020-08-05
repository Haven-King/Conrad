package dev.hephaestus.conrad.impl.config;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.data.ConfigSerializer;
import dev.hephaestus.conrad.impl.data.RootConfigSerializer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

public class RootConfigManager extends ConfigManager {
	public static final RootConfigManager INSTANCE = new RootConfigManager();

	public static void initialize() {
	}

	private final HashMap<String, Config> configs = new HashMap<>();

	public RootConfigManager() {
		for (Map.Entry<Class<? extends Config>, String> entry : ConfigManager.KEYS.entrySet()) {
			this.configs.put(entry.getValue(), RootConfigSerializer.INSTANCE.deserialize(entry.getKey()));
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
