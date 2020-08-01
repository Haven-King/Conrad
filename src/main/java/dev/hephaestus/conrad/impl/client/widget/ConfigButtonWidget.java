package dev.hephaestus.conrad.impl.client.widget;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.api.annotation.SaveType;
import dev.hephaestus.conrad.impl.client.screen.ConfigScreen;
import dev.hephaestus.conrad.impl.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;

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
		matrices.push();
		super.renderButton(matrices, mouseX, mouseY, delta);
		matrices.pop();
	}
}
