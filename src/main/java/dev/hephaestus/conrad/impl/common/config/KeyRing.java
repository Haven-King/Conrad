package dev.hephaestus.conrad.impl.common.config;

import com.google.common.collect.HashBiMap;
import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.common.util.ConradUtil;
import dev.hephaestus.conrad.impl.common.util.ReflectionUtil;

import java.lang.reflect.Method;
import java.util.*;

@SuppressWarnings("unchecked")
public class KeyRing {
	private static final HashMap<ConfigKey, ConfigDefinition> CONFIG_DEFINITION_MAP = new HashMap<>();

	static ConfigDefinition put(ConfigKey key, ConfigDefinition definition) {
		CONFIG_DEFINITION_MAP.put(key, definition);
		return definition;
	}

	public static ConfigDefinition get(ConfigKey key) {
		return CONFIG_DEFINITION_MAP.get(key);
	}

	public Iterable<Map.Entry<ConfigKey, ConfigDefinition>> entries() {
		return CONFIG_DEFINITION_MAP.entrySet();
	}
}
