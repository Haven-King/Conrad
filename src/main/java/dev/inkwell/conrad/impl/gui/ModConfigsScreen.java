package dev.inkwell.conrad.impl.gui;

import dev.inkwell.conrad.api.gui.ConfigScreenProvider;
import dev.inkwell.conrad.api.gui.builders.CategoryBuilder;
import dev.inkwell.conrad.api.gui.builders.ConfigScreenBuilderImpl;
import dev.inkwell.conrad.api.gui.builders.SectionBuilder;
import dev.inkwell.conrad.api.gui.screen.ConfigScreen;
import dev.inkwell.conrad.api.gui.widgets.*;
import dev.inkwell.conrad.api.gui.widgets.containers.RowContainer;
import dev.inkwell.conrad.impl.Conrad;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ModConfigsScreen implements ClientModInitializer {
    private static final Identifier CONFIGS_ICON_TEXTURE = Conrad.id("textures/gui/configure_button.png");

    private ConfigScreenBuilderImpl configScreenBuilder;

    @Override
    public void onInitializeClient() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!FabricLoader.getInstance().isModLoaded("modmenu") || ConradGuiConfig.SHOW_MODS_CONFIG_BUTTON.getValue()) {
                if (screen instanceof TitleScreen) {
                    this.modifyTitleScreen(screen);
                } else if (screen instanceof GameMenuScreen) {
                    this.modifyGameMenuScreen(screen);
                }
            }
        });

        this.configScreenBuilder = new ConfigScreenBuilderImpl();

        CategoryBuilder category = this.configScreenBuilder.startCategory(new TranslatableText("conrad.mod_configs"));
        SectionBuilder section = category.addSection(new LiteralText(""));

        MutableInt i = new MutableInt();

        Iterator<Map.Entry<String, Function<Screen, ? extends Screen>>> it = ConfigScreenProvider.getFactories();

        while (it.hasNext()) {
            Map.Entry<String, Function<Screen, ? extends Screen>> entry = it.next();
            String modId = entry.getKey();
            Function<Screen, ? extends Screen> screenBuilder = entry.getValue();

            FabricLoader.getInstance().getModContainer(modId).ifPresent(container -> {
                ModMetadata metadata = container.getMetadata();

                section.add(((parent, width, x, y, index) -> {
                    Identifier icon = IconHandler.getIcon(container, metadata);

                    WidgetComponent iconComponent = icon != null
                            ? new ImageWidget(parent, 0, 0, 30, 30, icon)
                            : new LabelComponent(parent, 0, 0, 30, 30, LiteralText.EMPTY);

                    WidgetComponent dummy = new SpacerComponent(parent, 0, 0, 0, 30);
                    WidgetComponent spacer = new SpacerComponent(parent, 0, 0, 10, 30);

                    Text name = new TranslatableText(metadata.getName().isEmpty() ? metadata.getId() : metadata.getName());

                    LabelComponent label = new LabelComponent(parent,
                            0, 0, width - 80, 30,
                            name
                    );

                    TextButton button = new TextButton(parent, 0, 0, 20, 20, 0, new LiteralText("▶"), b -> {
                        MinecraftClient.getInstance().openScreen(screenBuilder.apply(parent));
                        return true;
                    });

                    WidgetComponent component = new RowContainer(parent, x, y, i.getAndIncrement(), false, iconComponent, spacer, dummy, label, button);

                    List<Text> tooltips = new ArrayList<>();

                    if (!metadata.getAuthors().isEmpty()) {
                        tooltips.add(new TranslatableText("conrad.authors", metadata.getAuthors().stream().map(Person::getName).collect(Collectors.joining(", "))).formatted(Formatting.GRAY));
                    }

                    if (!metadata.getDescription().isEmpty()) {
                        tooltips.add(LiteralText.EMPTY);
                        tooltips.add(new LiteralText(metadata.getDescription()));
                    }

                    if (!tooltips.isEmpty()) {
                        tooltips.add(0, name.copy().styled(style -> style.withBold(true)));
                        component.addTooltips(tooltips);
                    }

                    return component;
                }));

//                if (it.hasNext()) {
//                    section.add(((parent, width, x, y, index) -> new SpacerComponent(parent, x, y, 0, 10)));
//                }
            });
        }

    }

    private void modifyTitleScreen(Screen screen) {
        List<AbstractButtonWidget> buttons = Screens.getButtons(screen);

        AbstractButtonWidget quitButton = null;
        AbstractButtonWidget accessibilityButton = null;

        for (AbstractButtonWidget button : buttons) {
            if (buttonHasText(button, "menu.quit")) {
                quitButton = button;
            } else if (buttonHasText(button, "narrator.button.accessibility")) {
                accessibilityButton = button;
            }
        }

        if (quitButton != null && accessibilityButton != null) {
            int margin = accessibilityButton.x - quitButton.x - quitButton.getWidth();
            int x = accessibilityButton.x + accessibilityButton.getWidth() + margin;

            buttons.add(new TexturedButtonWidget(
                    x, accessibilityButton.y, 20, 20,
                    0, 0, 20, CONFIGS_ICON_TEXTURE, 32, 64, b -> this.onPress(screen), (button, matrices, mouseX, mouseY) -> {
                screen.renderTooltip(matrices, new TranslatableText("conrad.mod_configs"), mouseX, mouseY);
            }, new TranslatableText("conrad.mod_configs")));
        }
    }

    private void modifyGameMenuScreen(Screen screen) {
        List<AbstractButtonWidget> buttons = Screens.getButtons(screen);

        buttons.add(new TexturedButtonWidget(
                screen.width - 25, 5, 20, 20,
                0, 0, 20, CONFIGS_ICON_TEXTURE, 32, 64, b -> this.onPress(screen), (button, matrices, mouseX, mouseY) -> {
            screen.renderTooltip(matrices, new TranslatableText("conrad.mod_configs"), mouseX, mouseY);
        }, new TranslatableText("conrad.mod_configs")));
    }

    private static boolean buttonHasText(AbstractButtonWidget button, String translationKey) {
        Text text = button.getMessage();
        return text instanceof TranslatableText && ((TranslatableText) text).getKey().equals(translationKey);
    }

    public void onPress(Screen parent) {
        MinecraftClient.getInstance().openScreen(new ConfigScreen(parent, this.configScreenBuilder, LiteralText.EMPTY));
    }
}
