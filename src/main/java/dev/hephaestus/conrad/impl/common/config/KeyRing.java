package dev.hephaestus.conrad.impl.common.config;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.hephaestus.conrad.impl.common.util.ReflectionUtil;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class KeyRing {
	private static final Multimap<String, ConfigKey> MOD_CONFIGS_MAP = HashMultimap.create();
	private static final Map<ConfigKey, ConfigDefinition> CONFIG_DEFINITION_MAP = new HashMap<>();
	private static final Map<Method, ValueKey> VALUE_KEY_MAP = new HashMap<>();
	private static final Map<ConfigKey, ConfigKey> ROOT_CONFIG_MAP = new HashMap<>();

	static ConfigDefinition put(ConfigKey key, ConfigDefinition definition) {
		if (definition.isRoot()) {
			MOD_CONFIGS_MAP.put(key.namespace, key);
		}

		CONFIG_DEFINITION_MAP.put(key, definition);
		return definition;
	}

	static void put(ConfigKey parent, ConfigKey child) {
		ROOT_CONFIG_MAP.put(child, parent);

		for (Map.Entry<ConfigKey, ConfigKey> entry : ROOT_CONFIG_MAP.entrySet()) {
			if (entry.getValue() == child) {
				ROOT_CONFIG_MAP.put(entry.getKey(), parent);
			}
		}
	}

	static ValueKey put(Method method, ValueKey key) {
		VALUE_KEY_MAP.putIfAbsent(method, key);
		return key;
	}

	public static ValueKey get(Method method) {
		return VALUE_KEY_MAP.get(ReflectionUtil.getDeclared(method));
	}

	public static ConfigKey getRoot(ConfigKey key) {
		return ROOT_CONFIG_MAP.getOrDefault(key, key);
	}

	public static ConfigDefinition getRootDefinition(ConfigKey key) {
		return CONFIG_DEFINITION_MAP.get(getRoot(key));
	}

	public static ConfigDefinition get(ConfigKey key) {
		return CONFIG_DEFINITION_MAP.get(key);
	}

	public static Iterable<Map.Entry<ConfigKey, ConfigDefinition>> entries() {
		return CONFIG_DEFINITION_MAP.entrySet();
	}

	public static Iterable<ConfigKey> entries(String modId) {
		return MOD_CONFIGS_MAP.get(modId);
	}
}
