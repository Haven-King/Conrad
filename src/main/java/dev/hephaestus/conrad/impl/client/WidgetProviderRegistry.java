package dev.hephaestus.conrad.impl.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.HashMap;

@Environment(EnvType.CLIENT)
public class WidgetProviderRegistry {
    public static final ConfigWidgetProvider<?> NONE = (builder, root, config, field) -> null;

    private static final HashMap<Class<?>, ConfigWidgetProvider<?>> DEFAULT_PROVIDERS = new HashMap<>();
    private static final HashMap<String, ConfigWidgetProvider<?>> ALTERNATE_PROVIDERS = new HashMap<>();

    public static void register(Class<?> configValueType, ConfigWidgetProvider<?> widgetProvider) {
        DEFAULT_PROVIDERS.putIfAbsent(configValueType, widgetProvider);
    }

    public static void register(String configWidgetType, ConfigWidgetProvider<?> widgetProvider) {
        ALTERNATE_PROVIDERS.putIfAbsent(configWidgetType, widgetProvider);
    }

    /**
     * Sets the ConfigWidgetProvider for a given type. Only use this if you're trying to override the default
     * widget providers for ALL instances of configValueType.
     * @param configValueType the type of the fields that should use this widget provider
     * @param widgetProvider the method to call to get a new instance of the given widget
     */
    public static void set(Class<?> configValueType, ConfigWidgetProvider<?> widgetProvider) {
        DEFAULT_PROVIDERS.put(configValueType, widgetProvider);
    }

    public static ConfigWidgetProvider<?> get(Class<?> configValueType) {
        return DEFAULT_PROVIDERS.getOrDefault(configValueType, NONE);
    }

    public static ConfigWidgetProvider<?> get(String configWidgetType) {
        return ALTERNATE_PROVIDERS.getOrDefault(configWidgetType, NONE);
    }

    static {
        register(Boolean.class, WidgetProviders.BOOLEAN_BUTTON);
        register(Boolean.TYPE, WidgetProviders.BOOLEAN_BUTTON);

        register(Double.class, WidgetProviders.DOUBLE_FIELD);
        register(Double.TYPE, WidgetProviders.DOUBLE_FIELD);

        register(Float.class, WidgetProviders.FLOAT_FIELD);
        register(Float.TYPE, WidgetProviders.FLOAT_FIELD);

        register(Integer.class, WidgetProviders.INT_FIELD);
        register(Integer.TYPE, WidgetProviders.INT_FIELD);

        register(Long.class, WidgetProviders.LONG_FIELD);
        register(Long.TYPE, WidgetProviders.LONG_FIELD);

        register(String.class, WidgetProviders.STRING_FIELD);

        register("COLOR", WidgetProviders.COLOR_FIELD);
        register("INT_SLIDER", WidgetProviders.INT_SLIDER);
        register("LONG_SLIDER", WidgetProviders.LONG_SLIDER);
    }
}