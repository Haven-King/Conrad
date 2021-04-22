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

package dev.inkwell.vivian.api.context;

import dev.inkwell.vivian.api.screen.ConfigScreen;
import dev.inkwell.vivian.api.widgets.WidgetComponent;

import java.util.function.Consumer;

@FunctionalInterface
public interface WidgetComponentFactory {
    /**
     * @param screen   the screen widgets are being added to
     * @param x        the leftmost edge of the widget list
     * @param y        the end of the widget list thus far
     * @param width    the width of the area that widgets should fill
     * @param consumer Any build widgets should be added here
     * @return Vertical distance to move subsequent widgets down by
     */
    int build(ConfigScreen screen, int x, int y, int width, Consumer<WidgetComponent> consumer);
}
