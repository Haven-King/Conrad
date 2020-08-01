package dev.hephaestus.conrad.test;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.api.annotation.SaveName;
import dev.hephaestus.conrad.api.annotation.SaveType;

@SaveName("charlie")
@SaveType(SaveType.Type.SERVER)
public class ServerConfig implements Config {
	public int integerValue = 5;
	public boolean booleanValue = false;
	public float floatValue = 0.151F;

	public double doubleValue = 0.133133331;

	public InnerTestConfig innerTestConfig = new InnerTestConfig();

	public static class InnerTestConfig implements Config {
		public int integerValue = 10;
		public boolean booleanValue = true;
	}
}