package dev.hephaestus.conrad.impl.config;

import dev.hephaestus.conrad.api.Config;

import java.util.*;

public abstract class ConfigManager implements Iterable<Config> {
	private static final LinkedHashSet<String> MODS = new LinkedHashSet<>();

	protected static final HashMap<Class<? extends Config>, String> KEYS = new HashMap<>();

	public static void putKey(Class<? extends Config> configClass, String key) {
		KEYS.put(configClass, key);
	}

	public static <T extends Config> String getKey(Class<T> configClass) {
		return KEYS.get(configClass);
	}

	public static int keyCount() {
		return KEYS.size();
	}

	public static boolean isRegistered(Class<? extends Config> configClass) {
		return KEYS.containsKey(configClass);
	}

	public static void putMod(String modid) {
		MODS.add(modid);
	}

	public static Collection<String> getModIds() {
		return MODS;
	}

	public abstract <T extends Config> T getConfig(Class<T> configClass);
	public abstract void putConfig(Config config);

	public Collection<Config> getConfigs(String modid) {
		LinkedList<Config> configs = new LinkedList<>();

		for (Config config : this) {
			if (getKey(config.getClass()).split("\\.")[0].equals(modid)) {
				configs.add(config);
			}
		}

		return configs;
	}

	public Config getFirst(String modid) {
		for (Config config : this) {
			if (getKey(config.getClass()).split("\\.")[0].equals(modid)) {
				return config;
			}
		}

		return new Config() {};
	}

	protected abstract void save(Config config);
}
