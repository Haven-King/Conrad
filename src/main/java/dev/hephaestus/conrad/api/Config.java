package dev.hephaestus.conrad.api;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.hephaestus.conrad.impl.client.ConfigWidgetProvider;

@SuppressWarnings("unused")
public interface Config extends Serializable {
	@JsonIgnore
	default Serializer getSerializer() {
		return YAMLSerializer.INSTANCE;
	}

	/**
	 * Specifies the save name (not including file type) of this config file.
	 * If not present, the default name is "config". This will cause problems
	 * if you have multiple configs registered that don't provide a SaveName.
	 */
	@Retention(RetentionPolicy.RUNTIME) @interface SaveName {
		String value();
	}

	/**
	 * Determines how this config option should be saved. Required on all config objects.
	 * Config "A" will be available on the server with SaveType CLIENT if the class of "A"
	 * is defined on both the client and server in question.
	 * Config "B" will be available on the client with SaveType LEVEL if the class of "B"
	 * is defined on both the client and server in question, AND either the player is a
	 * level 4 operator OR the server is a local integrated server.
	 */
	@Retention(RetentionPolicy.RUNTIME) @interface SaveType {
		Type value();

		enum Type {
			CLIENT,
			LEVEL
		}
	}

	class Entry {
		/**
		 * Specifies an alternate widget to the default for the type of the field.
		 * Three of these are registered by default in {@link dev.hephaestus.conrad.impl.client.WidgetProviderRegistry}
		 * and more can be registered with {@link dev.hephaestus.conrad.impl.client.WidgetProviderRegistry#register(String, ConfigWidgetProvider)}
		 */
		@Retention(RetentionPolicy.RUNTIME)
		public @interface Widget {
			String value();
		}

		public static class Bounds {
			@Retention(RetentionPolicy.RUNTIME)
			public @interface Discrete {
				long min() default Long.MIN_VALUE;
				long max() default Long.MAX_VALUE;
			}

			@Retention(RetentionPolicy.RUNTIME)
			public @interface Floating {
				double min() default Double.MIN_VALUE;
				double max() default Double.MAX_VALUE;
			}
		}

		@Retention(RetentionPolicy.RUNTIME)
		public @interface RequiresRestart {
		}
	}

	interface Serializer {
		void save(File file, Config config) throws IOException;
		<T extends Config> T load(File file, Class<T> configClass) throws IOException;
		String fileType();
	}
}
