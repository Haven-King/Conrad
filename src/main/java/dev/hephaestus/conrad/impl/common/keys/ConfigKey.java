package dev.hephaestus.conrad.impl.common.keys;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.api.networking.NetworkedObjectReader;
import dev.hephaestus.conrad.api.networking.NetworkedObjectWriter;

import java.util.Arrays;
import java.util.HashMap;

public final class ConfigKey implements Comparable<ConfigKey> {
	private static final HashMap<ConfigKey, ConfigKey> IDENTIFIERS = new HashMap<>();

	private final String namespace;
	private final String[] path;
	private final Config.Sync synced;

	private ConfigKey(String namespace, Config.Sync synced, String... path) {
		this.namespace = namespace;
		this.path = path;
		this.synced = synced;
	}

	public String getNamespace() {
		return this.namespace;
	}

	public ConfigKey root() {
		return ConfigKey.of(this.namespace, Config.Sync.FALSE, this.path[0]);
	}

	public boolean isRoot() {
		return this.path.length == 1;
	}

	public ConfigKey parent() {
		if (this.isRoot()) return this;

		String[] path = new String[this.path.length - 1];

		if (path.length >= 0) System.arraycopy(this.path, 0, path, 0, path.length);

		return ConfigKey.of(this.namespace, Config.Sync.FALSE, path);
	}

	public boolean isSynced() {
		return this.synced == Config.Sync.DEFAULT
				? !this.isRoot() && this.parent().isSynced()
				: this.synced.getAsBoolean();
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

	static ConfigKey of(String namespace, Config.Sync synced, String... path) {
		return IDENTIFIERS.computeIfAbsent(new ConfigKey(namespace, synced, path), id -> id);
	}

	static ConfigKey of(ConfigKey id, Config.Sync synced, String... path) {
		String[] newPath = new String[id.path.length + path.length];

		for (int i = 0; i < newPath.length; ++i) {
			boolean first = i < id.path.length;
			newPath[i] = (first ? id.path : path)[first ? i : i - id.path.length];
		}

		return ConfigKey.of(id.namespace, synced, newPath);
	}

	public static NetworkedObjectReader<ConfigKey> READER = (buf) -> {
		String namespace = buf.readString(32767);
		Config.Sync synced = buf.readEnumConstant(Config.Sync.class);
		String[] path = new String[buf.readVarInt()];

		for (int i = 0; i < path.length; ++i) {
			path[i] = buf.readString(32767);
		}

		return ConfigKey.of(namespace, synced, path);
	};

	public static NetworkedObjectWriter<ConfigKey> WRITER = (buf, value) -> {
		buf.writeString(((ConfigKey) value).namespace);
		buf.writeEnumConstant(((ConfigKey) value).synced);
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
