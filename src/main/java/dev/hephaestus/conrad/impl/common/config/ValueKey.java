package dev.hephaestus.conrad.impl.common.config;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.api.networking.NetworkedObjectReader;
import dev.hephaestus.conrad.api.networking.NetworkedObjectWriter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Objects;

public class ValueKey implements Comparable<ValueKey> {
	protected static final HashMap<ValueKey, ValueKey> KEYS = new HashMap<>();

	public static NetworkedObjectReader<ValueKey> READER = (buf) ->
			ValueKey.of(ConfigKey.READER.read(buf), buf.readString(32767), buf.readVarInt());

	public static NetworkedObjectWriter<ValueKey> WRITER = (buf, value) -> {
		ConfigKey.WRITER.write(buf, ((ValueKey) value).config);
		return buf.writeString(((ValueKey) value).valueName).writeVarInt(((ValueKey) value).priority);
	};

	private final ConfigKey config;
	private final String valueName;
	private final int priority;

	protected ValueKey(ConfigKey configKey, String valueName, int priority) {
		this.config = configKey;
		this.valueName = valueName;
		this.priority = priority;
	}

	public ConfigKey getConfigKey() {
		return config;
	}

	public String getName() {
		return this.valueName;
	}

	public int getPriority() {
		return this.priority;
	}

	@Override
	public String toString() {
		return config.toString() + "." + valueName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ValueKey valueKey = (ValueKey) o;
		return config.equals(valueKey.config) &&
				valueName.equals(valueKey.valueName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(config, valueName);
	}

	@Override
	public int compareTo(ValueKey o) {
		int i = this.config.compareTo(o.config);

		if (i == 0) {
			i = Integer.compare(this.priority, o.priority);
		}

		if (i == 0) {
			i = this.valueName.compareTo(o.valueName);
		}

		return i;
	}

	private static int priority(Method method) {
		if (method.isAnnotationPresent(Config.Value.Options.class)) {
			return method.getAnnotation(Config.Value.Options.class).priority();
		} else {
			return 100;
		}
	}

	public static ValueKey of(ConfigKey configKey, Method method) {
		Config.Value.Options options = method.getDeclaredAnnotation(Config.Value.Options.class);
		String valueName = options == null || options.name().equals("")
				? method.getName()
				: options.name();
		int priority = options == null ? 100 : options.priority();

		return KeyRing.put(method, of(configKey, valueName, priority));
	}

	static ValueKey of(ConfigKey configKey, String valueName, int priority) {
		return KEYS.computeIfAbsent(
				new ValueKey(
						configKey,
						valueName,
						priority
				), id -> id);
	}
}
