package dev.hephaestus.dummy;

import dev.hephaestus.conrad.api.Config;

@Config.SaveName("raymond")
@Config.SaveType(Config.SaveType.Type.LEVEL)
public class DummyServerConfig implements Config {
	public String message = "Your power level is: ";

	@Entry.Widget("COLOR")
	public int color = 0xFF00FF;
}
