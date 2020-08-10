package dev.hephaestus.conrad.impl.client;

import java.lang.reflect.Field;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;

import dev.hephaestus.conrad.api.Config;

public interface ConfigWidgetProvider<T> {
	AbstractConfigListEntry<T> getWidget(ConfigEntryBuilder builder, String key, Config config, Field field);
}
