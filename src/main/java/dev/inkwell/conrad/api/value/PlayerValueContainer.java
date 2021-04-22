/*
 * Copyright 2021 Haven King
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.inkwell.conrad.api.value;

import dev.inkwell.conrad.api.value.data.SaveType;

import java.util.UUID;

public final class PlayerValueContainer extends ValueContainer {
    private final UUID playerId;

    PlayerValueContainer(UUID playerId, SaveType... saveTypes) {
        super(null, saveTypes);
        this.playerId = playerId;
    }

    public static PlayerValueContainer of(UUID playerId, SaveType... saveTypes) {
        return new PlayerValueContainer(playerId, saveTypes);
    }

    public UUID getPlayerId() {
        return this.playerId;
    }
}
