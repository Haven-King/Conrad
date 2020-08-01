package dev.hephaestus.conrad.api;

import dev.hephaestus.conrad.impl.client.widget.config.ConfigWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.lang.reflect.Field;

public interface ConfigWidgetProvider {
	@Environment(EnvType.CLIENT)
	ConfigWidget<?> create(Config config, Field field, String key, int x, int initialY, int width, int entryWidth, Object... args);
}
