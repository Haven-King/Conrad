package dev.hephaestus.conrad.impl.common.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.api.Conrad;
import dev.hephaestus.conrad.impl.common.config.ValueContainer;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class ReflectionUtil {
	private static final HashMap<Class<? extends Config>, Class<? extends Config>> CLASS_TO_DECLARED_CONFIG_CLASS_MAP = new HashMap<>();
	private static final HashMap<Method, Method> METHOD_TO_DECLARED_METHOD_MAP = new HashMap<>();
	private static final ConcurrentHashMap<Class<? extends Config>, Class<? extends Config>> CLASS_TO_ROOT_CONFIG_CLASS_MAP = new ConcurrentHashMap<>();
	private static final BiMap<Class<?>, Class<?>> PRIMITIVE_TO_OBJECT_CLASS_MAP;

	static {
		ImmutableBiMap.Builder<Class<?>, Class<?>> builder = ImmutableBiMap.builder();

		builder.put(Boolean.TYPE, Boolean.class);
		builder.put(Character.TYPE, Character.class);
		builder.put(Byte.TYPE, Byte.class).put(Short.TYPE, Short.class);
		builder.put(Integer.TYPE, Integer.class);
		builder.put(Long.TYPE, Long.class);
		builder.put(Float.TYPE, Float.class);
		builder.put(Double.TYPE, Double.class);

		PRIMITIVE_TO_OBJECT_CLASS_MAP = builder.build();
	}

	public static Class<?> getClass(Class<?> potentialPrimitive) {
		return PRIMITIVE_TO_OBJECT_CLASS_MAP.getOrDefault(potentialPrimitive, potentialPrimitive);
	}

	public static Class<?>[] getClasses(Class<?> clazz) {
		return PRIMITIVE_TO_OBJECT_CLASS_MAP.containsKey(clazz)
				? new Class<?>[] {clazz, PRIMITIVE_TO_OBJECT_CLASS_MAP.get(clazz)}
				: PRIMITIVE_TO_OBJECT_CLASS_MAP.inverse().containsKey(clazz)
				? new Class<?>[] {clazz, PRIMITIVE_TO_OBJECT_CLASS_MAP.inverse().get(clazz)}
				: new Class<?>[] {clazz};
	}

	public static Class<? extends Config> getDeclared(Class<?> configClass) {
		return CLASS_TO_DECLARED_CONFIG_CLASS_MAP.computeIfAbsent((Class<? extends Config>) configClass, c -> {
			if (ReflectionUtil.isRootConfigClass(c)) {
				return c;
			}

			for (Class<?> i : c.getInterfaces()) {
				if (Config.class.isAssignableFrom(i) && !Proxy.isProxyClass(i) && i != Config.class) {
					return (Class<? extends Config>) i;
				}
			}

			return c;
		});
	}

	public static Method getDeclared(Method method) {
		return METHOD_TO_DECLARED_METHOD_MAP.computeIfAbsent(method, m -> {
			loop: for (Class<?> clazz : m.getDeclaringClass().getInterfaces()) {
				Method[] methods = clazz.getMethods();

				for (Method declared : methods) {
					if (declared.getName().equals(m.getName())
							&& declared.getReturnType() == m.getReturnType()
							&& declared.getParameterCount() == m.getParameterCount()
					) {
						if (m.getParameterCount() == 0) {
							m = declared;
							break loop;
						}

						for (int i = 0; i < m.getParameterCount(); ++i) {
							if (declared.getParameterTypes()[i] == m.getParameterTypes()[i]) {
								m = declared;
								break loop;
							}
						}
					}
				}
			}

			return m;
		});
	}

	public static Class<? extends Config> getRoot(Class<? extends Config> configClass) {
		if (!CLASS_TO_ROOT_CONFIG_CLASS_MAP.containsKey(configClass)) {
			Class<?> root = getDeclared(configClass);
			Class<?> declaringClass = root.getDeclaringClass();

			if (declaringClass != null && ReflectionUtil.isRootConfigClass(declaringClass)) {
				root = declaringClass;
			}

			if (declaringClass != null && Config.class.isAssignableFrom(declaringClass)) {
				root = getRoot((Class<? extends Config>) declaringClass);
			}

			CLASS_TO_ROOT_CONFIG_CLASS_MAP.put(configClass, (Class<? extends Config>) root);
		}

		return CLASS_TO_ROOT_CONFIG_CLASS_MAP.get(configClass);
	}

	public static Class<? extends Config> getRoot(Method method) {
		if (Config.class.isAssignableFrom(method.getDeclaringClass())) {
			return ReflectionUtil.getRoot((Class<? extends Config>) method.getDeclaringClass());
		} else {
			return null;
		}
	}

	public static boolean isRootConfigClass(Class<?> clazz) {
		return Config.class.isAssignableFrom(clazz) && clazz.getDeclaringClass() == null && !Proxy.isProxyClass(clazz) && clazz != Config.class;
	}

	public static Object invokeDefault(Method method) throws Throwable {
		return invokeDefault(
				Conrad.getConfig((Class<? extends Config>) method.getDeclaringClass()),
				method,
				null
		);
	}

	public static Object invokeDefault(Object object, Method method, Object[] args) throws Throwable {
		if (System.getProperty("java.version").startsWith("1.8.")) {
			Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class);
			constructor.setAccessible(true);
			Class<?> clazz = method.getDeclaringClass();

			return constructor.newInstance(clazz)
					.in(clazz)
					.unreflectSpecial(method, clazz)
					.bindTo(object)
					.invokeWithArguments(args);
		} else {
			Class<?> declaringClass = ReflectionUtil.getDeclared(method.getDeclaringClass());

			return MethodHandles.lookup().findSpecial(
					declaringClass,
					method.getName(),
					MethodType.methodType(method.getReturnType(), new Class[0]),
					declaringClass
			).bindTo(object).invokeWithArguments();
		}
	}
}
