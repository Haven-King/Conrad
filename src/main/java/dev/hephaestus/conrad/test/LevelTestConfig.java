package dev.hephaestus.conrad.test;

import dev.hephaestus.conrad.api.Config;

@Config.SaveName("raymond")
@Config.SaveType(Config.SaveType.Type.LEVEL)
public interface LevelTestConfig extends Config {
	default int getNumber() {
		return 0;
	}
}
