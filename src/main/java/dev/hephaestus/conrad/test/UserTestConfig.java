package dev.hephaestus.conrad.test;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.api.StronglyTypedList;
import dev.hephaestus.conrad.api.serialization.ConfigSerializer;
import dev.hephaestus.conrad.impl.common.serialization.JanksonSerializer;
import dev.hephaestus.math.impl.Color;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.util.version.SemanticVersionImpl;
import net.minecraft.text.TextColor;

@Config.Options(name = "test", type = Config.SaveType.USER, tooltips = 3)
public interface UserTestConfig extends Config {
	@Override
	default ConfigSerializer<?, ?> serializer() {
		return JanksonSerializer.INSTANCE;
	}

	@Override
	default SemanticVersion version() {
		try {
			return new SemanticVersionImpl("1.2.0", false);
		} catch (VersionParsingException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Value.Callback("meth:head")
	default int myFavoriteNumber() { return 7; }

	@Value.Options(name = "myName", tooltipCount = 10)
	default String as123asdasd123asdasd1() { return "Haven"; }

	@Value.Options(priority = 0)
	default Color myFavoriteColor() {
		return Color.ofRGBA(0, 255, 255, 255);
	}

	@Value.Options(type = Value.MethodType.UTIL)
	default TextColor color() {
		return TextColor.fromRgb(this.myFavoriteColor().value());
	}

	@Value.IntegerBounds(min = 0, max = 100)
	default StronglyTypedList<Double> aListOfIntegers() {
		return new StronglyTypedList<>(Double.class, 0D, 17D, 8D, 3D);
	}

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
