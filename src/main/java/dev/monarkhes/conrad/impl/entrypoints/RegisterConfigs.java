package dev.monarkhes.conrad.impl.entrypoints;

import dev.monarkhes.conrad.api.Config;
import dev.monarkhes.conrad.api.serialization.ConfigSerializer;
import dev.monarkhes.conrad.api.ConfigValue;
import dev.monarkhes.conrad.impl.*;
import dev.monarkhes.conrad.impl.lang.Translator;
import dev.monarkhes.conrad.impl.util.ConradException;
import dev.monarkhes.conrad.impl.value.ValueContainer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Locale;

public class RegisterConfigs implements PreLaunchEntrypoint {
	private static boolean FINISHED = false;

	@Override
	public void onPreLaunch() {
		Translator.init();

		for (EntrypointContainer<Config> container : FabricLoader.getInstance().getEntrypointContainers("conrad", Config.class)) {
			Config config = container.getEntrypoint();
			String modId = container.getProvider().getMetadata().getId();
			String name = config.name();
			ConfigKey root = new ConfigKey(modId, name);

			if (KeyRing.isRegisteredAsRootConfig(root)) {
				throw new ConradException("Attempted to register Config with duplicate name: " + root);
			}

			KeyRing.register(root, config);
			process(root, config.getClass());
			serialize(root, config);
		}

		finish();
	}

	private static void process(ConfigKey parent, Class<?> clazz) {
		for (Field field : clazz.getDeclaredFields()) {
			if (field.getType() == ConfigValue.class) {
				int modifier = field.getModifiers();

				if (!Modifier.isFinal(modifier)) {
					throw new ConradException("Field " + field.getName() + " is not final!");
				}

				if (!Modifier.isStatic(modifier)) {
					throw new ConradException("Field " + field.getName() + " is not static!");
				}

				if (!Modifier.isPublic(modifier)) {
					throw new ConradException("Field " + field.getName() + " is not public!");
				}

				try {
					ConfigValue<?> configValue = (ConfigValue<?>) field.get(null);

					if (configValue.getKey() != null) {
						throw new ConradException("ConfigKey " + configValue.getKey() + " already registered!");
					}

					ConfigKey key = new ConfigKey(parent, field.getName().toLowerCase(Locale.ENGLISH));
					configValue.setKey(key);
					KeyRing.register(configValue);

					ValueContainer.getInstance(KeyRing.getRootConfig(key).saveType()).putWithDefault(key, configValue.getDefaultValue());

					Conrad.LOGGER.info("Registered ConfigKey {}", key);
				} catch (IllegalAccessException e) {
					throw new ConradException("Error reading field " + field.getDeclaringClass().getName() + "." + field.getName());
				}
			}
		}

		Class<?>[] innerClasses = clazz.getDeclaredClasses();
		for (int i = innerClasses.length - 1; i >= 0; --i) {
			process(new ConfigKey(parent, name(innerClasses[i])), innerClasses[i]);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T, O extends T> void serialize(ConfigKey root, Config config) {
		String modId = root.getNamespace();
		Path file = FabricLoader.getInstance().getConfigDir().normalize();

		if (!modId.equals(config.name())) {
			file = file.resolve(modId);
		}

		file = file.resolve(config.name() + '.' + config.serializer().fileExtension());

		try {
			ConfigSerializer<T, O> serializer = (ConfigSerializer<T, O>) config.serializer();
			if (Files.exists(file)) {
				serializer.deserialize(config, root, serializer.read(Files.newInputStream(file)));
			} else {
				Files.createDirectories(file.getParent());
				serializer.writeValue(serializer.serialize(root), Files.newOutputStream(file));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String name(Class<?> clazz) {
		return clazz
				.getSimpleName()
				.replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2")
				.replaceAll("([a-z])([A-Z])", "$1_$2")
				.toLowerCase(Locale.ENGLISH);
	}

	public static boolean isFinished() {
		return FINISHED;
	}

	private static void finish() {
		FINISHED = true;
	}
}
