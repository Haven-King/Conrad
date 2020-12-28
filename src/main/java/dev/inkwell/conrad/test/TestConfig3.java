package dev.inkwell.conrad.test;

import dev.inkwell.conrad.api.Config;
import dev.inkwell.conrad.api.ConfigValue;
import dev.inkwell.conrad.api.SaveType;
import dev.inkwell.vivid.util.Array;
import dev.inkwell.vivid.util.Table;
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
