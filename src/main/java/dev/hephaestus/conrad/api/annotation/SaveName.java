package dev.hephaestus.conrad.api.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public @Retention(RetentionPolicy.RUNTIME) @interface SaveName {
	String value();
}