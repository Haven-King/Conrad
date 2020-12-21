package dev.inkwell.conrad.api;

import java.util.ArrayList;
import java.util.Arrays;

public class StronglyTypedList<T> extends ArrayList<T> {
	public final Class<T> valueClass;

	public StronglyTypedList(Class<T> valueClass) {
		this.valueClass = valueClass;
	}

	public StronglyTypedList(Class<T> valueClass, T ... values) {
		this.valueClass = valueClass;
		this.addAll(Arrays.asList(values));
	}
}
