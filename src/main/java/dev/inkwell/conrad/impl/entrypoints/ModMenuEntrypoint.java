package dev.inkwell.conrad.impl.entrypoints;

import dev.inkwell.conrad.api.ConfigValue;
import dev.inkwell.conrad.impl.ConfigKey;
import dev.inkwell.conrad.impl.EntryBuilderRegistry;
import dev.inkwell.conrad.impl.KeyRing;
import dev.inkwell.conrad.impl.ValueContainer;
import dev.inkwell.vivid.VividConfig;
import dev.inkwell.vivid.builders.CategoryBuilder;
import dev.inkwell.vivid.builders.ConfigScreenBuilderImpl;
import dev.inkwell.vivid.builders.SectionBuilder;
import dev.inkwell.vivid.builders.WidgetComponentBuilder;
import dev.inkwell.vivid.screen.ConfigScreen;
import dev.inkwell.vivid.util.Alignment;
import dev.inkwell.vivid.widgets.LabelComponent;
import dev.inkwell.vivid.widgets.TextButton;
import dev.inkwell.vivid.widgets.WidgetComponent;
import dev.inkwell.vivid.widgets.containers.RowContainer;
import dev.inkwell.vivid.widgets.value.ToggleComponent;
import dev.inkwell.vivid.widgets.value.ValueWidgetComponent;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModMenuEntrypoint implements ModMenuApi {
	private final Map<String, ConfigScreenFactory<?>> builders = new HashMap<>();

	public ModMenuEntrypoint() {
		for (String modId : KeyRing.getMods()) {
			ConfigScreenBuilderImpl builder = new ConfigScreenBuilderImpl();
			builders.put(modId, parent -> new ConfigScreen(parent, builder));

			for (ConfigKey key : KeyRing.getKeys(modId)) {
				Deque<ConfigValue<?>> values = new ArrayDeque<>(KeyRing.getValues(key));

				makeScreenBuilder(
						builder.startCategory(new TranslatableText(key.toString())), values,
						0, null, null
				);
			}
		}
	}
	
	private CategoryBuilder makeScreenBuilder(CategoryBuilder category, Deque<ConfigValue<?>> values, int level, @Nullable SectionBuilder section, @Nullable String sectionName) {
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
					return new RowContainer(parent, x, y, index, true, label, widget);
				});

				continue;
			}

			addEntry(section, value);
		}

		return category;
	}

	private <T> void addEntry(SectionBuilder section, ConfigValue<T> configValue) {
		Consumer<Object> saveConsumer = t -> {
			try {
				ValueContainer.ROOT.put(configValue.getKey(), t,true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		};

		section.add((parent, width, x, y, index) -> {
			WidgetComponent label = new LabelComponent(parent, 0, 0, width / 2, (int) (30 * parent.getScale()), new TranslatableText(configValue.getKey().toString()), true);

			WidgetComponentBuilder<T> builder = EntryBuilderRegistry.get(configValue);
			WidgetComponent widget = builder.build(parent, x, y, width / 2, (int) (30 * parent.getScale()), configValue::getDefaultValue, t -> {}, v -> {
				try {
					ValueContainer.ROOT.put(configValue.getKey(), v, true);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}, configValue.get());
			return new RowContainer(parent, x, y, index, true, label, widget);
		});
	}

//	@Override
//	public ConfigScreenFactory<?> getModConfigScreenFactory() {
//		return screen -> {
//			List<Group<Group<ListEntry>>> categories = new ArrayList<>();
//
//			return new ConfigScreen(screen, categories);
//		};
//	}


	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return null;
	}

	@Override
	public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
		return builders;
	}
}
