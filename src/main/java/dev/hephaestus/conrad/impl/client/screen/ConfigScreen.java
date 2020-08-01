package dev.hephaestus.conrad.impl.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.api.ConfigWidgetProvider;
import dev.hephaestus.conrad.api.DefaultConfigWidgetRegistry;
import dev.hephaestus.conrad.api.annotation.EntryWidth;
import dev.hephaestus.conrad.api.annotation.WidgetType;
import dev.hephaestus.conrad.impl.ConradUtils;
import dev.hephaestus.conrad.impl.client.widget.DropdownButtonWidget;
import dev.hephaestus.conrad.impl.client.widget.LabelWidget;
import dev.hephaestus.conrad.impl.client.widget.Widget;
import dev.hephaestus.conrad.impl.client.widget.WidgetList;
import dev.hephaestus.conrad.impl.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public class ConfigScreen<T extends Config> extends Screen {
	public static final Identifier ARROW_TEXTURE = new Identifier(ConradUtils.MOD_ID, "textures/gui/arrow2.png");

	private final Screen parent;
	private final String modId;
	private final Config config;

	private DropdownButtonWidget dropdownButtonWidget;
	private WidgetList widgetList;

	private int columnWidth, rowHeight;

	public ConfigScreen(Screen parent, String modId, T config) {
		super(new TranslatableText(ConfigManager.getKey(config.getClass())));
		this.parent = parent;
		this.modId = modId;
		this.config = config;
	}

	@Override
	public void init(MinecraftClient client, int width, int height) {
		super.init(client, width, height);

		this.columnWidth = width / 6;
		this.rowHeight = height / 6;

		this.dropdownButtonWidget = new DropdownButtonWidget(client, this.parent, this.config, this.modId, this.width / 2 - 150, rowHeight / 2 - 8);
		this.addChild(this.dropdownButtonWidget);

		this.widgetList = new WidgetList(this.rowHeight, width, height - this.rowHeight, 4);
		this.addAll(0, ConfigManager.getKey(this.config.getClass()), this.config);
		this.addChild(this.widgetList);
	}

	private void addAll(int depth, String parentKey, Config config) {
		for (Field field : config.getClass().getDeclaredFields()) {
			int initialWidgetY = this.rowHeight + this.widgetList.getHeight() + this.widgetList.getPadding() * 2;
			int dX = (this.width / 20) * depth;
			String key = parentKey + "." + field.getName();

			try {
				if (Config.class.isAssignableFrom(field.getType())) {
					this.widgetList.addChild(new LabelWidget(key, this.columnWidth + dX, initialWidgetY).underlined());
					this.addAll(depth + 1, key, (Config) field.get(config));
					continue;
				}
			} catch (IllegalAccessException e) {
				ConradUtils.LOG.warn("Failed to add widgets for \"{}\": Field \"{}\" is inaccessible in class \"{}\".", key, field.getName(), config.getClass());
			}

			Widget widget = createWidget(config, field, key, this.columnWidth, initialWidgetY, dX);

			if (widget != null) {
				this.widgetList.addChild(widget);
			}
		}
	}

	private Widget createWidget(Config config, Field field, String key, int x, int initialWidgetY, int dX) {
		ConfigWidgetProvider provider = DefaultConfigWidgetRegistry.get(field.getType());

		if (field.isAnnotationPresent(WidgetType.class)) {
			Class<? extends ConfigWidgetProvider> widgetClass = field.getAnnotation(WidgetType.class).value();
			try {
				provider = widgetClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		if (provider != null) {
			int entryWidth = field.isAnnotationPresent(EntryWidth.class)
					? (4 * this.columnWidth) / field.getAnnotation(EntryWidth.class).value()
					: (4 * this.columnWidth) / 4;

			return provider.create(
					config, field, key,
					x + dX, initialWidgetY, (this.columnWidth * 4) - dX, entryWidth
			);
		}

		return null;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);

		this.widgetList.render(matrices, mouseX, mouseY, delta);
		super.render(matrices, mouseX, mouseY, delta);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		Matrix4f matrix = matrices.peek().getModel();

		float x0 = this.width / 2F + this.textRenderer.getWidth(this.dropdownButtonWidget.getSelected().getMessage()) / 2F + 2;
		float x1 = x0 + 16;
		float y0 = this.height / 12F - 7.5F;
		float y1 = y0 + 16;
		float u0 = 0, u1 = 1;
		float v0 = 1, v1 = 2;

		MinecraftClient.getInstance().getTextureManager().bindTexture(ARROW_TEXTURE);
		bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE);
		bufferBuilder.vertex(matrix, x0, y1, 0).texture(u0, v1).next();
		bufferBuilder.vertex(matrix, x1, y1, 0).texture(u1, v1).next();
		bufferBuilder.vertex(matrix, x1, y0, 0).texture(u1, v0).next();
		bufferBuilder.vertex(matrix, x0, y0, 0).texture(u0, v0).next();
		bufferBuilder.end();
		BufferRenderer.draw(bufferBuilder);

		this.dropdownButtonWidget.render(matrices, mouseX, mouseY, delta);
	}

	@Override
	public void onClose() {
		MinecraftClient.getInstance().openScreen((this.parent));
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		this.widgetList.moveVertically((int) amount);
		return true;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			this.setDragging(true);
		}

		return this.dropdownButtonWidget.mouseClicked(mouseX, mouseY, button) || this.widgetList.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		this.dropdownButtonWidget.mouseMoved(mouseX, mouseY);
	}

	@Nullable
	@Override
	public Element getFocused() {
		return this.widgetList;
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		this.setDragging(false);
		return this.hoveredElement(mouseX, mouseY).filter((element) -> element.mouseReleased(mouseX, mouseY, button)).isPresent();
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		return (this.getFocused() != null && this.isDragging() && button == 0) && this.getFocused().mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return super.keyPressed(keyCode, scanCode, modifiers) || (this.getFocused() != null && this.getFocused().keyPressed(keyCode, scanCode, modifiers));
	}

	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		return super.keyReleased(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean charTyped(char chr, int keyCode) {
		return this.getFocused() != null && this.getFocused().charTyped(chr, keyCode);
	}

}
