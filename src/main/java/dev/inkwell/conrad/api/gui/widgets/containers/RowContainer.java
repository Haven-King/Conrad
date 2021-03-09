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

package dev.inkwell.conrad.api.gui.widgets.containers;

import dev.inkwell.conrad.api.gui.screen.ConfigScreen;
import dev.inkwell.conrad.api.gui.widgets.WidgetComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class RowContainer extends ComponentContainer {
    public RowContainer(ConfigScreen parent, int x, int y, int index, boolean shouldRenderHighlight, @NotNull WidgetComponent child, WidgetComponent... children) {
        super(parent, x, y, index, shouldRenderHighlight, child, children);
    }

    @Override
    protected void init() {
        for (WidgetComponent thing : this.children) {
            thing.setX(this.x + this.width);
            thing.setY(this.y);

            if (thing instanceof ComponentContainer) {
                ((ComponentContainer) thing).init();
            }

            this.width = this.width + thing.getWidth();
            this.height = Math.max(this.height, thing.getHeight());
        }
    }

    @Override
    public void renderBackground(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        for (int column = 0; column < this.children.size(); ++column) {
            int color = this.index % 2 == 0
                    ? column % 2 == 0 ? 0x40FFFFFF : 0x30FFFFFF
                    : column % 2 == 0 ? 0x30FFFFFF : 0x20FFFFFF;

            WidgetComponent child = this.children.get(column);
            DrawableHelper.fill(matrixStack, child.getX(), child.getY(), child.getX() + child.getWidth(), child.getY() + child.getHeight(), color);
        }
    }
}
