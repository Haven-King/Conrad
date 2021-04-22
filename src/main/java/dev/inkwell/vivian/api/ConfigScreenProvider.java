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

package dev.inkwell.vivian.api;

import dev.inkwell.conrad.impl.gui.ConfigScreenProviderImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

import java.util.Iterator;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public final class ConfigScreenProvider {
    private ConfigScreenProvider() {

    }

    public static Screen getScreen(String modId) {
        return getScreen(modId, MinecraftClient.getInstance().currentScreen);
    }

    public static Screen getScreen(String modId, Screen parent) {
        return ConfigScreenProviderImpl.get(modId, parent);
    }

    public static void open(String modId) {
        Screen screen = ConfigScreenProviderImpl.get(modId, MinecraftClient.getInstance().currentScreen);

        if (screen != null) {
            MinecraftClient.getInstance().openScreen(screen);
        }
    }

    public static void forEach(BiConsumer<String, Function<Screen, ? extends Screen>> consumer) {
        ConfigScreenProviderImpl.forEach(consumer);
    }

    public static Iterator<Map.Entry<String, Function<Screen, ? extends Screen>>> getFactories() {
        return ConfigScreenProviderImpl.getFactories();
    }
}
