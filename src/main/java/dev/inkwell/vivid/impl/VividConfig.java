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
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class VividConfig extends Config<Map<String, String>> implements Stylable  {
    @Environment(EnvType.CLIENT)
    private static final ScreenStyle STYLE = new ScreenStyle() {
        {
            this.backgroundTexture = new Identifier("textures/block/cobblestone.png");
            this.backgroundColor = 0x80808080;
        }

        @Override
        public void renderDecorations(MatrixStack matrices, int mouseX, int mouseY, float delta, int screenWidth, int screenHeight, int headerHeight) {
            super.renderDecorations(matrices, mouseX, mouseY, delta, screenWidth, screenHeight, headerHeight);
            fillGradient(matrices, 0, 0, screenWidth, screenHeight, 0x88000000 | (gradientColor & 0x00FFFFFF), (gradientColor & 0x00FFFFFF));
        }
    };

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
        return STYLE;
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