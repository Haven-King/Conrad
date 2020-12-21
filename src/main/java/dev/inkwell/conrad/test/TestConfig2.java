package dev.inkwell.conrad.test;

import dev.inkwell.conrad.api.Config;
import dev.inkwell.conrad.api.SaveType;
import org.jetbrains.annotations.NotNull;

public class TestConfig2 implements Config {
	@Override
	public @NotNull String name() {
		return "conrad";
	}

	@Override
	public @NotNull SaveType saveType() {
		return SaveType.LEVEL;
	}
}
