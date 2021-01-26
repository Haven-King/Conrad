package dev.monarkhes.conrad.test;

import dev.monarkhes.conrad.api.Config;
import dev.monarkhes.conrad.api.ConfigValue;
import dev.monarkhes.conrad.api.SaveType;
import dev.monarkhes.conrad.api.SyncType;
import dev.monarkhes.vivid.util.Array;
import dev.monarkhes.vivid.util.Table;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

import static dev.monarkhes.conrad.api.Config.*;

public class TestConfig implements Config {
	public static final ConfigValue<Boolean> ENABLED = value(true);
	public static final ConfigValue<Integer> YEAR_OF_BIRTH = value(1000).setMin(0).setMax(Calendar.getInstance().get(Calendar.YEAR));
	public static final ConfigValue<String> NAME = value("Jane Doe");
	public static final ConfigValue<Array<Integer>> TEST = array(() -> 0, 0, 1, 2, 3, 4, 5).setChildBounds(0, 10);
	public static final ConfigValue<Table<Integer>> TEST2 = table(() -> 0,
			new Table.Entry<>("alpha", 0),
			new Table.Entry<>("beta", -7),
			new Table.Entry<>("omega", 5),
			new Table.Entry<>("walrus", 40)
	);

	public static class A {
		public static final ConfigValue<Boolean> LIKE = value(true).sync(SyncType.INFO);
		public static final ConfigValue<String> FRUIT = value("Apple").sync(SyncType.INFO);
	}

	public static class B {
		public static final ConfigValue<Boolean> LIKE = value(true);
		public static final ConfigValue<String> FRUIT = value("Blueberry");
	}

	public static class C {
		public static final ConfigValue<Boolean> LIKE = value(false);
		public static final ConfigValue<String> FRUIT = value("Cherry");
	}

	public static class D {
		public static final ConfigValue<Boolean> LIKE = value(false);
		public static final ConfigValue<String> FRUIT = value("Durian");
	}

	public static class E {
		public static final ConfigValue<Boolean> LIKE = value(true);
		public static final ConfigValue<String> FRUIT = value("Elderberry");
	}

	public static class F {
		public static final ConfigValue<Boolean> LIKE = value(false);
		public static final ConfigValue<String> FRUIT = value("Fig");
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
