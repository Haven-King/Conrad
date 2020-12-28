package dev.inkwell.conrad.impl;

import dev.inkwell.conrad.api.Config;
import dev.inkwell.conrad.api.ConfigSerializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ValueContainer {
	public static ValueContainer ROOT = new ValueContainer(FabricLoader.getInstance().getConfigDir().normalize());

	private final ConcurrentMap<ConfigKey, Object> values = new ConcurrentHashMap<>();
	private final Path saveDirectory;

	public ValueContainer(Path saveDirectory) {
		this.saveDirectory = saveDirectory;
	}

	public void put(ConfigKey key, Object value, boolean sync) throws IOException {
		Object old = this.get(key);
		boolean modified = value != old;

		if (modified) {
			this.values.put(key, value);
			this.save(key, value, sync);
		}
	}

	public void putDefault(ConfigKey key, Object value) {
		this.values.put(key, value);
	}

	@SuppressWarnings("unchecked")
	public <T> T get(ConfigKey key) {
		return (T) this.values.get(key);
	}

	public boolean contains(ConfigKey key) {
		return this.values.containsKey(key);
	}

	@SuppressWarnings("unchecked")
	private <T, O extends T> void save(ConfigKey key, Object value, boolean sync) throws IOException {
		if (sync) {
			if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
				// TODO
			} else {
				// TODO
			}
		}

		ConfigKey rootKey = new ConfigKey(key.getNamespace(), key.getPath()[0]);
		Config root = KeyRing.getRootConfig(rootKey);
		ConfigSerializer<T, O> serializer = (ConfigSerializer<T, O>) root.serializer();
		Path path = this.saveDirectory.resolve(rootKey.getNamespace()).resolve(root.name()).getParent();

		Files.createDirectories(path);

		serializer.writeValue(
			serializer.serialize(rootKey),
			Files.newOutputStream(
				path.resolve(rootKey.getPath()[rootKey.getPath().length - 1] + "." + serializer.fileExtension())
			)
		);
	}
}
