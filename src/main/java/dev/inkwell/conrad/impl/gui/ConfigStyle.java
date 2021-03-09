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

import dev.inkwell.conrad.api.gui.screen.ScreenStyle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
class ConfigStyle extends ScreenStyle {
    ConfigStyle()         {
        this.backgroundTexture = new Identifier("textures/block/cobblestone.png");
        this.backgroundColor = 0x80808080;
    }

    @Override
    public void renderDecorations(MatrixStack matrices, int mouseX, int mouseY, float delta, int screenWidth, int screenHeight, int headerHeight) {
        super.renderDecorations(matrices, mouseX, mouseY, delta, screenWidth, screenHeight, headerHeight);
        fillGradient(matrices, 0, 0, screenWidth, screenHeight, 0x88000000 | (gradientColor & 0x00FFFFFF), (gradientColor & 0x00FFFFFF));
    }
}
