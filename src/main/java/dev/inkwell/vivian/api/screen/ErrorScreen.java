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

package dev.inkwell.vivian.api.screen;

import dev.inkwell.vivian.api.DrawableExtensions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

public class ErrorScreen extends Screen implements DrawableExtensions {
    private final ConfigScreen parent;
    private final Runnable andThen;

    protected ErrorScreen(ConfigScreen parent, Runnable andThen) {
        super(LiteralText.EMPTY);
        this.parent = parent;
        this.andThen = andThen;
    }

    @Override
    public void init(MinecraftClient client, int width, int height) {
        super.init(client, width, height);

        this.addButton(new ButtonWidget(
                width / 2 - 63,
                height / 2,
                60,
                20,
                new TranslatableText("gui.yes"),
                button -> {
                    this.parent.save();
                    this.andThen.run();
                }
        ));

        this.addButton(new ButtonWidget(
                width / 2 + 3,
                height / 2,
                60,
                20,
                new TranslatableText("gui.no"),
                button -> this.andThen.run()
        ));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);

        int errorCount = this.parent.getErrorCount();
        drawCenteredText(matrices, textRenderer, new TranslatableText("vivian.error.count." + (errorCount > 1 ? "plural" : "singular"), errorCount), width / 2F, height / 2F - textRenderer.fontHeight / 2F - 20, 0xFFFFFFFF);
        drawCenteredText(matrices, textRenderer, new TranslatableText("vivian.error.prompt"), width / 2F, height / 2F - (20 / 4F) * 3, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        if (this.client == null) return;

        this.client.openScreen(this.parent);
    }
}
