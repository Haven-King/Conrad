package dev.inkwell.conrad.impl.entrypoints;

import dev.inkwell.vivid.entry.base.ListEntry;
import dev.inkwell.vivid.screen.ConfigScreen;
import dev.inkwell.vivid.util.Group;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;

import java.util.ArrayList;
import java.util.List;

public class ModMenuEntrypoint implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return screen -> {
			List<Group<Group<ListEntry>>> categories = new ArrayList<>();

//			for (int i = 0; i < 5; ++i) {
//				Group<Group<ListEntry>> category = new Group<>(new LiteralText("Category " + i));
//
//				for (int j = 0; j < 5; ++j) {
//					Group<ListEntry> section = new Group<>(new LiteralText("Section " + i + "." + j));
//
//					for (int k = 0; k < 5; ++k) {
//						section.add(new IntegerEntry(new LiteralText("Value " + i + "." + j + "." + k), () -> 0, 0));
//					}
//
//					section.add(new ExternalButtonEntry(new LiteralText("External"), parent -> MinecraftClient.getInstance().openScreen(new ConfigScreen(parent, categories))));
//					section.add(new ExternalButtonEntry(new LiteralText("External"), parent -> MinecraftClient.getInstance().openScreen(new ConfigScreen(parent, categories))));
//
//					category.add(section);
//				}
//
//				categories.add(category);
//			}

			return new ConfigScreen(screen, categories);
		};
	}
}
