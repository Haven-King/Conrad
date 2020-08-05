package dev.hephaestus.conrad.impl.entrypoints;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.compat.ModMenuCompat;
import dev.hephaestus.conrad.impl.ConradUtils;
import dev.hephaestus.conrad.impl.config.ConfigManager;
import dev.hephaestus.conrad.impl.config.RootConfigManager;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

import java.lang.instrument.IllegalClassFormatException;
import java.util.List;

public class ConradPreLaunch implements PreLaunchEntrypoint {
	@Override
	public void onPreLaunch() {
		List<EntrypointContainer<Config>> containers = FabricLoader.getInstance().getEntrypointContainers("conrad", Config.class);

		for (EntrypointContainer<Config> container : containers) {
			try {
				String modid = container.getProvider().getMetadata().getId();
				processConfig(modid, container.getEntrypoint());

				if (FabricLoader.getInstance().isModLoaded("modmenu")) {
					ModMenuCompat.processConfig(container.getProvider());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		RootConfigManager.initialize();

		if (FabricLoader.getInstance().isModLoaded("modmenu")) {
			ModMenuCompat.complete();
		}

		ConradUtils.LOG.info("Registered {} configs", ConfigManager.keyCount());
	}

	private static Config processConfig(String modid, Config config) throws IllegalClassFormatException {
		if (ConfigManager.isRegistered(config.getClass())) {
			throw new IllegalArgumentException(String.format("Class %s already registered with key %s", config.getClass(), ConfigManager.getKey(config.getClass())));
		}

		if (!config.getClass().isAnnotationPresent(Config.SaveType.class)) {
			throw new IllegalClassFormatException(String.format("Class %s does not have the SaveType annotation.", config.getClass()));
		}

		String key;
		if (config.getClass().isAnnotationPresent(Config.SaveName.class)) {
			key = modid + "." + config.getClass().getAnnotation(Config.SaveName.class).value();
		} else {
			key = modid + ".config";
		}

		if (config.getClass().getDeclaredMethods().length > 0) {
			throw new IllegalClassFormatException(String.format("Class %s should not contain any methods.", config.getClass()));
		}

		ConfigManager.putKey(config.getClass(), key);
		ConfigManager.putMod(modid);

		return config;
	}
}
