package dev.hephaestus.conrad.impl.client.gui;

import dev.hephaestus.clothy.impl.builders.FieldBuilder;
import dev.hephaestus.conrad.api.gui.FieldBuilderProvider;
import dev.hephaestus.conrad.api.gui.FieldBuilderProviderRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.TranslatableText;

import java.util.List;

@Environment(EnvType.CLIENT)
public class DefaultFieldBuilderProviders implements ClientModInitializer {
	public static final FieldBuilderProvider<Boolean> BOOLEAN = (configBuilder, valueContainer, valueKey) -> {
		FieldBuilder<Boolean, ?> fieldBuilder = configBuilder.entryBuilder().startBooleanToggle(new TranslatableText(valueKey.toString()), valueContainer.get(valueKey));
		return FieldBuilderProvider.initialize(fieldBuilder, valueContainer, valueKey);
	};

	public static final FieldBuilderProvider<Integer> INTEGER = (configBuilder, valueContainer, valueKey) -> {
		FieldBuilder<Integer, ?> fieldBuilder = configBuilder.entryBuilder().startIntField(new TranslatableText(valueKey.toString()), valueContainer.get(valueKey));
		return FieldBuilderProvider.initialize(fieldBuilder, valueContainer, valueKey);
	};

	public static final FieldBuilderProvider<Long> LONG = (configBuilder, valueContainer, valueKey) -> {
		FieldBuilder<Long, ?> fieldBuilder = configBuilder.entryBuilder().startLongField(new TranslatableText(valueKey.toString()), valueContainer.get(valueKey));
		return FieldBuilderProvider.initialize(fieldBuilder, valueContainer, valueKey);
	};

	public static final FieldBuilderProvider<Float> FLOAT = (configBuilder, valueContainer, valueKey) -> {
		FieldBuilder<Float, ?> fieldBuilder = configBuilder.entryBuilder().startFloatField(new TranslatableText(valueKey.toString()), valueContainer.get(valueKey));
		return FieldBuilderProvider.initialize(fieldBuilder, valueContainer, valueKey);
	};

	public static final FieldBuilderProvider<Double> DOUBLE = (configBuilder, valueContainer, valueKey) -> {
		FieldBuilder<Double, ?> fieldBuilder = configBuilder.entryBuilder().startDoubleField(new TranslatableText(valueKey.toString()), valueContainer.get(valueKey));
		return FieldBuilderProvider.initialize(fieldBuilder, valueContainer, valueKey);
	};

	public static final FieldBuilderProvider<String> STRING = (configBuilder, valueContainer, valueKey) -> {
		FieldBuilder<String, ?> fieldBuilder = configBuilder.entryBuilder().startStrField(new TranslatableText(valueKey.toString()), valueContainer.get(valueKey));
		return FieldBuilderProvider.initialize(fieldBuilder, valueContainer, valueKey);
	};

	// TODO
//	public static final FieldBuilderProvider<Identifier> IDENTIFIER = (configBuilder, valueContainer, valueKey) -> {
//		FieldBuilder<Identifier, ?> fieldBuilder = configBuilder.entryBuilder().start(new TranslatableText(valueKey.toString()), valueContainer.get(valueKey));
//		return FieldBuilderProvider.initialize(fieldBuilder, valueContainer, valueKey);
//	};

	@Override
	public void onInitializeClient() {
		FieldBuilderProviderRegistry.register(Boolean.class, BOOLEAN);
		FieldBuilderProviderRegistry.register(Integer.class, INTEGER);
		FieldBuilderProviderRegistry.register(Long.class, LONG);
		FieldBuilderProviderRegistry.register(Float.class, FLOAT);
		FieldBuilderProviderRegistry.register(Double.class, DOUBLE);
		FieldBuilderProviderRegistry.register(String.class, STRING);
		// TODO
		//FieldBuilderProviderRegistry.register(Identifier.class, IDENTIFIER);
	}
}
