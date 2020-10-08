package dev.hephaestus.conrad.impl.common.config;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.api.Conrad;
import dev.hephaestus.conrad.api.serialization.ConfigSerializer;
import dev.hephaestus.conrad.impl.common.util.ConradUtil;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ConfigDefinition implements Iterable<Map.Entry<ValueKey, ValueDefinition>> {
	private final ConfigKey key;
	private final TreeMap<ValueKey, ValueDefinition> values = new TreeMap<>();
	private final ConfigSerializer<?, ?> serializer;
	private final boolean synced;

	private Path savePath = null;

	private ConfigDefinition(ConfigKey key, ConfigSerializer<?, ?> serializer, boolean synced) {
		this.key = key;
		this.serializer = serializer;
		this.synced = synced;
	}

	public ConfigKey getKey() {
		return this.key;
	}

	public ValueDefinition getDefinition(ValueKey key) {
		return this.values.get(key);
	}

	public ConfigSerializer<?,?> getSerializer() {
		return this.serializer;
	}

	public Path getSavePath() {
		return this.savePath;
	}

	public boolean isRoot() {
		return this.key.path.length == 1;
	}

	public boolean isSynced() {
		return this.synced;
	}

	public ConfigDefinition withSavePath(Path savePath) {
		this.savePath = savePath;
		return this;
	}

	@NotNull
	@Override
	public Iterator<Map.Entry<ValueKey, ValueDefinition>> iterator() {
		return this.values.entrySet().iterator();
	}

	public static <T extends Config> ConfigDefinition build(String modId, Class<T> configClass) {
		Config.Options options = configClass.getDeclaredAnnotation(Config.Options.class);
		Path savePath = Paths.get(modId);

		String[] path = options.name();
		for (int i = 0; i < path.length - 1; ++i) {
			savePath = savePath.resolve(path[i]);
		}

		return build(
				modId,
				configClass,
				path[path.length-1],
				options.synced().getAsBoolean()
		).withSavePath(savePath);
	}

	public static <T extends Config> ConfigDefinition build(String modId, Class<T> configClass, String name, boolean sync) {
		ConfigKey configKey = ConfigKey.of(modId, name);
		ConfigDefinition definition = new ConfigDefinition(configKey, Conrad.getConfig(configClass).serializer(), sync);

		for (Method method : configClass.getDeclaredMethods()) {
			Config.Value.MethodType type = ConradUtil.methodType(method);

			if (type == Config.Value.MethodType.GETTER) {
				if (Config.class.isAssignableFrom(method.getReturnType())) {
					@SuppressWarnings("unchecked")
					Class<? extends Config> nestedConfigClass = (Class<? extends Config>) method.getReturnType();
					Config.Value.Options options = method.getDeclaredAnnotation(Config.Value.Options.class);

					build(
							modId,
							nestedConfigClass,
							options == null || options.name().equals("") ? method.getName() : options.name(),
							options == null || options.synced() == Config.Sync.DEFAULT ? sync : options.synced().getAsBoolean()
					);
				}

				definition.values.computeIfAbsent(
						ValueKey.of(configKey, method),
						key -> ValueDefinition.of(definition, key, method)
				);
			}
		}

		return KeyRing.put(configKey, definition);
	}
}
