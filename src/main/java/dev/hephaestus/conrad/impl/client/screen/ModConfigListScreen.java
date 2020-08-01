package dev.hephaestus.conrad.impl.client.screen;

import dev.hephaestus.conrad.impl.client.widget.ButtonWidget;
import dev.hephaestus.conrad.impl.client.widget.WidgetList;
import dev.hephaestus.conrad.impl.config.ConfigManager;
import dev.hephaestus.conrad.impl.config.RootConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

public class ModConfigListScreen extends Screen {
	private final Screen parent;
	private WidgetList widgetList;

	public ModConfigListScreen(Screen parent) {
		super(new LiteralText("Conrad"));
		this.parent = parent;
	}

	@Override
	public void init(MinecraftClient client, int width, int height) {
		super.init(client, width, height);

		int headerHeight = height / 6;
		this.widgetList = new WidgetList(headerHeight, width, height - headerHeight, 2);

		for (String modId : ConfigManager.getModIds()) {
			this.add(modId);
		}

		this.addChild(this.widgetList);
	}

	private void add(String modId) {
		int elementY = this.height / 6 + this.widgetList.getHeight() + this.widgetList.getPadding() * 2;
		this.widgetList.addChild(new ButtonWidget(
				width / 2 - 150,
				elementY,
				300,
				20,
				new TranslatableText("conrad.title." + modId),
				(buttonWidget) -> MinecraftClient.getInstance().openScreen(new ConfigScreen<>(this, modId, RootConfigManager.INSTANCE.getFirst(modId)))
		));
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);

		int halfSize = this.textRenderer.getWidth(this.title) / 2;
		this.textRenderer.drawWithShadow(matrices, this.title, this.width / 2F - halfSize, this.height / 12F - 6, 0xFFFFFF);

		this.widgetList.render(matrices, mouseX, mouseY, delta);

		super.render(matrices, mouseX, mouseY, delta);
	}

	@Override
	public void onClose() {
		this.client.openScreen(this.parent);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return this.widgetList.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		this.widgetList.moveVertically((int) amount);
		return true;
	}
}
