package dev.hephaestus.conrad.test;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.api.serialization.ConfigSerializer;

@Config.SaveName("test")
@Config.Tooltip({"one", "two", "three"})
@Config.SaveType(Config.SaveType.Type.USER)
public interface UserTestConfig extends Config {
	@Override
	default ConfigSerializer<?, ?> serializer() {
		return JacksonSerializer.YAML;
	}

	default int getMyFavoriteNumber() { return 7; }
	default String getMyName() { return "Haven"; }

	// Nested configs need not provide a default implementation,
	// as the nested config itself should provide those default
	// values.
	InnerTestConfig getInnerTestConfig();

	interface InnerTestConfig extends Config {
		default float getMyBAC() { return 0.00F; }
		DoubleInnerTestConfig getInner();

		interface DoubleInnerTestConfig extends Config {
			default int getLeet() { return 1337; }
			TripleInnerTestConfig getInner();

			interface TripleInnerTestConfig extends Config {
				default boolean getGarbage() { return true; }
			}
		}
	}
}
