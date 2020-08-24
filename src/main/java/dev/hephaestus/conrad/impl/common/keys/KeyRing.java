package dev.hephaestus.conrad.impl.common.keys;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.common.util.ConradException;
import dev.hephaestus.conrad.impl.common.util.ConradUtil;
import dev.hephaestus.conrad.impl.common.util.ReflectionUtil;

import java.lang.reflect.Method;
import java.util.*;

@SuppressWarnings("unchecked")
public class KeyRing {
	private static final BiMap<Class<? extends Config>, ConfigKey> KEYS = HashBiMap.create();
	private static final Map<Config.Entry.MethodType, HashBiMap<ValueKey, Method>> METHODS = new HashMap<>();
	private static final Map<String, Collection<ConfigKey>> MOD_ID_TO_CONFIG_KEY_MAP = new TreeMap<>();
	private static final Map<ConfigKey, Collection<ValueKey>> CONFIG_KEY_VALUE_MAP = new TreeMap<>();

	@SuppressWarnings("unchecked")
	public static void put(ConfigKey configKey, Class<? extends Config> configClass) {
		KEYS.putIfAbsent(configClass, configKey);

		for (Method method: configClass.getDeclaredMethods()) {
			Config.Entry.MethodType type = ConradUtil.methodType(method);

			if (type != Config.Entry.MethodType.UTIL) {
				if (type == Config.Entry.MethodType.GETTER && Config.class.isAssignableFrom(method.getReturnType())) {
					put(ConfigKey.of(configKey, methodName(method)), (Class<? extends Config>) method.getReturnType());
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

		return KEYS.computeIfAbsent((Class<? extends Config>) configClass, c ->
				ConfigKey.of(ConradUtil.getModId(c), c.getAnnotation(Config.SaveName.class).value())
		);
	}

	public static Class<? extends Config> get(ConfigKey configKey) {
		ConradUtil.prove(KEYS.inverse().containsKey(configKey));
		ConradUtil.prove(Config.class.isAssignableFrom(KEYS.inverse().get(configKey)));

		return KEYS.inverse().get(configKey);
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

		return METHODS.computeIfAbsent(ConradUtil.methodType(method), methodType -> HashBiMap.create()).inverse().computeIfAbsent(method, m -> {
			ValueKey valueKey = ValueKey.of(m);
			return valueKey;
		});
	}

	public static Method get(ValueKey key) {
		return METHODS.get(Config.Entry.MethodType.GETTER).get(key);
	}

	public static boolean contains(Class<?> configClass) {
		return KEYS.containsKey(ReflectionUtil.getDeclared((Class<? extends Config>) configClass));
	}

	public static String methodName(Method method) {
		method = ReflectionUtil.getDeclared(method);

		if (method.isAnnotationPresent(Config.Entry.SaveName.class)) {
			return method.getAnnotation(Config.Entry.SaveName.class).value();
		} else if (method.getName().startsWith("set") || method.getName().startsWith("get")) {
			String name = method.getName();
			return Character.toLowerCase(name.charAt(3)) + name.substring(4);
		} else {
			throw new ConradException("Method '" + method.getName() + "' does not follow name scheme or provide SaveName annotation!");
		}
	}

	public static Collection<ConfigKey> getConfigKeys(String modId) {
		return MOD_ID_TO_CONFIG_KEY_MAP.get(modId);
	}

	public static Collection<ValueKey> getValueKeys(ConfigKey configKey) {
		return CONFIG_KEY_VALUE_MAP.get(configKey);
	}
}
