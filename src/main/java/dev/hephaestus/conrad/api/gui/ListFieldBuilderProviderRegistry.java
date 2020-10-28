package dev.hephaestus.conrad.api.gui;

import dev.hephaestus.clothy.api.ConfigBuilder;
import dev.hephaestus.clothy.impl.builders.FieldBuilder;
import dev.hephaestus.conrad.api.StronglyTypedList;
import dev.hephaestus.conrad.impl.common.config.ValueContainer;
import dev.hephaestus.conrad.impl.common.config.ValueKey;

import java.util.HashMap;
import java.util.List;

public class ListFieldBuilderProviderRegistry implements FieldBuilderProvider<Object> {
	public static ListFieldBuilderProviderRegistry INSTANCE = new ListFieldBuilderProviderRegistry();
	private static final HashMap<Class<?>, FieldBuilderProvider<?>> LIST_FIELD_BUILDER_PROVIDERS = new HashMap<>();

	// The ? should be the same, but Java generics are dumb and I don't want to deal with making that work.
	public static void register(Class<?> valueClass, FieldBuilderProvider<?> fieldBuilderProvider) {
		LIST_FIELD_BUILDER_PROVIDERS.putIfAbsent(valueClass, fieldBuilderProvider);
	}

	public static void override(Class<?> valueClass, FieldBuilderProvider<List<?>> fieldBuilderProvider) {
		LIST_FIELD_BUILDER_PROVIDERS.put(valueClass, fieldBuilderProvider);
	}

	@Override
	public FieldBuilder<Object, ?> getBuilder(ConfigBuilder configBuilder, ValueContainer valueContainer, ValueKey valueKey) {
		StronglyTypedList<?> list = valueContainer.get(valueKey);
		return (FieldBuilder<Object, ?>) LIST_FIELD_BUILDER_PROVIDERS.get(list.valueClass).getBuilder(configBuilder, valueContainer, valueKey);
	}
}
