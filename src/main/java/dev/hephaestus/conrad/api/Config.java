package dev.hephaestus.conrad.api;

import dev.hephaestus.conrad.api.serialization.ConfigSerializer;
import dev.hephaestus.conrad.impl.common.serialization.GsonSerializer;
import dev.hephaestus.conrad.impl.common.serialization.TagSerializer;
import dev.hephaestus.conrad.test.JacksonSerializer;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.util.version.SemanticVersionImpl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface Config {
	default ConfigSerializer<?, ?> serializer() {
		return GsonSerializer.INSTANCE;
	}

	default SemanticVersion version() {
		try {
			return new SemanticVersionImpl("1.0.0", false);
		} catch (VersionParsingException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Retention(RetentionPolicy.RUNTIME) @interface SaveName {
		String[] value();
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
			USER,
			LEVEL
		}
	}

	@Target(ElementType.TYPE_USE)
	@Retention(RetentionPolicy.RUNTIME) @interface Tooltip {
		String[] value();
	}

	class Entry {
		@Retention(RetentionPolicy.RUNTIME)
		public @interface SaveName {
			String value();
		}

		@Retention(RetentionPolicy.RUNTIME)
		public @interface Type {
			MethodType value();
		}

		public enum MethodType {
			SETTER,
			GETTER,
			UTIL
		}
	}
}
