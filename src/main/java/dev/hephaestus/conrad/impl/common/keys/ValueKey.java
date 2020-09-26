package dev.hephaestus.conrad.impl.common.keys;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.api.networking.NetworkedObjectReader;
import dev.hephaestus.conrad.api.networking.NetworkedObjectWriter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Objects;

public final class ValueKey implements Comparable<ValueKey> {
	private static final HashMap<ValueKey, ValueKey> IDENTIFIERS = new HashMap<>();

	private final ConfigKey config;
	private final String fieldName;
	private final Config.Sync synced;

	private ValueKey(ConfigKey configKey, String fieldName, Config.Sync synced) {
		this.config = configKey;
		this.fieldName = fieldName;
		this.synced = synced;
	}

	public ConfigKey getConfig() {
		return config;
	}

	static ValueKey of(ConfigKey configKey, String fieldName, Config.Sync synced) {
		return IDENTIFIERS.computeIfAbsent(
				new ValueKey(
						configKey,
						fieldName,
						synced
				), id -> id);
	}

	static ValueKey of(Method method) {
		return ValueKey.of(KeyRing.get(method.getDeclaringClass()), KeyRing.methodName(method), isSynced(method));
	}

	public String getName() {
		return this.fieldName;
	}

	public boolean isSynced() {
		return this.synced == Config.Sync.DEFAULT ? this.config.isSynced() : this.synced.getAsBoolean();
	}

	@Override
	public String toString() {
		return config.toString() + "." + fieldName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ValueKey valueKey = (ValueKey) o;
		return config.equals(valueKey.config) &&
				fieldName.equals(valueKey.fieldName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(config, fieldName);
	}

	public static NetworkedObjectReader<ValueKey> READER = (buf) -> {
		return ValueKey.of(ConfigKey.READER.read(buf), buf.readString(32767), buf.readEnumConstant(Config.Sync.class));
	};

	public static NetworkedObjectWriter<ValueKey> WRITER = (buf, value) -> {
		ConfigKey.WRITER.write(buf, ((ValueKey) value).config);
		return buf.writeString(((ValueKey) value).fieldName).writeEnumConstant(((ValueKey) value).synced);
	};

	@Override
	public int compareTo(ValueKey o) {
		int i = this.config.compareTo(o.config);

		if (i == 0) {
			i = this.fieldName.compareTo(o.fieldName);
		}

		return i;
	}

	private static Config.Sync isSynced(Method method) {
		if (method.isAnnotationPresent(Config.Value.Options.class)) {
			return method.getAnnotation(Config.Value.Options.class).synced();
		} else {
			return Config.Sync.DEFAULT;
		}
	}
}
