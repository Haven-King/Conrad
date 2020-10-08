package dev.hephaestus.conrad.impl.common;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.api.Conrad;
import dev.hephaestus.conrad.api.networking.NetworkSerializerRegistry;
import dev.hephaestus.conrad.api.serialization.ConfigSerializer;
import dev.hephaestus.conrad.impl.client.ConradModMenuEntrypoint;
import dev.hephaestus.conrad.impl.common.config.ConfigDefinition;
import dev.hephaestus.conrad.impl.common.config.ValueContainer;
import dev.hephaestus.conrad.impl.common.util.ConradUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.fabricmc.loader.api.metadata.CustomValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConradPreLaunchEntrypoint implements PreLaunchEntrypoint {
	private static boolean DONE = false;

	@Override
	public void onPreLaunch() {
		ValueContainer.init();
		NetworkSerializerRegistry.init();

		for (ModContainer container : FabricLoader.getInstance().getAllMods()) {
			if (container.getMetadata().containsCustomValue(ConradUtil.MOD_ID)) {
				handle(container.getMetadata().getId(), container.getMetadata().getCustomValue(ConradUtil.MOD_ID));
			}
		}

		DONE = true;
	}

	public static boolean isDone() {
		return DONE;
	}

	private static void handle(String modId, CustomValue customValue) {
		if (customValue.getType() == CustomValue.CvType.STRING) {
			try {
				process(modId, customValue.getAsString());
			} catch (AssertionError | ClassNotFoundException | VersionParsingException e) {
				e.printStackTrace();
			}
		} else if (customValue.getType() == CustomValue.CvType.ARRAY) {
			for (CustomValue childValue : customValue.getAsArray()) {
				handle(modId, childValue);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static <T, O extends T> void process(String modId, String className) throws ClassNotFoundException, VersionParsingException {
		Class<? extends Config> configClass = (Class<? extends Config>) Class.forName(className);

		ConradUtil.prove(configClass.getInterfaces()[0] == Config.class);
		ConradUtil.prove(configClass.isAnnotationPresent(Config.Options.class));

		ConfigDefinition definition = ConfigDefinition.build(modId, configClass);

		Config config = Conrad.getConfig(configClass);

		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT && FabricLoader.getInstance().isModLoaded("modmenu")) {
			ConradModMenuEntrypoint.processConfig(modId);
		}

		try {
			ConfigSerializer<T, O> serializer = (ConfigSerializer<T, O>) config.serializer();
			Path file = FabricLoader.getInstance().getConfigDir().resolve(definition.getSavePath()).resolve(definition.getKey().getName() + "." + serializer.fileExtension());
			String fileName = file.getFileName().toString();

			if (Files.exists(file)) {
				O configFileObject = serializer.read(Files.newInputStream(file));

				SemanticVersion oldVersion = SemanticVersion.parse((String) serializer.getSerializer(String.class).deserialize(serializer.get(configFileObject, "version")));
				SemanticVersion newVersion = config.version();

				if (oldVersion.compareTo(newVersion) != 0) {
					String[] split = fileName.split("\\.(?=[^.]+$)");
					ConradUtil.LOG.warn("Old config version found: " + fileName);
					ConradUtil.LOG.warn("    Found Version:    " + oldVersion);
					ConradUtil.LOG.warn("    Expected Version: " + newVersion);
					ConradUtil.LOG.warn("    Backing up old config to " + split[0] + "-" + oldVersion + "." + split[1]);

					Path moved = file.getParent().resolve(split[0] + "-" + oldVersion + "." + split[1]);
					Files.copy(file, moved);
				} else {
					serializer.deserialize(config, configFileObject);
					return;
				}
			}

			ConradUtil.LOG.info("Saving default config file: " + fileName);
			serializer.writeValue(serializer.serialize(config), Files.newOutputStream(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
