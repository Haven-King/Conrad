package dev.monarkhes.conrad.api;

import java.util.function.Predicate;

public enum SyncType {
    /**
     * Does not do any syncing
     */
    NONE(),

    /**
     * Used to inform users of each other's config settings
     */
    PEER(SaveType.USER),

    /**
     * Used to keep either logical side informed of the appropriate setting on the other
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
