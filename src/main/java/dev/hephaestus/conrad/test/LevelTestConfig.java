package dev.hephaestus.conrad.test;

import dev.hephaestus.conrad.api.Config;

@Config.Options(name = "raymond", type = Config.SaveType.LEVEL)
public interface LevelTestConfig extends Config {
	default int number() {
		return 0;
	}
}
