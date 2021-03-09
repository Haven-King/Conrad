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

package dev.inkwell.conrad.api.value.data;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class StringIdentifiable {
    protected final String name;

    public StringIdentifiable(@NotNull String name) {
        this.name = name;
    }

    public final String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public void addLines(Consumer<String> stringConsumer) {
        stringConsumer.accept(this.toString());
    }
}
