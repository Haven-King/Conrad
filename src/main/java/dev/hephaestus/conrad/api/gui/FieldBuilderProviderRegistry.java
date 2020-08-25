package dev.hephaestus.conrad.api.gui;

import dev.hephaestus.clothy.api.ConfigBuilder;
import dev.hephaestus.clothy.impl.builders.FieldBuilder;
import dev.hephaestus.conrad.impl.client.gui.DefaultFieldBuilderProviders;
import dev.hephaestus.conrad.impl.common.config.ValueContainer;
import dev.hephaestus.conrad.impl.common.keys.KeyRing;
import dev.hephaestus.conrad.impl.common.keys.ValueKey;
import dev.hephaestus.conrad.impl.common.util.ReflectionUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.HashMap;

@Environment(EnvType.CLIENT)
public class FieldBuilderProviderRegistry {
	private static final HashMap<Class<?>, FieldBuilderProvider<?>> FIELD_BUILDERS = new HashMap<>();

	public static <T> void register(Class<T> clazz, FieldBuilderProvider<T> fieldBuilderProvider) {
		for (Class<?> otherClazz : ReflectionUtil.getClasses(clazz)) {
			FIELD_BUILDERS.putIfAbsent(otherClazz, fieldBuilderProvider);
		}
	}

	public static void override(Class<?> clazz, FieldBuilderProvider<?> fieldBuilderProvider) {
		FIELD_BUILDERS.put(clazz, fieldBuilderProvider);
	}

	public static boolean contains(Class<?> clazz) {
		return FIELD_BUILDERS.containsKey(clazz);
	}

	public static FieldBuilder<?, ?> getEntry(ConfigBuilder configBuilder, ValueContainer valueContainer, ValueKey valueKey) {
		Class<?> clazz = KeyRing.get(valueKey).getReturnType();
		return FIELD_BUILDERS.get(clazz).getBuilder(configBuilder, valueContainer, valueKey);
	}
}
