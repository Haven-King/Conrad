package dev.hephaestus.conrad.impl.common;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.api.Conrad;
import dev.hephaestus.conrad.api.networking.NetworkSerializerRegistry;
import dev.hephaestus.conrad.api.properties.PropertyType;
import dev.hephaestus.conrad.api.serialization.ConfigSerializer;
import dev.hephaestus.conrad.impl.client.ConradModMenuEntrypoint;
import dev.hephaestus.conrad.impl.common.config.ConfigDefinition;
import dev.hephaestus.conrad.impl.common.config.ValueContainer;
import dev.hephaestus.conrad.impl.common.util.ConradUtil;
import dev.hephaestus.conrad.impl.common.util.Translator;
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
import java.nio.file.StandardCopyOption;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

public class ConradPreLaunchEntrypoint implements PreLaunchEntrypoint {
	@Override
	public void onPreLaunch() {
		ValueContainer.init();
		PropertyType.init();
		NetworkSerializerRegistry.init();

		for (ModContainer container : FabricLoader.getInstance().getAllMods()) {
			if (container.getMetadata().containsCustomValue(ConradUtil.MOD_ID)) {
				Translator.init(container.getMetadata().getId());
				handle(container.getMetadata().getId(), container.getMetadata().getCustomValue(ConradUtil.MOD_ID));
			}
		}

		ValueContainer.ROOT.done();
	}

	private static void handle(String modId, CustomValue customValue) {
		if (customValue.getType() == CustomValue.CvType.STRING) {
			try {
				process(modId, customValue.getAsString());
			} catch (AssertionError | ClassNotFoundException | VersionParsingException | IOException e) {
				e.printStackTrace();
			}
		} else if (customValue.getType() == CustomValue.CvType.ARRAY) {
			for (CustomValue childValue : customValue.getAsArray()) {
				handle(modId, childValue);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static <T, O extends T> void process(String modId, String className) throws ClassNotFoundException, VersionParsingException, IOException {
		Class<? extends Config> configClass = (Class<? extends Config>) Class.forName(className);

		ConradUtil.prove(configClass.getInterfaces()[0] == Config.class);
		ConradUtil.prove(configClass.isAnnotationPresent(Config.Options.class));

		ConradUtil.put(configClass, modId);

		ConfigDefinition configDefinition = ConfigDefinition.build(modId, configClass);

		Config config = Conrad.getConfig(configClass);

		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT && FabricLoader.getInstance().isModLoaded("modmenu")) {
			ConradModMenuEntrypoint.processConfig(modId);
		}

		if (configDefinition.getSaveType() != Config.SaveType.USER || FabricLoader.getInstance().getEnvironmentType() != EnvType.SERVER) {
			ConfigSerializer<T, O> serializer = (ConfigSerializer<T, O>) config.serializer();
			Path file = FabricLoader.getInstance().getConfigDir().normalize().resolve(configDefinition.getSavePath()).resolve(configDefinition.getKey().getName() + "." + serializer.fileExtension());
			Files.createDirectories(file.getParent());
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
					Files.copy(file, moved, StandardCopyOption.REPLACE_EXISTING);
				} else {
					serializer.deserialize(ValueContainer.ROOT, configDefinition, configFileObject);
					return;
				}
			}

			ConradUtil.LOG.info("Saving default config file: " + fileName);
			serializer.writeValue(serializer.serialize(ValueContainer.ROOT, configDefinition), Files.newOutputStream(file, CREATE, WRITE));
		}
	}
}
