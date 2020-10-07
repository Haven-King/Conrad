package dev.hephaestus.clothy.impl.gui.entries;

public interface BoundedFieldEntry<T extends Number> {
	BoundedFieldEntry<T> setMin(T min);
	BoundedFieldEntry<T> setMax(T max);
	T getMin();
	T getMax();

	default boolean isGreaterThanMax(T value) {
		if (this.getMax() == null) {
			return false;
		}

		if (value instanceof Double) {
			return ((Double) value).compareTo(this.getMax().doubleValue()) > 0;
		} else if (value instanceof Float) {
			return ((Float) value).compareTo(this.getMax().floatValue()) > 0;
		} else if (value instanceof Long) {
			return ((Long) value).compareTo(this.getMax().longValue()) > 0;
		} else if (value instanceof Integer) {
			return ((Integer) value).compareTo(this.getMax().intValue()) > 0;
		} else {
			return true;
		}
	}

	default boolean isLessThanMin(T value) {
		if (this.getMin() == null) {
			return false;
		}

		if (value instanceof Double) {
			return ((Double) value).compareTo(this.getMin().doubleValue()) < 0;
		} else if (value instanceof Float) {
			return ((Float) value).compareTo(this.getMin().floatValue()) < 0;
		} else if (value instanceof Long) {
			return ((Long) value).compareTo(this.getMin().longValue()) < 0;
		} else if (value instanceof Integer) {
			return ((Integer) value).compareTo(this.getMin().intValue()) < 0;
		} else {
			return true;
		}
	}
}
