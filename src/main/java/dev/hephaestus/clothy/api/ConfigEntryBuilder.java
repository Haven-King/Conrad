package dev.hephaestus.clothy.api;

import dev.hephaestus.clothy.impl.gui.entries.DropdownBoxEntry.DefaultSelectionCellCreator;
import dev.hephaestus.clothy.impl.gui.entries.DropdownBoxEntry.SelectionCellCreator;
import dev.hephaestus.clothy.impl.gui.entries.DropdownBoxEntry.SelectionTopCellElement;
import dev.hephaestus.clothy.impl.ConfigEntryBuilderImpl;
import dev.hephaestus.clothy.impl.builders.*;
import dev.hephaestus.clothy.impl.builders.compound.*;
import dev.hephaestus.clothy.impl.builders.compound.DropdownMenuBuilder.TopCellElementBuilder;
import dev.hephaestus.clothy.impl.builders.primitive.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.function.Function;

@Environment(EnvType.CLIENT)
public interface ConfigEntryBuilder {
    
    static ConfigEntryBuilder create() {
        return ConfigEntryBuilderImpl.create();
    }
    
    Text getResetButtonKey();
    
    ConfigEntryBuilder setResetButtonKey(Text resetButtonKey);

    IntegerListBuilder startIntList(Text fieldNameKey);

    LongListBuilder startLongList(Text fieldNameKey);

    FloatListBuilder startFloatList(Text fieldNameKey);

    DoubleListBuilder startDoubleList(Text fieldNameKey);

    StringListBuilder startStrList(Text fieldNameKey);

    SubCategoryBuilder startSubCategory(Text fieldNameKey);

    BooleanToggleBuilder startBooleanToggle(Text fieldNameKey);
    
    StringFieldBuilder startStrField(Text fieldNameKey);
    
    ColorFieldBuilder startColorField(Text fieldNameKey);

    TextDescriptionBuilder startTextDescription();
    
    <T extends Enum<?>> EnumSelectorBuilder<T> startEnumSelector(Text fieldNameKey, Class<T> clazz);

    IntegerFieldBuilder startIntField(Text fieldNameKey);
    
    LongFieldBuilder startLongField(Text fieldNameKey);
    
    FloatFieldBuilder startFloatField(Text fieldNameKey);
    
    DoubleFieldBuilder startDoubleField(Text fieldNameKey);
    
    IntSliderBuilder startIntSlider(Text fieldNameKey, int min, int max);
    
    LongSliderBuilder startLongSlider(Text fieldNameKey, long min, long max);

    <T> SelectorBuilder<T> startSelector(Text fieldNameKey, T[] valuesArray);

    <T> DropdownMenuBuilder<T> startDropdownMenu(Text fieldNameKey, SelectionTopCellElement<T> topCellElement, SelectionCellCreator<T> cellCreator);
    
    default <T> DropdownMenuBuilder<T> startDropdownMenu(Text fieldNameKey, SelectionTopCellElement<T> topCellElement) {
        return startDropdownMenu(fieldNameKey, topCellElement, new DefaultSelectionCellCreator<>());
    }
    
    default <T> DropdownMenuBuilder<T> startDropdownMenu(Text fieldNameKey, T value, Function<String, T> toObjectFunction, SelectionCellCreator<T> cellCreator) {
        return startDropdownMenu(fieldNameKey, TopCellElementBuilder.of(value, toObjectFunction), cellCreator);
    }
    
    default <T> DropdownMenuBuilder<T> startDropdownMenu(Text fieldNameKey, T value, Function<String, T> toObjectFunction, Function<T, Text> toTextFunction, SelectionCellCreator<T> cellCreator) {
        return startDropdownMenu(fieldNameKey, TopCellElementBuilder.of(value, toObjectFunction, toTextFunction), cellCreator);
    }
    
    default <T> DropdownMenuBuilder<T> startDropdownMenu(Text fieldNameKey, T value, Function<String, T> toObjectFunction) {
        return startDropdownMenu(fieldNameKey, TopCellElementBuilder.of(value, toObjectFunction), new DefaultSelectionCellCreator<>());
    }
    
    default <T> DropdownMenuBuilder<T> startDropdownMenu(Text fieldNameKey, T value, Function<String, T> toObjectFunction, Function<T, Text> toTextFunction) {
        return startDropdownMenu(fieldNameKey, TopCellElementBuilder.of(value, toObjectFunction, toTextFunction), new DefaultSelectionCellCreator<>());
    }
    
    default DropdownMenuBuilder<String> startStringDropdownMenu(Text fieldNameKey, String value, SelectionCellCreator<String> cellCreator) {
        return startDropdownMenu(fieldNameKey, TopCellElementBuilder.of(value, s -> s, LiteralText::new), cellCreator);
    }
    
    default DropdownMenuBuilder<String> startStringDropdownMenu(Text fieldNameKey, String value, Function<String, Text> toTextFunction, SelectionCellCreator<String> cellCreator) {
        return startDropdownMenu(fieldNameKey, TopCellElementBuilder.of(value, s -> s, toTextFunction), cellCreator);
    }
    
    default DropdownMenuBuilder<String> startStringDropdownMenu(Text fieldNameKey, String value) {
        return startDropdownMenu(fieldNameKey, TopCellElementBuilder.of(value, s -> s, LiteralText::new), new DefaultSelectionCellCreator<>());
    }
    
    default DropdownMenuBuilder<String> startStringDropdownMenu(Text fieldNameKey, String value, Function<String, Text> toTextFunction) {
        return startDropdownMenu(fieldNameKey, TopCellElementBuilder.of(value, s -> s, toTextFunction), new DefaultSelectionCellCreator<>());
    }
}
