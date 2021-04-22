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

package dev.inkwell.conrad.impl;

import dev.inkwell.conrad.api.Config;
import dev.inkwell.conrad.api.value.ValueKey;
import dev.inkwell.conrad.api.value.data.Constraint;
import dev.inkwell.conrad.api.value.data.DataType;
import dev.inkwell.conrad.api.value.data.SaveType;
import dev.inkwell.conrad.api.value.data.SyncType;
import dev.inkwell.conrad.api.value.serialization.ConfigSerializer;
import dev.inkwell.conrad.api.value.serialization.FlatOwenSerializer;
import dev.inkwell.owen.OwenElement;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class TestConfig extends Config<OwenElement> {
    public static final ValueKey<Boolean> TEST_BOOLEAN_1 = value(true);
    public static final ValueKey<Boolean> TEST_BOOLEAN_2 = value(false);
    public static final ValueKey<Boolean> TEST_BOOLEAN_3 = value(false);
    public static final ValueKey<Integer> DELAY = builder(20).bounds(0, 100).build();
    public static final ValueKey<Direction> DIRECTION = value(() -> Direction.DOWN);
    public static final ValueKey<TestDataClass> DATA_CLASS_TEST = value(TestDataClass::new);
    private static final Pattern ALPHABETIC = Pattern.compile("[a-zA-Z]+");
    private static final Constraint<String> ALPHABETIC_CONSTRAINT = new Constraint<String>("alphabetic") {
        @Override
        public boolean passes(String value) {
            return ALPHABETIC.matcher(value).matches();
        }
    };
    public static final ValueKey<String> FAVORITE_WORD = builder("Default")
            .with(ALPHABETIC_CONSTRAINT) // Words should only be made
            .with(DataType.SYNC_TYPE, SyncType.P2P)
            .with((oldValue, newValue, playerId) -> {
                if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
                    System.out.printf("Player %s's favorite word is %s%n", playerId, newValue);
                }
            })
            .build();

    @Override
    public @NotNull ConfigSerializer<OwenElement> getSerializer() {
        return FlatOwenSerializer.INSTANCE;
    }

    @Override
    public @NotNull SaveType getSaveType() {
        return SaveType.USER;
    }

    @Override
    public @NotNull String getName() {
        return "test";
    }
}
