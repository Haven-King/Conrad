package dev.inkwell.conrad.api;

import dev.inkwell.conrad.impl.JsonSerializer;
import dev.inkwell.vivid.util.Array;
import dev.inkwell.vivid.util.Table;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public interface Config {
	@NotNull String name();

	@NotNull SaveType saveType();

	@NotNull default ConfigSerializer<?, ?> serializer() {
		return JsonSerializer.INSTANCE;
	}

	static <T> ConfigValue<T> value(Supplier<T> defaultValue) {
		return new ConfigValue<>(defaultValue);
	}

	static ConfigValue<Integer> value(int defaultValue) {
		return new ConfigValue<>(() -> defaultValue);
	}

	static ConfigValue<Long> value(long defaultValue) {
		return new ConfigValue<>(() -> defaultValue);
	}

	static ConfigValue<Float> value(float defaultValue) {
		return new ConfigValue<>(() -> defaultValue);
	}

	static ConfigValue<Double> value(double defaultValue) {
		return new ConfigValue<>(() -> defaultValue);
	}

	static ConfigValue<Boolean> value(boolean defaultValue) {
		return new ConfigValue<>(() -> defaultValue);
	}

	static ConfigValue<Byte> value(byte defaultValue) {
		return new ConfigValue<>(() -> defaultValue);
	}

	static ConfigValue<Short> value(short defaultValue) {
		return new ConfigValue<>(() -> defaultValue);
	}

	static ConfigValue<Character> value(char defaultValue) {
		return new ConfigValue<>(() -> defaultValue);
	}

	static ConfigValue<String> value(String defaultValue) {
		return new ConfigValue<>(() -> defaultValue);
	}

	@SafeVarargs
	@SuppressWarnings("unchecked")
	static <T> ParentConfigValue<Array<T>, T> array(Supplier<T> defaultValue, T... values) {
		return new ParentConfigValue<>(() -> new Array<>((Class<T>) defaultValue.get().getClass(), defaultValue, values));
	}

	@SafeVarargs
	@SuppressWarnings("unchecked")
	static <T> ParentConfigValue<Table<T>, T> table(Supplier<T> defaultValue, Table.Entry<String, T>... values) {
		return new ParentConfigValue<>(() -> new Table<T>((Class<T>) defaultValue.get().getClass(), defaultValue, values));
	}

//	static <T> ParentConfigValue<Table<T>, T> table()
}
