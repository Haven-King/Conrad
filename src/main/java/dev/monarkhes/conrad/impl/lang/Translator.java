package dev.monarkhes.conrad.impl.lang;

import dev.monarkhes.conrad.impl.Conrad;
import dev.monarkhes.conrad.impl.util.ConradException;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.StringVisitable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for translating things on the dedicated server and before there is an instance of {@link MinecraftClient} available.
 */
public class Translator {
    private static final Pattern ARG_FORMAT = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");
    private static final StringVisitable NULL_ARGUMENT = StringVisitable.plain("null");

    private static final Map<String, Language> LANGUAGES = new HashMap<>();

    public static void init() {
        FabricLoader.getInstance().getAllMods().forEach(modContainer -> {
            try {
                Files.walk(modContainer.getPath("assets")).forEach(path -> {
                    if (path.toString().matches(".*lang.[a-z_]+(\\.json)$")) {
                        String langCode = path.getFileName().toString().split("\\.")[0];
                        try {
                            LANGUAGES.computeIfAbsent(langCode, Language::new).add(modContainer.getMetadata().getId(), path);
                        } catch (IOException e) {
                            Conrad.LOGGER.error(e.getMessage());
                        }
                    }
                });
            } catch (IOException e) {
                Conrad.LOGGER.error(e.getMessage());
            }
        });
    }

    private static @NotNull String getLanguage() {
        FabricLoader loader = FabricLoader.getInstance();
        Path gameDir = loader.getGameDir().normalize();
        if (loader.getEnvironmentType() == EnvType.CLIENT) {
            try {
                for (String line : Files.readAllLines(gameDir.resolve("options.txt"))) {
                    if (line.matches("lang:[a-z_]+")) {
                        return line.split(":", 2)[1];
                    }
                }
            } catch (IOException e) {
                Conrad.LOGGER.error("Error loading lang options on client: {}", e.getMessage());
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
                    Conrad.LOGGER.error("Error loading lang options on server: {}", e.getMessage());
                }
            } else {
                try {
                    Files.write(options, "lang:en_us".getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    Conrad.LOGGER.error("Error saving lang options on server: {}", e.getMessage());
                }
            }
        }

        return "en_us";
    }

    public static @Nullable String translate(@NotNull String key, Object... args) {
        String lang = getLanguage();

        @Nullable String translation = LANGUAGES.getOrDefault(lang, LANGUAGES.get(lang)).translate(key);

        if (translation == null) return null;

        Matcher matcher = ARG_FORMAT.matcher(translation);

        try {
            int i = 0;

            int j;
            int l;

            StringBuilder builder = new StringBuilder();

            for(j = 0; matcher.find(j); j = l) {
                int k = matcher.start();
                l = matcher.end();
                String string2;
                if (k > j) {
                    string2 = translation.substring(j, k);
                    if (string2.indexOf(37) != -1) {
                        throw new IllegalArgumentException();
                    }

                    builder.append(string2);
                }

                string2 = matcher.group(2);
                String string3 = translation.substring(k, l);
                if ("%".equals(string2) && "%%".equals(string3)) {
                    builder.append("%");
                } else {
                    if (!"s".equals(string2)) {
                        throw new ConradException("Unsupported format: '" + string3 + "'");
                    }

                    String string4 = matcher.group(1);
                    int m = string4 != null ? Integer.parseInt(string4) - 1 : i++;
                    if (m < args.length) {
                        builder.append(getArg(key, args, m));
                    }
                }
            }

            if (j < translation.length()) {
                String string = translation.substring(j);
                if (string.indexOf(37) != -1) {
                    throw new IllegalArgumentException();
                }

                builder.append(string);
            }

            return builder.toString();
        } catch (IllegalArgumentException var11) {
            throw new ConradException(var11);
        }
    }

    private static String getArg(@NotNull String key, Object[] args, int index) {
        if (index >= args.length) {
            throw new ConradException("Failed to translate key \"%s\": couldn't get arg %d from args %s", key, index, args);
        } else {
            Object object = args[index];
            return object == null ? "null" : object.toString();
        }
    }

    public static @NotNull Collection<String> getTooltips(String key) {
        return LANGUAGES.get(getLanguage()).getTooltips(key);
    }
}
