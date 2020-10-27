package dev.hephaestus.conrad.impl.common.util;

import dev.hephaestus.jankson.Jankson;
import dev.hephaestus.jankson.JsonElement;
import dev.hephaestus.jankson.JsonObject;
import dev.hephaestus.jankson.JsonPrimitive;
import dev.hephaestus.jankson.api.SyntaxError;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.TranslatableText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Translator {
	private static final HashMap<String, HashMap<String, String>> TRANSLATIONS = new HashMap<>();
	private static final Jankson JANKSON = new Jankson();
	private static String LANG_CODE = "en_us";

	public static String getLangCode() {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT && MinecraftClient.getInstance() != null) {
			LANG_CODE = MinecraftClient.getInstance().getLanguageManager().getLanguage().getCode();
		}

		return LANG_CODE;
	}

	public static String translate(TranslatableText text) {
		Map<String, String> map = TRANSLATIONS.get(text.getKey());
		String langCode = getLangCode();

		if (map == null) {
			return text.getKey();
		} else if (map.containsKey(langCode)) {
			return map.get(langCode);
		} else {
			return map.getOrDefault("en_us", text.getKey());
		}
	}

	public static void init(String modid) {
		ModContainer modContainer = FabricLoader.getInstance().getModContainer(modid).get();
		Path langFolder = modContainer.getPath("assets/" + modid + "/lang");

		try {
			for (Path langFile : Files.newDirectoryStream(langFolder)) {
				String langCode = langFile.getFileName().toString().split("\\.")[0];
				JsonObject langObject = JANKSON.load(Files.newInputStream(langFile));
				for (Map.Entry<String, JsonElement> langPair : langObject.entrySet()) {
					TRANSLATIONS.computeIfAbsent(langPair.getKey(), key -> new HashMap<>()).put(langCode, ((JsonPrimitive) langPair.getValue()).asString());
				}
			}
		} catch (IOException | SyntaxError e) {
			e.printStackTrace();
		}
	}

	static {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			Path optionsFile = FabricLoader.getInstance().getGameDir().normalize().resolve("options.txt");

			if (Files.exists(optionsFile)) {
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(optionsFile)));

					reader.lines().forEach(line -> {
						String[] option = line.split(":");
						if (option[0].equals("lang")) {
							LANG_CODE = option[1];
						}
					});

					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
