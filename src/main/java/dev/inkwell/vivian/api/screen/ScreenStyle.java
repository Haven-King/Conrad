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

package dev.inkwell.vivian.api.screen;

import net.minecraft.util.Identifier;

import java.util.Optional;

public class ScreenStyle {
    public static final ScreenStyle DEFAULT = new ScreenStyle();

    private boolean renderBackgroundTexture = true;
    private Identifier backgroundTexture = new Identifier("textures/block/dirt.png");
    private int backgroundColor = 0xFF404040;

    public ScreenStyle() {

    }

    public ScreenStyle(Identifier backgroundTexture) {
        this.backgroundTexture = backgroundTexture;
    }

    public Optional<Identifier> getBackgroundTexture() {
        return this.renderBackgroundTexture ? Optional.of(backgroundTexture) : Optional.empty();
    }

    public int getBackgroundColor() {
        return 0xFF404040;
    }

    public ScreenStyle backgroundTexture(Identifier texture) {
        this.backgroundTexture = texture;
        return this;
    }

    public ScreenStyle renderBackground(boolean bl) {
        this.renderBackgroundTexture = bl;
        return this;
    }

    public ScreenStyle backgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }
}
