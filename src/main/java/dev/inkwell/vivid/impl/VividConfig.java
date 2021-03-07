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

package dev.inkwell.vivid.impl;

import dev.inkwell.conrad.api.Config;
import dev.inkwell.conrad.api.Stylable;
import dev.inkwell.optionionated.api.data.SaveType;
import dev.inkwell.optionionated.api.serialization.ConfigSerializer;
import dev.inkwell.optionionated.api.serialization.PropertiesSerializer;
import dev.inkwell.optionionated.api.util.Version;
import dev.inkwell.optionionated.api.value.ValueKey;
import dev.inkwell.vivid.api.screen.ScreenStyle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvironmentInterface;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@EnvironmentInterface(value = EnvType.CLIENT, itf = Stylable.class)
public class VividConfig extends Config<Map<String, String>> implements Stylable  {
    @Override
    public @NotNull ConfigSerializer<Map<String, String>> getSerializer() {
        return PropertiesSerializer.INSTANCE;
    }

    @Override
    public @NotNull SaveType getSaveType() {
        return SaveType.USER;
    }

    @Override
    public boolean upgrade(@Nullable Version from, Map<String, String> representation) {
        return false;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public ScreenStyle getStyle() {
        return new VividConfigStyle();
    }

    @Override
    public @NotNull String getName() {
        return "vivid";
    }

    public static final class Animations {
        public static final ValueKey<Boolean> ENABLED = value(true);
        public static final ValueKey<Float> SPEED = value(0.2F);
    }
}