package dev.hephaestus.conrad.impl.config;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import com.google.common.collect.HashBiMap;

import dev.hephaestus.conrad.api.Config;

public abstract class ConfigManager implements Iterable<Config> {
	private static final LinkedHashSet<String> MODS = new LinkedHashSet<>();
	private static final LinkedList<String> KEY_LIST = new LinkedList<>();

	protected static final HashBiMap<Class<? extends Config>, String> KEYS = HashBiMap.create();

	public static void putKey(Class<? extends Config> configClass, String key) {
		KEYS.put(configClass, key);
		KEY_LIST.add(key);
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

	/**
	 * @return a list of mod id's with registered configs.
	 */
	@SuppressWarnings("unused")
	public static Collection<String> getModIds() {
		return MODS;
	}

	public abstract <T extends Config> T getConfig(Class<T> configClass);
	public abstract void putConfig(Config config);

	public final Collection<Config> getConfigs(String modid) {
		LinkedList<Config> configs = new LinkedList<>();

		for (String key : KEY_LIST) {
			if (key.split("\\.")[0].equals(modid)) {
				configs.add(this.getConfig(KEYS.inverse().get(key)));
			}
		}

		return configs;
	}

	protected abstract void save(Config config);
}
