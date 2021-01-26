package dev.monarkhes.conrad.test;

import dev.monarkhes.conrad.api.Config;
import dev.monarkhes.conrad.api.ConfigValue;
import dev.monarkhes.conrad.api.SaveType;
import org.jetbrains.annotations.NotNull;

import static dev.monarkhes.conrad.api.Config.*;

public class TestConfig2 implements Config {
	public static final ConfigValue<String> NAME = value("Jane Doe");

	public static class A {
		public static final ConfigValue<Boolean> LIKE = value(true);
		public static final ConfigValue<String> FRUIT = value("Apple");

		public static class B {
			public static final ConfigValue<Boolean> LIKE = value(true);
			public static final ConfigValue<String> FRUIT = value("Blueberry");

			public static class C {
				public static final ConfigValue<Boolean> LIKE = value(true);
				public static final ConfigValue<String> FRUIT = value("Cherry");

				public static class D {
					public static final ConfigValue<Boolean> LIKE = value(true);
					public static final ConfigValue<String> FRUIT = value("Durian");

					public static class E {
						public static final ConfigValue<Boolean> LIKE = value(true);
						public static final ConfigValue<String> FRUIT = value("Elderberry");
					}
				}
			}
		}
	}

	@Override
	public @NotNull String name() {
		return "conrad";
	}

	@Override
	public @NotNull SaveType saveType() {
		return SaveType.LEVEL;
	}
}
