package dev.hephaestus.conrad.impl.client.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

public class TexturedButtonWidget extends ButtonWidget {
	private final Identifier texture;
	private final int u;
	private final int v;
	private final int uWidth;
	private final int vHeight;

	public TexturedButtonWidget(int x, int y, int width, int height, int u, int v, Identifier texture, int uWidth, int vHeight, PressAction onPress) {
		super(x, y, width, height, LiteralText.EMPTY, onPress);
		this.uWidth = uWidth;
		this.vHeight = vHeight;
		this.u = u;
		this.v = v;
		this.texture = texture;
	}

	@Override
	public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		MinecraftClient client = MinecraftClient.getInstance();
		client.getTextureManager().bindTexture(this.texture);
		RenderSystem.color4f(1, 1, 1, 1f);
		RenderSystem.disableDepthTest();
		int adjustedV = this.v;
		if (!active) {
			adjustedV += this.height * 2;
		} else if (this.isHovered()) {
			adjustedV += this.height;
		}

		drawTexture(matrices, this.x, this.y, this.u, adjustedV, this.width, this.height, this.uWidth, this.vHeight);
		RenderSystem.enableDepthTest();
	}
}
