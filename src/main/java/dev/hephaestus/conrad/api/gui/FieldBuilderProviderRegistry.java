package dev.hephaestus.conrad.api.gui;

import dev.hephaestus.clothy.api.ConfigBuilder;
import dev.hephaestus.clothy.impl.builders.FieldBuilder;
import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.common.config.ValueContainer;
import dev.hephaestus.conrad.impl.common.config.KeyRing;
import dev.hephaestus.conrad.impl.common.config.ValueKey;
import dev.hephaestus.conrad.impl.common.util.ConradException;
import dev.hephaestus.conrad.impl.common.util.ReflectionUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Method;
import java.util.HashMap;

@Environment(EnvType.CLIENT)
public class FieldBuilderProviderRegistry {
	private static final HashMap<Class<?>, FieldBuilderProvider<?>> FIELD_BUILDERS = new HashMap<>();
	private static final HashMap<Identifier, FieldBuilderProvider<?>> CUSTOM_FIELD_BUILDERS = new HashMap<>();

	/**
	 * Used to register a FieldBuilderProvider for a class that doesn't yet have one.
	 * To change the FieldBuilderProvider for an existing class, see {@link FieldBuilderProviderRegistry#override}.
	 * @param clazz the type of the value that should use this FieldBuilderProvider
	 * @param fieldBuilderProvider
	 */
	public static <T> void register(Class<T> clazz, FieldBuilderProvider<?> fieldBuilderProvider) {
		for (Class<?> otherClazz : ReflectionUtil.getClasses(clazz)) {
			FIELD_BUILDERS.putIfAbsent(otherClazz, fieldBuilderProvider);
		}
	}

	/**
	 * Used to register an alternative FieldBuilderProvider that is declared for use via the
	 * {@link dev.hephaestus.conrad.api.Config.Value.Widget} annotation.
	 * Note that no type checking is done here to make sure that methods provide the right value type for the
	 * FieldBuilderProvider passed to this method.
	 * @param id passed to {@link dev.hephaestus.conrad.api.Config.Value.Widget} to identify the FieldBuilderProvider
	 *           to be used for the method.
	 * @param fieldBuilderProvider
	 */
	public static <T> void register(Identifier id, FieldBuilderProvider<T> fieldBuilderProvider) {
		CUSTOM_FIELD_BUILDERS.put(id, fieldBuilderProvider);
	}

	/**
	 * Used to override an existing FieldBuilderProvider for a class with another.
	 * @param clazz the type of the value that should use this FieldBuilderProvider
	 * @param fieldBuilderProvider
	 */
	public static void override(Class<?> clazz, FieldBuilderProvider<?> fieldBuilderProvider) {
		FIELD_BUILDERS.put(clazz, fieldBuilderProvider);
	}

	@ApiStatus.Internal
	public static boolean contains(Class<?> clazz) {
		return FIELD_BUILDERS.containsKey(clazz);
	}

	@ApiStatus.Internal
	public static FieldBuilder<?, ?> getEntry(ConfigBuilder configBuilder, ValueContainer valueContainer, ValueKey valueKey) {
		Method method = KeyRing.get(valueKey);
		if (method.isAnnotationPresent(Config.Value.Widget.class)) {
			Identifier id = new Identifier(method.getAnnotation(Config.Value.Widget.class).value());
			if (CUSTOM_FIELD_BUILDERS.containsKey(id)) {
				return CUSTOM_FIELD_BUILDERS.get(id).getBuilder(configBuilder, valueContainer, valueKey);
			} else {
				throw new ConradException("Custom field builder provider not registered: '" + id + "'!");
			}
		} else {
			Class<?> clazz = KeyRing.get(valueKey).getReturnType();
			return FIELD_BUILDERS.get(clazz).getBuilder(configBuilder, valueContainer, valueKey);
		}
	}
}
