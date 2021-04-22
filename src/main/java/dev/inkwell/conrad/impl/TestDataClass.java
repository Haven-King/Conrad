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

package dev.inkwell.conrad.impl;

import dev.inkwell.conrad.api.value.util.Array;
import dev.inkwell.conrad.api.value.util.Table;

public class TestDataClass {
    public int test1 = 10;
    public Table<Inner> testTable = new Table<>(Inner.class, Inner::new);
    private final int test2 = 30;
    private final String test3 = "new horizons";

    public static class Inner {
        public int test1 = 1;
        public double test3 = 30;
        public Array<String> testArray = new Array<>(String.class, () -> "NEW", "ABC", "DEF", "GHI");
    }
}
