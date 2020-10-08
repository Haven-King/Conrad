package dev.hephaestus.conrad.api.properties;

import dev.hephaestus.conrad.api.Config;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class PropertyType<T> {
	public static final PropertyType<Long> INT_BOUNDS = PropertyRegistry.register("int_bounds", Config.Value.IntegerBounds.class, (annotation) ->
		new BoundedProperty<Long>() {
			@Override
			public Long getMin() {
				Config.Value.IntegerBounds bounds = (Config.Value.IntegerBounds) annotation;
				return bounds.min() == Long.MIN_VALUE ? null : bounds.min();
			}

			@Override
			public Long getMax() {
				Config.Value.IntegerBounds bounds = (Config.Value.IntegerBounds) annotation;
				return bounds.max() == Long.MAX_VALUE ? null : bounds.max();
			}

			@Override
			public void addTooltips(Consumer<Text> textConsumer) {
				Config.Value.IntegerBounds bounds = (Config.Value.IntegerBounds) annotation;
				if (bounds.min() > Long.MIN_VALUE) textConsumer.accept(new LiteralText("@min " + bounds.min()));
				if (bounds.max() < Long.MAX_VALUE) textConsumer.accept(new LiteralText("@max " + bounds.max()));
			}

			@Override
			public boolean check(Long value) {
				Config.Value.IntegerBounds bounds = (Config.Value.IntegerBounds) annotation;
				return value >= bounds.min() && value <= bounds.max();
			}
		}
	);

	public static final PropertyType<Double> FLOAT_BOUNDS = PropertyRegistry.register("floating_bounds", Config.Value.FloatingBounds.class, (annotation) ->
		new BoundedProperty<Double>() {
			@Override
			public Double getMin() {
				Config.Value.FloatingBounds bounds = (Config.Value.FloatingBounds) annotation;
				return bounds.min() > Double.MIN_VALUE ? bounds.min() : null;
			}

			@Override
			public Double getMax() {
				Config.Value.FloatingBounds bounds = (Config.Value.FloatingBounds) annotation;
				return bounds.max() < Double.MAX_VALUE ? bounds.max() : null;
			}

			@Override
			public void addTooltips(Consumer<Text> textConsumer) {
				Config.Value.FloatingBounds bounds = (Config.Value.FloatingBounds) annotation;
				if (bounds.min() > Double.MIN_VALUE) textConsumer.accept(new LiteralText("@min " + bounds.min()));
				if (bounds.max() < Double.MAX_VALUE) textConsumer.accept(new LiteralText("@max " + bounds.max()));
			}

			@Override
			public boolean check(Double value) {
				Config.Value.FloatingBounds bounds = (Config.Value.FloatingBounds) annotation;
				return value >= bounds.min() && value <= bounds.max();
			}
		}
	);

	private final String id;

	public PropertyType(String id) {
		this.id = id;
	}

	public String getId() {
		return this.id;
	}
}
