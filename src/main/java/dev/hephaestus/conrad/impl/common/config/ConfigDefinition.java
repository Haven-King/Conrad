package dev.hephaestus.conrad.impl.common.config;

import com.google.common.collect.ImmutableMap;
import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.api.Conrad;
import dev.hephaestus.conrad.api.serialization.ConfigSerializer;
import dev.hephaestus.conrad.impl.common.util.ConradUtil;
import dev.hephaestus.conrad.impl.common.util.ReflectionUtil;
import net.fabricmc.loader.api.SemanticVersion;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class ConfigDefinition {
	private final ConfigKey key;
	private final ConfigSerializer<?, ?> serializer;
	private final SemanticVersion version;
	private final boolean synced;
	private final int tooltipCount;

	private Map<ValueKey, ValueDefinition> values;
	private Map<ConfigKey, ConfigDefinition> children;
	private Config.SaveType saveType;
	private Path savePath = null;

	private ConfigDefinition(ConfigKey key, ConfigSerializer<?, ?> serializer, SemanticVersion version, boolean synced, int tooltipCount) {
		this.key = key;
		this.serializer = serializer;
		this.version = version;
		this.synced = synced;
		this.tooltipCount = tooltipCount;
	}

	private ConfigDefinition with(Config.SaveType saveType, Path savePath) {
		this.saveType = saveType;
		this.savePath = savePath;
		return this;
	}

	private ConfigDefinition with(Map<ValueKey, ValueDefinition> values, Map<ConfigKey, ConfigDefinition> children) {
		this.values = ImmutableMap.copyOf(values);
		this.children = ImmutableMap.copyOf(children);
		return this;
	}

	public ConfigKey getKey() {
		return this.key;
	}

	public ValueDefinition getValueDefinition(ValueKey key) {
		return this.values.get(key);
	}

	public ConfigDefinition getChildDefinition(ConfigKey configKey) {
		return this.children.get(configKey);
	}

	public Collection<Map.Entry<ValueKey, ValueDefinition>> getValues() {
		return this.values.entrySet();
	}

	public Collection<Map.Entry<ConfigKey, ConfigDefinition>> getChildren() {
		return this.children.entrySet();
	}

	public ConfigSerializer<?,?> getSerializer() {
		return this.serializer;
	}

	public SemanticVersion getVersion() {
		return this.version;
	}

	public Config.SaveType getSaveType() {
		return this.saveType;
	}

	public Path getSavePath() {
		return this.savePath;
	}

	public int getTooltipCount() {
		return this.tooltipCount;
	}

	public boolean isRoot() {
		return this.key.path.length == 1;
	}

	public boolean isSynced() {
		return this.synced;
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
				options.synced().getAsBoolean(),
				options.tooltipCount(),
				path[path.length-1]
		).with(options.saveType(), savePath);
	}

	public static <T extends Config> ConfigDefinition build(String modId, Class<T> configClass, boolean sync, int tooltipCount, String... path) {
		ConfigKey configKey = ConfigKey.of(modId, path);
		Config config = Conrad.getConfig(configClass);
		ConfigDefinition definition = new ConfigDefinition(configKey, config.serializer(), config.version(), sync, tooltipCount);

		Map<ValueKey, ValueDefinition> values = new TreeMap<>();
		Map<ConfigKey, ConfigDefinition> children = new TreeMap<>();

		for (Method method : configClass.getDeclaredMethods()) {
			Config.Value.MethodType type = ConradUtil.methodType(method);

			if (type == Config.Value.MethodType.GETTER) {
				if (Config.class.isAssignableFrom(method.getReturnType())) {
					@SuppressWarnings("unchecked")
					Class<? extends Config> nestedConfigClass = (Class<? extends Config>) method.getReturnType();
					Config.Value.Options options = method.getDeclaredAnnotation(Config.Value.Options.class);
					ValueKey valueKey = ValueKey.of(configKey, method);

					values.computeIfAbsent(
							valueKey,
							key -> ValueDefinition.of(definition, key, method)
					);

					String[] childPath = Arrays.copyOf(path, path.length + 1);
					childPath[childPath.length - 1] = options == null || options.name().equals("")
							? method.getName() :
							options.name();

					ConfigDefinition child = build(
							modId,
							nestedConfigClass,
							options == null || options.synced() == Config.Sync.DEFAULT ? sync : options.synced().getAsBoolean(),
							options == null ? 0 : options.tooltipCount(),
							childPath
					);

					children.putIfAbsent(
							child.key.withPriority(valueKey.getPriority()),
							child
					);

					KeyRing.put(child.key, child);
					KeyRing.put(configKey, child.key);
				} else {
					ValueKey valueKey = ValueKey.of(configKey, method);

					try {
						ValueContainer.ROOT.put(valueKey, ReflectionUtil.invokeDefault(method), false);
					} catch (Throwable e) {
						ConradUtil.LOG.warn("Error saving default value for key {}: {}", valueKey, e.getMessage());
					}

					values.computeIfAbsent(
							valueKey,
							key -> ValueDefinition.of(definition, key, method)
					);
				}
			}
		}

		return KeyRing.put(configKey, definition.with(values, children));
	}
}
