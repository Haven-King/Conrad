package dev.hephaestus.conrad.test;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.api.serialization.ConfigSerializer;
import dev.hephaestus.conrad.impl.common.serialization.JanksonSerializer;

@Config.Options(name = "test", type = Config.SaveType.USER, tooltips = 3)
public interface UserTestConfig extends Config {
	@Override
	default ConfigSerializer<?, ?> serializer() {
		return JanksonSerializer.INSTANCE;
	}

	@Value.Callback("meth:head")
	default int myFavoriteNumber() { return 7; }

	@Value.Options(name = "myName", tooltipCount = 10)
	default String as123asdasd123asdasd1() { return "Haven"; }

	// Nested configs need not provide a default implementation,
	// as the nested config itself should provide those default
	// values.
	InnerTestConfig innerTestConfig();

	interface InnerTestConfig extends Config {
		@Value.Options(tooltipCount = 3)
		@Value.FloatingBounds(min = 0.0, max = 1.0)
		default double myBAC() { return 0.00F; }
		DoubleInnerTestConfig innerTestConfig();

		interface DoubleInnerTestConfig extends Config {
			default int leet() { return 1337; }
			TripleInnerTestConfig inner();

			interface TripleInnerTestConfig extends Config {
				default boolean garbage() { return true; }
			}
		}
	}
}
