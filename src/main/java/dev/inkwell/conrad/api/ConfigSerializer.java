package dev.inkwell.conrad.api;

import dev.inkwell.conrad.impl.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

public abstract class ConfigSerializer<E, O extends E> {
	private final HashMap<Class<?>, ValueSerializer<E, ?, ?>> serializableTypes = new HashMap<>();
	private final HashMap<Class<? extends E>, Class<?>> valueSerializers = new HashMap<>();

	protected final void addSerializer(Class<?> valueClass, Class<? extends E> representationClass,  ValueSerializer<E, ?, ?> valueSerializer) {
		this.serializableTypes.putIfAbsent(valueClass, valueSerializer);

		valueClass = ReflectionUtil.getClass(valueClass);

		for (Class<?> clazz : ReflectionUtil.getClasses(valueClass)) {
			this.serializableTypes.put(clazz, valueSerializer);
		}

		this.valueSerializers.put(representationClass, valueClass);
	}

	protected final boolean canSerialize(Class<?> valueClass) {
		return this.serializableTypes.containsKey(valueClass);
	}

	public final ValueSerializer<E, ?, ?> getSerializer(Class<?> clazz) {
		return serializableTypes.containsKey(clazz) ? serializableTypes.get(clazz) : serializableTypes.get(valueSerializers.get(clazz));
	}

	public abstract O start();

	protected abstract <R extends E> R add(O object, String key, R representation, @Nullable String comment);

	public abstract <V> V get(O object, String key);

	public abstract O read(InputStream in) throws IOException;

	protected abstract void write(O object, OutputStream out) throws IOException;

	public abstract String fileExtension();

	@SuppressWarnings("unchecked")
	@ApiStatus.Internal
	public final void writeValue(Object object, OutputStream out) throws IOException {
		this.write((O) object, out);
	}


	@ApiStatus.Internal
	public final void deserialize(Config config, ConfigKey configKey, O root) {
		SaveType saveType = config.saveType();

		if (saveType == SaveType.USER && FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
			return;
		}

		for (ConfigValue<?> value : KeyRing.getValues(configKey)) {
			doNested(root, value.getKey(), (object, key) ->
				{
					try {
						ValueContainer.ROOT.put(value.getKey(), this.get(object, key), false);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			);
		}
	}

	@ApiStatus.Internal
	public final O serialize(ConfigKey configKey) {
		O root = this.start();

		for (ConfigValue<?> value : KeyRing.getValues(configKey)) {
			doNested(root, value.getKey(), (object, key) -> {
						Object v = value.get();
						this.add(object, key, this.getSerializer(v.getClass()).serializeValue(v), null);
					}
			);
		}

		return root;
	}

	@ApiStatus.Internal
	private final void doNested(O root, ConfigKey key, Consumer<O, String> consumer) {
		O object = root;
		String[] path = key.getPath();

		for (int i = 1; i < path.length; ++i)  {
			if (i == path.length - 1) {
				consumer.consume(object, path[i]);
			} else {
				if (this.get(object, path[i]) == null) {
					object = this.add(object, path[i], this.start(), null);
				} else {
					object = this.get(object, path[i]);
				}
			}
		}
	}

	private interface Consumer<T1, T2> {
		void consume(T1 t1, T2 t2);
	}
}
