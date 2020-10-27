package dev.hephaestus.conrad.impl.client;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.api.gui.FieldBuilderProviderRegistry;
import dev.hephaestus.conrad.impl.common.config.*;
import dev.hephaestus.conrad.impl.common.util.ConradUtil;
import dev.hephaestus.conrad.mixin.server.MinecraftServerAccessor;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import dev.hephaestus.clothy.api.ConfigBuilder;
import dev.hephaestus.clothy.api.ConfigCategory;
import dev.hephaestus.clothy.api.EntryContainer;
import dev.hephaestus.clothy.impl.builders.SubCategoryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.*;

@Environment(EnvType.CLIENT)
public class ConradScreenFactory implements ConfigScreenFactory<Screen> {
	private static final Text NEW_LINE = new LiteralText(" ");

	private final String modId;

	public ConradScreenFactory(String modId) {
		this.modId = modId;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Screen create(Screen screen) {
		ConfigBuilder builder = ConfigBuilder.create();
		builder.setTitle(new TranslatableText("conrad.title." + this.modId));

		for (ConfigKey configKey : KeyRing.entries(this.modId)) {
			ConfigDefinition configDefinition = KeyRing.get(configKey);

			if (configDefinition.isRoot()
					&& (
						configDefinition.getSaveType() == Config.SaveType.USER
						|| MinecraftClient.getInstance().player == null // This means we're on the title screen
						|| MinecraftClient.getInstance().player.hasPermissionLevel(4))) { // Ops can change configs
				Config.SaveType saveType = configDefinition.getSaveType();
				ConfigCategory entryContainer = builder.getOrCreateCategory(of(configKey));

				ArrayList<Text> tooltips = new ArrayList<>();
				tooltips.add(new TranslatableText("conrad.environment." + saveType.name().toLowerCase()).styled(style -> style.withColor(Formatting.YELLOW)));

				MutableText saveTypeText;
				if (saveType == Config.SaveType.USER) {
					saveTypeText = null;
				} else {
					if (MinecraftClient.getInstance().world == null) {
						saveTypeText = new TranslatableText("conrad.saveType.default");
					} else {
						if (MinecraftClient.getInstance().isIntegratedServerRunning() && MinecraftClient.getInstance().getServer() != null) {
							saveTypeText = new TranslatableText("conrad.saveType.level", ((MinecraftServerAccessor) MinecraftClient.getInstance().getServer()).getSession().getDirectoryName());
						} else if (MinecraftClient.getInstance().getCurrentServerEntry() != null) {
							saveTypeText = new TranslatableText("conrad.saveType.server", MinecraftClient.getInstance().getCurrentServerEntry().name);
						} else {
							saveTypeText = null;
						}
					}
				}

				if (saveTypeText != null) {
					tooltips.add(saveTypeText.styled((style -> style.withColor(Formatting.GRAY).withItalic(true))));
				}

				int tooltipCount = configDefinition.getTooltipCount();

				if (tooltipCount > 0) {
					tooltips.add(NEW_LINE);
				}

				for (int i = 0; i < tooltipCount; ++i) {
					tooltips.add(new TranslatableText(configKey.toString() + ".tooltip." + i));
				}

				entryContainer.setTooltipSupplier(() -> Optional.of(tooltips));


				ValueContainer valueContainer = saveType == Config.SaveType.LEVEL
						? ValueContainer.getInstance()
						: ValueContainer.ROOT;

				add(configDefinition, entryContainer, valueContainer, builder);
			}
		}

		return builder.build();
	}

	private static void add(ConfigDefinition configDefinition, EntryContainer entryContainer, ValueContainer valueContainer, ConfigBuilder builder) {
		for (Map.Entry<ValueKey, ValueDefinition> entry : configDefinition.getValues()) {
			if (!Config.class.isAssignableFrom(entry.getValue().getType())) {
				if (FieldBuilderProviderRegistry.contains(entry.getValue().getType())) {
					entryContainer.addEntry(FieldBuilderProviderRegistry.getEntry(builder, valueContainer, entry.getKey()).build(valueContainer, entry.getValue()));
				} else {
					ConradUtil.LOG.warn("Issue building config entry for {}: a provider has not been registered.", entry.getKey().toString());
				}
			}
		}

		for (Map.Entry<ConfigKey, ConfigDefinition> entry : configDefinition.getChildren()) {
			SubCategoryBuilder subCategory = builder.entryBuilder().startSubCategory(of(entry.getKey()));
			add(entry.getValue(), subCategory, valueContainer, builder);
			entryContainer.addEntry(subCategory.build(null, null));
		}
	}

	private static TranslatableText of(Object object) {
		return new TranslatableText(object.toString());
	}
}
