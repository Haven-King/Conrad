package dev.hephaestus.conrad.impl.common.util;

import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

public class PropertyConsumer implements Consumer<MutableText> {
	private static final Text MARKER = new LiteralText("@");
	private static final Style STYLE = Style.EMPTY.withColor(Formatting.GRAY).withItalic(true);

	private final Consumer<Text> innerConsumer;
	private final boolean serializing;

	public PropertyConsumer(Consumer<Text> innerConsumer, boolean serializing) {
		this.innerConsumer = innerConsumer;
		this.serializing = serializing;
	}

	public void accept(MutableText text) {
		this.innerConsumer.accept(
				this.serializing
					? MARKER.copy().append(text)
					: text.setStyle(STYLE)
				);
	}
}
