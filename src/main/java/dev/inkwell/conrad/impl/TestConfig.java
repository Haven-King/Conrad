package dev.inkwell.conrad.impl;

import dev.inkwell.conrad.api.Config;
import dev.inkwell.conrad.api.value.ValueKey;
import dev.inkwell.conrad.api.value.data.DataType;
import dev.inkwell.conrad.api.value.data.SaveType;
import dev.inkwell.conrad.api.value.data.SyncType;
import dev.inkwell.conrad.api.value.serialization.ConfigSerializer;
import dev.inkwell.conrad.api.value.serialization.FlatOwenSerializer;
import dev.inkwell.conrad.api.value.util.Version;
import dev.inkwell.owen.OwenElement;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TestConfig extends Config<OwenElement> {
    public static final ValueKey<String> FAVORITE_WORD = builder("Default")
            .with(DataType.SYNC_TYPE, SyncType.P2P)
            .with((oldValue, newValue, playerId) -> {
                if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
                    System.out.printf("Player %s's favorite word is %s%n", playerId, newValue);
                }
            }).build();

    @Override
    public @NotNull ConfigSerializer<OwenElement> getSerializer() {
        return FlatOwenSerializer.INSTANCE;
    }

    @Override
    public @NotNull SaveType getSaveType() {
        return SaveType.USER;
    }

    @Override
    public boolean upgrade(@Nullable Version from, OwenElement representation) {
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "test";
    }
}
