package dev.hephaestus.conrad.impl.common.keys;

import dev.hephaestus.conrad.api.networking.NetworkedObjectReader;
import dev.hephaestus.conrad.api.networking.NetworkedObjectWriter;
import net.minecraft.network.PacketByteBuf;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Objects;

public final class ValueKey implements Comparable<ValueKey> {
	private static final HashMap<ValueKey, ValueKey> IDENTIFIERS = new HashMap<>();

	private final ConfigKey config;
	private final String fieldName;

	private ValueKey(ConfigKey config, String fieldName) {
		this.config = config;
		this.fieldName = fieldName;
	}

	public ConfigKey getConfig() {
		return config;
	}

	static ValueKey of(ConfigKey config, String key) {
		return IDENTIFIERS.computeIfAbsent(new ValueKey(config, key), id -> id);
	}

	static ValueKey of(Method method) {
		return ValueKey.of(KeyRing.get(method.getDeclaringClass()), KeyRing.methodName(method));
	}

	public String getName() {
		return this.fieldName;
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
		return ValueKey.of(ConfigKey.READER.read(buf), buf.readString(32767));
	};

	public static NetworkedObjectWriter<ValueKey> WRITER = (buf, value) -> {
		ConfigKey.WRITER.write(buf, ((ValueKey) value).config);
		return buf.writeString(((ValueKey) value).fieldName);
	};

	@Override
	public int compareTo(ValueKey o) {
		int i = this.config.compareTo(o.config);

		if (i == 0) {
			i = this.fieldName.compareTo(o.fieldName);
		}

		return i;
	}
}
