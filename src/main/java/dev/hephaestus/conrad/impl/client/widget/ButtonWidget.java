package dev.hephaestus.conrad.impl.client.widget;

import net.minecraft.text.Text;

public class ButtonWidget extends net.minecraft.client.gui.widget.ButtonWidget implements Widget {
	public ButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress) {
		super(x, y, width, height, message, onPress);
	}

	@Override
	public void moveVertically(int dY) {
		this.y += dY;
	}
}
