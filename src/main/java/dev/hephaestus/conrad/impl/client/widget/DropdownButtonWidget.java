package dev.hephaestus.conrad.impl.client.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.api.annotation.SaveType;
import dev.hephaestus.conrad.impl.client.screen.ConfigScreen;
import dev.hephaestus.conrad.impl.config.ConfigManager;
import dev.hephaestus.conrad.impl.config.RootConfigManager;
import dev.hephaestus.conrad.impl.duck.ConfigManagerProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.Matrix4f;

import java.util.LinkedList;
import java.util.List;

import static dev.hephaestus.conrad.impl.client.widget.WidgetList.ARROW_TEXTURE;

public class DropdownButtonWidget extends AbstractParentElement implements Drawable {
	public static boolean open = false;

	private final String modId;
	private final int x, y;

	private final List<ConfigButtonWidget> selected = new LinkedList<>();
	private final List<ConfigButtonWidget> configButtons = new LinkedList<>();

	public DropdownButtonWidget(MinecraftClient client, Screen parent, Config selected, String modId, int x, int y) {
		this.modId = modId;
		this.x = x;
		this.y = y;

		for (Config rootConfig : RootConfigManager.INSTANCE.getConfigs(modId)) {
			Config config;
			if (rootConfig.getClass().getAnnotation(SaveType.class).value() == SaveType.Type.CLIENT) {
				config = rootConfig;
			} else if (client.isIntegratedServerRunning() && client.getServer() != null) {
				config = ConfigManagerProvider.of(client.getServer()).getConfigManager().getConfig(rootConfig.getClass());
			} else if (client.getCurrentServerEntry() != null) {
				config = ConfigManagerProvider.of(client.getCurrentServerEntry()).getConfigManager().getConfig(rootConfig.getClass());
			} else {
				config = rootConfig;
			}

			if (config.getClass() == selected.getClass()) {
				ConfigButtonWidget buttonWidget = new ConfigButtonWidget(parent, modId, config, this.x, this.y, true);
				buttonWidget.active = false;

				for (ConfigButtonWidget widget : this.configButtons) {
					widget.moveVertically(20);
				}

				this.configButtons.add(buttonWidget);
				this.selected.add(0, buttonWidget);
			} else {
				this.configButtons.add(new ConfigButtonWidget(parent, modId, config, this.x, this.y + 20 * this.configButtons.size(), false));
			}
		}
	}

	@Override
	public List<? extends Element> children() {
		if (this.open) {
			return this.configButtons;
		} else {
			return this.selected;
		}
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return mouseX >= (double)this.x && mouseY >= (double)this.y && mouseX < (double)(this.x + 300) && mouseY < (double)(this.y + this.getHeight());
	}

	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		open = this.isMouseOver(mouseX, mouseY);
	}

	private int getHeight() {
		return this.children().size() * 20;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		for (Element element : this.children()) {
			if (element.mouseClicked(mouseX, mouseY, button)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		matrices.push();
		matrices.translate(0, 0, 3);

		for (ConfigButtonWidget widget : this.open ? this.configButtons : this.selected) {
			widget.render(matrices, mouseX, mouseY, delta);
			if (widget.isHovered()) {
				widget.renderToolTip(matrices, mouseX, mouseY);
			}
		}

		matrices.pop();
	}

	public ConfigButtonWidget getSelected() {
		return this.selected.get(0);
	}

	public class ConfigButtonWidget extends AbstractButtonWidget implements Widget {
		private final Screen parent;
		private final String modId;
		private final Config config;
		private final Text sideLabel;

		public ConfigButtonWidget(Screen parent, String modId, Config config, int x, int initialY, boolean selected) {
			super(x, initialY, 300, 20,
					new TranslatableText(ConfigManager.getKey(config.getClass()))
			);

			this.parent = parent;
			this.modId = modId;
			this.config = config;
			this.sideLabel = new TranslatableText(config.getClass().getAnnotation(SaveType.class).value() == SaveType.Type.CLIENT ? "conrad.client" : "conrad.server")
					.styled(style -> style.withColor(TextColor.fromRgb(0xFFffffCC)));
		}

		@Override
		public void renderToolTip(MatrixStack matrices, int mouseX, int mouseY) {
			this.parent.renderTooltip(matrices, this.sideLabel, mouseX, mouseY);
		}

		@Override
		public void moveVertically(int dY) {
			this.y += dY;
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			MinecraftClient.getInstance().openScreen(new ConfigScreen<>(this.parent, this.modId, this.config));
			DropdownButtonWidget.open = false;
		}

		@Override
		public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
			if (DropdownButtonWidget.this.selected.contains(this) && !DropdownButtonWidget.open) {
				this.drawCenteredText(matrices, MinecraftClient.getInstance().textRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, 0xFFFFFFFF);
			} else {
				super.renderButton(matrices, mouseX, mouseY, delta);
			}
		}
	}
}
