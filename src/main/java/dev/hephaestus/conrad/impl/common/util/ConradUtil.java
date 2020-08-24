package dev.hephaestus.conrad.impl.common.util;

import dev.hephaestus.conrad.api.Config;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.*;

public class ConradUtil {
	public static final String MOD_ID = "conrad";
	public static final Logger LOG = LogManager.getLogger("Conrad");

	public static Identifier id(String... path) {
		return new Identifier(MOD_ID, String.join(".", path));
	}

	private static final HashMap<Class<? extends Config>, String> CONFIG_CLASS_TO_MOD_ID_MAP = new HashMap<>();
	private static final HashMap<String, Collection<Class<? extends Config>>> MOD_ID_TO_CONFIG_CLASS_MAP = new HashMap<>();

	public static Config.Entry.MethodType methodType(Method method) {
		Method declaredMethod = ReflectionUtil.getDeclared(method);

		if (declaredMethod.isAnnotationPresent(Config.Entry.Type.class)) {
			return declaredMethod.getAnnotation(Config.Entry.Type.class).value();
		} else if (method.getName().startsWith("get") && method.getParameterCount() == 0 && method.getReturnType() != Void.TYPE) {
			return Config.Entry.MethodType.GETTER;
		} else if (method.getName().startsWith("set") && method.getParameterCount() == 1 && method.getReturnType() == Void.TYPE) {
			return Config.Entry.MethodType.SETTER;
		} else {
			return Config.Entry.MethodType.UTIL;
		}
	}

	public static void put(Class<? extends Config> configClass, String modId) {
		CONFIG_CLASS_TO_MOD_ID_MAP.put(configClass, modId);
		MOD_ID_TO_CONFIG_CLASS_MAP.computeIfAbsent(modId, key -> new ArrayList<>()).add(configClass);
	}

	public static String getModId(Class<? extends Config> configClass) {
		return CONFIG_CLASS_TO_MOD_ID_MAP.get(ReflectionUtil.getRoot(configClass));
	}

	public static Collection<Class<? extends Config>> getConfigClasses(String modId) {
		return MOD_ID_TO_CONFIG_CLASS_MAP.get(modId);
	}

	public static void prove(boolean bool) {
		if (!bool) {
			throw new AssertionError();
		}
	}
}
