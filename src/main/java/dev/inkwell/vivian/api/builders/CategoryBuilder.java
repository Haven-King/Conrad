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

import dev.inkwell.vivian.api.context.WidgetComponentFactory;
import dev.inkwell.vivian.api.screen.ConfigScreen;
import dev.inkwell.vivian.api.util.Group;
import dev.inkwell.vivian.api.widgets.SpacerComponent;
import dev.inkwell.vivian.api.widgets.WidgetComponent;
import dev.inkwell.vivian.api.widgets.value.SectionHeaderComponent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class CategoryBuilder extends Group<WidgetComponentFactory> {
    private Runnable saveCallback = () -> {
    };

    private BooleanSupplier condition = () -> true;

    public CategoryBuilder(MutableText name) {
        super(name);
    }

    public CategoryBuilder addTooltip(Text tooltip) {
        this.add(tooltip);

        return this;
    }

    public void addSection(Text name, List<Text> tooltips) {
        this.add((screen, x, y, width, consumer) -> {
            WidgetComponent component = new SectionHeaderComponent(screen, x, y, width, 25, name).withTooltips(tooltips);
            consumer.accept(component);
            return component.getHeight();
        });
    }

    public void addSection(Text name, Text... tooltips) {
        this.add((screen, x, y, width, consumer) -> {
            WidgetComponent component = new SectionHeaderComponent(screen, x, y, width, 25, name).withTooltips(tooltips);
            consumer.accept(component);
            return component.getHeight();
        });
    }

    public CategoryBuilder setSaveCallback(Runnable saveCallback) {
        this.saveCallback = saveCallback;
        return this;
    }

    public void setCondition(BooleanSupplier condition) {
        this.condition = condition;
    }

    public boolean shouldShow() {
        return this.condition.getAsBoolean();
    }

    public void save() {
        this.saveCallback.run();
    }

    public int build(ConfigScreen parent, int x, int width, int y, Consumer<WidgetComponent> consumer) {
        for (int i = 0; i < this.size(); ++i) {
            consumer.accept(new SpacerComponent(parent, x, y, width, 10));
            y += 5;

            y += this.get(i).build(parent, x, y, width, consumer);
        }

        return y;
    }
}
