package dev.hephaestus.conrad.impl.client.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.hephaestus.conrad.impl.ConradUtils;
import dev.hephaestus.conrad.impl.client.widget.config.ConfigWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class WidgetList implements ParentElement, Widget {
	public static final Identifier ARROW_TEXTURE = new Identifier(ConradUtils.MOD_ID, "textures/gui/arrow.png");

	private final List<Widget> children = new LinkedList<>();
	private final int y, height, padding;

	private Element focused;
	private int childrenHeight, scrollAmount;
	private float buttonAlpha = 0F;

	private int left, right, top, bottom;

	public WidgetList(int y, int width, int height, int padding) {
		this.y = y;
		this.height = height;
		this.padding = padding;

		this.left = 0;
		this.right = width;
		this.top = y;
		this.bottom = height + y;
	}

	public void addChild(Widget widget) {
		widget.moveVertically(this.children.size() * this.padding);
		this.children.add(widget);
		this.childrenHeight += widget.getHeight() + this.padding;
	}

	public int getMinOffset() {
		return (-this.childrenHeight + this.height - 75);
	}

	@Override
	public void moveVertically(int dY) {
		if (this.scrollAmount == 0 && dY > 0) {
			return;
		}

		if (this.scrollAmount + dY < this.getMinOffset()) {
			return;
		}

		if (this.scrollAmount + dY > 0) {
			dY = -scrollAmount;
		}

		for (Widget widget : this.children) {
			widget.moveVertically(dY);
		}

		this.scrollAmount += dY;
	}

	@Override
	public int getHeight() {
		return this.childrenHeight;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();

		RenderSystem.depthFunc(515);
		RenderSystem.disableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
		RenderSystem.disableAlphaTest();
		RenderSystem.shadeModel(7425);
		RenderSystem.disableTexture();

		bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);
		bufferBuilder.vertex(this.left, this.bottom, 0.0D).color(0, 0, 0, 128).next();
		bufferBuilder.vertex(this.right, this.bottom, 0.0D).color(0, 0, 0, 128).next();
		bufferBuilder.vertex(this.right, this.top, 0.0D).color(0, 0, 0, 128).next();
		bufferBuilder.vertex(this.left, this.top, 0.0D).color(0, 0, 0, 128).next();
		tessellator.draw();

		RenderSystem.enableTexture();
		RenderSystem.shadeModel(7424);
		RenderSystem.enableAlphaTest();
		RenderSystem.disableBlend();

		Window window = MinecraftClient.getInstance().getWindow();

		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(0, 0, window.getFramebufferWidth(), 5 * (window.getFramebufferHeight() / 6));

		for (Element element : this.children) {
			if (element instanceof Drawable) {
				((Drawable) element).render(matrices, mouseX, mouseY, delta);
			}
		}

		GL11.glDisable(GL11.GL_SCISSOR_TEST);

		if (this.scrollAmount - 20 > this.getMinOffset()) {
			this.buttonAlpha += delta / 5;
		} else {
			this.buttonAlpha -= delta / 5;
		}

		this.buttonAlpha = MathHelper.clamp(this.buttonAlpha, 0, 1);

		float x0 = 0;
		float x1 = x0 + 32;
		float y0 = this.height + this.y - 32 - 50 * MathHelper.abs(MathHelper.sin((float)(Util.getMeasuringTimeMs() % 3000L) / 1500F * 6.2831855F) * 0.1F);
		float y1 = y0 + 32;
		float u0 = 0, u1 = 1;
		float v0 = 1, v1 = 2;

		RenderSystem.depthFunc(519);
		RenderSystem.depthMask(false);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableTexture();

		Matrix4f matrix = matrices.peek().getModel();

		MinecraftClient.getInstance().getTextureManager().bindTexture(ARROW_TEXTURE);
		bufferBuilder.begin(7, VertexFormats.POSITION_COLOR_TEXTURE);
		bufferBuilder.vertex(matrix, x0, y1, 0).color(1F, 1F, 1F, buttonAlpha).texture(u0, v1).next();
		bufferBuilder.vertex(matrix, x1, y1, 0).color(1F, 1F, 1F, buttonAlpha).texture(u1, v1).next();
		bufferBuilder.vertex(matrix, x1, y0, 0).color(1F, 1F, 1F, buttonAlpha).texture(u1, v0).next();
		bufferBuilder.vertex(matrix, x0, y0, 0).color(1F, 1F, 1F, buttonAlpha).texture(u0, v0).next();
		bufferBuilder.end();

		BufferRenderer.draw(bufferBuilder);

		RenderSystem.disableBlend();
		RenderSystem.depthMask(true);
		RenderSystem.disableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
		RenderSystem.disableAlphaTest();
		RenderSystem.shadeModel(7425);
		RenderSystem.disableTexture();

		bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
		bufferBuilder.vertex(this.left, (this.top + 4), 0.0D).texture(0.0F, 1.0F).color(0, 0, 0, 0).next();
		bufferBuilder.vertex(this.right, (this.top + 4), 0.0D).texture(1.0F, 1.0F).color(0, 0, 0, 0).next();
		bufferBuilder.vertex(this.right, this.top, 0.0D).texture(1.0F, 0.0F).color(0, 0, 0, 255).next();
		bufferBuilder.vertex(this.left, this.top, 0.0D).texture(0.0F, 0.0F).color(0, 0, 0, 255).next();
		bufferBuilder.vertex(this.left, this.bottom, 0.0D).texture(0.0F, 1.0F).color(0, 0, 0, 255).next();
		bufferBuilder.vertex(this.right, this.bottom, 0.0D).texture(1.0F, 1.0F).color(0, 0, 0, 255).next();
		bufferBuilder.vertex(this.right, (this.bottom - 4), 0.0D).texture(1.0F, 0.0F).color(0, 0, 0, 0).next();
		bufferBuilder.vertex(this.left, (this.bottom - 4), 0.0D).texture(0.0F, 0.0F).color(0, 0, 0, 0).next();
		tessellator.draw();

		RenderSystem.enableTexture();
	}

	@Override
	public List<? extends Element> children() {
		return this.children;
	}

	@Override
	public boolean isDragging() {
		return false;
	}

	@Override
	public void setDragging(boolean dragging) {

	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		Widget focused = null;

		for (Widget widget : this.children) {
			if (widget.mouseClicked(mouseX, mouseY, button)) {
				focused = widget;
			}
		}

		this.setFocused(focused);
		if (button == 0) {
			this.setDragging(true);
		}

		this.validate();

		return focused != null;
	}

	@Nullable
	@Override
	public Element getFocused() {
		return this.focused;
	}

	@Override
	public void setFocused(@Nullable Element focused) {
		this.focused = focused;
	}

	public boolean validate() {
		boolean allChildrenValid = true;
		for (Widget widget : this.children) {
			if (widget instanceof ConfigWidget) {
				allChildrenValid = allChildrenValid && ((ConfigWidget<?>) widget).validate();
			}
		}

		return allChildrenValid;
	}

	public int getPadding() {
		return this.padding;
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		for (Widget widget : this.children) {
			if (widget instanceof ConfigWidget) {
				((ConfigWidget<?>) widget).focus(widget.isMouseOver(mouseX, mouseY));
			}
		}

		return true;
	}
}
