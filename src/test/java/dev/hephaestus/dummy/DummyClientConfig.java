package dev.hephaestus.dummy;

import dev.hephaestus.conrad.api.Config;

@Config.SaveName("chelsea")
@Config.SaveType(Config.SaveType.Type.CLIENT)
public class DummyClientConfig implements Config {
	@Entry.Widget("INT_SLIDER")
	@Entry.Bounds.Discrete(min = 0, max = 9000)
	public int powerLevel = 1337;
}
