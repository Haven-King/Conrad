package dev.hephaestus.conrad.impl.client;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.ConradUtils;
import me.shedaniel.clothconfig2.impl.builders.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.TranslatableText;

@Environment(EnvType.CLIENT)
public class WidgetProviders {
    public static final ConfigWidgetProvider<Boolean> BOOLEAN_BUTTON = (builder, key, config, field) -> {
        BooleanToggleBuilder booleanToggleBuilder = builder.startBooleanToggle(
                new TranslatableText(key),
                (Boolean) config.get(field)
        );

        Object defaultValue = ConradUtils.getDefault(config.getClass(), field);

        if (defaultValue instanceof Boolean) {
            booleanToggleBuilder.setDefaultValue((Boolean) defaultValue);
        }

        booleanToggleBuilder.setSaveConsumer(value -> config.set(field, value));

        return booleanToggleBuilder.build();
    };

    public static final ConfigWidgetProvider<Integer> COLOR_FIELD = (builder, key, config, field) -> {
        ColorFieldBuilder colorFieldBuilder = builder.startColorField(
                new TranslatableText(key),
                (Integer) config.get(field)
        );

        Object defaultValue = ConradUtils.getDefault(config.getClass(), field);

        if (defaultValue instanceof Integer) {
            colorFieldBuilder.setDefaultValue((Integer) defaultValue);
        }

        colorFieldBuilder.setSaveConsumer(value -> config.set(field, value));

        return colorFieldBuilder.build();
    };

    public static final ConfigWidgetProvider<Double> DOUBLE_FIELD = (builder, key, config, field) -> {
        DoubleFieldBuilder doubleFieldBuilder = builder.startDoubleField(
                new TranslatableText(key),
                (Double) config.get(field)
        );

        Object defaultValue = ConradUtils.getDefault(config.getClass(), field);

        if (defaultValue instanceof Double) {
            doubleFieldBuilder.setDefaultValue((Double) defaultValue);
        }

        doubleFieldBuilder.setSaveConsumer(value -> config.set(field, value));

        return doubleFieldBuilder.build();
    };

//    @SuppressWarnings("unchecked")
//    public static final ConfigWidgetProvider<List<Double>> DOUBLE_LIST = (builder, key, config, field) -> {
//        DoubleListBuilder doubleListBuilder = builder.startDoubleList(
//                new TranslatableText(key),
//                (List<Double>) config.get(field)
//        );
//
//        Object defaultValue = ConradUtils.getDefault(config.getClass(), field);
//
//        if (defaultValue != null) {
//            doubleListBuilder.setDefaultValue((List<Double>) defaultValue);
//        }
//
//        doubleListBuilder.setSaveConsumer(value -> config.set(field, value));
//
//        return doubleListBuilder.build();
//    };

    public static final ConfigWidgetProvider<Float> FLOAT_FIELD = (builder, key, config, field) -> {
        FloatFieldBuilder floatFieldBuilder = builder.startFloatField(
                new TranslatableText(key),
                (Float) config.get(field)
        );

        Object defaultValue = ConradUtils.getDefault(config.getClass(), field);

        if (defaultValue instanceof Float) {
            floatFieldBuilder.setDefaultValue((Float) defaultValue);
        }

        floatFieldBuilder.setSaveConsumer(value -> config.set(field, value));

        return floatFieldBuilder.build();
    };

    public static final ConfigWidgetProvider<Integer> INT_FIELD = (builder, key, config, field) -> {
        IntFieldBuilder intFieldBuilder = builder.startIntField(
                new TranslatableText(key),
                (Integer) config.get(field)
        );

        Object defaultValue = ConradUtils.getDefault(config.getClass(), field);

        if (defaultValue instanceof Integer) {
            intFieldBuilder.setDefaultValue((Integer) defaultValue);
        }

        intFieldBuilder.setSaveConsumer(value -> config.set(field, value));

        return intFieldBuilder.build();
    };

    public static final ConfigWidgetProvider<Integer> INT_SLIDER = (builder, key, config, field) -> {
        Config.Entry.Bounds.Discrete bounds = field.getAnnotation(Config.Entry.Bounds.Discrete.class);

        IntSliderBuilder intSliderBuilder = builder.startIntSlider(
                new TranslatableText(key),
                (Integer) config.get(field),
                (int) bounds.min(),
                (int) bounds.max()
        );

        Object defaultValue = ConradUtils.getDefault(config.getClass(), field);

        if (defaultValue instanceof Integer) {
            intSliderBuilder.setDefaultValue((Integer) defaultValue);
        }

        intSliderBuilder.setSaveConsumer(value -> config.set(field, value));

        return intSliderBuilder.build();
    };

    public static final ConfigWidgetProvider<Long> LONG_FIELD = (builder, key, config, field) -> {
        LongFieldBuilder longFieldBuilder = builder.startLongField(
                new TranslatableText(key),
                (Long) config.get(field)
        );

        Object defaultValue = ConradUtils.getDefault(config.getClass(), field);

        if (defaultValue instanceof Long) {
            longFieldBuilder.setDefaultValue((Long) defaultValue);
        }

        longFieldBuilder.setSaveConsumer(value -> config.set(field, value));

        return longFieldBuilder.build();
    };

    public static final ConfigWidgetProvider<Long> LONG_SLIDER = (builder, key, config, field) -> {
        Config.Entry.Bounds.Discrete bounds = field.getAnnotation(Config.Entry.Bounds.Discrete.class);

        LongSliderBuilder longSliderBuilder = builder.startLongSlider(
                new TranslatableText(key),
                (Long) config.get(field),
                bounds.min(),
                bounds.max()
        );

        Object defaultValue = ConradUtils.getDefault(config.getClass(), field);

        if (defaultValue instanceof Long) {
            longSliderBuilder.setDefaultValue((Long) defaultValue);
        }

        longSliderBuilder.setSaveConsumer(value -> config.set(field, value));

        return longSliderBuilder.build();
    };

    public static final ConfigWidgetProvider<String> STRING_FIELD = (builder, key, config, field) -> {
        StringFieldBuilder stringFieldBuilder = builder.startStrField(
                new TranslatableText(key),
                (String) config.get(field)
        );

        Object defaultValue = ConradUtils.getDefault(config.getClass(), field);

        if (defaultValue instanceof String) {
            stringFieldBuilder.setDefaultValue((String) defaultValue);
        }

        stringFieldBuilder.setSaveConsumer(value -> config.set(field, value));

        return stringFieldBuilder.build();
    };
}
