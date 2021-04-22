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

package dev.inkwell.vivian.api.widgets.compound;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.inkwell.conrad.api.value.ConfigDefinition;
import dev.inkwell.vivian.api.builders.ConfigScreenBuilder;
import dev.inkwell.vivian.api.screen.ConfigScreen;
import dev.inkwell.vivian.api.screen.ScreenStyle;
import dev.inkwell.vivian.api.widgets.value.ValueWidgetComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class SubScreenWidget<T> extends ValueWidgetComponent<T> implements ConfigScreenBuilder {
    protected final ConfigDefinition<?> config;
    protected final Text name;
    protected ConfigScreen screen;

    public SubScreenWidget(ConfigDefinition<?> config, ConfigScreen parent, int x, int y, Supplier<@NotNull T> defaultValueSupplier, Consumer<T> changedListener, Consumer<T> saveConsumer, @NotNull T value, Text name) {
        super(parent, x, y, 20, 20, defaultValueSupplier, changedListener, saveConsumer, value);
        this.config = config;
        this.name = name;
    }

    @Override
    public void renderContents(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        drawCenteredString(
                matrixStack,
                textRenderer,
                "▶",
                this.x + this.width / 2F,
                this.textYPos(),
                0xFFFFFFFF
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isMouseOver(mouseX, mouseY)) {
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            this.parent.tryLeave(() ->
                    MinecraftClient.getInstance().openScreen((this.screen = this.build(this.parent))));
        }

        return false;
    }

    @Override
    public void renderBackground(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        minecraftClient.getTextureManager().bindTexture(AbstractButtonWidget.WIDGETS_LOCATION);
        int textureOffset = this.isMouseOver(mouseX, mouseY) ? 2 : 1;
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        DrawableHelper.drawTexture(matrixStack, this.x + this.width - 20, this.y, 0, 0, 46 + textureOffset * 20, 10, this.height, 256, 256);
        DrawableHelper.drawTexture(matrixStack, this.x + this.width - 10, this.y, 0, 200 - 10, 46 + textureOffset * 20, 10, this.height, 256, 256);
    }

    @Override
    public ScreenStyle getStyle() {
        return this.parent.style;
    }

    @Override
    protected @Nullable Text getDefaultValueAsText() {
        try {
            return this.getDefaultValue().getClass().getMethod("toString").getDeclaringClass() == Object.class ? null : new LiteralText(this.getDefaultValue().toString());
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    protected void refresh() {
        ConfigScreen newScreen = this.build(this.parent);
        int scrollAmount = this.screen.getScrollAmount();
        newScreen.setScrollAmount(scrollAmount);
        MinecraftClient.getInstance().openScreen(this.screen = newScreen);
    }
}
