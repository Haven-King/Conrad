package dev.hephaestus.clothy.impl;

import dev.hephaestus.clothy.api.ConfigEntryBuilder;
import dev.hephaestus.clothy.impl.builders.EnumSelectorBuilder;
import dev.hephaestus.clothy.impl.builders.SelectorBuilder;
import dev.hephaestus.clothy.impl.builders.SubCategoryBuilder;
import dev.hephaestus.clothy.impl.builders.TextDescriptionBuilder;
import dev.hephaestus.clothy.impl.builders.compound.*;
import dev.hephaestus.clothy.impl.builders.primitive.*;
import dev.hephaestus.clothy.impl.gui.entries.*;
import dev.hephaestus.clothy.impl.gui.entries.DropdownBoxEntry.SelectionCellCreator;
import dev.hephaestus.clothy.impl.gui.entries.DropdownBoxEntry.SelectionTopCellElement;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class ConfigEntryBuilderImpl implements ConfigEntryBuilder {
    
    private Text resetButtonKey = new TranslatableText("text.clothy.reset_value");
    
    private ConfigEntryBuilderImpl() {
    }
    
    public static ConfigEntryBuilderImpl create() {
        return new ConfigEntryBuilderImpl();
    }
    
    public static ConfigEntryBuilderImpl createImmutable() {
        return new ConfigEntryBuilderImpl() {
            @Override
            public ConfigEntryBuilder setResetButtonKey(Text resetButtonKey) {
                throw new UnsupportedOperationException("This is an immutable entry builder!");
            }
        };
    }
    
    @Override
    public Text getResetButtonKey() {
        return resetButtonKey;
    }
    
    @Override
    public ConfigEntryBuilder setResetButtonKey(Text resetButtonKey) {
        this.resetButtonKey = resetButtonKey;
        return this;
    }
    
    @Override
    public IntegerListBuilder startIntList(Text fieldNameKey) {
        return new IntegerListBuilder(resetButtonKey, fieldNameKey, list -> new IntegerListCell(0, list));
    }
    
    @Override
    public LongListBuilder startLongList(Text fieldNameKey) {
        return new LongListBuilder(resetButtonKey, fieldNameKey, list -> new LongListCell(0L, list));
    }
    
    @Override
    public FloatListBuilder startFloatList(Text fieldNameKey) {
        return new FloatListBuilder(resetButtonKey, fieldNameKey, list -> new FloatListCell(0F, list));
    }
    
    @Override
    public DoubleListBuilder startDoubleList(Text fieldNameKey) {
        return new DoubleListBuilder(resetButtonKey, fieldNameKey, list -> new DoubleListCell(0D, list));
    }
    
    @Override
    public StringListBuilder startStrList(Text fieldNameKey) {
        return new StringListBuilder(resetButtonKey, fieldNameKey, list -> new StringListListEntry.StringListCell("", list));
    }
    
    @Override
    public SubCategoryBuilder startSubCategory(Text fieldNameKey) {
        return new SubCategoryBuilder(resetButtonKey, fieldNameKey);
    }

    @Override
    public BooleanToggleBuilder startBooleanToggle(Text fieldNameKey) {
        return new BooleanToggleBuilder(resetButtonKey, fieldNameKey);
    }
    
    @Override
    public StringFieldBuilder startStrField(Text fieldNameKey) {
        return new StringFieldBuilder(resetButtonKey, fieldNameKey);
    }
    
    @Override
    public ColorFieldBuilder startColorField(Text fieldNameKey) {
        return new ColorFieldBuilder(resetButtonKey, fieldNameKey);
    }
    
    @Override
    public TextDescriptionBuilder startTextDescription() {
        return new TextDescriptionBuilder(resetButtonKey, new LiteralText(UUID.randomUUID().toString()));
    }
    
    @Override
    public <T extends Enum<?>> EnumSelectorBuilder<T> startEnumSelector(Text fieldNameKey, Class<T> clazz) {
        return new EnumSelectorBuilder<>(resetButtonKey, fieldNameKey, clazz);
    }

    @Override
    public <T> SelectorBuilder<T> startSelector(Text fieldNameKey, T[] valuesArray) {
        return new SelectorBuilder<>(resetButtonKey, fieldNameKey, valuesArray);
    }
    
    @Override
    public IntegerFieldBuilder startIntField(Text fieldNameKey) {
        return new IntegerFieldBuilder(resetButtonKey, fieldNameKey);
    }
    
    @Override
    public LongFieldBuilder startLongField(Text fieldNameKey) {
        return new LongFieldBuilder(resetButtonKey, fieldNameKey);
    }
    
    @Override
    public FloatFieldBuilder startFloatField(Text fieldNameKey) {
        return new FloatFieldBuilder(resetButtonKey, fieldNameKey);
    }
    
    @Override
    public DoubleFieldBuilder startDoubleField(Text fieldNameKey) {
        return new DoubleFieldBuilder(resetButtonKey, fieldNameKey);
    }
    
    @Override
    public IntSliderBuilder startIntSlider(Text fieldNameKey, int min, int max) {
        return new IntSliderBuilder(resetButtonKey, fieldNameKey, min, max);
    }
    
    @Override
    public LongSliderBuilder startLongSlider(Text fieldNameKey, long min, long max) {
        return new LongSliderBuilder(resetButtonKey, fieldNameKey, min, max);
    }

    @Override
    public <T> DropdownMenuBuilder<T> startDropdownMenu(Text fieldNameKey, SelectionTopCellElement<T> topCellElement, SelectionCellCreator<T> cellCreator) {
        return new DropdownMenuBuilder<>(resetButtonKey, fieldNameKey, topCellElement, cellCreator);
    }
    
}
