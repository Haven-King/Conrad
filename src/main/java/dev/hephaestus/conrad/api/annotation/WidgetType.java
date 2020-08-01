package dev.hephaestus.conrad.api.annotation;

import dev.hephaestus.conrad.api.ConfigWidgetProvider;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public @Retention(RetentionPolicy.RUNTIME) @interface WidgetType {
	Class<ConfigWidgetProvider> value();
}
