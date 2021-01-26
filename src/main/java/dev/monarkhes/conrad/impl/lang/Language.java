package dev.monarkhes.conrad.impl.lang;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.monarkhes.conrad.impl.Conrad;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Language {
    private final String langCode;
    private final Map<String, String> byTranslationKey = new HashMap<>();
    private final Map<String, Map<String, String>> byModId = new HashMap<>();
    private final Multimap<String, String> tooltips = LinkedHashMultimap.create();

    public Language(String langCode) {
        this.langCode = langCode;
    }

    public void add(String mod, Path languageFile) throws IOException {
        JsonElement json = new JsonParser().parse(Files.newBufferedReader(languageFile));

        if (!json.isJsonObject()) {
            Conrad.LOGGER.error("Error parsing language file \"{}\": not a JSON object.", languageFile.toString());
            return;
        }

        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
            if (!entry.getValue().isJsonPrimitive() || !entry.getValue().getAsJsonPrimitive().isString()) {
                Conrad.LOGGER.error("Error parsing language file \"{}\": non-string value \"{}\".", languageFile.toString(), entry.getKey());
                continue;
            }

            String key = entry.getKey();
            String translation = entry.getValue().getAsString();
            this.byTranslationKey.put(key, translation);
            this.byModId.computeIfAbsent(mod, id -> new HashMap<>()).put(key, translation);

            if (key.matches(".*\\.tooltip.*")) {
                key = key.split("\\.tooltip", 2)[0];
                tooltips.put(key, translation);
            }
        }
    }

    public @Nullable String translate(@NotNull String key) {
        return this.byTranslationKey.get(key);
    }

    public @NotNull Collection<String> getTooltips(@NotNull String key) {
        return this.tooltips.get(key);
    }
}
