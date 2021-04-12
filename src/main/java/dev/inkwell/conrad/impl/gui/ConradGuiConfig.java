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

package dev.inkwell.conrad.impl.gui;

import dev.inkwell.conrad.api.Config;
import dev.inkwell.conrad.api.gui.screen.ScreenStyle;
import dev.inkwell.conrad.api.value.data.DataType;
import dev.inkwell.conrad.api.value.data.SaveType;
import dev.inkwell.conrad.api.value.serialization.ConfigSerializer;
import dev.inkwell.conrad.api.value.serialization.FlatOwenSerializer;
import dev.inkwell.conrad.api.value.util.DataCollector;
import dev.inkwell.conrad.api.value.util.Version;
import dev.inkwell.conrad.api.value.ValueKey;
import dev.inkwell.owen.OwenElement;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConradGuiConfig extends Config<OwenElement>  {
    private static final ScreenStyle STYLE = new ScreenStyle(new Identifier("textures/block/cobblestone.png"));

    public static final ValueKey<Boolean> SHOW_MODS_CONFIG_BUTTON = value(true);

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
        return false;
    }

    @Override
    public @NotNull String getName() {
        return "gui";
    }

    @Override
    public void addConfigData(@NotNull DataCollector collector) {
        collector.add(DataType.SCREEN_STYLE, STYLE);
    }
}