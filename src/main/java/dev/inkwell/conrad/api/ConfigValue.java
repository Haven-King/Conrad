package dev.inkwell.conrad.api;

import dev.inkwell.conrad.impl.ConfigKey;
import dev.inkwell.conrad.impl.ConradException;
import dev.inkwell.conrad.impl.entrypoints.RegisterConfigs;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ConfigValue<T> implements Supplier<T> {
	private final Supplier<T> defaultValue;
	private final boolean sync;

	private ConfigKey key = null;

	private ConfigValue(@NotNull Supplier<T> defaultValue, boolean sync) {
		this.defaultValue = defaultValue;
		this.sync = sync;

		if (RegisterConfigs.isFinished()) {
			throw new ConradException("Cannot create ConfigValue after registration has completed!");
		}
	}

	private ConfigValue(@NotNull T defaultValue, boolean sync) {
		this(() -> defaultValue, sync);
	}

	public T get() {
		return this.defaultValue.get();
	}

	@ApiStatus.Internal
	public boolean isSynced() {
		return this.sync;
	}

	@ApiStatus.Internal
	public @Nullable ConfigKey getKey() {
		return this.key;
	}

	@ApiStatus.Internal
	public void setKey(ConfigKey key) {
		this.key = key;
	}

	public static <T> ConfigValue<T> of(Supplier<T> defaultValue) {
		return new ConfigValue<>(defaultValue, false);
	}

	public static <T> ConfigValue<T> of(T defaultValue) {
		return new ConfigValue<>(defaultValue, false);
	}

	public static <T> ConfigValue<T> of(Supplier<T> defaultValue, boolean sync) {
		return new ConfigValue<>(defaultValue, sync);
	}

	public static <T> ConfigValue<T> of(T defaultValue, boolean sync) {
		return new ConfigValue<>(defaultValue, sync);
	}
}
