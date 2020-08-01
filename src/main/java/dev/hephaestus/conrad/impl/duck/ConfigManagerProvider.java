package dev.hephaestus.conrad.impl.duck;

import dev.hephaestus.conrad.impl.config.ConfigManager;
import dev.hephaestus.conrad.impl.config.server.PlayerConfigManager;

public interface ConfigManagerProvider {
	static ConfigManagerProvider of(Object provider) {
		return (ConfigManagerProvider) provider;
	}

	ConfigManager getConfigManager();
	PlayerConfigManager getPlayerConfigManager();
}
