package dev.hephaestus.conrad.impl.client.widget;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;

public interface Widget extends Drawable, Element {
	void moveVertically(int dY);
	int getHeight();
}
