package dev.hephaestus.clothy.impl.gui.entries;

import org.jetbrains.annotations.NotNull;

public class DoubleListCell extends BoundedListListEntry.BoundedListCell<Double, DoubleListCell, DoubleListListEntry> {
	public DoubleListCell(Double value, final DoubleListListEntry listListEntry) {
		super(value, listListEntry);
	}

	@Override
	protected boolean isValidText(@NotNull String text) {
		return text.chars().allMatch(c -> Character.isDigit(c) || c == '-' || c == '.');
	}

	public Double getValue() {
		try {
			return Double.valueOf(widget.getText());
		} catch (NumberFormatException e) {
			return 0d;
		}
	}
}
