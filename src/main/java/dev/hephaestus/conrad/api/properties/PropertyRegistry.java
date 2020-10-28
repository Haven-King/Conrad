package dev.hephaestus.conrad.api.properties;

import java.lang.annotation.Annotation;
import java.util.HashMap;

public class PropertyRegistry {
	private static final HashMap<Class<?>, PropertyType<?>> PROPERTY_TYPES = new HashMap<>();
	private static final HashMap<Class<?>, ValueProperty.Builder> PROPERTY_BUILDERS = new HashMap<>();

	public static <T, A extends Annotation> PropertyType<T> register(String id, Class<A> annotationClass, ValueProperty.Builder builder) {
		PropertyType<T> type = new PropertyType<>(id);

		PROPERTY_BUILDERS.put(annotationClass, builder);
		PROPERTY_TYPES.putIfAbsent(annotationClass, type);

		return type;
	}

	public static <A extends Annotation> boolean isProperty(Class<A> annotationClass) {
		return PROPERTY_BUILDERS.containsKey(annotationClass);
	}

	@SuppressWarnings("unchecked")
	public static <T> PropertyType<T> getType(Class<? extends Annotation> annotationClass) {
		return (PropertyType<T>) PROPERTY_TYPES.get(annotationClass);
	}

	public static <A extends Annotation> ValueProperty.Builder getBuilder(Class<A> annotationClass) {
		return PROPERTY_BUILDERS.get(annotationClass);
	}
}
