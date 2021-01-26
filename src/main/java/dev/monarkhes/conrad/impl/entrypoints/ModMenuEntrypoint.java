package dev.monarkhes.conrad.impl.entrypoints;

import dev.monarkhes.conrad.api.ConfigValue;
import dev.monarkhes.conrad.impl.ConfigKey;
import dev.monarkhes.conrad.impl.client.EntryBuilderRegistry;
import dev.monarkhes.conrad.impl.KeyRing;
import dev.monarkhes.conrad.impl.value.ValueContainer;
import dev.monarkhes.conrad.impl.lang.Translator;
import dev.monarkhes.vivid.builders.CategoryBuilder;
import dev.monarkhes.vivid.builders.ConfigScreenBuilderImpl;
import dev.monarkhes.vivid.builders.SectionBuilder;
import dev.monarkhes.vivid.builders.WidgetComponentBuilder;
import dev.monarkhes.vivid.screen.ConfigScreen;
import dev.monarkhes.vivid.util.Alignment;
import dev.monarkhes.vivid.widgets.LabelComponent;
import dev.monarkhes.vivid.widgets.TextButton;
import dev.monarkhes.vivid.widgets.WidgetComponent;
import dev.monarkhes.vivid.widgets.containers.RowContainer;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ModMenuEntrypoint implements ModMenuApi {
	private final Map<String, ConfigScreenFactory<?>> builders = new HashMap<>();

	public ModMenuEntrypoint() {
		for (String modId : KeyRing.getMods()) {
			ConfigScreenBuilderImpl builder = new ConfigScreenBuilderImpl();
			builders.put(modId, parent -> new ConfigScreen(parent, builder));

			for (ConfigKey key : KeyRing.getKeys(modId)) {
				Deque<ConfigValue<?>> values = new ArrayDeque<>(KeyRing.getValues(key));

				CategoryBuilder category = builder.startCategory(new TranslatableText(key.toString()));

				Translator.getTooltips(key.toString()).forEach(string -> {
					category.add(new LiteralText(string));
				});

				makeScreenBuilder(category, values, 0, null, null);
			}
		}
	}
	
	private CategoryBuilder makeScreenBuilder(CategoryBuilder category, Deque<ConfigValue<?>> values, int level, @Nullable SectionBuilder section, @Nullable String sectionName) {
		category.setSaveCallback(Networking::sendConfigValues);

		while (!values.isEmpty()) {
			ConfigValue<?> value = values.pop();

			if (value.getKey().getPath().length < level + 2) {
				values.push(value);
				break;
			}

			if (section == null && value.getKey().getPath().length == level + 2) {
				section = category.addSection(new LiteralText(""));
				sectionName = value.getKey().getParent().toString();
			} else if (value.getKey().getPath().length == level + 3 && (sectionName == null || !sectionName.equals(value.getKey().getParent().toString()))) {
				section = category.addSection(new TranslatableText(value.getKey().getParent().toString()));
				@Nullable SectionBuilder finalSection = section;

				if (section != null) {
					Translator.getTooltips(value.getKey().getParent().toString()).forEach(string -> {
						finalSection.add(new LiteralText(string));
					});
				}

				sectionName = value.getKey().getParent().toString();
			} else if (value.getKey().getPath().length > level + 3) {
				if (section == null) {
					section = category.addSection(new LiteralText(""));
					sectionName = value.getKey().getParent().toString();
				}

				ConfigKey key = value.getKey().getParent();

				ConfigScreenBuilderImpl builder = new ConfigScreenBuilderImpl();
				CategoryBuilder innerCategory = builder.startCategory(new TranslatableText(key.toString()));

				SectionBuilder innerSection = innerCategory.addSection(new LiteralText(""));
				String innerSectionName = "";

				addEntry(innerSection, value);

				makeScreenBuilder(innerCategory, values, level + 2, innerSection, innerSectionName);

				section.add((parent, width, x, y, index) -> {
					WidgetComponent label = new LabelComponent(parent, 0, 0, width / 2, (int) (30 * parent.getScale()), new TranslatableText(key.toString()), true);
					WidgetComponent widget = new TextButton(parent, 0, 0, width / 2, (int) (30 * parent.getScale()), 0, new LiteralText("▶"), Alignment.RIGHT, button -> {MinecraftClient.getInstance().openScreen(new ConfigScreen(button.getParent(), builder)); return true;});
					WidgetComponent component = new RowContainer(parent, x, y, index, true, label, widget);
					component.addTooltips(Translator.getTooltips(key.toString()).stream().map(LiteralText::new).collect(Collectors.toList()));
					return component;
				});

				continue;
			}

			if (section != null) {
				addEntry(section, value);
			}
		}

		return category;
	}

	private <T> void addEntry(SectionBuilder section, ConfigValue<T> configValue) {
		Consumer<T> saveConsumer = t -> {
			try {
				ValueContainer.getInstance(KeyRing.getRootConfig(configValue.getKey()).saveType()).put(configValue.getKey(), t);
			} catch (IOException e) {
				e.printStackTrace();
			}
		};

		section.add((parent, width, x, y, index) -> {
			WidgetComponent label = new LabelComponent(parent, 0, 0, width / 2, (int) (30 * parent.getScale()), new TranslatableText(configValue.getKey().toString()), true);

			WidgetComponentBuilder<T> builder = EntryBuilderRegistry.get(configValue);
			WidgetComponent widget = builder.build(parent, x, y, width / 2, (int) (30 * parent.getScale()), configValue::getDefaultValue, t -> {}, saveConsumer, configValue.get());

			WidgetComponent component = new RowContainer(parent, x, y, index, true, label, widget);
			component.addTooltips(Translator.getTooltips(configValue.getKey().toString()).stream().map(LiteralText::new).collect(Collectors.toList()));

			return component;
		});
	}

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return null;
	}

	@Override
	public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
		return builders;
	}
}
