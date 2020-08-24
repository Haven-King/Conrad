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
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.fabricmc.loader.api.metadata.CustomValue;

import java.io.*;

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
		Class<?> configClass = Class.forName(className);

		ConradUtil.prove(configClass.getInterfaces()[0] == Config.class);
		ConradUtil.prove(configClass.isAnnotationPresent(Config.SaveName.class));
		ConradUtil.prove(configClass.isAnnotationPresent(Config.SaveType.class));

		ConradUtil.put((Class<? extends Config>) configClass, modId);
		KeyRing.put(KeyRing.get(configClass), (Class<? extends Config>) configClass);

		Config config = Conrad.getConfig((Class<? extends Config>) configClass);
		ConradModMenuEntrypoint.processConfig(modId);

		try {
			ConfigSerializer<?, ?> serializer = config.serializer();
			File file = SerializationUtil.saveFolder(FabricLoader.getInstance().getConfigDir(), (Class<? extends Config>) configClass).resolve(SerializationUtil.saveName((Class<? extends Config>) configClass) + "." + serializer.fileExtension()).toFile();

			if (file.exists()) {
				serializer.deserialize(config, serializer.read(new FileInputStream(file)));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
