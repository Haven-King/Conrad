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

package dev.inkwell.conrad.api.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.inkwell.conrad.api.gui.Category;
import dev.inkwell.conrad.api.gui.DrawableExtensions;
import dev.inkwell.conrad.api.gui.builders.ConfigScreenBuilder;
import dev.inkwell.conrad.api.gui.util.Group;
import dev.inkwell.conrad.api.gui.widgets.WidgetComponent;
import dev.inkwell.conrad.api.gui.widgets.value.ValueWidgetComponent;
import dev.inkwell.conrad.impl.gui.widgets.Mutable;
import dev.inkwell.conrad.impl.mixin.TitleScreenAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

// TODO: This class needs to be cleaned up/split up in general
public class ConfigScreen extends Screen implements DrawableExtensions {
    public static final int CONTENT_WIDTH = 330;
    public static final int HEADER_SIZE = 43;
    public static final int FOOTER_SIZE = 32;

    private final List<Text> tooltips = new ArrayList<>();
    private final Screen parent;
    private ConfigScreenBuilder provider;
    private List<Category> categories;
    private boolean isSaveDialogOpen = false;
    private int activeCategory = 0;
    private int scrollAmount = 0;
    private int visibleHeight;
    private float margin;
    private int lastMouseX;
    private int lastMouseY;
    private double clickedX;
    private float lastTickDelta;
    private int contentHeight;
    // TODO: Better error hoisting?
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private boolean hasError = false;

    private AbstractButtonWidget yesButton;
    private AbstractButtonWidget noButton;

    private Runnable andThen = this::onClose;

    public ConfigScreen(Screen parent, ConfigScreenBuilder provider, Text title) {
        super(title);
        this.provider = provider;
        this.parent = parent;
    }

    public void setProvider(ConfigScreenBuilder provider) {
        this.setActiveCategory(0);
        this.provider = provider;
        this.init(this.client, this.width, this.height);
    }

    @Override
    public void init(MinecraftClient client, int width, int height) {
        super.init(client, width, height);

        this.visibleHeight = this.height - HEADER_SIZE - FOOTER_SIZE;
        this.margin = (width - CONTENT_WIDTH) / 2F;

        int padding = 3;
        int buttonWidth = 60;
        int buttonHeight = 20;

        this.categories = this.provider.build(this, (int) margin, CONTENT_WIDTH, HEADER_SIZE + 5);

        MutableInt contentHeight = new MutableInt();
        this.iterateActive(widget -> contentHeight.add(widget.getHeight()));

        this.contentHeight = contentHeight.getValue();
        this.scrollAmount = 0;

        int l = 0;

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        for (Group<?> group : this.categories) {
            l = MathHelper.ceil(Math.max(l, textRenderer.getWidth(group.getName())));
        }

        int categoryWidth = l + 20;

        yesButton = new ButtonWidget(
                width / 2 - buttonWidth - padding,
                height / 2,
                buttonWidth,
                buttonHeight,
                new TranslatableText("gui.yes"),
                button -> {
                    this.iterate(widget -> {
                        if (widget instanceof Mutable) {
                            ((Mutable) widget).save();
                        }
                    });

                    this.categories.forEach(Category::save);

                    this.andThen.run();
                    this.andThen = this::onClose;

                    this.isSaveDialogOpen = false;
                    yesButton.visible = false;
                    noButton.visible = false;
                }
        );

        noButton = new ButtonWidget(
                width / 2 + padding,
                height / 2,
                buttonWidth,
                buttonHeight,
                new TranslatableText("gui.no"),
                button -> {
                    this.andThen.run();
                    this.andThen = this::onClose;
                }
        );

        AbstractButtonWidget backButton = new ConradButtonWidget(this, (int) margin + CONTENT_WIDTH / 2 + 7, this.height - 26, this.CONTENT_WIDTH / 2 - 20, 20, new TranslatableText("gui.back"), button -> {
            if (!this.isSaveDialogOpen) {
                if (this.changedCount() > 0) {
                    this.isSaveDialogOpen = true;
                    yesButton.visible = true;
                    noButton.visible = true;
                } else if (this.client != null) {
                    this.client.openScreen(this.parent);
                }
            }
        }, Collections.emptyList());

        this.addButton(backButton);

        yesButton.visible = false;
        noButton.visible = false;

        int categoryButtonsSpan = ((categoryWidth + 5) * categories.size());

        int x = this.width / 2 - categoryButtonsSpan / 2;

        for (int i = 0; i < categories.size(); ++i) {
            int categoryId = i;
            ButtonWidget button = new ConradButtonWidget(
                    this,
                    x,
                    20,
                    categoryWidth,
                    20,
                    categories.get(i).getName(),
                    (b) -> this.tryLeave(() -> this.setActiveCategory(categoryId)),
                    categories.get(categoryId).getTooltips(),
                    categories.size() > 1
            );

            if (i == activeCategory) {
                button.active = false;
            }

            this.addButton(button);

            x += categoryWidth + 5;
        }
    }

    @Override
    public void renderBackground(MatrixStack matrices) {
        Optional<Identifier> backgroundTexture = this.provider.getStyle().getBackgroundTexture();


        if (parent != null && !backgroundTexture.isPresent()) {
            if (parent instanceof TitleScreen) {
                TitleScreenAccessor accessor = (TitleScreenAccessor) parent;
                if (accessor.getBackgroundFadeStart() == 0L && accessor.getDoBackgroundFade()) {
                    accessor.setBackgroundFadeStart(Util.getMeasuringTimeMs());
                }

                float f = accessor.getDoBackgroundFade() ? (float)(Util.getMeasuringTimeMs() - accessor.getBackgroundFadeStart()) / 1000.0F : 1.0F;
                fill(matrices, 0, 0, parent.width, parent.height, -1);
                accessor.getBackgroundRenderer().render(lastTickDelta, MathHelper.clamp(f, 0.0F, 1.0F));
            } else {
                parent.renderBackground(matrices);
            }
        } else if (backgroundTexture.isPresent()) {
            int backgroundColor = this.provider.getStyle().getBackgroundColor();
            int a = backgroundColor >> 24;
            int r = (backgroundColor >> 16) & 0xFF;
            int g = (backgroundColor >> 8) & 0xFF;
            int b = (backgroundColor) & 0xFF;

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            MinecraftClient.getInstance().getTextureManager().bindTexture(backgroundTexture.get());
            //noinspection deprecation
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            //noinspection deprecation
            bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
            bufferBuilder.vertex(0.0D, this.height, 0.0D).texture(0.0F, (float) this.height / 32.0F).color(r, g, b, a).next();
            bufferBuilder.vertex(this.width, this.height, 0.0D).texture(this.width / 32.0F, this.height / 32.0F).color(r, g, b, a).next();
            bufferBuilder.vertex(this.width, 0.0D, 0.0D).texture(this.width / 32.0F, 0).color(r, g, b, a).next();
            bufferBuilder.vertex(0.0D, 0.0D, 0.0D).texture(0.0F, 0).color(r, g, b, a).next();
            tessellator.draw();
        } else {
            super.renderBackground(matrices);
        }
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);

        if (this.parent != null) {
            parent.resize(client, width, height);
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float tickDelta) {
        if (this.client == null) return;

        int useMouseX;
        int useMouseY;

        if (this.isSaveDialogOpen) {
            useMouseX = lastMouseX;
            useMouseY = lastMouseY;
        } else {
            useMouseX = mouseX;
            useMouseY = mouseY;
        }

        this.lastTickDelta = tickDelta;
        this.renderBackground(matrices);

        fill(matrices, 0, this.HEADER_SIZE, this.width, this.height - this.FOOTER_SIZE, 0x80000000);

        if (contentHeight > visibleHeight) {
            float ratio = 1F - (contentHeight - visibleHeight) / (float) contentHeight;
            int startX = (int) (this.margin + this.CONTENT_WIDTH + 3);

            int width = 6;
            int height = (int) (ratio * visibleHeight);
            int startY = scrollAmount == 0 ? HEADER_SIZE : HEADER_SIZE + (int) ((visibleHeight - height) * this.scrollAmount / (float) this.minScrollAmount());
            fill(matrices, startX, HEADER_SIZE, startX + width, HEADER_SIZE + visibleHeight, 0xFF000000, 1F);
            fill(matrices, startX, startY, startX + width, startY + height, 0xFF808080, 1F);
            fill(matrices, startX, startY, startX + width - 1, startY + height - 1, 0xFFC0C0C0, 1F);
        }

        super.render(matrices, useMouseX, useMouseY, tickDelta);

        matrices.push();

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        Window window = client.getWindow();
        float test = ((this.height - this.HEADER_SIZE - this.FOOTER_SIZE) / (float) this.height);
        GL11.glScissor(0, (int) (window.getFramebufferHeight() * (this.FOOTER_SIZE / (float) this.height)), window.getFramebufferWidth(), (int) (window.getFramebufferHeight() * test));

        this.hasError = false;

        this.iterateActive(widget -> {
            if (widget != this.getFocused()) {
                widget.render(matrices, mouseX, mouseY, tickDelta, true);

                if (widget instanceof ValueWidgetComponent) {
                    this.hasError |= ((ValueWidgetComponent<?>) widget).hasError();
                }
            }
        });

        if (this.getFocused() instanceof WidgetComponent) {
            ((WidgetComponent) this.getFocused()).render(matrices, mouseX, mouseY, tickDelta, true);
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        fillGradient(matrices, 0, this.HEADER_SIZE, this.width, this.HEADER_SIZE + 4, 0xFF000000, 0);
        fillGradient(matrices, 0, this.height - this.FOOTER_SIZE - 4, this.width, this.height - this.FOOTER_SIZE, 0, 0xFF000000);

        matrices.pop();

        if (!tooltips.isEmpty()) {
            List<OrderedText> lines = new ArrayList<>();
            this.tooltips.forEach(tooltip -> lines.addAll(textRenderer.wrapLines(tooltip, (int) (this.width * (2/3F)))));
            this.renderOrderedTooltip(matrices, lines, useMouseX, useMouseY);
            tooltips.clear();
        }

        if (this.isSaveDialogOpen) {
            fill(matrices, 0, 0, width, height, 0xA0000000);

            int buttonHeight = 20;

            int changedCount = this.changedCount();
            drawCenteredText(matrices, textRenderer, new TranslatableText("conrad.unsaved.count." + (changedCount > 1 ? "plural" : "singular"), changedCount), width / 2F, height / 2F - textRenderer.fontHeight / 2F - buttonHeight, 0xFFFFFFFF);
            drawCenteredText(matrices, textRenderer, new TranslatableText("conrad.unsaved.prompt"), width / 2F, height / 2F - (buttonHeight / 4F) * 3, 0xFFFFFFFF);

            yesButton.render(matrices, mouseX, mouseY, tickDelta);
            noButton.render(matrices, mouseX, mouseY, tickDelta);
        } else {
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }

        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 8, 0xFFFFFF);
    }

    private void setActiveCategory(int category) {
        this.activeCategory = category;

        for (int i = 0; i < categories.size(); ++i) {
            this.buttons.get(i + 1).active = i != category;
        }

        this.scrollAmount = 0;

        MutableInt contentHeight = new MutableInt();
        this.iterateActive(widget -> contentHeight.add(widget.getHeight()));

        this.contentHeight = contentHeight.getValue();
    }

    @Override
    public void onClose() {
        if (this.client == null) return;

        this.client.openScreen(this.parent);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, final double amount) {
        double scrollAmount = amount * 5;

        int newScrollAmount = MathHelper.clamp((int) (this.scrollAmount + scrollAmount), this.minScrollAmount(), this.maxScrollAmount());
        int dY = newScrollAmount - this.scrollAmount;

        this.iterateActive(widget -> widget.scroll(dY));

        this.scrollAmount += dY;

        return true;
    }

    private int maxScrollAmount() {
        return 0;
    }

    private int minScrollAmount() {
        if (this.contentHeight > this.visibleHeight) {
            return -this.contentHeight + this.visibleHeight - this.HEADER_SIZE + this.FOOTER_SIZE;
        } else {
            return 0;
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.isSaveDialogOpen) {
            return false;
        } else {
            boolean bl = clickedX > margin + CONTENT_WIDTH + 3 && clickedX < margin + CONTENT_WIDTH + 9;

            if (bl) {
                float ratio = visibleHeight / (float) contentHeight;
                int startY = (int) (this.HEADER_SIZE - scrollAmount * ratio);
                int height = (int) (ratio * visibleHeight) - this.HEADER_SIZE;

                double centerY = startY + height / 2F;

                double scrollAmount = -(mouseY - centerY) / 5D;

                this.mouseScrolled(mouseX, mouseY, scrollAmount);
            } else {
                MutableBoolean mut = new MutableBoolean();
                this.iterateActive(widget -> mut.setValue(widget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)));

                if (mut.booleanValue()) {
                    return true;
                }
            }

            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isSaveDialogOpen) {
            if (yesButton.mouseClicked(mouseX, mouseY, button)) {
                return true;
            } else if (noButton.mouseClicked(mouseX, mouseY, button)) {
                this.isSaveDialogOpen = false;
                yesButton.visible = false;
                noButton.visible = false;
                return true;
            } else {
                this.isSaveDialogOpen = false;
                return true;
            }
        } else {
            boolean bl = super.mouseClicked(mouseX, mouseY, button);

            clickedX = mouseX;

            if (bl) {
                this.iterateActive(widget -> widget.setFocused(false));
            } else {
                if (getFocused() == null || !getFocused().isMouseOver(mouseX, mouseY)) {
                    MutableBoolean mut = new MutableBoolean();

                    this.iterateActive(widget -> {
                        if (widget.holdsFocus()) {
                            widget.setFocused(widget.isMouseOver(mouseX, mouseY));

                            if (widget.isFocused()) {
                                this.setFocused(widget.getFocusElement(mouseX, mouseY));
                            }
                        }

                        mut.setValue(mut.getValue() || widget.mouseClicked(mouseX, mouseY, button));
                    });

                    bl = mut.booleanValue();
                } else {
                    bl = getFocused().mouseClicked(mouseX, mouseY, button);
                }
            }

            if (!bl) {
                if (this.getFocused() instanceof WidgetComponent) {
                    ((WidgetComponent) this.getFocused()).setFocused(false);
                }

                this.setFocused(null);
            }

            return bl;
        }
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        if (!this.isSaveDialogOpen) {
            if (this.getFocused() != null) {
                return this.getFocused().charTyped(chr, keyCode);
            }
        }

        return false;
    }

    private int changedCount() {
        MutableInt changed = new MutableInt(0);

        this.iterate(widget -> {
            if (widget instanceof Mutable && ((Mutable) widget).hasChanged()) {
                changed.add(1);
            }
        });

        return changed.getValue();
    }

    public void tryLeave(@NotNull Runnable andThen) {
        int changed = this.changedCount();

        if (changed > 0) {
            this.isSaveDialogOpen = true;
            yesButton.visible = true;
            noButton.visible = true;

            this.andThen = andThen;

            return;
        }

        andThen.run();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.isSaveDialogOpen) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.isSaveDialogOpen = false;
            }

            return false;
        } else {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE && this.changedCount() > 0) {
                this.isSaveDialogOpen = true;
                yesButton.visible = true;
                noButton.visible = true;
                return false;
            }

            boolean bl = false;

            if (this.getFocused() != null) {
                bl = this.getFocused().keyPressed(keyCode, scanCode, modifiers);
            }

            return bl || super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    public ScreenStyle getStyle() {
        return this.provider.getStyle();
    }

    public void addTooltips(TooltipAccess widget) {
        if (widget.isMouseOver(this.lastMouseX, lastMouseY)) {
            widget.addTooltips(this.tooltips::add);
        }
    }

    @Override
    public void renderOrderedTooltip(MatrixStack matrices, List<? extends OrderedText> lines, int x, int y) {
        if (!lines.isEmpty() && this.client != null) {
            int textMaxWidth = 0;

            for (OrderedText orderedText : lines) {
                int j = this.textRenderer.getWidth(orderedText);
                if (j > textMaxWidth) {
                    textMaxWidth = j;
                }
            }

            int textStartX = x - (textMaxWidth / 2);
            int textStartY = y - 10 - (textRenderer.fontHeight * lines.size());
            int n = 8;

            if (lines.size() > 1) {
                n += 2 + (lines.size() - 1) * 10;
            }

            if (textStartY + n + 6 > this.height) {
                textStartY = this.height - n - 6;
            }

            matrices.push();

            matrices.translate(-textStartX, -textStartY, 0);

            matrices.translate(textStartX, textStartY, 0);

            float offset = 10;

            float startY = 1F - (textStartY + n + offset) / (this.height);
            float endY = 1F - (textStartY - offset) / (this.height);

            if (textStartY - offset < 0) {
                float dY = (endY - startY) * this.height;
                matrices.translate(0, dY, 0);
            }

            if (textStartX - offset < 0) {
                float dX = 3 - (textStartX - offset);
                matrices.translate(dX, 0, 0);
            }

            if (textStartX + textMaxWidth + offset > (this.width)) {
                float dX = -(textStartX + textMaxWidth + offset - (this.width)) - 3;
                matrices.translate(dX, 0, 0);
            }

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);
            Matrix4f matrix4f = matrices.peek().getModel();

            fillGradient(matrix4f, bufferBuilder, textStartX - 3, textStartY - 4, textStartX + textMaxWidth + 3, textStartY - 3, 400, -267386864, -267386864);
            fillGradient(matrix4f, bufferBuilder, textStartX - 3, textStartY + n + 3, textStartX + textMaxWidth + 3, textStartY + n + 4, 400, -267386864, -267386864);
            fillGradient(matrix4f, bufferBuilder, textStartX - 3, textStartY - 3, textStartX + textMaxWidth + 3, textStartY + n + 3, 400, -267386864, -267386864);
            fillGradient(matrix4f, bufferBuilder, textStartX - 4, textStartY - 3, textStartX - 3, textStartY + n + 3, 400, -267386864, -267386864);
            fillGradient(matrix4f, bufferBuilder, textStartX + textMaxWidth + 3, textStartY - 3, textStartX + textMaxWidth + 4, textStartY + n + 3, 400, -267386864, -267386864);
            fillGradient(matrix4f, bufferBuilder, textStartX - 3, textStartY - 3 + 1, textStartX - 3 + 1, textStartY + n + 3 - 1, 400, 1347420415, 1344798847);
            fillGradient(matrix4f, bufferBuilder, textStartX + textMaxWidth + 2, textStartY - 3 + 1, textStartX + textMaxWidth + 3, textStartY + n + 3 - 1, 400, 1347420415, 1344798847);
            fillGradient(matrix4f, bufferBuilder, textStartX - 3, textStartY - 3, textStartX + textMaxWidth + 3, textStartY - 3 + 1, 400, 1347420415, 1347420415);
            fillGradient(matrix4f, bufferBuilder, textStartX - 3, textStartY + n + 2, textStartX + textMaxWidth + 3, textStartY + n + 3, 400, 1344798847, 1344798847);
            RenderSystem.enableDepthTest();
            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.shadeModel(7425);
            bufferBuilder.end();
            BufferRenderer.draw(bufferBuilder);
            RenderSystem.shadeModel(7424);
            RenderSystem.disableBlend();
            RenderSystem.enableTexture();
            VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
            matrices.translate(0.0D, 0.0D, 400.0D);

            for (int s = 0; s < lines.size(); ++s) {
                OrderedText orderedText2 = lines.get(s);
                if (orderedText2 != null) {
                    this.textRenderer.draw(orderedText2, textStartX, textStartY, -1, true, matrix4f, immediate, false, 0, 15728880);
                }

                if (s == 0) {
                    textStartY += 2;
                }

                textStartY += 10;
            }

            immediate.draw();
            matrices.pop();
        }
    }

    private void iterate(Consumer<WidgetComponent> action) {
        for (Group<Group<WidgetComponent>> category : this.categories) {
            this.iterate(category, action);
        }
    }

    private void iterateActive(Consumer<WidgetComponent> action) {
        this.iterate(this.categories.get(this.activeCategory), action);
    }

    private void iterate(Group<Group<WidgetComponent>> category, Consumer<WidgetComponent> action) {
        for (Group<WidgetComponent> section : category) {
            for (WidgetComponent widget : section) {
                action.accept(widget);
            }
        }
    }
}
