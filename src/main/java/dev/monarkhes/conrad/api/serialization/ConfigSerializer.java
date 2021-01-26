package dev.monarkhes.conrad.api.serialization;

import dev.monarkhes.conrad.api.Config;
import dev.monarkhes.conrad.api.ConfigValue;
import dev.monarkhes.conrad.api.SaveType;
import dev.monarkhes.conrad.impl.*;
import dev.monarkhes.conrad.impl.util.ReflectionUtil;
import dev.monarkhes.conrad.impl.value.ValueContainer;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class ConfigSerializer<E, O extends E> {
	private final HashMap<Class<?>, ValueSerializer<E, ?, ?>> serializableTypes = new HashMap<>();
	private final HashMap<Class<?>, Function<ConfigValue<?>, ValueSerializer<E, ?, ?>>> typeDependentSerializers = new HashMap<>();

	@SuppressWarnings("unchecked")
	public final <T> void addSerializer(Class<T> valueClass, ValueSerializer<E, ?, T> valueSerializer) {
		this.serializableTypes.putIfAbsent(valueClass, valueSerializer);

		valueClass = (Class<T>) ReflectionUtil.getClass(valueClass);

		for (Class<?> clazz : ReflectionUtil.getClasses(valueClass)) {
			this.serializableTypes.putIfAbsent(clazz, valueSerializer);
		}
	}

	public final <T> void addSerializer(Class<T> valueClass, Function<ConfigValue<T>, ValueSerializer<E, ?, T>> serializerBuilder) {
		this.typeDependentSerializers.putIfAbsent(valueClass, (Function) serializerBuilder);
	}

	protected final boolean canSerialize(Class<?> valueClass) {
		return this.serializableTypes.containsKey(valueClass);
	}

	@SuppressWarnings("unchecked")
	public final <V> ValueSerializer<E, ?, V> getSerializer(ConfigValue<V> configValue) {
		V defaultValue = configValue.getDefaultValue();

		if (typeDependentSerializers.containsKey(defaultValue.getClass())) {
			return (ValueSerializer<E, ?, V>) typeDependentSerializers.get(defaultValue.getClass()).apply(configValue);
		}

		return (ValueSerializer<E, ?, V>) this.getSerializer(defaultValue.getClass());
	}

	public final <V> ValueSerializer<E, ?, V> getSerializer(Supplier<V> defaultValue) {
		return this.getSerializer(new ConfigValue<>(defaultValue, null));
	}

	@SuppressWarnings("unchecked")
	public final <V> ValueSerializer<E, ?, V> getSerializer(Class<V> valueClass) {
		return (ValueSerializer<E, ?, V>) serializableTypes.get(valueClass);
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
						ValueSerializer<E, ?, ?> serializer = this.getSerializer(value);
						E representation = this.get(object, key);
						if (representation != null) {
							ValueContainer.getInstance(KeyRing.getRootConfig(value.getKey()).saveType()).put(value.getKey(), serializer.deserialize(representation));
						} else {
							Conrad.LOGGER.info("Missing key: " + value.getKey());
						}
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
						this.add(object, key, this.getSerializer(value).serializeValue(v), null);
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
