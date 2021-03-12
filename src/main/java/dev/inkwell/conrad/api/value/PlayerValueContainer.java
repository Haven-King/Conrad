package dev.inkwell.conrad.api.value;

import dev.inkwell.conrad.api.value.data.SaveType;

import java.util.UUID;

public final class PlayerValueContainer extends ValueContainer {
    private final UUID playerId;

    PlayerValueContainer(UUID playerId, SaveType... saveTypes) {
        super(null, saveTypes);
        this.playerId = playerId;
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    public static PlayerValueContainer of(UUID playerId, SaveType... saveTypes) {
        return new PlayerValueContainer(playerId, saveTypes);
    }
}
