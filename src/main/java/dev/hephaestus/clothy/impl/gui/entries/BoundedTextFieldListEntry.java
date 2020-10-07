package dev.hephaestus.clothy.impl.gui.entries;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class BoundedTextFieldListEntry<T extends Number> extends TextFieldListEntry<T> implements BoundedFieldEntry<T> {
	private T min = null, max = null;

	protected BoundedTextFieldListEntry(Text fieldName, T original, Text resetButtonKey, Supplier<T> defaultValue, Consumer<T> saveConsumer, @Nullable Function<T, Optional<List<Text>>> tooltipSupplier, boolean requiresRestart) {
		super(fieldName, original, resetButtonKey, defaultValue, saveConsumer, tooltipSupplier, requiresRestart);
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
	protected final void textFieldPreRender(TextFieldWidget widget) {
		try {
			T value = this.getValue();
			if (this.isGreaterThanMax(value) || this.isLessThanMin(value)) {
				widget.setEditableColor(16733525);
			} else {
				widget.setEditableColor(14737632);
			}
		} catch (NumberFormatException ex) {
			widget.setEditableColor(16733525);
		}
	}

	@Override
	public final Optional<Text> getError() {
		try {
			T value = this.getValue();

			if (this.isGreaterThanMax(value)) {
				return Optional.of(new TranslatableText("text.clothy.error.too_large", this.getMax()));
			} else if (this.isLessThanMin(value)) {
				return Optional.of(new TranslatableText("text.clothy.error.too_small", this.getMin()));
			}
		} catch (NumberFormatException ex) {
			return Optional.of(new TranslatableText("text.clothy.error.not_valid_number"));
		}

		return super.getError();
	}
}
