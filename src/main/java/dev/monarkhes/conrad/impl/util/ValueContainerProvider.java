package dev.monarkhes.conrad.impl.util;

import dev.monarkhes.conrad.impl.value.ValueContainer;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Map;
import java.util.UUID;

public interface ValueContainerProvider extends Iterable<Map.Entry<UUID, ValueContainer>> {
    ValueContainer getValueContainer();
    ValueContainer getPlayerValueContainer(UUID playerId);
    int playerCount();

    default ValueContainer getPlayerValueContainer(PlayerEntity playerEntity) {
        return this.getPlayerValueContainer(playerEntity.getUuid());
    }
}
