package dev.hephaestus.clothy.impl.gui.entries;

import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class SliderEntry<T extends Number> extends TooltipListEntry<T> implements BoundedFieldEntry<T> {
	private T min = null, max = null;

	public SliderEntry(Text fieldName, @Nullable Function<T, Optional<List<Text>>> tooltipSupplier, Consumer<T> saveConsumer, Supplier<T> defaultValue) {
		super(fieldName, tooltipSupplier, saveConsumer, defaultValue);
	}

	public SliderEntry(Text fieldName, @Nullable Function<T, Optional<List<Text>>> tooltipSupplier, boolean requiresRestart, Consumer<T> saveConsumer, Supplier<T> defaultValue) {
		super(fieldName, tooltipSupplier, requiresRestart, saveConsumer, defaultValue);
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
}
