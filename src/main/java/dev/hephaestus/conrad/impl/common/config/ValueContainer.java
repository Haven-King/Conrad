package dev.hephaestus.conrad.impl.common.config;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.api.Conrad;
import dev.hephaestus.conrad.api.serialization.ConfigSerializer;
import dev.hephaestus.conrad.impl.common.keys.KeyRing;
import dev.hephaestus.conrad.impl.common.keys.ValueKey;
import dev.hephaestus.conrad.impl.common.util.ConradException;
import dev.hephaestus.conrad.impl.common.util.ConradUtil;
import dev.hephaestus.conrad.impl.common.util.ReflectionUtil;
import dev.hephaestus.conrad.impl.common.util.SerializationUtil;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ValueContainer implements Iterable<Map.Entry<ValueKey, Object>>, InvocationHandler {
	public static final ValueContainer ROOT = new ValueContainer();

	private static final HashMap<ValueKey, Object> DEFAULT_VALUES = new HashMap<>();

	protected final HashMap<ValueKey, Object> values = new HashMap<>();

	private final Path saveDirectory;

	public ValueContainer(Path saveDirectory) {
		this.saveDirectory = saveDirectory;

		if (ValueContainer.ROOT != null) {
			for (Map.Entry<ValueKey, Object> entry : ValueContainer.ROOT) {
				try {
					this.put(entry.getKey(), entry.getValue());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public ValueContainer() {
		this(FabricLoader.getInstance().getConfigDir());
	}

	public boolean containsDefault(ValueKey key) {
		return DEFAULT_VALUES.containsKey(key);
	}

	public void put(ValueKey key, Object value) throws IOException {
		DEFAULT_VALUES.putIfAbsent(key, value);
		this.values.put(key, value);
		this.save(key, value);
	}

	protected void save(ValueKey key, Object value) throws IOException {
		Class<? extends Config> configClass = KeyRing.get(key.getConfig().root());
		Config config = Conrad.getConfig(configClass);
		ConfigSerializer<?, ?> serializer = config.serializer();
		Path path = SerializationUtil.saveFolder(this.saveDirectory, configClass);

		Files.createDirectories(path);

		serializer.writeValue(
				serializer.serialize(config),
				new FileOutputStream(
						path.resolve(SerializationUtil.saveName(configClass) + "." + serializer.fileExtension()).toFile()
				)
		);
	}

	@SuppressWarnings("unchecked")
	public final <T> T get(ValueKey key) {
		return (T) this.values.get(key);
	}

	@Override
	public Iterator<Map.Entry<ValueKey, Object>> iterator() {
		return this.values.entrySet().iterator();
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Config.Entry.MethodType methodType = ConradUtil.methodType(method);
		if (methodType == Config.Entry.MethodType.UTIL) return ReflectionUtil.invokeDefault(proxy, method, args);

		ValueKey key = KeyRing.get(method);

		if (methodType == Config.Entry.MethodType.SETTER) {
			throw new ConradException(method.getName());
		} else {
			if (!DEFAULT_VALUES.containsKey(key)) {
				if (method.isDefault()) {
					this.put(key, ReflectionUtil.invokeDefault(proxy, method, args));
				} else if (Config.class.isAssignableFrom(method.getReturnType())) {
					this.put(key, Proxy.newProxyInstance(method.getReturnType().getClassLoader(), new Class[] {method.getReturnType()}, this));
				} else {
					throw new ConradException("Method '" + method.getName() + "' must be default or return an object that extends Config!");
				}
			}

			return this.get(key);
		}
	}

	public static class Remote extends ValueContainer {
		@Override
		public void put(ValueKey key, Object value) {
			this.values.put(key, value);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T getDefault(ValueKey key) {
		return (T) DEFAULT_VALUES.get(key);
	}

	public static void init() {}
}
