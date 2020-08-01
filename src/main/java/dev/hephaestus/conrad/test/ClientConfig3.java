package dev.hephaestus.conrad.test;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.api.annotation.SaveName;
import dev.hephaestus.conrad.api.annotation.SaveType;

@SaveName("chelsea")
@SaveType(SaveType.Type.CLIENT)
public class ClientConfig3 implements Config {
	public int integerValue1 = 1;
	public int integerValue2 = 2;
	public int integerValue3 = 3;

	public int integerValue4 = 4;

	public boolean booleanValue1 = false;
	public boolean booleanValue2 = true;
	public boolean booleanValue3 = false;
	public boolean booleanValue4 = true;
	public boolean booleanValue5 = false;
	public boolean booleanValue6 = true;
	public boolean booleanValue7 = false;
	public boolean booleanValue8 = true;

	public InnerTestConfig innerTestConfig = new InnerTestConfig();

	public static class InnerTestConfig implements Config {
		public int integerValue = 10;
		public boolean booleanValue = true;
	}
}