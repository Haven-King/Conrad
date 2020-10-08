package dev.hephaestus.clothy.impl.builders.primitive;

import dev.hephaestus.clothy.api.AbstractConfigListEntry;
import dev.hephaestus.clothy.impl.builders.FieldBuilder;
import dev.hephaestus.clothy.impl.gui.entries.BoundedFieldEntry;
import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.api.properties.BoundedProperty;
import dev.hephaestus.conrad.api.properties.PropertyType;
import dev.hephaestus.conrad.impl.common.config.ValueDefinition;
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
	protected void handleProperties(ValueDefinition valueDefinition) {
		BoundedProperty<T> boundedProperty = null;
		if (valueDefinition.hasProperty(PropertyType.INT_BOUNDS)) {
			boundedProperty = (BoundedProperty<T>) valueDefinition.getProperty(PropertyType.INT_BOUNDS);
		} else if (valueDefinition.hasProperty(PropertyType.FLOAT_BOUNDS)) {
			boundedProperty = (BoundedProperty<T>) valueDefinition.getProperty(PropertyType.FLOAT_BOUNDS);
		}

		if (boundedProperty != null) {
			if (boundedProperty.getMin() != null) {
				this.setMin(boundedProperty.getMin());
			}

			if (boundedProperty.getMax() != null) {
				this.setMax(boundedProperty.getMax());
			}
		}
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
