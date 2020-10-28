package dev.hephaestus.conrad.impl.common.config;

import net.minecraft.server.network.ServerPlayerEntity;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class PlayerValueContainers {
	protected final HashMap<UUID, PlayerValueContainer> valueContainers = new HashMap<>();

	public PlayerValueContainer get(UUID uuid) {
		return this.valueContainers.computeIfAbsent(uuid, key -> new PlayerValueContainer());
	}

	public void put(ServerPlayerEntity playerEntity, ValueKey valueKey, Object value) throws IOException {
		valueContainers.computeIfAbsent(playerEntity.getUuid(), key -> new PlayerValueContainer()).put(valueKey, value, false);
	}
}
