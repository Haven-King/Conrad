/*
 * Copyright 2021 Haven King
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.inkwell.conrad.impl.gui;

import dev.inkwell.conrad.impl.Conrad;
import dev.inkwell.vivian.api.ConfigScreenProvider;
import dev.inkwell.vivian.api.builders.CategoryBuilder;
import dev.inkwell.vivian.api.builders.ConfigScreenBuilderImpl;
import dev.inkwell.vivian.api.widgets.ImageWidget;
import dev.inkwell.vivian.api.widgets.LabelComponent;
import dev.inkwell.vivian.api.widgets.SpacerComponent;
import dev.inkwell.vivian.api.widgets.TextButton;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ModConfigsScreen implements ClientModInitializer {
    private static final Identifier CONFIGS_ICON_TEXTURE = Conrad.id("textures/gui/configure_button.png");

    private ConfigScreenBuilderImpl configScreenBuilder;

    private static boolean buttonHasText(AbstractButtonWidget button, String translationKey) {
        Text text = button.getMessage();
        return text instanceof TranslatableText && ((TranslatableText) text).getKey().equals(translationKey);
    }

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

        Iterator<Map.Entry<String, Function<Screen, ? extends Screen>>> it = ConfigScreenProvider.getFactories();

        while (it.hasNext()) {
            Map.Entry<String, Function<Screen, ? extends Screen>> entry = it.next();
            String modId = entry.getKey();
            Function<Screen, ? extends Screen> screenBuilder = entry.getValue();

            FabricLoader.getInstance().getModContainer(modId).ifPresent(container -> {
                ModMetadata metadata = container.getMetadata();

                Text name = new TranslatableText(metadata.getName().isEmpty() ? metadata.getId() : metadata.getName());

                category.add((parent, x, y, width, consumer) -> {
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
                    }

                    consumer.accept(new SpacerComponent(parent, x, y, width, 30).withTooltips(tooltips));

                    Identifier icon = IconHandler.getIcon(container, metadata);

                    if (icon != null) {
                        consumer.accept(new ImageWidget(parent, x, y, 30, 30, icon));
                    }

                    consumer.accept(new LabelComponent(parent, x + 50, y, width - 85, 30, name));

                    consumer.accept(new TextButton(parent, x + width - 20, y + 5, 20, 20, 0, new LiteralText("▶"), b -> {
                        MinecraftClient.getInstance().openScreen(screenBuilder.apply(parent));
                        return true;
                    }));

                    return 30;
                });
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
                    0, 0, 20, CONFIGS_ICON_TEXTURE, 32, 64, b -> this.onPress(screen), (button, matrices, mouseX, mouseY) ->
                    screen.renderTooltip(matrices, new TranslatableText("conrad.mod_configs"), mouseX, mouseY), new TranslatableText("conrad.mod_configs")));
        }
    }

    private void modifyGameMenuScreen(Screen screen) {
        List<AbstractButtonWidget> buttons = Screens.getButtons(screen);

        buttons.add(new TexturedButtonWidget(
                screen.width - 25, 5, 20, 20,
                0, 0, 20, CONFIGS_ICON_TEXTURE, 32, 64, b -> this.onPress(screen), (button, matrices, mouseX, mouseY) ->
                screen.renderTooltip(matrices, new TranslatableText("conrad.mod_configs"), mouseX, mouseY), new TranslatableText("conrad.mod_configs")));
    }

    public void onPress(Screen parent) {
        MinecraftClient.getInstance().openScreen(this.configScreenBuilder.build(parent));
    }
}
