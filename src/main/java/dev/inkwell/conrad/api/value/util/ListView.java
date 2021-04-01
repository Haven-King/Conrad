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

package dev.inkwell.conrad.api.value.util;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class ListView<T> implements Iterable<T> {
    @SuppressWarnings("rawtypes")
    private static final ListView EMPTY = new ListView<>(Collections.emptyList());

    private final List<T> list;

    public ListView(List<T> list) {
        this.list = list;
    }

    public ListView(Collection<T> collection) {
        this.list = new ArrayList<>(collection);
    }

    public int size() {
        return this.list.size();
    }

    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    public T get(int index) {
        return this.list.get(index);
    }

    public boolean contains(T value) {
        return this.list.contains(value);
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return this.list.iterator();
    }


    @SuppressWarnings("unchecked")
    public static <D> ListView<D> empty() {
        return EMPTY;
    }
}
