package dev.hephaestus.conrad.api.serialization;

import dev.hephaestus.conrad.api.StronglyTypedList;
import dev.hephaestus.conrad.impl.common.config.*;
import org.jetbrains.annotations.Nullable;
import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.common.util.ReflectionUtil;
import dev.hephaestus.conrad.impl.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.TranslatableText;

import java.io.*;
import java.util.*;

public abstract class ConfigSerializer<E, O extends E> {
	private final HashMap<Class<?>, ValueSerializer<E, ?, ?>> serializableTypes = new HashMap<>();
	private final HashMap<Class<? extends E>, Class<?>> valueSerializers = new HashMap<>();
	private final HashMap<Class<?>, ValueSerializer<E, ?, ?>> listSerializers = new HashMap<>();

	protected final void addSerializer(Class<?> valueClass, Class<? extends E> representationClass, ValueSerializer<E, ?, ?> valueSerializer) {
		this.addSerializer(valueClass, representationClass, valueSerializer, null);
	}

	protected final void addSerializer(Class<?> valueClass, Class<? extends E> representationClass,  ValueSerializer<E, ?, ?> valueSerializer, @Nullable ValueSerializer<E, ?, ?> listSerializer) {
		this.serializableTypes.putIfAbsent(valueClass, valueSerializer);

		valueClass = ReflectionUtil.getClass(valueClass);

		for (Class<?> clazz : ReflectionUtil.getClasses(valueClass)) {
			this.serializableTypes.put(clazz, valueSerializer);
		}

		this.valueSerializers.put(representationClass, valueClass);

		if (listSerializer != null) {
			this.listSerializers.putIfAbsent(valueClass, listSerializer);
		}
	}

	protected final boolean canSerialize(Class<?> valueClass) {
		return this.serializableTypes.containsKey(valueClass);
	}

	public final ValueSerializer<E, ?, ?> getSerializer(Class<?> clazz) {
		return serializableTypes.containsKey(clazz) ? serializableTypes.get(clazz) : serializableTypes.get(valueSerializers.get(clazz));
	}

	public final O serialize(ValueContainer container, ConfigDefinition configDefinition) {
		O object = this.start(configDefinition);

		for (Map.Entry<ValueKey, ValueDefinition> entry : configDefinition.getValues()) {
			Class<?> type = entry.getValue().getType();
			E serializedValue = null;

			if (type == StronglyTypedList.class) {
				StronglyTypedList<?> defaultValue = container.get(entry.getKey());
				serializedValue = this.listSerializers.get(defaultValue.valueClass).serialize(
						container.get(entry.getKey())
				);
			} else if (!Config.class.isAssignableFrom(type)) {
				Object value = container.get(entry.getKey());
				serializedValue = this.getSerializer(value.getClass()).serializeValue(container.get(entry.getKey()));
			}

			if (serializedValue != null) {
				StringBuilder builder = new StringBuilder();

				entry.getValue().getTooltips(true, text -> {
					if (builder.length() > 0) {
						builder.append('\n');
					}

					String string = text instanceof TranslatableText
							? Translator.translate((TranslatableText) text)
							: text.asString();

					builder.append(string);
				});

				this.add(object, entry.getKey().getName(), serializedValue, builder.toString());
			}
		}

		for (Map.Entry<ConfigKey, ConfigDefinition> entry : configDefinition.getChildren()) {
			this.add(object, entry.getKey().getName(), this.serialize(container, entry.getValue()), null);
		}

		return object;
	}

	@SuppressWarnings("unchecked")
	public final void deserialize(ValueContainer container, ConfigDefinition configDefinition, Object object) {
		Config.SaveType saveType = configDefinition.getSaveType();

		if (saveType == Config.SaveType.USER && FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
			return;
		}

		for (Map.Entry<ValueKey, ValueDefinition> entry : configDefinition.getValues()) {
			Class<?> type = entry.getValue().getType();
			Object result = null;

			if (type == StronglyTypedList.class) {
				StronglyTypedList<?> defaultValue = container.get(entry.getKey());
				result = this.listSerializers.get(defaultValue.valueClass).deserialize(
						this.get((O) object, entry.getKey().getName()
				));
			} else if (!Config.class.isAssignableFrom(type)) {
				result = this.getSerializer(type).deserialize(this.get((O) object, entry.getKey().getName()));
			}

			if (result == null) {
				result = container.get(entry.getKey());
			}

			if (result != null && !Config.class.isAssignableFrom(result.getClass())) {
				container.put(entry.getKey(), result, false);
			}
		}

		for (Map.Entry<ConfigKey, ConfigDefinition> entry : configDefinition.getChildren()) {
			this.deserialize(container, entry.getValue(), this.get((O) object, entry.getKey().getName()));
		}
	}

	@SuppressWarnings("unchecked")
	public final void writeValue(Object object, OutputStream out) throws IOException {
		this.write((O) object, out);
	}

	public abstract O start(ConfigDefinition configDefinition);
	protected abstract <R extends E> void add(O object, String key, R representation, @Nullable String comment);
	public abstract <V> V get(O object, String key);

	public abstract O read(InputStream in) throws IOException;
	protected abstract void write(O object, OutputStream out) throws IOException;

	public abstract String fileExtension();
}
