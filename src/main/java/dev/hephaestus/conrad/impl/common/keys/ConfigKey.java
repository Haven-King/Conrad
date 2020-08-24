package dev.hephaestus.conrad.impl.common.keys;

import dev.hephaestus.conrad.api.networking.NetworkedObjectReader;
import dev.hephaestus.conrad.api.networking.NetworkedObjectWriter;
import net.minecraft.network.PacketByteBuf;

import java.util.Arrays;
import java.util.HashMap;

public final class ConfigKey implements Comparable<ConfigKey> {
	private static final HashMap<ConfigKey, ConfigKey> IDENTIFIERS = new HashMap<>();

	private final String namespace;
	private final String[] path;

	private ConfigKey(String namespace, String... path) {
		this.namespace = namespace;
		this.path = path;
	}

	public String getNamespace() {
		return this.namespace;
	}

	public ConfigKey root() {
		return ConfigKey.of(this.namespace, this.path[0]);
	}

	public boolean isRoot() {
		return this.path.length == 1;
	}

	public ConfigKey parent() {
		if (this.isRoot()) return this;

		String[] path = new String[this.path.length - 1];

		for (int i = 0; i < path.length; ++i) {
			path[i] = this.path[i];
		}

		return ConfigKey.of(this.namespace, path);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ConfigKey that = (ConfigKey) o;
		return namespace.equals(that.namespace) &&
				Arrays.equals(path, that.path);
	}

	@Override
	public int hashCode() {
		return 31 * this.namespace.hashCode() + Arrays.hashCode(path);
	}

	@Override
	public String toString() {
		return this.namespace + ":" + String.join("/", this.path);
	}

	static ConfigKey of(String namespace, String... path) {
		return IDENTIFIERS.computeIfAbsent(new ConfigKey(namespace, path), id -> id);
	}

	static ConfigKey of(ConfigKey id, String... path) {
		String[] newPath = new String[id.path.length + path.length];

		for (int i = 0; i < newPath.length; ++i) {
			boolean first = i < id.path.length;
			newPath[i] = (first ? id.path : path)[first ? i : i - id.path.length];
		}

		return ConfigKey.of(id.namespace, newPath);
	}

	public static ConfigKey fromString(String string) {
		String[] split = string.split(":");
		return ConfigKey.of(
				split[0],
				split[1].split("/")
		);
	}

	public static NetworkedObjectReader<ConfigKey> READER = (buf) -> {
		String namespace = buf.readString(32767);
		String[] path = new String[buf.readVarInt()];

		for (int i = 0; i < path.length; ++i) {
			path[i] = buf.readString(32767);
		}

		return ConfigKey.of(namespace, path);
	};

	public static NetworkedObjectWriter<ConfigKey> WRITER = (buf, value) -> {
		buf.writeString(((ConfigKey) value).namespace);
		buf.writeVarInt(((ConfigKey) value).path.length);

		for (String string : ((ConfigKey) value).path) {
			buf.writeString(string);
		}

		return buf;
	};

	@Override
	public int compareTo(ConfigKey o) {
		int i = this.namespace.compareTo(o.namespace);

		if (i == 0) {
			if (this.path.length != o.path.length) {
				i = Integer.compare(this.path.length, o.path.length);
			} else {
				for (int j = 0; j < this.path.length; ++j) {
					i = this.path[j].compareTo(o.path[j]);

					if (i != 0) break;
				}
			}
		}

		return i;
	}
}
