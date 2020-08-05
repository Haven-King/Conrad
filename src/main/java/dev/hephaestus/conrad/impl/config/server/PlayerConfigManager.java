package dev.hephaestus.conrad.impl.config.server;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.config.ConfigManager;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class PlayerConfigManager {
	private final HashMap<UUID, HashMap<String, Config>> configs = new HashMap<>();

	@SuppressWarnings("unchecked")
	public <T extends Config> Optional<T> getConfig(ServerPlayerEntity player, Class<T> configClass) {
		if (this.configs.containsKey(player.getUuid())) {
			return Optional.ofNullable((T) this.configs.get(player.getUuid()).get(ConfigManager.getKey(configClass)));
		}

		return Optional.empty();
	}

	public void putConfig(Config config, ServerPlayerEntity player) {
		configs.computeIfAbsent(player.getUuid(), key -> new HashMap<>()).put(ConfigManager.getKey(config.getClass()), config);
	}
}