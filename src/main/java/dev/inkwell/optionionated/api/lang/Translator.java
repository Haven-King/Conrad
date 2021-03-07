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

package dev.inkwell.optionionated.api.lang;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for translating things on the dedicated server and before there is an instance of {@link MinecraftClient} available.
 */
public class Translator {
    private static final Logger LOGGER = LogManager.getLogger("Fabric|Translator");

    private static final Map<String, Language> LANGUAGES = new HashMap<>();

    static {
        FabricLoader.getInstance().getAllMods().forEach(modContainer -> {
            try {
                if (!Files.exists(modContainer.getPath("assets"))) return;

                Files.walk(modContainer.getPath("assets")).forEach(path -> {
                    if (path.toString().matches(".*lang.[a-z_]+(\\.json)$")) {
                        String langCode = path.getFileName().toString().split("\\.")[0];

                        try {
                            LANGUAGES.computeIfAbsent(langCode, langCode1 -> new Language()).add(modContainer.getMetadata().getId(), path);
                        } catch (IOException e) {
                            LOGGER.error(e.getMessage());
                        }
                    }
                });
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        });
    }

    private static @NotNull String getLanguage() {
        FabricLoader loader = FabricLoader.getInstance();
        Path gameDir = loader.getGameDir().normalize();

        if (loader.getEnvironmentType() == EnvType.CLIENT) {
            try {
                Path path = gameDir.resolve("options.txt");

                if (Files.exists(path)) {
                    for (String line : Files.readAllLines(path)) {
                        if (line.matches("lang:[a-z_]+")) {
                            return line.split(":", 2)[1];
                        }
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Error loading lang options on client: {}", e.getMessage());
            }
        } else {
            Path options = gameDir.resolve("lang.txt");

            if (Files.exists(options)) {
                try {
                    for (String line : Files.readAllLines(gameDir.resolve("lang.txt"))) {
                        if (line.matches("lang:[a-z_]+")) {
                            return line.split(":", 2)[1];
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error("Error loading lang options on server: {}", e.getMessage());
                }
            } else {
                try {
                    Files.write(options, "lang:en_us".getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    LOGGER.error("Error saving lang options on server: {}", e.getMessage());
                }
            }
        }

        return "en_us";
    }

    public static @NotNull Collection<String> getComments(String key) {
        return LANGUAGES.get(getLanguage()).getComments(key);
    }
}
