package dev.hephaestus.conrad.impl.common.util;

import java.util.function.Supplier;

public class Lazy<T> {
	private T value = null;

	public T computeIfAbsent(Supplier<T> supplier) {
		if (this.value == null) {
			this.value = supplier.get();
		}

		return this.value;
	}
}
