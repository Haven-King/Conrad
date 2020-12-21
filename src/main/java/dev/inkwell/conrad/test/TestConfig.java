package dev.inkwell.conrad.test;

import dev.inkwell.conrad.api.Config;
import dev.inkwell.conrad.api.ConfigValue;
import dev.inkwell.conrad.api.SaveType;
import org.jetbrains.annotations.NotNull;

public class TestConfig implements Config {
	public static final ConfigValue<Boolean> ENABLED = ConfigValue.of(true);
	public static final ConfigValue<Integer> YEAR_OF_BIRTH = ConfigValue.of(8310);
	public static final ConfigValue<String> NAME = ConfigValue.of("Jane Doe");

	public static class A {
		public static final ConfigValue<Boolean> LIKE = ConfigValue.of(true);
		public static final ConfigValue<String> FRUIT = ConfigValue.of("Apple");
	}

	public static class B {
		public static final ConfigValue<Boolean> LIKE = ConfigValue.of(true);
		public static final ConfigValue<String> FRUIT = ConfigValue.of("Blueberry");
	}

	public static class C {
		public static final ConfigValue<Boolean> LIKE = ConfigValue.of(false);
		public static final ConfigValue<String> FRUIT = ConfigValue.of("Cherry");
	}

	public static class D {
		public static final ConfigValue<Boolean> LIKE = ConfigValue.of(false);
		public static final ConfigValue<String> FRUIT = ConfigValue.of("Durian");
	}

	public static class E {
		public static final ConfigValue<Boolean> LIKE = ConfigValue.of(true);
		public static final ConfigValue<String> FRUIT = ConfigValue.of("Elderberry");
	}

	public static class F {
		public static final ConfigValue<Boolean> LIKE = ConfigValue.of(false);
		public static final ConfigValue<String> FRUIT = ConfigValue.of("Fig");
	}

	@Override
	public @NotNull String name() {
		return "monroe";
	}

	@Override
	public @NotNull SaveType saveType() {
		return SaveType.LEVEL;
	}
}
