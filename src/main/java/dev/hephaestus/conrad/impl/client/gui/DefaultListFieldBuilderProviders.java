package dev.hephaestus.conrad.impl.client.gui;

import dev.hephaestus.conrad.api.StronglyTypedList;
import dev.hephaestus.conrad.api.gui.FieldBuilderProvider;
import dev.hephaestus.conrad.api.gui.FieldBuilderProviderRegistry;
import dev.hephaestus.conrad.api.gui.ListFieldBuilderProviderRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.text.TranslatableText;

public class DefaultListFieldBuilderProviders implements ClientModInitializer {
	public static final FieldBuilderProvider<StronglyTypedList<Integer>> INTEGER = (configBuilder, valueContainer, valueKey) ->
			configBuilder.entryBuilder().startIntList(new TranslatableText(valueKey.toString()));

	public static final FieldBuilderProvider<StronglyTypedList<Double>> DOUBLE = (configBuilder, valueContainer, valueKey) ->
			configBuilder.entryBuilder().startDoubleList(new TranslatableText(valueKey.toString()));

	@Override
	public void onInitializeClient() {
		ListFieldBuilderProviderRegistry.register(Integer.class, INTEGER);
		ListFieldBuilderProviderRegistry.register(Double.class, DOUBLE);
		FieldBuilderProviderRegistry.register(StronglyTypedList.class, ListFieldBuilderProviderRegistry.INSTANCE);
	}
}
