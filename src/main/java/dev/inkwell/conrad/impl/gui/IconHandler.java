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

package dev.inkwell.conrad.impl.gui;

import dev.inkwell.conrad.impl.Conrad;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class IconHandler {
    private static final Logger LOGGER = LogManager.getLogger("Conrad | IconHandler");
    private static final Map<String, Identifier> CACHE = new HashMap<>();

    private IconHandler() {

    }

    public static @Nullable Identifier getIcon(ModContainer modContainer, ModMetadata metadata) {
        return CACHE.computeIfAbsent(metadata.getId(), modId -> {
            Optional<String> path = metadata.getIconPath(Integer.MAX_VALUE);

            if (path.isPresent()) {
                NativeImageBackedTexture texture = getIcon(modContainer, path.get());

                if (texture != null && texture.getImage() != null) {
                    Identifier iconId = Conrad.id(modId + "_icon");
                    MinecraftClient.getInstance().getTextureManager().registerTexture(iconId, texture);

                    return iconId;
                }
            }

            return null;
        });
    }

    public static @Nullable NativeImageBackedTexture getIcon(ModContainer iconSource, String iconPath) {
        try {
            Path path = iconSource.getPath(iconPath);
            try (InputStream inputStream = Files.newInputStream(path)) {
                NativeImage image = NativeImage.read(Objects.requireNonNull(inputStream));
                Validate.validState(image.getHeight() == image.getWidth(), "Icon must be square");
                return new NativeImageBackedTexture(image);
            }

        } catch (Throwable t) {
            if (!iconPath.equals("assets/" + iconSource.getMetadata().getId() + "/icon.png")) {
                LOGGER.error("Invalid mod icon for icon source {}: {}", iconSource.getMetadata().getId(), iconPath, t);
            }

            return null;
        }
    }
}
