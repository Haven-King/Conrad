package dev.hephaestus.clothy.impl.gui.entries;

import org.jetbrains.annotations.NotNull;

public class FloatListCell extends BoundedListListEntry.BoundedListCell<Float, FloatListCell, FloatListListEntry> {
	public FloatListCell(Float value, FloatListListEntry listListEntry) {
		super(value, listListEntry);
	}

	@Override
	protected boolean isValidText(@NotNull String text) {
		return text.chars().allMatch(c -> Character.isDigit(c) || c == '-' || c == '.');
	}

	public Float getValue() {
		try {
			return Float.valueOf(widget.getText());
		} catch (NumberFormatException e) {
			return 0f;
		}
	}
}
