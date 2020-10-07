package dev.hephaestus.clothy.impl.builders.compound;

import dev.hephaestus.clothy.impl.builders.FieldBuilder;
import dev.hephaestus.clothy.impl.gui.entries.AbstractListCell;
import dev.hephaestus.clothy.impl.gui.entries.AbstractListListEntry;
import dev.hephaestus.conrad.api.StronglyTypedList;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public abstract class ListBuilder<T, C extends AbstractListCell<T, C, A>, A extends AbstractListListEntry<T, C, A>> extends FieldBuilder<StronglyTypedList<T>, A> {
	private final @NotNull Function<A, C> createNewInstance;
	private Function<T, Optional<Text>> cellErrorSupplier;
	private Text addTooltip = new TranslatableText("text.clothy.list.add"), removeTooltip = new TranslatableText("text.clothy.list.remove");
	protected boolean deleteButtonEnabled = true, insertInFront = true, expanded = false;

	protected ListBuilder(Text resetButtonKey, Text fieldNameKey, @NotNull Function<A, C> createNewInstance) {
		super(resetButtonKey, fieldNameKey);
		this.createNewInstance = createNewInstance;
	}

	public final Function<T, Optional<Text>> getCellErrorSupplier() {
		return cellErrorSupplier;
	}

	public final ListBuilder<T, C, A> setCellErrorSupplier(Function<T, Optional<Text>> cellErrorSupplier) {
		this.cellErrorSupplier = cellErrorSupplier;
		return this;
	}

	public final ListBuilder<T, C, A> setDeleteButtonEnabled(boolean deleteButtonEnabled) {
		this.deleteButtonEnabled = deleteButtonEnabled;
		return this;
	}

	public final ListBuilder<T, C, A> setInsertInFront(boolean insertInFront) {
		this.insertInFront = insertInFront;
		return this;
	}

	public final ListBuilder<T, C, A> setAddButtonTooltip(Text addTooltip) {
		this.addTooltip = addTooltip;
		return this;
	}

	public final ListBuilder<T, C, A> setRemoveButtonTooltip(Text removeTooltip) {
		this.removeTooltip = removeTooltip;
		return this;
	}

	public final ListBuilder<T, C, A> setExpanded(boolean expanded) {
		this.expanded = expanded;
		return this;
	}

	protected abstract A baseWidget(StronglyTypedList<T> value);

	@Override
	protected A withValue(StronglyTypedList<T> value) {
		A entry = this.baseWidget(value);

		if (this.errorSupplier != null) {
			entry.setErrorSupplier(() -> errorSupplier.apply(value));
		}

		entry.setCreateNewInstance(this.createNewInstance);
		entry.setCellErrorSupplier(this.cellErrorSupplier);
		entry.setAddTooltip(this.addTooltip);
		entry.setRemoveTooltip(this.removeTooltip);

		return entry;
	}
}
