package dev.monarkhes.conrad.impl;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import dev.monarkhes.conrad.api.Config;
import dev.monarkhes.conrad.api.ConfigValue;
import dev.monarkhes.conrad.impl.util.ConradException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class KeyRing {
	private static final Multimap<String, ConfigKey> KEYS_BY_MOD_ID = HashMultimap.create();
	private static final Multimap<ConfigKey, ConfigValue<?>> VALUES_BY_CONFIG = LinkedHashMultimap.create();
	private static final Map<ConfigKey, Config> CONFIGS = new HashMap<>();
	private static final Map<ConfigKey, ConfigValue<?>> VALUES_BY_KEY = new HashMap<>();

	public static void register(ConfigValue<?> value) {
		ConfigKey key = value.getKey();

		if (key == null) {
			throw new ConradException("Attempted to register null ConfigKey!");
		}

		ConfigKey root = new ConfigKey(key.getNamespace(), key.getPath()[0]);

		if (!KEYS_BY_MOD_ID.get(key.getNamespace()).contains(root)) {
			KEYS_BY_MOD_ID.put(key.getNamespace(), root);
		}

		VALUES_BY_CONFIG.put(root, value);
		VALUES_BY_KEY.put(key, value);
	}

	public static void register(ConfigKey configKey, Config config) {
		CONFIGS.put(configKey, config);
	}

	public static Collection<String> getMods() {
		return KEYS_BY_MOD_ID.keySet();
	}

	public static Collection<ConfigKey> getKeys(String modId) {
		return KEYS_BY_MOD_ID.get(modId);
	}

	public static Collection<ConfigValue<?>> getValues(ConfigKey configKey) {
		return VALUES_BY_CONFIG.get(configKey);
	}

	public static Config getRootConfig(ConfigKey configKey) {
		return CONFIGS.get(configKey.getRoot());
	}

	public static ConfigValue<?> get(ConfigKey key) {
		return VALUES_BY_KEY.get(key);
	}

	public static boolean isRegisteredAsRootConfig(ConfigKey configKey) {
		return VALUES_BY_CONFIG.containsKey(configKey);
	}
}
