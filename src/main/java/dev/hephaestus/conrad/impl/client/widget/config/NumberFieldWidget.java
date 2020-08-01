package dev.hephaestus.conrad.impl.client.widget.config;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.api.ConfigWidgetProvider;
import dev.hephaestus.conrad.impl.client.WrappedNumberType;
import dev.hephaestus.conrad.impl.client.widget.Widget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.lang.reflect.Field;

@Environment(EnvType.CLIENT)
public class NumberFieldWidget<N extends Number & Comparable<N>> extends ConfigWidget<N> {
	public static ConfigWidgetProvider INTEGER = ((config, field, x, y, width, entryWidth, key, args) -> new NumberFieldWidget<Integer>(config, field, x, y, width, entryWidth, key, WrappedNumberType.INTEGER));
	public static ConfigWidgetProvider LONG = ((config, field, x, y, width, entryWidth, key, args) -> new NumberFieldWidget<Long>(config, field, x, y, width, entryWidth, key, WrappedNumberType.LONG));
	public static ConfigWidgetProvider FLOAT = ((config, field, x, y, width, entryWidth, key, args) -> new NumberFieldWidget<Float>(config, field, x, y, width, entryWidth, key, WrappedNumberType.FLOAT));
	public static ConfigWidgetProvider DOUBLE = ((config, field, x, y, width, entryWidth, key, args) -> new NumberFieldWidget<Double>(config, field, x, y, width, entryWidth, key, WrappedNumberType.DOUBLE));
	public static ConfigWidgetProvider SHORT = ((config, field, x, y, width, entryWidth, key, args) -> new NumberFieldWidget<Short>(config, field, x, y, width, entryWidth, key, WrappedNumberType.SHORT));
	public static ConfigWidgetProvider BYTE = ((config, field, x, y, width, entryWidth, key, args) -> new NumberFieldWidget<Byte>(config, field, x, y, width, entryWidth, key, WrappedNumberType.BYTE));

	private final WrappedNumberType<N> wrapper;
	private final TextField textField;

	protected NumberFieldWidget(Config config, Field field, String labelKey, int x, int initialY, int width, int entryWidth, Object... args) {
		super(config, field, labelKey, x, initialY, width, entryWidth, args);
		this.wrapper = (WrappedNumberType<N>) args[0];
		this.textField = new TextField(x + width - entryWidth, initialY, entryWidth, 20, LiteralText.EMPTY);

		try {
			this.textField.setText(this.wrapper.toString((N) field.get(config)));
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected N getValue() {
		return this.wrapper.parse(this.textField.getText());
	}

	@Override
	protected Widget getEntryWidget() {
		return this.textField;
	}

	@Override
	public boolean validate() {
		boolean bl = NumberFieldWidget.this.wrapper.canParse(this.textField.getText());

		if (bl) {
			this.textField.setEditableColor(0xe0e0e0);
		} else {
			this.textField.setEditableColor(0xFFFF8888);
		}

		return bl;
	}

	@Override
	public int getHeight() {
		return this.textField.getHeight();
	}

	public class TextField extends TextFieldWidget implements Widget {
		public TextField(int x, int y, int width, int height, Text text) {
			super(MinecraftClient.getInstance().textRenderer, x, y, width, height, text);
		}

		@Override
		public void write(String string) {
			String oldText = this.getText();
			super.write(string);

			if (!NumberFieldWidget.this.wrapper.canParse(this.getText())) {
				this.setText(oldText);
			}

			NumberFieldWidget.this.validate();
		}

		@Override
		public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
			return super.keyPressed(keyCode, scanCode, modifiers);
		}

		@Override
		public void moveVertically(int dY) {
			this.y += dY;
		}
	}
}
