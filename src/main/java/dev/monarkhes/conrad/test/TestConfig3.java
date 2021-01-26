package dev.monarkhes.conrad.test;

import dev.monarkhes.conrad.api.Config;
import dev.monarkhes.conrad.api.ConfigValue;
import dev.monarkhes.conrad.api.SaveType;
import dev.monarkhes.vivid.util.Array;
import dev.monarkhes.vivid.util.Table;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

public class TestConfig3 implements Config {
    public static final ConfigValue<Table<TestThing<Direction>>> TEST = Config.table(
            () -> new TestThing<>(Direction.NORTH, new Array<>(String.class, () -> ""))
    );

    @Override
    public @NotNull String name() {
        return "dex";
    }

    @Override
    public @NotNull SaveType saveType() {
        return SaveType.USER;
    }
}
