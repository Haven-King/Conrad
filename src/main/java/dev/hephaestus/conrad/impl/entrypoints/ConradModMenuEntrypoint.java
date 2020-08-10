package dev.hephaestus.conrad.impl.entrypoints;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.Tooltip;
import me.shedaniel.clothconfig2.gui.AbstractConfigScreen;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import me.shedaniel.math.Point;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.api.Conrad;
import dev.hephaestus.conrad.impl.client.WidgetProviderRegistry;
import dev.hephaestus.conrad.impl.config.RootConfigManager;
import dev.hephaestus.conrad.impl.data.ConfigSerializer;
import dev.hephaestus.conrad.impl.data.NetworkedConfigSerializer;
import dev.hephaestus.conrad.mixin.server.MinecraftServerAccessor;

public class ConradModMenuEntrypoint implements ConfigScreenFactory<Screen> {
	private final String modid;

	public ConradModMenuEntrypoint(String modid) {
		this.modid = modid;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Screen create(Screen screen) {
		ConfigBuilder builder = ConfigBuilder.create();
		builder.setParentScreen(screen);
		builder.setTitle(new TranslatableText("conrad." + this.modid));

		List<Config> configs = new ArrayList<>();

		for (Config rootConfig : RootConfigManager.INSTANCE.getConfigs(this.modid)) {
			Config.SaveType.Type saveType = rootConfig.getClass().getAnnotation(Config.SaveType.class).value();

			if (saveType == Config.SaveType.Type.CLIENT
					|| MinecraftClient.getInstance().isIntegratedServerRunning()
					|| MinecraftClient.getInstance().getCurrentServerEntry() == null
					|| (saveType == Config.SaveType.Type.LEVEL && MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().player.hasPermissionLevel(4))
			) {
				Config config = Conrad.getConfig(rootConfig.getClass());

				ConfigCategory category = builder.getOrCreateCategory(new TranslatableText("conrad." + this.modid + "." + config.getClass().getSimpleName()));

				Text[] tooltips;

				if (saveType == Config.SaveType.Type.CLIENT) {
					tooltips = new Text[1];
				} else {
					if (MinecraftClient.getInstance().world == null) {
						tooltips = new Text[3];
						tooltips[1] = new TranslatableText("conrad.saveType.default.0");
						tooltips[2] = new TranslatableText("conrad.saveType.default.1");
						((MutableText) tooltips[2]).styled((style -> style.withColor(Formatting.GRAY).withItalic(true)));
					} else {
						tooltips = new Text[2];

						if (MinecraftClient.getInstance().isIntegratedServerRunning() && MinecraftClient.getInstance().getServer() != null) {
							tooltips[1] = new TranslatableText("conrad.saveType.level", ((MinecraftServerAccessor) MinecraftClient.getInstance().getServer()).getSession().getDirectoryName());
						} else if (MinecraftClient.getInstance().getCurrentServerEntry() != null) {
							tooltips[1] = new TranslatableText("conrad.saveType.server", MinecraftClient.getInstance().getCurrentServerEntry().name);
						} else {
							tooltips[1] = LiteralText.EMPTY;
						}
					}

					((MutableText) tooltips[1]).styled((style -> style.withColor(Formatting.GRAY).withItalic(true)));
				}

				tooltips[0] = new TranslatableText("conrad.environment." + saveType.name().toLowerCase()).styled(style -> style.withColor(Formatting.YELLOW));

				category.setTooltipSupplier(() -> Optional.of(tooltips));

				processTopLevelConfig("conrad." + this.modid + config.getClass().getSimpleName(), config, builder, category);
				configs.add(config);
			}
		}

		builder.setSavingRunnable(() -> configs.forEach(config -> {
			ConfigSerializer.getInstance(config.getClass().getAnnotation(Config.SaveType.class).value()).serialize(config);

			if (config.getClass().getAnnotation(Config.SaveType.class).value() == Config.SaveType.Type.CLIENT
					&& MinecraftClient.getInstance().getCurrentServerEntry() != null
			) {
				NetworkedConfigSerializer.INSTANCE.serialize(config);
			}
		}));

		AbstractConfigScreen configScreen = (AbstractConfigScreen) builder.build();

		configScreen.addTooltip(Tooltip.of(new Point(10, 10), new LiteralText("WOAH")));

		return configScreen;
	}

	@Environment(EnvType.CLIENT)
	private void processTopLevelConfig(String root, Config config, ConfigBuilder builder, ConfigCategory category) {
		for (Field field : config.getClass().getDeclaredFields()) {
			String key = root + "." + field.getName();

			if (Config.class.isAssignableFrom(field.getType())) {
				SubCategoryBuilder subCategoryBuilder = builder.entryBuilder().startSubCategory(new TranslatableText(key));

				try {
					processNestedConfig(key, (Config) field.get(config), builder, subCategoryBuilder);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}

				category.addEntry(subCategoryBuilder.build());
			} else {
				AbstractConfigListEntry<?> entry = (field.isAnnotationPresent(Config.Entry.Widget.class)
						? WidgetProviderRegistry.get(field.getAnnotation(Config.Entry.Widget.class).value())
						: WidgetProviderRegistry.get(field.getType())).getWidget(builder.entryBuilder(), key, config, field);

				entry.setRequiresRestart(field.isAnnotationPresent(Config.Entry.RequiresRestart.class));

				category.addEntry(entry);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	private void processNestedConfig(String root, Config config, ConfigBuilder builder, SubCategoryBuilder category) {
		for (Field field : config.getClass().getDeclaredFields()) {
			String key = root + "." + field.getName();

			if (Config.class.isAssignableFrom(field.getType())) {
				SubCategoryBuilder subCategoryBuilder = builder.entryBuilder().startSubCategory(new TranslatableText(key));

				try {
					processNestedConfig(key, (Config) field.get(config), builder, subCategoryBuilder);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}

				category.add(subCategoryBuilder.build());
			} else {
				AbstractConfigListEntry<?> entry = (field.isAnnotationPresent(Config.Entry.Widget.class)
						? WidgetProviderRegistry.get(field.getAnnotation(Config.Entry.Widget.class).value())
						: WidgetProviderRegistry.get(field.getType())).getWidget(builder.entryBuilder(), key, config, field);

				entry.setRequiresRestart(field.isAnnotationPresent(Config.Entry.RequiresRestart.class));

				category.add(entry);
			}
		}
	}
}
