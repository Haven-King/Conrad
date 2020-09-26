package dev.hephaestus.conrad.impl.common;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.api.Conrad;
import dev.hephaestus.conrad.api.networking.NetworkSerializerRegistry;
import dev.hephaestus.conrad.api.serialization.ConfigSerializer;
import dev.hephaestus.conrad.impl.client.ConradModMenuEntrypoint;
import dev.hephaestus.conrad.impl.common.config.ValueContainer;
import dev.hephaestus.conrad.impl.common.util.ConradUtil;
import dev.hephaestus.conrad.impl.common.keys.KeyRing;
import dev.hephaestus.conrad.impl.common.util.SerializationUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.fabricmc.loader.api.metadata.CustomValue;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConradPreLaunchEntrypoint implements PreLaunchEntrypoint {
	@Override
	public void onPreLaunch() {
		ValueContainer.init();
		NetworkSerializerRegistry.init();

		for (ModContainer container : FabricLoader.getInstance().getAllMods()) {
			if (container.getMetadata().containsCustomValue(ConradUtil.MOD_ID)) {
				handle(container.getMetadata().getId(), container.getMetadata().getCustomValue(ConradUtil.MOD_ID));
			}
		}

		String lsdgf = "asdsa";

		lsdgf.split("\\$playerName");
	}

	private static void handle(String modId, CustomValue customValue) {
		if (customValue.getType() == CustomValue.CvType.STRING) {
			try {
				process(modId, customValue.getAsString());
			} catch (AssertionError | ClassNotFoundException e) {
				e.printStackTrace();
			}
		} else if (customValue.getType() == CustomValue.CvType.ARRAY) {
			for (CustomValue childValue : customValue.getAsArray()) {
				handle(modId, childValue);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static void process(String modId, String className) throws ClassNotFoundException {
		Class<? extends Config> configClass = (Class<? extends Config>) Class.forName(className);

		ConradUtil.prove(configClass.getInterfaces()[0] == Config.class);
		ConradUtil.prove(configClass.isAnnotationPresent(Config.Options.class));

		ConradUtil.put(configClass, modId);
		KeyRing.put(KeyRing.get(configClass), configClass);

		Config config = Conrad.getConfig(configClass);

		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT && FabricLoader.getInstance().isModLoaded("modmenu")) {
			ConradModMenuEntrypoint.processConfig(modId);
		}

		try {
			ConfigSerializer<?, ?> serializer = config.serializer();
			Path file = SerializationUtil.saveFolder(FabricLoader.getInstance().getConfigDir(), configClass).resolve(SerializationUtil.saveName(configClass) + "." + serializer.fileExtension());

			if (Files.exists(file)) {
				serializer.deserialize(config, serializer.read(Files.newInputStream(file)));
			} else {
				serializer.writeValue(serializer.serialize(config), Files.newOutputStream(file));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
