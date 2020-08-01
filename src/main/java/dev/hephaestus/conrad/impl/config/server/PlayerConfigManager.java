package dev.hephaestus.conrad.impl.config.server;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.config.ConfigManager;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.UUID;

public class PlayerConfigManager {
	private final HashMap<UUID, HashMap<String, Config>> configs = new HashMap<>();

	@SuppressWarnings("unchecked")
	public <T extends Config> T getConfig(ServerPlayerEntity player, Class<T> configClass) {
		return (T) configs.get(player.getUuid()).get(ConfigManager.getKey(configClass));
	}

	public void putConfig(Config config, ServerPlayerEntity player) {
		configs.computeIfAbsent(player.getUuid(), key -> new HashMap<>()).put(ConfigManager.getKey(config.getClass()), config);
	}
}