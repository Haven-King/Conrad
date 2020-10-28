package dev.hephaestus.conrad.test;

import dev.hephaestus.conrad.api.Config;

@Config.Options(name = "raymond", saveType = Config.SaveType.LEVEL)
public interface LevelTestConfig extends Config {
	@Value.Callback("meth:head")
	default int number() {
		return 0;
	}
}
