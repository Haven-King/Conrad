package dev.hephaestus.conrad.impl.client;

import dev.hephaestus.clothy.impl.builders.FieldBuilder;
import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.api.gui.FieldBuilderProviderRegistry;
import dev.hephaestus.conrad.impl.common.config.ValueContainer;
import dev.hephaestus.conrad.impl.common.keys.ConfigKey;
import dev.hephaestus.conrad.impl.common.keys.KeyRing;
import dev.hephaestus.conrad.impl.common.keys.ValueKey;
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

import java.io.IOException;
import java.lang.reflect.Method;
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
		LinkedHashMap<ConfigKey, EntryContainer> containers = new LinkedHashMap<>();
		Deque<ConfigKey> keyDeque = new ArrayDeque<>();

		for (ConfigKey configKey : KeyRing.getConfigKeys(this.modId)) {
			Class<? extends Config> configClass = KeyRing.get(configKey.root());
			Config.SaveType saveType = configClass.getAnnotation(Config.Options.class).type();
			ValueContainer valueContainer = saveType == Config.SaveType.LEVEL
					? ValueContainer.getInstance()
					: ValueContainer.ROOT;

			EntryContainer entryContainer = containers.computeIfAbsent(configKey, key -> {
				keyDeque.addLast(key);
				EntryContainer container =  key.isRoot()
						? builder.getOrCreateCategory(of(key))
						: builder.entryBuilder().startSubCategory(of(key));

				if (container instanceof ConfigCategory) {

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

					int tooltipCount = configClass.getAnnotation(Config.Options.class).tooltips();

					if (tooltipCount > 0) {
						tooltips.add(NEW_LINE);
					}

					for (int i = 0; i < tooltipCount; ++i) {
						tooltips.add(new TranslatableText(configKey.toString() + ".tooltip." + i));
					}

					((ConfigCategory) container).setTooltipSupplier(() -> Optional.of(tooltips));
				}

				return container;
			});

			for (ValueKey valueKey : KeyRing.getValueKeys(configKey)) {
				if (!Config.class.isAssignableFrom(KeyRing.get(valueKey).getReturnType())) {
					if (FieldBuilderProviderRegistry.contains(KeyRing.get(valueKey).getReturnType())) {
						entryContainer.addEntry(FieldBuilderProviderRegistry.getEntry(builder, valueContainer, valueKey).build(valueContainer, valueKey));
					} else {
						ConradUtil.LOG.warn("Issue building config entry for {}: a provider has not been registered.", valueKey.toString());
					}
				}
			}
		}

		while (!keyDeque.isEmpty()) {
			ConfigKey configKey = keyDeque.removeLast();
			EntryContainer container = containers.get(configKey);

			if (container instanceof SubCategoryBuilder) {
				containers.get(configKey.parent()).addEntry(((SubCategoryBuilder) container).build(null, null));
			}
		}

		return builder.build();
	}

	private static TranslatableText of(Object object) {
		return new TranslatableText(object.toString());
	}
}
