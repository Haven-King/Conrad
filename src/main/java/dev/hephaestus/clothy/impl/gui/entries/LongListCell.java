package dev.hephaestus.clothy.impl.gui.entries;

import org.jetbrains.annotations.NotNull;

public class LongListCell extends BoundedListListEntry.BoundedListCell<Long, LongListCell, LongListListEntry> {
	public LongListCell(Long value, LongListListEntry listListEntry) {
		super(value, listListEntry);
	}

	@Override
	protected boolean isValidText(@NotNull String text) {
		return text.chars().allMatch(c -> Character.isDigit(c) || c == '-');
	}

	public Long getValue() {
		try {
			return Long.valueOf(widget.getText());
		} catch (NumberFormatException e) {
			return 0L;
		}
	}
}
