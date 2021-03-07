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

package dev.inkwell.vivian.api.util;

import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Group<T> implements Iterable<T> {
    protected final MutableText name;
    protected final List<Text> tooltips = new ArrayList<>();
    private final List<T> list = new ArrayList<>();
    private final Set<T> set = new HashSet<>();

    public Group(MutableText name) {
        this.name = name;
    }

    public Group() {
        this(new LiteralText(""));
    }

    @SafeVarargs
    public final Group<T> add(T... members) {
        for (T member : members) {
            if (!set.contains(member)) {
                list.add(member);
                set.add(member);
            }
        }

        return this;
    }

    public final Group<T> add(Text tooltip) {
        this.tooltips.add(tooltip);

        return this;
    }

    public final Group<T> addAll(Collection<Text> tooltips) {
        this.tooltips.addAll(tooltips);

        return this;
    }

    public MutableText getName() {
        return this.name;
    }

    public int size() {
        return list.size();
    }

    public T get(int index) {
        return list.get(index);
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    public List<Text> getTooltips() {
        return this.tooltips;
    }
}
