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

package dev.inkwell.vivian.api.constraints;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Bounded<T extends Number> extends Constraint {
    boolean isWithinBounds(T value);

    @Nullable T getMin();

    void setMin(@Nullable T min);

    @Nullable T getMax();

    void setMax(@Nullable T max);

    default void setBounds(T min, T max) {
        setMin(min);
        setMax(max);
    }

    default void addConstraintTooltips(List<Text> tooltips) {
        if (getMin() != null) {
            tooltips.add(new TranslatableText("vivian.constraint.min", getMin()));
        }

        if (getMax() != null) {
            tooltips.add(new TranslatableText("vivian.constraint.max", getMax()));
        }
    }
}
