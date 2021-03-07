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

package dev.inkwell.oliver.api.data;

import java.util.function.Predicate;

public enum SyncType {
    /**
     * Used to inform users of each other's config settings.
     */
    P2P(SaveType.USER),

    /**
     * Used to keep either logical side informed of the appropriate setting on the other.
     */
    INFO(SaveType.USER, SaveType.LEVEL);

    private final Predicate<SaveType> saveTypePredicate;

    SyncType(SaveType... saveTypes) {
        if (saveTypes.length == 0) {
            saveTypePredicate = t -> true;
        } else {
            saveTypePredicate = saveType -> {
                for (SaveType type : saveTypes) {
                    if (type == saveType) return true;
                }

                return false;
            };
        }
    }

    public boolean matches(SaveType saveType) {
        return this.saveTypePredicate.test(saveType);
    }
}
