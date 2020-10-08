package dev.hephaestus.conrad.api.properties;

public abstract class BoundedProperty<T> extends ValueProperty<T> {
	public abstract T getMin();
	public abstract T getMax();
}
