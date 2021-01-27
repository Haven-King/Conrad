package dev.monarkhes.conrad.test;

import net.fabricmc.loader.api.config.data.Constraint;

public class Color {
	public static final Constraint<Color> NO_ALPHA = new Constraint<Color>("fabric:bounds/color") {
		@Override
		public boolean passes(Color value) {
			return value.value <= 0xFFFFFF;
		}

		@Override
		public String toString() {
			return super.toString() + "[0 <= value <= 0xFFFFFF]";
		}
	};

	public final int value;
	public final int a;
	public final int r;
	public final int g;
	public final int b;

	public Color(int value) {
		this.value = value;
		this.a = (value >> 24) & 0xFF;
		this.r = (value >> 16) & 0xFF;
		this.g = (value >> 8) & 0xFF;
		this.b = value & 0xFF;
	}
}
