package dev.hephaestus.conrad.api;

import dev.hephaestus.conrad.api.serialization.ConfigSerializer;
import dev.hephaestus.conrad.impl.common.serialization.JanksonSerializer;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.util.version.SemanticVersionImpl;
import net.minecraft.util.Identifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.BooleanSupplier;

public interface Config {
	default ConfigSerializer<?, ?> serializer() {
		return JanksonSerializer.INSTANCE;
	}

	/**
	 * Allows you to specify the version of a config schema. If an older version is found when a config file is parsed,
	 * that file will be renamed to {@link Options#name()}-{@return}
	 */
	default SemanticVersion version() {
		try {
			return new SemanticVersionImpl("1.0.0", false);
		} catch (VersionParsingException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Determines how this config option should be saved. Required on all config objects.
	 */
	enum SaveType {
		/**
		 * A config with a {@code SaveType} of {@code USER} will be available on the client at all times. It will be
		 * available on the server only if {@link Options#synced()} on the class or {@link Value.Options#synced()}
		 * on the method for an individual config value return {@code true}.
		 *
		 * {@code USER} configs are saved on the client, in the root "config" folder of the Minecraft installation.
		 */
		USER,

		/**
		 * A config with a {@code SaveType} of {@code LEVEL} will be available on the logical server at all times.
		 * It will be available on the client only if either {@link Options#synced()} on the class or
		 * {@link Value.Options#synced()} on the method of an individual config value return {@code true}.
		 *
		 * {@code LEVEL} configs are saved in a few different places:
		 * <ul>
		 *     <li>The root "config" folder of the Minecraft client installation contains default config values. These
		 *     are used when creating a new world, and can be modified by accessing Mod Menu from the main menu.</li>
		 *     <li>The "config" folder of a world will contain the {@code LEVEL} configs for that particular save.
		 *     They can be modified by accessing Mod Menu while playing on a single player world.</li>
		 *     <li>The "config" folder of a dedicated Minecraft server instance contains the config values for that
		 *     server. These can be modified by a level 4 operator by accessing Mod Menu while connected to the server.
		 *     </li>
		 * </ul>
		 */
		LEVEL
	}

	/**
	 * Cannot just be a straight boolean, as methods must inherit the value of the {@link Config} they are declared
	 * in if they don't specify behavior themselves.
	 */
	enum Sync implements BooleanSupplier {
		TRUE(true),
		FALSE(false),
		DEFAULT(null);

		private final Boolean value;

		Sync(Boolean value) {
			this.value = value;
		}

		@Override
		public boolean getAsBoolean() {
			return this.value;
		}

		public static Sync of(Boolean bool) {
			if (bool == null) {
				return DEFAULT;
			} else if (bool) {
				return TRUE;
			} else {
				return FALSE;
			}
		}
	}

	/**
	 * Options for handling a declared config schema.
	 * Only the root config interface should be annotated.
	 */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@interface Options {
		/**
		 * The name that will be used to save this config to a file.
		 * This should not contain the file extension, as that is returned by the {@link ConfigSerializer}.
		 * It can contain multiple strings, with each string before the last one representing folders, and the last
		 * string representing the name the file will be saved under.
		 *
		 * e.g. {@code {"a", "b", "c", "d", "e"}} could be saved under {@code .minecraft/config/a/b/c/d/e.json}.
		 */
		String[] name() default "config";

		/**
		 * The method that will be used to save this config file. See {@link SaveType}.
		 */
		SaveType type();

		/**
		 * The number of tooltips that are shown when hovering over the category on the config screen.
		 * Tooltips are automatically wrapped to fit in the window, so doing that manually isn't necessary.
		 */
		int tooltips() default 0;

		/**
		 * If the client should know about level values and vice-versa.
		 * Level configs will always be synced to operators so that they can modify the configs from mod menu.
		 */
		Sync synced() default Sync.DEFAULT;
	}

	class Value {
		@Target(ElementType.METHOD)
		@Retention(RetentionPolicy.RUNTIME)
		public @interface Options {
			/**
			 * The key that will be used when saving this config value to a file.
			 */
			String name() default "";

			MethodType type() default MethodType.GETTER;

			/**
			 * The number of tooltips that are shown when hovering over this config option on the config screen.
			 * Tooltips are automatically wrapped to fit in the window, so doing that manually isn't necessary.
			 */
			int tooltipCount() default 0;

			/**
			 * If the client should know about level values and vice-versa.
			 * If a method annotated with either {@link Config.Sync#TRUE} or {@link Config.Sync#FALSE} will always
			 * override the value of {@link Config.Options#synced()}. This allows you to sync specific values to/from
			 * the server. Level configs will always be synced to ops so that they can modify the configs from mod menu.
			 */
			Config.Sync synced() default Config.Sync.DEFAULT;

			/**
			 * The priority of this config value in the config file and on the config screen.
			 * Lower values appear higher up, while higher values appear farther down.
			 * Methods with the same priority value will be sorted alphabetically by method name.
			 * Nested configs are always moved to the bottom. Priority can be used for sorting between nested
			 * config methods.
			 */
			int priority() default 100;
		}

		@Target(ElementType.METHOD)
		@Retention(RetentionPolicy.RUNTIME)
		public @interface Widget {
			/**
			 * Identifier of a
			 */
			String value();
		}

		public enum MethodType {
			/**
			 * These methods are what you declare in your config class for each config value you wish to have.
			 */
			GETTER,

			/**
			 * Util methods can be either static or default methods that invoke your other config options.
			 * These can be helpful for more complex config options, or for aggregating or transforming
			 * config options into formats that are easier to work with in code, when a fully custom
			 * serializer would be overkill.
			 */
			UTIL
		}

		/**
		 * Identifier of the custom callback you want to fire when this config value is saved.
		 * Should be in the form of "modid:callback_name". The callback method itself should be registered by calling
		 * {@link Conrad#registerCallback(Identifier, SaveCallback)} in your mod initializer.
		 */
		@Target(ElementType.METHOD)
		@Retention(RetentionPolicy.RUNTIME)
		public @interface Callback {
			String[] value();
		}

		@Target(ElementType.METHOD)
		@Retention(RetentionPolicy.RUNTIME)
		public @interface IntegerBounds {
			long min() default Long.MIN_VALUE;
			long max() default Long.MAX_VALUE;
		}

		@Target(ElementType.METHOD)
		@Retention(RetentionPolicy.RUNTIME)
		public @interface FloatingBounds {
			double min() default Double.MIN_VALUE;
			double max() default Double.MAX_VALUE;
		}

		@Target(ElementType.METHOD)
		@Retention(RetentionPolicy.RUNTIME)
		public @interface Matches {
			String value();
		}
	}
}
