package dev.hephaestus.clothy.impl.gui.entries;

import dev.hephaestus.conrad.api.StronglyTypedList;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class BoundedListListEntry<T extends Number, C extends AbstractTextFieldListCell<T, C, SELF>, SELF extends AbstractTextFieldListListEntry<T, C, SELF>> extends AbstractTextFieldListListEntry<T, C, SELF> implements BoundedFieldEntry<T> {
	private T min = null, max = null;

	public BoundedListListEntry(Text fieldName, StronglyTypedList<T> value, boolean defaultExpanded, @NotNull Function<StronglyTypedList<T>, Optional<List<Text>>> tooltipSupplier, Consumer<StronglyTypedList<T>> saveConsumer, Supplier<StronglyTypedList<T>> defaultValue, Text resetButtonKey, boolean requiresRestart, boolean deleteButtonEnabled, boolean insertInFront, BiFunction<T, SELF, C> createNewCell) {
		super(fieldName, value, defaultExpanded, tooltipSupplier, saveConsumer, defaultValue, resetButtonKey, requiresRestart, deleteButtonEnabled, insertInFront, createNewCell);
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

	public static abstract class BoundedListCell<T extends Number & Comparable<T>, SELF extends AbstractTextFieldListCell<T, SELF, OUTER_SELF>, OUTER_SELF extends BoundedListListEntry<T, SELF, OUTER_SELF>> extends AbstractTextFieldListCell<T, SELF, OUTER_SELF> implements BoundedFieldEntry<T> {
		public BoundedListCell(@Nullable T value, OUTER_SELF listListEntry) {
			super(value, listListEntry);
		}

		@Override
		@SuppressWarnings("unchecked")
		protected final T substituteDefault(@Nullable T value) {
			return value == null ? (T) Integer.valueOf(0) : value;
		}

		@Override
		public BoundedFieldEntry<T> setMin(T min) {
			return this;
		}

		@Override
		public BoundedFieldEntry<T> setMax(T max) {
			return this;
		}

		@Override
		public T getMin() {
			return this.listListEntry.getMin();
		}

		@Override
		public T getMax() {
			return this.listListEntry.getMax();
		}

		@Override
		public final Optional<Text> getError() {
			try {
				if (this.isGreaterThanMax(this.getValue())) {
					return Optional.of(new TranslatableText("text.clothy.error.too_large", this.listListEntry.getMax()));
				} else if (this.isLessThanMin(this.getValue())) {
					return Optional.of(new TranslatableText("text.clothy.error.too_small", this.listListEntry.getMin()));
				}
			} catch (NumberFormatException ex) {
				return Optional.of(new TranslatableText("text.clothy.error.not_valid_number_float"));
			}
			return Optional.empty();
		}
	}
}
