package dev.hephaestus.conrad.impl.client;

import me.shedaniel.clothconfig2.impl.builders.BooleanToggleBuilder;
import me.shedaniel.clothconfig2.impl.builders.ColorFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.DoubleFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.FloatFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.IntFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.IntSliderBuilder;
import me.shedaniel.clothconfig2.impl.builders.LongFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.LongSliderBuilder;
import me.shedaniel.clothconfig2.impl.builders.StringFieldBuilder;

import net.minecraft.text.TranslatableText;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.ConradUtils;

@Environment(EnvType.CLIENT)
@SuppressWarnings("ConstantConditions")
public class WidgetProviders {
	public static final ConfigWidgetProvider<Boolean> BOOLEAN_BUTTON = (builder, key, config, field) -> {
		BooleanToggleBuilder booleanToggleBuilder = builder.startBooleanToggle(
				new TranslatableText(key),
				(Boolean) ConradUtils.getValue(config, field)
		);

		Object defaultValue = ConradUtils.getDefault(config.getClass(), field);

		if (defaultValue instanceof Boolean) {
			booleanToggleBuilder.setDefaultValue((Boolean) defaultValue);
		}

		booleanToggleBuilder.setSaveConsumer(value -> ConradUtils.setValue(config, field, value));

		return booleanToggleBuilder.build();
	};

	public static final ConfigWidgetProvider<Integer> COLOR_FIELD = (builder, key, config, field) -> {
		ColorFieldBuilder colorFieldBuilder = builder.startColorField(
				new TranslatableText(key),
				(Integer) ConradUtils.getValue(config, field)
		);

		Object defaultValue = ConradUtils.getDefault(config.getClass(), field);

		if (defaultValue instanceof Integer) {
			colorFieldBuilder.setDefaultValue((Integer) defaultValue);
		}

		colorFieldBuilder.setSaveConsumer(value -> ConradUtils.setValue(config, field, value));

		return colorFieldBuilder.build();
	};

	public static final ConfigWidgetProvider<Double> DOUBLE_FIELD = (builder, key, config, field) -> {
		DoubleFieldBuilder doubleFieldBuilder = builder.startDoubleField(
				new TranslatableText(key),
				(Double) ConradUtils.getValue(config, field)
		);

		Object defaultValue = ConradUtils.getDefault(config.getClass(), field);

		if (defaultValue instanceof Double) {
			doubleFieldBuilder.setDefaultValue((Double) defaultValue);
		}

		doubleFieldBuilder.setSaveConsumer(value -> ConradUtils.setValue(config, field, value));

		return doubleFieldBuilder.build();
	};

	public static final ConfigWidgetProvider<Float> FLOAT_FIELD = (builder, key, config, field) -> {
		FloatFieldBuilder floatFieldBuilder = builder.startFloatField(
				new TranslatableText(key),
				(Float) ConradUtils.getValue(config, field)
		);

		Object defaultValue = ConradUtils.getDefault(config.getClass(), field);

		if (defaultValue instanceof Float) {
			floatFieldBuilder.setDefaultValue((Float) defaultValue);
		}

		floatFieldBuilder.setSaveConsumer(value -> ConradUtils.setValue(config, field, value));

		return floatFieldBuilder.build();
	};

	public static final ConfigWidgetProvider<Integer> INT_FIELD = (builder, key, config, field) -> {
		IntFieldBuilder intFieldBuilder = builder.startIntField(
				new TranslatableText(key),
				(Integer) ConradUtils.getValue(config, field)
		);

		Object defaultValue = ConradUtils.getDefault(config.getClass(), field);

		if (defaultValue instanceof Integer) {
			intFieldBuilder.setDefaultValue((Integer) defaultValue);
		}

		intFieldBuilder.setSaveConsumer(value -> ConradUtils.setValue(config, field, value));

		return intFieldBuilder.build();
	};

	public static final ConfigWidgetProvider<Integer> INT_SLIDER = (builder, key, config, field) -> {
		Config.Entry.Bounds.Discrete bounds = field.getAnnotation(Config.Entry.Bounds.Discrete.class);

		IntSliderBuilder intSliderBuilder = builder.startIntSlider(
				new TranslatableText(key),
				(Integer) ConradUtils.getValue(config, field),
				(int) bounds.min(),
				(int) bounds.max()
		);

		Object defaultValue = ConradUtils.getDefault(config.getClass(), field);

		if (defaultValue instanceof Integer) {
			intSliderBuilder.setDefaultValue((Integer) defaultValue);
		}

		intSliderBuilder.setSaveConsumer(value -> ConradUtils.setValue(config, field, value));

		return intSliderBuilder.build();
	};

	public static final ConfigWidgetProvider<Long> LONG_FIELD = (builder, key, config, field) -> {
		LongFieldBuilder longFieldBuilder = builder.startLongField(
				new TranslatableText(key),
				(Long) ConradUtils.getValue(config, field)
		);

		Object defaultValue = ConradUtils.getDefault(config.getClass(), field);

		if (defaultValue instanceof Long) {
			longFieldBuilder.setDefaultValue((Long) defaultValue);
		}

		longFieldBuilder.setSaveConsumer(value -> ConradUtils.setValue(config, field, value));

		return longFieldBuilder.build();
	};

	public static final ConfigWidgetProvider<Long> LONG_SLIDER = (builder, key, config, field) -> {
		Config.Entry.Bounds.Discrete bounds = field.getAnnotation(Config.Entry.Bounds.Discrete.class);

		LongSliderBuilder longSliderBuilder = builder.startLongSlider(
				new TranslatableText(key),
				(Long) ConradUtils.getValue(config, field),
				bounds.min(),
				bounds.max()
		);

		Object defaultValue = ConradUtils.getDefault(config.getClass(), field);

		if (defaultValue instanceof Long) {
			longSliderBuilder.setDefaultValue((Long) defaultValue);
		}

		longSliderBuilder.setSaveConsumer(value -> ConradUtils.setValue(config, field, value));

		return longSliderBuilder.build();
	};

	public static final ConfigWidgetProvider<String> STRING_FIELD = (builder, key, config, field) -> {
		StringFieldBuilder stringFieldBuilder = builder.startStrField(
				new TranslatableText(key),
				(String) ConradUtils.getValue(config, field)
		);

		Object defaultValue = ConradUtils.getDefault(config.getClass(), field);

		if (defaultValue instanceof String) {
			stringFieldBuilder.setDefaultValue((String) defaultValue);
		}

		stringFieldBuilder.setSaveConsumer(value -> ConradUtils.setValue(config, field, value));

		return stringFieldBuilder.build();
	};
}
