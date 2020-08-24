package dev.hephaestus.conrad.impl.common.util;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.common.keys.KeyRing;

import java.nio.file.Path;

public class SerializationUtil {
	public static Path saveFolder(Path path, Class<? extends Config> configClass) {
		path = path.normalize().resolve(KeyRing.get(configClass).getNamespace());

		if (configClass.isAnnotationPresent(Config.SaveName.class)) {
			String[] saveName = configClass.getAnnotation(Config.SaveName.class).value();

			if (saveName.length > 1) {
				for (int i = 0; i < saveName.length - 1; ++i) {
					path = path.resolve(saveName[i]);
				}
			}
		}

		return path;
	}

	public static String saveName(Class<? extends Config> configClass) {
		if (configClass.isAnnotationPresent(Config.SaveName.class)) {
			String[] saveName = configClass.getAnnotation(Config.SaveName.class).value();
			return saveName[saveName.length - 1];
		}

		return "config";
	}
}
