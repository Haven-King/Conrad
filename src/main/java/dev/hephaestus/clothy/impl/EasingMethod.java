package dev.hephaestus.clothy.impl;

import java.util.function.Function;

public enum EasingMethod {
	NONE(v -> 1.0),
	LINEAR(v -> v);

	private final Function<Double, Double> function;

	EasingMethod(Function<Double, Double> function) {
		this.function = function;
	}

	public double apply(double v) {
		return function.apply(v);
	}

	@Override
	public String toString() {
		return name();
	}
}
