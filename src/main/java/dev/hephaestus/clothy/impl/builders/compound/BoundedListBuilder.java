package dev.hephaestus.clothy.impl.builders.compound;

import dev.hephaestus.clothy.impl.gui.entries.AbstractListCell;
import dev.hephaestus.clothy.impl.gui.entries.AbstractListListEntry;
import dev.hephaestus.clothy.impl.gui.entries.BoundedFieldEntry;
import dev.hephaestus.conrad.api.StronglyTypedList;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

public abstract class BoundedListBuilder<T extends Number, C extends AbstractListCell<T, C, A> & BoundedFieldEntry<T>, A extends AbstractListListEntry<T, C, A> & BoundedFieldEntry<T>> extends ListBuilder<T, C, A> implements BoundedFieldEntry<T> {
	private T min = null, max = null;

	protected BoundedListBuilder(Text resetButtonKey, Text fieldNameKey, @NotNull Function<A, C> createNewInstance) {
		super(resetButtonKey, fieldNameKey, createNewInstance);
	}

	@Override
	public final BoundedFieldEntry<T> setMin(T min) {
		this.min = min;
		return this;
	}

	@Override
	public final BoundedFieldEntry<T> setMax(T max) {
		this.max = max;
		return this;
	}

	@Override
	public final T getMin() {
		return this.min;
	}

	@Override
	public final T getMax() {
		return this.max;
	}

	@Override
	protected final A withValue(StronglyTypedList<T> value) {
		A entry = super.withValue(value);

		entry.setMin(this.min);
		entry.setMax(this.max);

		return entry;
	}
}
