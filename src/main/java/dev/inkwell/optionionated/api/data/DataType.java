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

package dev.inkwell.optionionated.api.data;

import dev.inkwell.optionionated.api.util.ListView;
import dev.inkwell.optionionated.api.value.ValueKey;
import dev.inkwell.vivid.api.screen.ScreenStyle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Represent a type of data that can be attached to a specific config value or config definition.
 * See {@link ValueKey#getData(DataType)}.
 */
public class DataType<T> extends StringIdentifiable {
    public static final DataType<String> COMMENT = new DataType<String>("comment") {
        @Override
        public void addLines(ListView<String> data, Consumer<String> consumer) {
            data.forEach(comment -> {
                for (String s : comment.split("\\r?\\n")) {
                    consumer.accept(s);
                }
            });
        }
    };
    public static final DataType<SyncType> SYNC_TYPE = new DataType<>("sync_type");

    @Environment(EnvType.CLIENT)
    public static final DataType<ScreenStyle> SCREEN_STYLE = new DataType<>("screen_style");

    public DataType(@NotNull String name) {
        super(name);
    }

    public void addLines(ListView<T> data, Consumer<String> consumer) {
        // Don't add lines by default
    }
}
