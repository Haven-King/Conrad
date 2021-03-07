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

package dev.inkwell.vivid.api.builders;

import dev.inkwell.vivid.api.screen.ConfigScreen;
import dev.inkwell.vivid.api.util.Group;
import dev.inkwell.vivid.api.widgets.WidgetComponent;
import dev.inkwell.vivid.api.widgets.value.SectionHeaderComponent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class SectionBuilder extends Group<SectionBuilder.WidgetBuilder> {
    public SectionBuilder(MutableText title) {
        super(title);
    }

    public SectionBuilder addTooltip(Text tooltip) {
        this.add(tooltip);

        return this;
    }

    public Group<WidgetComponent> build(ConfigScreen parent, int contentLeft, int contentWidth, int y, Integer index) {
        Group<WidgetComponent> section = new Group<>(this.name);
        section.addAll(this.tooltips);

        int offset = y;

        if (!this.getName().getString().isEmpty()) {
            WidgetComponent component = new SectionHeaderComponent(parent, contentLeft, offset, contentWidth, (int) (45 * parent.getScale()), this.name, false).withColor(parent.getStyle().sectionColor);
            component.addTooltips(this.tooltips);
            section.add(component);
            offset += component.getHeight();
        }

        for (WidgetBuilder builder : this) {
            WidgetComponent component = builder.build(parent, contentWidth, contentLeft, offset, index++);
            section.add(component);
            offset += component.getHeight();
        }

        return section;
    }

    @FunctionalInterface
    public interface WidgetBuilder {
        WidgetComponent build(ConfigScreen parent, int width, int x, int y, int index);
    }
}
