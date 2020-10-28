package dev.hephaestus.conrad.api.properties;

import net.minecraft.text.Text;

import java.lang.annotation.Annotation;
import java.util.function.Consumer;

public abstract class ValueProperty<T> {
	public abstract void addTooltips(Consumer<Text> textConsumer);
	public abstract boolean check(T value);

	@FunctionalInterface
	public interface Builder {
		ValueProperty<?> from(Annotation annotation);
	}
}
