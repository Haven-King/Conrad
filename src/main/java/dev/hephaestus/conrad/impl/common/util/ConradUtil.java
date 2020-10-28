package dev.hephaestus.conrad.impl.common.util;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.api.networking.NetworkSerializerRegistry;
import dev.hephaestus.conrad.impl.common.config.*;
import dev.hephaestus.conrad.impl.common.networking.packets.all.ConfigValuePacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

public class ConradUtil {
	public static final String MOD_ID = "conrad";
	public static final Logger LOG = LogManager.getLogger("Conrad");

	public static Identifier id(String... path) {
		return new Identifier(MOD_ID, String.join(".", path));
	}

	private static final HashMap<Class<? extends Config>, String> CONFIG_CLASS_TO_MOD_ID_MAP = new HashMap<>();
	private static final HashMap<String, Collection<Class<? extends Config>>> MOD_ID_TO_CONFIG_CLASS_MAP = new HashMap<>();

	public static Config.Value.MethodType methodType(Method method) {
		Method declaredMethod = ReflectionUtil.getDeclared(method);

		Class<?> clazz = ReflectionUtil.getDeclared(method.getDeclaringClass());

		boolean contains = false;
		for (Class<?> i : clazz.getInterfaces()) {
			if (i.equals(Config.class)) {
				contains = true;
				break;
			}
		}

		if (!contains) return Config.Value.MethodType.UTIL;

		contains = false;
		Method[] methods = clazz.getMethods();
		for (Method m : clazz.getMethods()) {
			if (m.equals(declaredMethod)) {
				contains = true;
				break;
			}
		}

		if (!contains) return Config.Value.MethodType.UTIL;

		if (declaredMethod.isAnnotationPresent(Config.Value.Options.class)) {
			return declaredMethod.getAnnotation(Config.Value.Options.class).type();
		} else if (method.getParameterCount() == 0 && method.getReturnType() != Void.TYPE) {
			return Config.Value.MethodType.GETTER;
//		} else if (method.getParameterCount() == 1 && method.getReturnType() == Void.TYPE) {
//			return Config.Value.MethodType.SETTER;
		} else {
			return Config.Value.MethodType.UTIL;
		}
	}

	public static String getModId(Class<? extends Config> configClass) {
		return CONFIG_CLASS_TO_MOD_ID_MAP.get(configClass);
	}

	public static void put(Class<? extends Config> configClass, String modId) {
		CONFIG_CLASS_TO_MOD_ID_MAP.put(configClass, modId);
		MOD_ID_TO_CONFIG_CLASS_MAP.computeIfAbsent(modId, key -> new ArrayList<>()).add(configClass);
	}

	public static void prove(boolean bool) {
		if (!bool) {
			throw new AssertionError();
		}
	}

	public static <T> T either(T t1, T t2) {
		return t1 == null ? t2 : t1;
	}

	public static void sendValues(ServerPlayerEntity player) {
		if (player != null && player.hasPermissionLevel(4)) {
			for (Map.Entry<ValueKey, Object> entry : ValueContainer.ROOT) {
				ConfigDefinition configDefinition = KeyRing.get(entry.getKey().getConfigKey());
				ValueDefinition valueDefinition = configDefinition.getValueDefinition(entry.getKey());

				if (NetworkSerializerRegistry.contains(valueDefinition.getType())
						&& configDefinition.getSaveType() == Config.SaveType.LEVEL) {
					new ConfigValuePacket(ConfigValuePacket.INFO, entry.getKey(), entry.getValue()).send(player);
				}
			}
		}
	}
}
