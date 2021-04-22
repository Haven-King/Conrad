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

package dev.inkwell.vivian.api.builders;

import dev.inkwell.vivian.api.screen.ConfigScreen;
import dev.inkwell.vivian.api.screen.ScreenStyle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.MutableText;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ConfigScreenBuilderImpl implements ConfigScreenBuilder {
    private final List<CategoryBuilder> categories = new ArrayList<>();

    private ScreenStyle style = ScreenStyle.DEFAULT;

    public CategoryBuilder startCategory(MutableText name) {
        CategoryBuilder category = new CategoryBuilder(name);
        categories.add(category);

        return category;
    }

    @Override
    public ScreenStyle getStyle() {
        return this.style;
    }

    public void setStyle(ScreenStyle style) {
        this.style = style;
    }

    @Override
    public ConfigScreen build(Screen parent) {
        return new ConfigScreen(parent, style, 0, this.categories.get(0).getName(), this.categories.toArray(new CategoryBuilder[0]));
    }
}
