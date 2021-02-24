package dev.monarkhes.conrad.impl.entrypoints;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import dev.monarkhes.conrad.api.EntryBuilderRegistry;
import dev.monarkhes.vivid.builders.CategoryBuilder;
import dev.monarkhes.vivid.builders.ConfigScreenBuilderImpl;
import dev.monarkhes.vivid.builders.SectionBuilder;
import dev.monarkhes.vivid.builders.WidgetComponentFactory;
import dev.monarkhes.vivid.screen.ConfigScreen;
import dev.monarkhes.vivid.util.Alignment;
import dev.monarkhes.vivid.widgets.LabelComponent;
import dev.monarkhes.vivid.widgets.TextButton;
import dev.monarkhes.vivid.widgets.WidgetComponent;
import dev.monarkhes.vivid.widgets.containers.RowContainer;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.fabricmc.fabric.api.config.v1.FabricSaveTypes;
import net.fabricmc.fabric.api.config.v1.Translator;
import net.fabricmc.loader.api.config.ConfigDefinition;
import net.fabricmc.loader.api.config.ConfigManager;
import net.fabricmc.loader.api.config.data.DataType;
import net.fabricmc.loader.api.config.util.ListView;
import net.fabricmc.loader.api.config.value.ValueContainer;
import net.fabricmc.loader.api.config.value.ValueKey;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ConfigScreenProvider implements ModMenuApi {
    private static final Multimap<String, ListView<ValueKey<?>>> CONFIGS = LinkedHashMultimap.create();

    private final Map<String, ConfigScreenFactory<?>> factories = new HashMap<>();

    public static void register(ListView<ValueKey<?>> values) {
        CONFIGS.put(values.get(0).getConfig().getNamespace(), values);
    }

    public ConfigScreenProvider() {
        for (String modId : CONFIGS.keySet()) {
            ConfigScreenBuilderImpl builder = new ConfigScreenBuilderImpl();
            this.factories.put(modId, parent -> new ConfigScreen(parent, builder));

            for (ListView<ValueKey<?>> values : CONFIGS.get(modId)) {
                ConfigDefinition<?> config = values.get(0).getConfig();
                String name = config.toString();
                CategoryBuilder category = builder.startCategory(new TranslatableText(name));

                category.setCondition(() -> {
                    PlayerEntity player;
                    return config.getSaveType() == FabricSaveTypes.USER
                            || ((player = MinecraftClient.getInstance().player) == null)
                            || player.hasPermissionLevel(4);
                });

                config.getData(DataType.COMMENT).forEach(string -> category.add(new LiteralText(string)));

                Deque<ValueKey<?>> deque = new ArrayDeque<>();
                values.forEach(deque::add);

                this.makeScreenBuilder(config, category, deque, 0, null, null);
            }
        }
    }

    private void makeScreenBuilder(ConfigDefinition<?> config, CategoryBuilder category, Deque<ValueKey<?>> values, int level, @Nullable SectionBuilder section, @Nullable String sectionName) {
        category.setSaveCallback(() -> Conrad.syncAndSave(config));


        String currentSectionName = sectionName;

        while (!values.isEmpty()) {
            ValueKey<?> value = values.pop();

            String parent = parent(value);

            if (value.getPath().length <= level || sectionName != null && !parent.startsWith(sectionName)) {
                values.push(value);
                return;
            } else if (section == null && value.getPath().length == level + 1) {
                section = category.addSection(new LiteralText(""));
                currentSectionName = parent;
            } else if (value.getPath().length == level + 2 && (currentSectionName == null || !currentSectionName.equals(parent))) {
                section = category.addSection(new TranslatableText(parent));
                @Nullable SectionBuilder finalSection = section;

                if (section != null) {
                    Translator.getComments(parent).forEach(string -> finalSection.add(new LiteralText(string)));
                }

                currentSectionName = parent;
            } else if (value.getPath().length > level + 2) {
                if (section == null) {
                    section = category.addSection(new LiteralText(""));
                    currentSectionName = parent;
                }

                ConfigScreenBuilderImpl builder = new ConfigScreenBuilderImpl();
                CategoryBuilder innerCategory = builder.startCategory(new TranslatableText(parent));

                SectionBuilder innerSection = innerCategory.addSection(new TranslatableText(" "));

                addEntry(innerSection, value);

                makeScreenBuilder(config, innerCategory, values, level + 2, innerSection, parent);

                section.add((screen, width, x, y, index) -> {
                    WidgetComponent label = new LabelComponent(screen, 0, 0, width / 2, (int) (30 * screen.getScale()), new TranslatableText(parent), true);
                    WidgetComponent widget = new TextButton(screen, 0, 0, width / 2, (int) (30 * screen.getScale()), 0, new LiteralText("▶"), Alignment.RIGHT, button -> {
                        MinecraftClient.getInstance().openScreen(new ConfigScreen(button.getParent(), builder));
                        return true;
                    });
                    WidgetComponent component = new RowContainer(screen, x, y, index, true, label, widget);
                    component.addTooltips(Translator.getComments(parent).stream().map(LiteralText::new).collect(Collectors.toList()));
                    return component;
                });

                continue;
            }

            if (section != null) {
                addEntry(section, value);
            }
        }

    }

    private <T> void addEntry(SectionBuilder section, ValueKey<T> configValue) {
        section.add((parent, width, x, y, index) -> {
            WidgetComponent label = new LabelComponent(parent, 0, 0, width / 2, (int) (30 * parent.getScale()), new TranslatableText(configValue.toString()), true);

            WidgetComponentFactory<T> factory = EntryBuilderRegistry.get(configValue);
            ValueContainer container = Conrad.container(configValue.getConfig());
            WidgetComponent widget = factory.build(parent, x, y, width / 2, (int) (30 * parent.getScale()),
                    configValue::getDefaultValue, t -> {},
                    v -> configValue.setValue(v, container),
                    configValue.getValue(container));

            WidgetComponent component = new RowContainer(parent, x, y, index, true, label, widget);

            Collection<Text> comments = new ArrayList<>();
            ConfigManager.getComments(configValue).forEach(string -> comments.add(new LiteralText(string)));
            component.addTooltips(comments);

            return component;
        });
    }

    private static String parent(ValueKey<?> valueKey) {
        StringBuilder builder = new StringBuilder(valueKey.getConfig().toString());

        String[] path = valueKey.getPath();
        for (int i = 0; i < path.length - 1; ++i) {
            builder.append('/').append(path[i]);
        }

        return builder.toString();
    }

    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        return factories;
    }
}
