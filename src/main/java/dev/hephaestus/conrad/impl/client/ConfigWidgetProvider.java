package dev.hephaestus.conrad.impl.client;

import dev.hephaestus.conrad.api.Config;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.lang.reflect.Field;

public interface ConfigWidgetProvider<T> {
    static Text getName(String root, Field field) {
        return new TranslatableText(root + "." + field.getName());
    }

    AbstractConfigListEntry<T> getWidget(ConfigEntryBuilder builder, String key, Config config, Field field);
}
