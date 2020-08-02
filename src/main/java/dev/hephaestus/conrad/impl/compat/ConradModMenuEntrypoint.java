package dev.hephaestus.conrad.impl.compat;

import dev.hephaestus.conrad.api.Config;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.TranslatableText;

import java.util.ArrayList;
import java.util.List;

public class ConradModMenuEntrypoint implements ConfigScreenFactory<Screen> {
	private final String modid;
	private final List<Config> configs = new ArrayList<>();

	public ConradModMenuEntrypoint(String modid) {
		this.modid = modid;
	}

	public void add(Config config) {
		this.configs.add(config);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Screen create(Screen screen) {
		ConfigBuilder builder = ConfigBuilder.create();
		builder.setParentScreen(screen);
		builder.setTitle(new TranslatableText("conrad." + this.modid));

		for (Config config : this.configs) {
			builder.getOrCreateCategory(new TranslatableText("conrad." + this.modid + "." + config.getClass().toString()));
		}

		return builder.build();
	}
}
