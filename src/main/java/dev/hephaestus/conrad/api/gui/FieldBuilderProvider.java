package dev.hephaestus.conrad.api.gui;

import dev.hephaestus.clothy.api.ConfigBuilder;
import dev.hephaestus.clothy.impl.builders.FieldBuilder;
import dev.hephaestus.conrad.impl.common.config.ValueContainer;
import dev.hephaestus.conrad.impl.common.keys.ValueKey;
import dev.hephaestus.conrad.impl.common.util.ConradUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.io.IOException;

@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface FieldBuilderProvider<T> {
	FieldBuilder<T, ?> getBuilder(ConfigBuilder configBuilder, ValueContainer valueContainer, ValueKey valueKey);

	@SuppressWarnings("unchecked")
	static <T> FieldBuilder<T, ?> initialize(FieldBuilder<T, ?> fieldBuilder, ValueContainer valueContainer, ValueKey valueKey) {
		fieldBuilder.setDefaultValue((T) valueContainer.get(valueKey));
		fieldBuilder.setSaveConsumer(value -> {
			try {
				valueContainer.put(valueKey, value);
			} catch (IOException e) {
				ConradUtil.LOG.warn("Exception while saving config value {}: {}", valueKey.getName(), e.getMessage());
			}
		});

		return fieldBuilder;
	}
}
