package dev.hephaestus.conrad.impl.client.widget.config;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.api.annotation.SaveType;
import dev.hephaestus.conrad.impl.client.widget.DropdownButtonWidget;
import dev.hephaestus.conrad.impl.client.widget.Widget;
import dev.hephaestus.conrad.impl.data.ConfigSerializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;

import java.lang.reflect.Field;

public abstract class ConfigWidget<T> implements Widget {
	protected final Config config;
	private final Field field;
	private final TranslatableText label;

	protected final int x;
	protected final int width;

	private int y;
	private boolean focused;

	/**
	 * @param config the Config object to be changed.
	 * @param field field in the Config object that is changed with this widget is interacted with.
	 * @param labelKey the key for this widget's translation. Can be derived from the Config object alone
	 *                  for top level Config objects, but not for nested ones, hence the argument.
	 * @param x horizontal offset of the widget
	 * @param initialY initial vertical offset of the widget
	 */
	protected ConfigWidget(Config config, Field field, String labelKey, int x, int initialY, int width, int entryWidth, Object... args) {
		this.config = config;
		this.field = field;
		this.label = new TranslatableText(labelKey);

		this.x = x;
		this.width = width;

		this.y = initialY;
	}

	@Override
	public void moveVertically(int dY) {
		this.y += dY;
		this.getEntryWidget().moveVertically(dY);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if ((!DropdownButtonWidget.open && this.isMouseOver(mouseX, mouseY)) || this.isFocused()) {
			DrawableHelper.fill(matrices, 0, this.y - 2, 10000, this.y + this.getHeight() + 2, 0x22FFFFFF);
		}

		this.getEntryWidget().render(matrices, mouseX, mouseY, delta);
		MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, this.label, this.x, this.y + 6, 0xFFFFFFFF);
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return mouseX >= (double)this.x && mouseY >= (double)this.y && mouseX < (double)(this.x + this.width) && mouseY < (double)(this.y + this.getHeight());
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return this.getEntryWidget().mouseClicked(mouseX, mouseY, button);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		return this.getEntryWidget().mouseReleased(mouseX, mouseY, button);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		return this.getEntryWidget().mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return this.getEntryWidget().keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		return this.getEntryWidget().keyReleased(keyCode, scanCode, modifiers);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean charTyped(char chr, int keyCode) {
		return this.getEntryWidget().charTyped(chr, keyCode);
	}

	public void save() throws IllegalAccessException {
		this.field.set(this.config, this.getValue());
		ConfigSerializer.getInstance(this.config.getClass().getAnnotation(SaveType.class).value()).serialize(this.config);
	}

	public boolean isFocused() {
		return this.focused;
	}

	public void focus(boolean focused) {
		this.focused = focused;
	}

	protected abstract T getValue();
	protected abstract Widget getEntryWidget();

	/**
	 * Ensures that whatever value this widget represents is a valid value
	 */
	public abstract boolean validate();
}
