package dev.hephaestus.conrad.impl.compat;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;

import net.fabricmc.loader.api.ModContainer;

import dev.hephaestus.conrad.impl.entrypoints.ConradModMenuEntrypoint;

public class ModMenuCompat implements ModMenuApi {
	private static final HashMap<String, ConradModMenuEntrypoint> ENTRY_POINTS = new HashMap<>();
	private static Map<String, ConfigScreenFactory<?>> SCREEN_FACTORIES = null;

	public static void processConfig(ModContainer modContainer) {
		ENTRY_POINTS.computeIfAbsent(modContainer.getMetadata().getId(), ConradModMenuEntrypoint::new);
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
}
