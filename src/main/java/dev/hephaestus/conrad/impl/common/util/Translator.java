package dev.hephaestus.conrad.impl.common.util;

import dev.hephaestus.conrad.impl.client.util.ClientUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Language;

public class Translator {
	public static String translate(TranslatableText text) {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			return ClientUtil.translate(text);
		} else {
			return Language.getInstance().get(text.getKey());
		}
	}
}
