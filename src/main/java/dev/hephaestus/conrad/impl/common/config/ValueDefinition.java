package dev.hephaestus.conrad.impl.common.config;

import com.google.common.collect.ImmutableSet;
import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.common.util.PropertyConsumer;
import dev.hephaestus.conrad.api.properties.PropertyRegistry;
import dev.hephaestus.conrad.api.properties.PropertyType;
import dev.hephaestus.conrad.api.properties.ValueProperty;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings("rawtypes")
public class ValueDefinition {
	private final ValueKey key;
	private final Class<?> type;
	private final int tooltipCount;
	private final boolean synced;
	private final Map<PropertyType, ValueProperty> properties = new LinkedHashMap<>();

	private Set<Identifier> callbacks = Collections.emptySet();
	private Identifier widget = null;

	public ValueDefinition(ValueKey key, Class<?> type, int tooltipCount, boolean synced) {
		this.key = key;
		this.type = type;
		this.tooltipCount = tooltipCount;
		this.synced = synced;
	}

	public ValueDefinition with(Identifier widget) {
		this.widget = widget;
		return this;
	}

	public ValueKey getKey() {
		return this.key;
	}

	public Set<Identifier> getCallbacks() {
		return this.callbacks;
	}

	public Identifier getWidget() {
		return this.widget;
	}

	public boolean isSynced() {
		return this.synced;
	}

	public void getTooltips(boolean serializing, Consumer<Text> textConsumer) {
		if (this.tooltipCount > 0) {
			for (int i = 0; i < this.tooltipCount; ++i) {
				textConsumer.accept(new TranslatableText(this.key + ".tooltip." + i));
			}
		}

		Consumer<MutableText> propertyConsumer = new PropertyConsumer(textConsumer, serializing);

		for (PropertyType<?> type : this.properties.keySet()) {
			this.properties.get(type).addTooltips(propertyConsumer);
		}
	}

	public Class<?> getType() {
		return this.type;
	}

	public boolean hasProperty(PropertyType<?> type) {
		return this.properties.containsKey(type);
	}

	@SuppressWarnings("unchecked")
	public <T> ValueProperty<T> getProperty(PropertyType<T> type) {
		return this.properties.get(type);
	}

	public static ValueDefinition of(ConfigDefinition configDefinition, ValueKey valueKey, Method method) {
		ValueDefinition valueDefinition;

		if (method.isAnnotationPresent(Config.Value.Options.class)) {
			Config.Value.Options options = method.getAnnotation(Config.Value.Options.class);
			valueDefinition = new ValueDefinition(valueKey, method.getReturnType(), options.tooltipCount(), options.synced() == Config.Sync.DEFAULT
					? configDefinition.isSynced()
					: options.synced().getAsBoolean()
			);


			if (method.isAnnotationPresent(Config.Value.Callback.class)) {
				HashSet<Identifier> callbacks = new HashSet<>();

				for (String callback : (method.getAnnotation(Config.Value.Callback.class).value())) {
					callbacks.add(new Identifier(callback));
				}

				valueDefinition.callbacks = ImmutableSet.copyOf(callbacks);
			}

			if (method.isAnnotationPresent(Config.Value.Widget.class)) {
				valueDefinition.widget = new Identifier(method.getAnnotation(Config.Value.Widget.class).value());
			}
		} else {
			valueDefinition = new ValueDefinition(valueKey, method.getReturnType(), 0, false);
		}

		for (Annotation annotation : method.getDeclaredAnnotations()) {
			if (PropertyRegistry.isProperty(annotation.annotationType())) {
				valueDefinition.properties.put(
					PropertyRegistry.getType(annotation.annotationType()),
					PropertyRegistry.getBuilder(annotation.annotationType()).from(annotation)
				);
			}
		}

		return valueDefinition;
	}
}
