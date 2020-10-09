package dev.hephaestus.clothy.impl.builders.primitive;

import dev.hephaestus.clothy.api.AbstractConfigListEntry;
import dev.hephaestus.clothy.impl.builders.FieldBuilder;
import dev.hephaestus.clothy.impl.gui.entries.BoundedFieldEntry;
import net.minecraft.text.Text;

public abstract class BoundedFieldBuilder<T extends Number, A extends AbstractConfigListEntry<T> & BoundedFieldEntry<T>> extends FieldBuilder<T, A> implements BoundedFieldEntry<T> {
	private T min = null, max = null;

	protected BoundedFieldBuilder(Text resetButtonKey, Text fieldNameKey) {
		super(resetButtonKey, fieldNameKey);
	}

	@Override
	public final BoundedFieldBuilder<T, A> setMin(T min) {
		this.min = min;
		return this;
	}

	@Override
	public final BoundedFieldBuilder<T, A> setMax(T max) {
		this.max = max;
		return this;
	}

	@Override
	public T getMin() {
		return this.min;
	}

	@Override
	public T getMax() {
		return this.max;
	}

	protected abstract A baseWidget(T value);

	protected final A withValue(T value) {
		A entry = this.baseWidget(value);

		if (min != null) {
			entry.setMin(min);
		}

		if (max != null) {
			entry.setMax(max);
		}

		if (errorSupplier != null) {
			entry.setErrorSupplier(() -> errorSupplier.apply(entry.getValue()));
		}

		return entry;
	}
}