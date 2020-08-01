package dev.hephaestus.conrad.api;

import dev.hephaestus.conrad.impl.client.widget.config.BooleanButtonWidget;
import dev.hephaestus.conrad.impl.client.widget.config.NumberFieldWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.HashMap;

@Environment(EnvType.CLIENT)
public class DefaultConfigWidgetRegistry {
	private static final HashMap<Class<?>, ConfigWidgetProvider> DEFAULT_PROVIDERS = new HashMap<>();

	public static void register(Class<?> configValueType, ConfigWidgetProvider widgetProvider) {
		DEFAULT_PROVIDERS.putIfAbsent(configValueType, widgetProvider);
	}

	/**
	 * Sets the ConfigWidgetProvider for a given type. Only use this if you're trying to override the default
	 * widget providers for ALL instances of configValueType.
	 * @param configValueType the type of the fields that should use this widget provider
	 * @param widgetProvider the method to call to get a new instance of the given widget
	 */
	public static void set(Class<?> configValueType, ConfigWidgetProvider widgetProvider) {
		DEFAULT_PROVIDERS.put(configValueType, widgetProvider);
	}

	public static ConfigWidgetProvider get(Class<?> configValueType) {
		return DEFAULT_PROVIDERS.get(configValueType);
	}

	static {
		register(Boolean.class, BooleanButtonWidget::new);
		register(Boolean.TYPE, BooleanButtonWidget::new);

		register(Integer.class, NumberFieldWidget.INTEGER);
		register(Integer.TYPE, NumberFieldWidget.INTEGER);
		register(Long.class, NumberFieldWidget.LONG);
		register(Long.TYPE, NumberFieldWidget.LONG);
		register(Float.class, NumberFieldWidget.FLOAT);
		register(Float.TYPE, NumberFieldWidget.FLOAT);
		register(Double.class, NumberFieldWidget.DOUBLE);
		register(Double.TYPE, NumberFieldWidget.DOUBLE);
		register(Short.class, NumberFieldWidget.SHORT);
		register(Short.TYPE, NumberFieldWidget.SHORT);
		register(Byte.class, NumberFieldWidget.BYTE);
		register(Byte.TYPE, NumberFieldWidget.BYTE);

	}
}
