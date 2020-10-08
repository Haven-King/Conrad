package dev.hephaestus.conrad.impl.common.config;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.api.properties.PropertyRegistry;
import dev.hephaestus.conrad.api.properties.PropertyType;
import dev.hephaestus.conrad.api.properties.ValueProperty;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ValueDefinition {
	private final ValueKey key;
	private final int tooltipCount;
	private final boolean synced;
	private final Map<PropertyType, ValueProperty> properties = new LinkedHashMap<>();

	public ValueDefinition(ValueKey key, int tooltipCount, boolean synced) {
		this.key = key;
		this.tooltipCount = tooltipCount;
		this.synced = synced;
	}

	public ValueKey getKey() {
		return this.key;
	}

	public boolean isSynced() {
		return this.synced;
	}

	public void getTooltips(Consumer<Text> textConsumer) {
		if (this.tooltipCount > 0) {
			for (int i = 0; i < this.tooltipCount; ++i) {
				textConsumer.accept(new TranslatableText(this.key + ".tooltip." + i));
			}
		}

		for (PropertyType<?> type : this.properties.keySet()) {
			this.properties.get(type).addTooltips(textConsumer);
		}
	}

	public boolean hasProperty(PropertyType<?> type) {
		return this.properties.containsKey(type);
	}

	public <T> ValueProperty<T> getProperty(PropertyType<T> type) {
		return this.properties.get(type);
	}

	public static ValueDefinition of(ConfigDefinition definition, ValueKey valueKey, Method method) {
		ValueDefinition valueDefinition;

		if (method.isAnnotationPresent(Config.Value.Options.class)) {
			Config.Value.Options options = method.getAnnotation(Config.Value.Options.class);
			valueDefinition = new ValueDefinition(valueKey, options.tooltipCount(), options.synced() == Config.Sync.DEFAULT
				? definition.isSynced()
				: options.synced().getAsBoolean()
			);
		} else {
			valueDefinition = new ValueDefinition(valueKey, 0, false);
		}

		for (Annotation annotation : method.getDeclaredAnnotations()) {
			if (PropertyRegistry.isProperty(annotation.getClass())) {
				valueDefinition.properties.put(
					PropertyRegistry.getType(annotation.getClass()),
					PropertyRegistry.getBuilder(annotation.getClass()).from(annotation)
				);
			}
		}

		return valueDefinition;
	}
}
