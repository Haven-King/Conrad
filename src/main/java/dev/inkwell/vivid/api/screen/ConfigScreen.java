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

package dev.inkwell.vivid.api.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.inkwell.vivid.api.Category;
import dev.inkwell.vivid.api.DrawableExtensions;
import dev.inkwell.vivid.api.builders.ConfigScreenBuilder;
import dev.inkwell.vivid.api.util.Group;
import dev.inkwell.vivid.api.widgets.WidgetComponent;
import dev.inkwell.vivid.api.widgets.value.SectionHeaderComponent;
import dev.inkwell.vivid.impl.widgets.Mutable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static dev.inkwell.vivid.impl.Vivid.BLUR;

// TODO: This class needs to be cleaned up/split up in general
public class ConfigScreen extends Screen implements DrawableExtensions {
    public final List<Text> tooltips = new ArrayList<>();
    private final Screen parent;
    private ConfigScreenBuilder provider;
    private List<Category> categories;
    private boolean isSaveDialogOpen = false;
    private ScreenStyle style = ScreenStyle.DEFAULT;
    private int activeCategory = 0;
    private int scrollAmount = 0;
    private int contentWidth;
    private int headerSize;
    private int visibleHeight;
    private float margin;
    private float scale;
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

    public ConfigScreen(Screen parent, ConfigScreenBuilder provider) {
        super(LiteralText.EMPTY);
        this.provider = provider;
        this.parent = parent;
    }

    public void setProvider(ConfigScreenBuilder provider) {
        this.setActiveCategory(0);
        this.provider = provider;
        this.init(this.client, this.width, this.height);
    }

    public ConfigScreen withStyle(ScreenStyle style) {
        this.style = style;
        return this;
    }

    @Override
    public void init(MinecraftClient client, int width, int height) {
        super.init(client, width, height);

        this.headerSize = client.textRenderer.fontHeight * 3;
        this.contentWidth = height > width ? (width - 12) : width / 2;
        this.visibleHeight = this.height - headerSize;
        this.margin = height > width ? 6 : width / 4F;

        double test = client.getWindow().getScaleFactor();

        this.scale = (float) (0.5 + 0.125 * ((3 - (test - 1))));

        int padding = 3;
        int buttonWidth = 45;
        int buttonHeight = 15;

        this.categories = this.provider.build(this, (int) margin, contentWidth, headerSize);

        MutableInt contentHeight = new MutableInt();
        this.iterateActive(widget -> {
            contentHeight.add(widget.getHeight());
        });

        this.contentHeight = contentHeight.getValue();
        this.scrollAmount = 0;

        int l = 0;

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        for (Group<?> group : this.categories) {
            l = Math.max(l, textRenderer.getWidth(group.getName()));
        }

        int categoryWidth = l;

        yesButton = new FancyButton(this,
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

        noButton = new FancyButton(this,
                width / 2 + padding,
                height / 2,
                buttonWidth,
                buttonHeight,
                new TranslatableText("gui.no"),
                button -> {
                    this.onClose();
                }
        );

        yesButton.visible = false;
        noButton.visible = false;

        for (int i = 0; i < categories.size(); ++i) {
            int categoryId = i;
            ButtonWidget button = new CategoryButtonWidget(
                    this,
                    (int) (margin + i * (contentWidth / categories.size()) + (contentWidth / (categories.size() * 2)) - categoryWidth / 2),
                    0,
                    categoryWidth,
                    12,
                    categories.get(i).getName(),
                    (b) -> this.tryLeave(() -> this.setActiveCategory(categoryId)),
                    categories.get(categoryId).getTooltips());

            if (i == activeCategory) {
                button.active = false;
            }

            this.addButton(button);
        }
    }

    @Override
    public void renderBackground(MatrixStack matrices) {
        this.style.renderBackgroundFromPresets(this, this.parent, matrices, lastTickDelta);
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
        this.style.renderDecorations(matrices, useMouseX, useMouseY, tickDelta, this.width, this.height, this.headerSize);

        if (contentHeight > visibleHeight) {
            WidgetComponent firstWidget = (this.categories.get(this.activeCategory).get(0).get(0));
            int padding = (firstWidget instanceof SectionHeaderComponent ? firstWidget.getHeight() : 0);

            float ratio = (visibleHeight - headerSize - padding / 2F) / (float) contentHeight;
            int startX = height > width ? (int) (contentWidth + margin + 2) : (int) (margin * 3 + 2);

            int startY = (int) (this.headerSize - scrollAmount * ratio) + padding;
            int height = (int) (ratio * (visibleHeight - padding)) - this.headerSize;
            boolean hovered = useMouseX >= startX && useMouseY >= startY && useMouseX <= startX + 3 && useMouseY <= startY + height;
            this.style.renderScrollbar(matrices, startX, startY, 3, height, false, hovered);
        }

        super.render(matrices, useMouseX, useMouseY, tickDelta);

        matrices.push();

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        Window window = client.getWindow();
        float test = ((this.height - this.headerSize) / (float) this.height);
        GL11.glScissor(0, 0, window.getFramebufferWidth(), (int) (window.getFramebufferHeight() * test));

        this.hasError = false;

        this.iterateActive(widget -> {
            if (widget != this.getFocused()) {
                widget.render(matrices, mouseX, mouseY, tickDelta, true);
            }
        });

        if (this.getFocused() instanceof WidgetComponent) {
            ((WidgetComponent) this.getFocused()).render(matrices, mouseX, mouseY, tickDelta, true);
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        matrices.pop();

        if (!tooltips.isEmpty()) {
            this.renderTooltip(matrices, tooltips, useMouseX, useMouseY);
            tooltips.clear();
        }

        if (this.isSaveDialogOpen) {
            fill(matrices, 0, 0, width, height, 0x80000000);
            BLUR.setUniformValue("Progress", 1F);
            BLUR.setUniformValue("Radius", 4F);
            BLUR.setUniformValue("Start", 0F, 0F);
            BLUR.setUniformValue("End", 1F, 1F);
            BLUR.render(1F);

            int buttonHeight = 20;

            int changedCount = this.changedCount();
            drawCenteredText(matrices, textRenderer, new TranslatableText("vivid.unsaved.count." + (changedCount > 1 ? "plural" : "singular"), changedCount), width / 2F, height / 2F - textRenderer.fontHeight / 2F - buttonHeight, 0xFFFFFFFF, 1.25F * scale);
            drawCenteredText(matrices, textRenderer, new TranslatableText("vivid.unsaved.prompt"), width / 2F, height / 2F - (buttonHeight / 4F) * 3, 0xFFFFFFFF, 1.25F * scale);

            yesButton.render(matrices, mouseX, mouseY, tickDelta);
            noButton.render(matrices, mouseX, mouseY, tickDelta);
        } else {
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }
    }

    private void setActiveCategory(int category) {
        this.activeCategory = category;

        for (int i = 0; i < categories.size(); ++i) {
            this.buttons.get(i).active = i != category;
        }

        this.scrollAmount = 0;

        MutableInt contentHeight = new MutableInt();
        this.iterateActive(widget -> {
            contentHeight.add(widget.getHeight());
        });

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

        this.iterateActive(widget -> {
            widget.scroll(dY);
        });

        this.scrollAmount += dY;

        return true;
    }

    private int maxScrollAmount() {
        return 0;
    }

    private int minScrollAmount() {
        if (this.contentHeight > this.visibleHeight) {
            return -this.contentHeight + this.visibleHeight - this.headerSize;
        } else {
            return 0;
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.isSaveDialogOpen) {
            return false;
        } else {
            boolean bl = height > width
                    ? clickedX > margin + contentWidth + 3
                    : clickedX > (margin) * 3 + 2 && clickedX < margin * 3 + 5;
            if (bl) {
                float ratio = visibleHeight / (float) contentHeight;
                int startY = (int) (this.headerSize - scrollAmount * ratio);
                int height = (int) (ratio * visibleHeight) - this.headerSize;

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
            }

            return false;
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
                                this.setFocused(widget);
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
        if (this.isSaveDialogOpen) {
            return false;
        } else {
            if (this.getFocused() != null) {
                return this.getFocused().charTyped(chr, keyCode);
            }

            return false;
        }
    }

    @Override
    public void tick() {
        if (this.isSaveDialogOpen) {

        } else {
            if (this.getFocused() instanceof WidgetComponent) {
                ((WidgetComponent) this.getFocused()).tick();
            } else {
                this.iterateActive(WidgetComponent::tick);
            }
        }
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

    public boolean tryLeave(@NotNull Runnable andThen) {
        int changed = this.changedCount();

        if (changed > 0) {
            this.isSaveDialogOpen = true;
            yesButton.visible = true;
            noButton.visible = true;

            this.andThen = andThen;

            return false;
        }

        andThen.run();

        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.isSaveDialogOpen) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.isSaveDialogOpen = false;
            }

            return false;
        } else {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                int changed = this.changedCount();

                if (changed > 0) {
                    this.isSaveDialogOpen = true;
                    yesButton.visible = true;
                    noButton.visible = true;
                    return false;
                }
            }

            boolean bl = false;

            if (this.getFocused() != null) {
                bl = this.getFocused().keyPressed(keyCode, scanCode, modifiers);
            }

            return bl || super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    public ScreenStyle getStyle() {
        return this.style;
    }

    public void addTooltips(TooltipAccess widget) {
        if (widget.isMouseOver(this.lastMouseX, lastMouseY)) {
            widget.addTooltips(this.tooltips::add);
        }
    }

    @Override
    public void renderOrderedTooltip(MatrixStack matrices, List<? extends OrderedText> lines, int x, int y) {
        if (!lines.isEmpty() && this.client != null) {
            int i = 0;

            for (OrderedText orderedText : lines) {
                int j = this.textRenderer.getWidth(orderedText);
                if (j > i) {
                    i = j;
                }
            }

            float k = x - (i / 4F);
            float l = y - 10 - (textRenderer.fontHeight * lines.size() * scale);
            int n = 8;
            if (lines.size() > 1) {
                n += 2 + (lines.size() - 1) * 10;
            }

            k *= scale;
            l *= scale;

            if (l + n + 6 > this.height) {
                l = this.height - n - 6;
            }

            k /= scale;
            l /= scale;

            matrices.push();

            matrices.translate(-k, -l, 0);
            matrices.scale(scale, scale, 0);

            k /= scale;
            l /= scale;

            matrices.translate(k, l, 0);

            int offset = 10;

            float startX = (k - offset) / (this.width / this.scale);
            float startY = 1F - (l + n + offset) / (this.height / this.scale);
            float endX = (k + i + offset) / (this.width / this.scale);
            float endY = 1F - (l - offset) / (this.height / this.scale);

            if (l - offset < 0) {
                float dY = n + offset + this.textRenderer.fontHeight * 3;
                matrices.translate(0, dY, 0);
                startY -= (dY / this.height) * scale;
                endY -= (dY / this.height) * scale;
            }

            if (k - offset < 0) {
                float dX = 3 - (k - offset);
                matrices.translate(dX, 0, 0);
                startX += (dX / this.width) * scale;
                endX += (dX / this.width) * scale;
            }

            if (k + i + offset > (this.width / scale)) {
                float dX = -(k + i + offset - (this.width / scale)) - 3;
                matrices.translate(dX, 0, 0);
                startX += (dX / this.width) * scale;
                endX += (dX / this.width) * scale;
            }

            BLUR.setUniformValue("Progress", 1F);
            BLUR.setUniformValue("Radius", 4F);
            BLUR.setUniformValue("Start", startX, startY);
            BLUR.setUniformValue("End", endX, endY);
            BLUR.render(1F);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);
            Matrix4f matrix4f = matrices.peek().getModel();

            int color = 0x80000000;

            fill(matrix4f, bufferBuilder, k - offset, l - offset, k + i + offset, l + n + offset, 400, color);
            RenderSystem.enableDepthTest();
            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            bufferBuilder.end();
            BufferRenderer.draw(bufferBuilder);
            RenderSystem.disableBlend();
            RenderSystem.enableTexture();
            VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
            matrices.translate(0.0D, 0.0D, 400.0D);

            for (int s = 0; s < lines.size(); ++s) {
                OrderedText orderedText2 = lines.get(s);
                if (orderedText2 != null) {
                    this.textRenderer.draw(orderedText2, k, l, -1, true, matrix4f, immediate, false, 0, 15728880);
                }

                if (s == 0) {
                    l += 2;
                }

                l += 10;
            }

            immediate.draw();
            matrices.pop();
        }
    }

    public float getScale() {
        return this.scale;
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
