package dev.hephaestus.conrad.api.gui;

import dev.hephaestus.clothy.api.ConfigBuilder;
import dev.hephaestus.clothy.impl.builders.FieldBuilder;
import dev.hephaestus.conrad.api.Conrad;
import dev.hephaestus.conrad.impl.common.config.ValueContainer;
import dev.hephaestus.conrad.impl.common.keys.KeyRing;
import dev.hephaestus.conrad.impl.common.keys.ValueKey;
import dev.hephaestus.conrad.impl.common.util.ConradUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface FieldBuilderProvider<T> {
	FieldBuilder<T, ?> getBuilder(ConfigBuilder configBuilder, ValueContainer valueContainer, ValueKey valueKey);

	@SuppressWarnings("unchecked")
	static <T> FieldBuilder<T, ?> initialize(FieldBuilder<T, ?> fieldBuilder, ValueContainer valueContainer, ValueKey valueKey) {
		fieldBuilder.setDefaultValue((T) ValueContainer.getDefault(valueKey));
		fieldBuilder.setSaveConsumer(newValue -> {
			try {
				valueContainer.put(valueKey, newValue, true);
			} catch (IOException e) {
				ConradUtil.LOG.warn("Exception while saving config value {}: {}", valueKey.getName(), e.getMessage());
			}
		});

		Method method = KeyRing.get(valueKey);

		List<Text> tooltips = new ArrayList<>();

		ConradUtil.getTooltips(method, tooltips::add);

		if (tooltips.size() > 0) {
			fieldBuilder.setTooltip(Optional.of(tooltips));
		}

		return fieldBuilder;
	}
}
