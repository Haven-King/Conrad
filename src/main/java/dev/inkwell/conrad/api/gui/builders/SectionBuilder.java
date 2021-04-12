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

package dev.inkwell.conrad.api.gui.builders;

import dev.inkwell.conrad.api.gui.screen.ConfigScreen;
import dev.inkwell.conrad.api.gui.util.Group;
import dev.inkwell.conrad.api.gui.widgets.SpacerComponent;
import dev.inkwell.conrad.api.gui.widgets.WidgetComponent;
import dev.inkwell.conrad.api.gui.widgets.value.SectionHeaderComponent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.Iterator;

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
            WidgetComponent component = new SectionHeaderComponent(parent, contentLeft + contentWidth / 2, offset, contentWidth, 25, this.name, false);
            component.addTooltips(this.tooltips);
            section.add(component);
            offset += component.getHeight();
        }

        for (Iterator<WidgetBuilder> iterator = this.iterator(); iterator.hasNext(); ) {
            WidgetBuilder builder = iterator.next();
            WidgetComponent component = builder.build(parent, contentWidth, contentLeft, offset, index++);
            section.add(component);
            offset += component.getHeight();

            if (iterator.hasNext()) {
                section.add(new SpacerComponent(parent, contentLeft, offset, contentWidth, 7));
                offset += 7;
            }
        }

        return section;
    }

    @FunctionalInterface
    public interface WidgetBuilder {
        WidgetComponent build(ConfigScreen parent, int width, int x, int y, int index);
    }
}
