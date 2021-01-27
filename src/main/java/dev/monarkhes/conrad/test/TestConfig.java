package dev.monarkhes.conrad.test;

import dev.monarkhes.conrad.api.Config;
import dev.monarkhes.conrad.api.EntryBuilderRegistry;
import dev.monarkhes.vivid.util.Alignment;
import dev.monarkhes.vivid.widgets.value.entry.StringEntryWidget;
import net.fabricmc.fabric.api.config.v1.DataTypes;
import net.fabricmc.fabric.api.config.v1.FabricSaveTypes;
import net.fabricmc.fabric.api.config.v1.SyncType;
import net.fabricmc.loader.api.config.SaveType;
import net.fabricmc.loader.api.config.serialization.TomlSerializer;
import net.fabricmc.loader.api.config.util.Array;
import net.fabricmc.loader.api.config.util.Table;
import net.fabricmc.loader.api.config.value.ValueKey;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Locale;

import static dev.monarkhes.conrad.test.Color.NO_ALPHA;

public class TestConfig extends Config {
	public static final ValueKey<Boolean> ENABLED = value(true);
	public static final ValueKey<Integer> YEAR_OF_BIRTH = new ValueKey.Builder<>(() -> 1000)
			.with(new Bounds.Int(0, Calendar.getInstance().get(Calendar.YEAR)))
			.build();

	public static final ValueKey<String> NAME = value("Jane Doe");
	public static final ValueKey<Array<Integer>> TEST = new ValueKey.CollectionBuilder<>(
			() -> new Array<>(Integer.class, () -> 0, 0, 1, 2, 3, 4, 5))
			.constraint(new Bounds.Int(0, 10))
			.build();

	public static final ValueKey<Table<Integer>> TEST2 = table(() -> 0,
			new Table.Entry<>("alpha", 0),
			new Table.Entry<>("beta", -7),
			new Table.Entry<>("omega", 5),
			new Table.Entry<>("walrus", 40)
	);

	public static final ValueKey<Color> MY_FAVORITE_COLOR = new ValueKey.Builder<>(
			() -> new Color(-1))
			.with(NO_ALPHA)
			.with(DataTypes.SYNC_TYPE, SyncType.P2P, SyncType.INFO)
			.build();

	public static final ValueKey<Table<Color>> TAGS = new ValueKey.CollectionBuilder<>(() -> new Table<>(Color.class, () -> new Color(0xFFFFFF)))
			.constraint(NO_ALPHA)
			.with(DataTypes.SYNC_TYPE, SyncType.P2P, SyncType.INFO)
			.build();

	@Override
	public @NotNull SaveType getSaveType() {
		return FabricSaveTypes.USER;
	}

	@Override
	public @NotNull String getName() {
		return "monroe";
	}

	public static class A {
		public static final ValueKey<Boolean> LIKE = new ValueKey.Builder<>(() -> true)
				.with(DataTypes.SYNC_TYPE, SyncType.INFO)
				.build();

		public static final ValueKey<String> FRUIT = new ValueKey.Builder<>(() -> "Apple")
				.with(DataTypes.SYNC_TYPE, SyncType.INFO)
				.build();
	}

	public static class B {
		public static final ValueKey<Boolean> LIKE = value(true);
		public static final ValueKey<String> FRUIT = value("Blueberry");
	}

	public static class C {
		public static final ValueKey<Boolean> LIKE = value(false);
		public static final ValueKey<String> FRUIT = value("Cherry");
	}

	public static class D {
		public static final ValueKey<Boolean> LIKE = value(false);
		public static final ValueKey<String> FRUIT = value("Durian");
	}

	public static class E {
		public static final ValueKey<Boolean> LIKE = value(true);
		public static final ValueKey<String> FRUIT = value("Elderberry");
	}

	public static class F {
		public static final ValueKey<Boolean> LIKE = value(false);
		public static final ValueKey<String> FRUIT = value("Fig");
	}

	static {
		TomlSerializer.INSTANCE.addSerializer(Color.class, ColorSerializer.INSTANCE);

		EntryBuilderRegistry.register(Color.class, ((configValue, parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value) -> {
			return new ColorEntryWidget(parent, x, y, width, height, Alignment.RIGHT, defaultValueSupplier, changedListener, saveConsumer, value);
		}));
	}

	static class ColorSerializer implements TomlSerializer.ValueSerializer<Color> {
		public static final ColorSerializer INSTANCE = new ColorSerializer();

		@Override
		public Object serialize(Color value) {
			if (value.value == -1) {
				return "0xFFFFFFFF";
			} else {
				return "0x" + Integer.toUnsignedString(value.value, 16).toUpperCase(Locale.ROOT);
			}
		}

		@Override
		public Color deserialize(Object object) {
			String string = (String) object;

			if (string.equalsIgnoreCase("0xFFFFFFFF")) {
				return new Color(-1);
			} else {
				return new Color(Integer.parseUnsignedInt(string.substring(2), 16));
			}
		}
	}
}
