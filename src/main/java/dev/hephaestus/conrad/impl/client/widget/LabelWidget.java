package dev.hephaestus.conrad.impl.client.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

@Environment(EnvType.CLIENT)
public class LabelWidget implements Widget {
	private final TextRenderer textRenderer;
	private final TranslatableText text;
	private final int x;

	private int y;

	public LabelWidget(String key, int x, int initialY) {
		this.textRenderer = MinecraftClient.getInstance().textRenderer;
		this.text = new TranslatableText(key);
		this.x = x;
		this.y = initialY;
	}

	@Override
	public void moveVertically(int dY) {
		this.y += dY;
	}

	@Override
	public int getHeight() {
		return 5;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.textRenderer.drawWithShadow(matrices, this.text, x, y, 0xFFFFFFFF);
	}

	public LabelWidget underlined() {
		this.text.styled(style -> style.withFormatting(Formatting.UNDERLINE));
		return this;
	}
}
