package dev.hephaestus.conrad.impl.client.widget.config;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.client.widget.Widget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.lang.reflect.Field;

@Environment(EnvType.CLIENT)
public class BooleanButtonWidget extends ConfigWidget<Boolean> {
	private final BooleanButton buttonWidget;
	private boolean value;

	public BooleanButtonWidget(Config config, Field field, String labelKey, int x, int initialY, int width, int entryWidth, Object... args) {
		super(config, field, labelKey, x, initialY, width, entryWidth, args);

		try {
			this.value = field.getBoolean(config);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		this.buttonWidget = new BooleanButton(this.x + width - entryWidth, initialY, entryWidth, 20, LiteralText.EMPTY);
		this.updateButtonText();
	}

	private void updateButtonText() {
		this.buttonWidget.setMessage(new LiteralText(String.valueOf(this.value)).formatted(this.value ? Formatting.GREEN : Formatting.RED));
	}

	@Override
	protected Boolean getValue() {
		return this.value;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		boolean bl = this.buttonWidget.mouseClicked(mouseX, mouseY, button);

		if (bl) {
			this.value = !this.value;
			this.updateButtonText();
		}

		return bl;
	}

	@Override
	protected Widget getEntryWidget() {
		return this.buttonWidget;
	}

	@Override
	public boolean validate() {
		return true;
	}

	@Override
	public int getHeight() {
		return this.buttonWidget.getHeight();
	}

	public static class BooleanButton extends ButtonWidget implements Widget {
		public BooleanButton(int x, int y, int width, int height, Text message) {
			super(x, y, width, height, message, (button -> {}));
		}

		@Override
		public void moveVertically(int dY) {
			this.y += dY;
		}
	}

	@Override
	public boolean isFocused() {
		return false;
	}
}
