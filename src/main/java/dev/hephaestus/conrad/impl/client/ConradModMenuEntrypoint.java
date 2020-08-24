package dev.hephaestus.conrad.impl.client;

import com.google.common.collect.ImmutableMap;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

import java.util.HashMap;
import java.util.Map;

public class ConradModMenuEntrypoint implements ModMenuApi {
	private static final HashMap<String, ConradScreenFactory> ENTRY_POINTS = new HashMap<>();
	private static Map<String, ConfigScreenFactory<?>> SCREEN_FACTORIES = null;

	public static void processConfig(String modId) {
		ENTRY_POINTS.computeIfAbsent(modId, ConradScreenFactory::new);
	}

	public static void complete() {
		ImmutableMap.Builder<String, ConfigScreenFactory<?>> builder = ImmutableMap.builder();
		builder.putAll(ENTRY_POINTS);
		SCREEN_FACTORIES = builder.build();
	}

	@Override
	public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
		if (SCREEN_FACTORIES == null) complete();
		return SCREEN_FACTORIES;
	}

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			return ENTRY_POINTS.get("conrad");
		} else {
			return screen -> getConfigScreenFactory().apply(screen);
		}
	}
}
