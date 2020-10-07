package dev.hephaestus.conrad.impl.common.keys;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.common.util.ConradUtil;
import dev.hephaestus.conrad.impl.common.util.ReflectionUtil;

import java.lang.reflect.Method;
import java.util.*;

@SuppressWarnings("unchecked")
public class KeyRing {
	private static final BiMap<Class<? extends Config>, ConfigKey> CONFIG_KEY_MAP = HashBiMap.create();
	private static final Map<Config.Value.MethodType, HashBiMap<ValueKey, Method>> METHODS = new HashMap<>();
	private static final Map<String, Collection<ConfigKey>> MOD_ID_TO_CONFIG_KEY_MAP = new TreeMap<>();
	private static final Map<ConfigKey, Collection<ValueKey>> CONFIG_KEY_VALUE_MAP = new TreeMap<>();

	@SuppressWarnings("unchecked")
	public static void put(ConfigKey configKey, Class<? extends Config> configClass) {
		CONFIG_KEY_MAP.putIfAbsent(configClass, configKey);

		for (Method method: configClass.getDeclaredMethods()) {
			Config.Value.MethodType type = ConradUtil.methodType(method);

			if (type != Config.Value.MethodType.UTIL) {
				if (type == Config.Value.MethodType.GETTER && Config.class.isAssignableFrom(method.getReturnType())) {
					put(ConfigKey.of(configKey, Config.Sync.of(configKey.isSynced()), methodName(method)), (Class<? extends Config>) method.getReturnType());
				}

				ValueKey valueKey = ValueKey.of(method);
				METHODS.computeIfAbsent(type, t -> HashBiMap.create()).put(valueKey, method);
				MOD_ID_TO_CONFIG_KEY_MAP.computeIfAbsent(ConradUtil.getModId(ReflectionUtil.getRoot(method)), k -> new TreeSet<>()).add(configKey);
				CONFIG_KEY_VALUE_MAP.computeIfAbsent(configKey, key -> new TreeSet<>()).add(valueKey);
			}
		}
	}

	/**
	 * Returns the key of the given class if it exists
	 * @param configClass class of the config object to query, must extend {@link Config}
	 * @return the key associated with the given config class
	 */
	public static ConfigKey get(Class<?> configClass) {
		ConradUtil.prove(Config.class.isAssignableFrom(configClass));
		configClass = ReflectionUtil.getDeclared(configClass);

		return CONFIG_KEY_MAP.computeIfAbsent((Class<? extends Config>) configClass, c -> {
			Config.Options options = c.getAnnotation(Config.Options.class);
			return ConfigKey.of(ConradUtil.getModId(c), options.synced(), options.name());
		});
	}

	public static Class<? extends Config> get(ConfigKey configKey) {
		ConradUtil.prove(CONFIG_KEY_MAP.inverse().containsKey(configKey));
		ConradUtil.prove(Config.class.isAssignableFrom(CONFIG_KEY_MAP.inverse().get(configKey)));

		return CONFIG_KEY_MAP.inverse().get(configKey);
	}

	/**
	 * Returns the key of the given method if it exists
	 * @param method can be either a getter or setter method of a class that extends {@link Config}
	 * @return the key associated with the given method
	 */
	public static ValueKey get(Method method) {
		method = ReflectionUtil.getDeclared(method);

		ConradUtil.prove(Config.class.isAssignableFrom(method.getDeclaringClass()));
		ConradUtil.prove(KeyRing.contains(ReflectionUtil.getDeclared(method).getDeclaringClass()));

		return METHODS.computeIfAbsent(ConradUtil.methodType(method), methodType -> HashBiMap.create()).inverse().computeIfAbsent(method, ValueKey::of);
	}

	public static Method get(ValueKey key) {
		return METHODS.get(Config.Value.MethodType.GETTER).get(key);
	}

	public static boolean contains(Class<?> configClass) {
		return CONFIG_KEY_MAP.containsKey(ReflectionUtil.getDeclared(configClass));
	}

	public static String methodName(Method method) {
		method = ReflectionUtil.getDeclared(method);

		if (method.isAnnotationPresent(Config.Value.Options.class)) {
			String name = method.getAnnotation(Config.Value.Options.class).name();
			if (!name.equals("")) return name;
		}

		return method.getName();
	}

	public static Collection<Class<? extends Config>> getConfigClasses() {
		return CONFIG_KEY_MAP.keySet();
	}

	public static Collection<ConfigKey> getConfigKeys(String modId) {
		return MOD_ID_TO_CONFIG_KEY_MAP.get(modId);
	}

	public static Collection<ValueKey> getValueKeys(ConfigKey configKey) {
		return CONFIG_KEY_VALUE_MAP.get(configKey);
	}
}
