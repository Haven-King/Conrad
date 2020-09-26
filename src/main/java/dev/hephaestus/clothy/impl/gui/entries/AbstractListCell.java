package dev.hephaestus.clothy.impl.gui.entries;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * @param <T>           the configuration object type
 * @param <SELF>        the "curiously recurring template pattern" type parameter for this class
 * @param <OUTER_SELF>> the "curiously recurring template pattern" type parameter for the outer class
 * @see AbstractListListEntry
 */
public abstract class AbstractListCell<T, SELF extends AbstractListCell<T, SELF, OUTER_SELF>, OUTER_SELF extends AbstractListListEntry<T, SELF, OUTER_SELF>> extends BaseListCell {
	protected final OUTER_SELF listListEntry;

	public AbstractListCell(@Nullable T value, OUTER_SELF listListEntry) {
		this.listListEntry = listListEntry;
		this.setErrorSupplier(() -> Optional.ofNullable(listListEntry.cellErrorSupplier).flatMap(cellErrorFn -> cellErrorFn.apply(this.getValue())));
	}

	public abstract T getValue();
}
