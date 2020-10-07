package dev.hephaestus.clothy.impl.gui.entries;

import org.jetbrains.annotations.NotNull;

public class IntegerListCell extends BoundedListListEntry.BoundedListCell<Integer, IntegerListCell, IntegerListListEntry> {
	public IntegerListCell(Integer value, IntegerListListEntry listListEntry) {
		super(value, listListEntry);
	}

	@Override
	protected boolean isValidText(@NotNull String text) {
		return text.chars().allMatch(c -> Character.isDigit(c) || c == '-');
	}

	public Integer getValue() {
		try {
			return Integer.valueOf(widget.getText());
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}
