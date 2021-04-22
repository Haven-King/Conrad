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

import com.mojang.blaze3d.systems.RenderSystem;
import dev.inkwell.conrad.impl.gui.widgets.Mutable;
import dev.inkwell.conrad.impl.mixin.TitleScreenAccessor;
import dev.inkwell.vivian.api.builders.CategoryBuilder;
import dev.inkwell.vivian.api.widgets.WidgetComponent;
import dev.inkwell.vivian.api.widgets.value.ValueWidgetComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class ConfigScreen extends Screen {
    public static final int CONTENT_WIDTH = 330;
    public static final int HEADER_SIZE = 43;
    public static final int FOOTER_SIZE = 32;
    public final ScreenStyle style;
    private final Screen parent;
    private final int activeCategory;
    private final CategoryBuilder[] categories;
    private final List<OrderedText> tooltips = new ArrayList<>();

    // Initialized variables; don't change after calling init
    private int visibleHeight, margin, frameBufferWidth, scissorStartY, scissorEndY;

    // Dynamic fields
    private int errors, changes, contentHeight, scrollAmount;

    private double clickedX;
    private double clickedY;

    public ConfigScreen(Screen parent, ScreenStyle style, int activeCategory, Text title, CategoryBuilder... categories) {
        super(title);
        this.parent = parent;
        this.style = style;
        this.activeCategory = activeCategory;
        this.categories = Arrays.stream(categories).filter(CategoryBuilder::shouldShow).toArray(CategoryBuilder[]::new);

        if (activeCategory < 0 || activeCategory >= this.categories.length) {
            throw new RuntimeException("Invalid category index");
        }
    }

    @Override
    public void init(MinecraftClient client, int width, int height) {
        super.init(client, width, height);

        this.visibleHeight = this.height - HEADER_SIZE - FOOTER_SIZE;
        this.margin = (this.width - CONTENT_WIDTH) / 2;

        int l = 0;

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        for (CategoryBuilder category : this.categories) {
            l = MathHelper.ceil(Math.max(l, textRenderer.getWidth(category.getName())));
        }

        // Scissor info
        Window window = client.getWindow();
        float normalized = ((this.height - HEADER_SIZE - FOOTER_SIZE) / (float) this.height);
        this.frameBufferWidth = window.getFramebufferWidth();
        this.scissorStartY = (int) (window.getFramebufferHeight() * (FOOTER_SIZE / (float) this.height));
        this.scissorEndY = (int) (window.getFramebufferHeight() * normalized);

        // Back button
        this.addButton(new ConradButtonWidget(margin + CONTENT_WIDTH / 2 + 7, this.height - 26, CONTENT_WIDTH / 2 - 20, 20, new TranslatableText("gui.back"), button -> {
            if (this.client != null) {
                this.client.openScreen(this.changes > 0 ? new ConfirmScreen(this, this::onClose) : this.parent);
            }
        }, true));

        // Category buttons
        int categoryWidth = l + 20;
        int categoryButtonsSpan = ((categoryWidth + 5) * this.categories.length);
        int x = this.width / 2 - categoryButtonsSpan / 2;

        this.scrollAmount = 0;

        for (int i = 0; i < this.categories.length; ++i) {
            int categoryId = i;
            Text name = this.categories[i].getName();
            ButtonWidget button = new ConradButtonWidget(
                    x,
                    20,
                    categoryWidth,
                    20,
                    name,
                    (b) -> this.tryLeave(() -> client.openScreen(new ConfigScreen(this.parent, style, categoryId, this.title, this.categories))),
                    (b, matrices, mouseX, mouseY) -> this.renderTooltip(matrices, this.categories[categoryId].getTooltips(), mouseX, mouseY),
                    categories.length > 1
            );

            if (i == activeCategory) {
                button.active = false;
                this.contentHeight = categories[i].build(this, this.margin, CONTENT_WIDTH, HEADER_SIZE, this.children::add);
            }

            this.addButton(button);

            x += categoryWidth + 5;
        }
    }

    @Override
    public void renderBackground(MatrixStack matrices) {
        Optional<Identifier> backgroundTexture = this.style.getBackgroundTexture();

        if (this.client != null && this.parent != null && !backgroundTexture.isPresent()) {
            if (this.parent instanceof TitleScreen) {
                TitleScreenAccessor accessor = (TitleScreenAccessor) this.parent;
                if (accessor.getBackgroundFadeStart() == 0L && accessor.getDoBackgroundFade()) {
                    accessor.setBackgroundFadeStart(Util.getMeasuringTimeMs());
                }

                float f = accessor.getDoBackgroundFade() ? (float) (Util.getMeasuringTimeMs() - accessor.getBackgroundFadeStart()) / 1000.0F : 1.0F;
                fill(matrices, 0, 0, this.parent.width, this.parent.height, -1);
                accessor.getBackgroundRenderer().render(this.client.getTickDelta(), MathHelper.clamp(f, 0.0F, 1.0F));
            } else {
                this.parent.renderBackground(matrices);
            }
        } else if (backgroundTexture.isPresent()) {
            int backgroundColor = this.style.getBackgroundColor();
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

        fill(matrices, 0, HEADER_SIZE, this.width, this.height - FOOTER_SIZE, 0x80000000);
        fillGradient(matrices, 0, HEADER_SIZE, this.width, HEADER_SIZE + 4, 0xFF000000, 0);
        fillGradient(matrices, 0, this.height - FOOTER_SIZE - 4, this.width, this.height - FOOTER_SIZE, 0, 0xFF000000);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        this.errors = 0;
        this.changes = 0;

        if (this.client != null) {
            for (Element element : this.children) {
                if (element != this.getFocused()) {
                    this.render(element, matrices, mouseX, mouseY, delta);
                }
            }

            if (this.getFocused() != null) {
                matrices.push();
                matrices.translate(0, 0, 100);
                this.render(this.getFocused(), matrices, mouseX, mouseY, delta);
                matrices.pop();
            }

            if (this.contentHeight > this.visibleHeight) {
                float ratio = 1F - (contentHeight - visibleHeight) / (float) contentHeight;
                int startX = this.margin + CONTENT_WIDTH + 3;

                int width = 6;
                int height = (int) (ratio * visibleHeight);
                int startY = this.scrollAmount == 0 ? HEADER_SIZE : HEADER_SIZE + (int) ((visibleHeight - height) * this.scrollAmount / (float) this.minScrollAmount());
                fill(matrices, startX, HEADER_SIZE, startX + width, HEADER_SIZE + visibleHeight, 0xFF000000);
                fill(matrices, startX, startY, startX + width, startY + height, 0xFF808080);
                fill(matrices, startX, startY, startX + width - 1, startY + height - 1, 0xFFC0C0C0);
            }
        }

        this.renderOrderedTooltip(matrices, this.tooltips, mouseX, mouseY);
        this.tooltips.clear();
    }

    private void render(Element element, MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (element instanceof Drawable) {
            if (element instanceof WidgetComponent) {
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
                GL11.glScissor(0, this.scissorStartY, this.frameBufferWidth, this.scissorEndY);
            }

            ((Drawable) element).render(matrices, mouseX, mouseY, delta);

            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }

        if (element instanceof ValueWidgetComponent && ((ValueWidgetComponent<?>) element).hasError()) {
            this.errors++;
        }

        if (element instanceof Mutable) {
            this.changes += ((Mutable) element).hasChanged() ? 1 : 0;
            this.errors += ((Mutable) element).hasError() ? 1 : 0;
        }

        if (this.categories.length > 1) {
            drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 8, 0xFFFFFF);
        }
    }

    private int minScrollAmount() {
        if (this.contentHeight > this.visibleHeight) {
            return (this.visibleHeight - this.contentHeight);
        } else {
            return 0;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, final double amount) {
        double scrollAmount = amount * 5;

        int newScrollAmount = MathHelper.clamp((int) (this.scrollAmount + scrollAmount), this.minScrollAmount(), 0);
        int dY = newScrollAmount - this.scrollAmount;

        for (Element element : this.children) {
            if (element instanceof WidgetComponent) {
                ((WidgetComponent) element).scroll(dY);
            }
        }

        this.scrollAmount += dY;

        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (this.changes > 0) {
                this.tryLeave(this::onClose);
            } else {
                this.onClose();
            }

            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.clickedX = mouseX;
        this.clickedY = mouseY;

        Iterator<? extends Element> var6 = this.children().iterator();

        Element element;
        do {
            if (!var6.hasNext()) {
                this.setFocused(null);
                return false;
            }

            element = var6.next();
        } while (!element.mouseClicked(mouseX, mouseY, button));

        this.setFocused(element);

        if (button == 0) {
            this.setDragging(true);
        }

        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        boolean bl = this.clickedX > this.margin + CONTENT_WIDTH + 3 && this.clickedX < this.margin + CONTENT_WIDTH + 9
                && this.clickedY > HEADER_SIZE && this.clickedY < this.height - FOOTER_SIZE;

        if (bl) {
            float ratio = this.visibleHeight / (float) this.contentHeight;
            int startY = (int) (HEADER_SIZE - this.scrollAmount * ratio);
            int height = (int) (ratio * this.visibleHeight) - HEADER_SIZE;

            double centerY = startY + height / 2F;

            double scrollAmount = -(mouseY - centerY) / 5D;

            this.mouseScrolled(mouseX, mouseY, scrollAmount);
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    public int getChangedCount() {
        return this.changes;
    }

    public int getErrorCount() {
        return this.errors;
    }

    @Override
    public void onClose() {
        if (this.client == null) return;

        this.client.openScreen(this.parent);
    }

    public void tryLeave(@NotNull Runnable andThen) {
        if (this.client != null && this.errors > 0) {
            this.client.openScreen(new ErrorScreen(this, andThen));
        }
        if (this.client != null && this.changes > 0) {
            this.client.openScreen(new ConfirmScreen(this, andThen));
        } else {
            andThen.run();
        }
    }

    public void save() {
        this.children.forEach(element -> {
            if (element instanceof Mutable) {
                ((Mutable) element).save();
            }
        });

        this.categories[this.activeCategory].save();
    }

    public void addTooltips(Text text) {
        if (this.client != null) {
            if (text == LiteralText.EMPTY) {
                this.tooltips.add(OrderedText.EMPTY);
            } else {

                TextRenderer textRenderer = this.client.textRenderer;

                if (this.tooltips.size() > 0) {
                    this.tooltips.add(LiteralText.EMPTY.asOrderedText());
                }

                this.tooltips.addAll(textRenderer.wrapLines(text, (CONTENT_WIDTH / 3) * 2));
            }
        }
    }

    public void addTooltips(List<Text> lines) {
        if (this.client != null) {
            TextRenderer textRenderer = this.client.textRenderer;
            List<OrderedText> ordered = new ArrayList<>();

            for (Text line : lines) {
                if (line == LiteralText.EMPTY) {
                    ordered.add(OrderedText.EMPTY);
                } else {
                    ordered.addAll(textRenderer.wrapLines(line, (CONTENT_WIDTH / 3) * 2));
                }
            }

            if (this.tooltips.size() > 0 && ordered.size() > 0) {
                this.tooltips.add(LiteralText.EMPTY.asOrderedText());
            }

            this.tooltips.addAll(ordered);
        }
    }

    @Override
    public void renderOrderedTooltip(MatrixStack matrices, List<? extends OrderedText> lines, int mouseX, int mouseY) {
        if (!lines.isEmpty() && this.client != null) {
            int maxLineWidth = 0;

            for (OrderedText line : lines) {
                maxLineWidth = Math.max(maxLineWidth, this.textRenderer.getWidth(line));
            }

            int x = mouseX - maxLineWidth / 2;
            int n = 8;

            if (lines.size() > 1) {
                n += 2 + (lines.size() - 1) * 10;
            }

            int y = mouseY - n - 6;

            if (x < 6) {
                x = 6;
            }

            if (x + maxLineWidth > this.width - 6) {
                x = this.width - maxLineWidth - 6;
            }

            if (y + n + 6 > this.height) {
                y = this.height - n - 6;
            }

            if (y < 6) {
                y = 6;
            }

            matrices.push();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);
            Matrix4f matrix4f = matrices.peek().getModel();
            fillGradient(matrix4f, bufferBuilder, x - 3, y - 4, x + maxLineWidth + 3, y - 3, 400, -267386864, -267386864);
            fillGradient(matrix4f, bufferBuilder, x - 3, y + n + 3, x + maxLineWidth + 3, y + n + 4, 400, -267386864, -267386864);
            fillGradient(matrix4f, bufferBuilder, x - 3, y - 3, x + maxLineWidth + 3, y + n + 3, 400, -267386864, -267386864);
            fillGradient(matrix4f, bufferBuilder, x - 4, y - 3, x - 3, y + n + 3, 400, -267386864, -267386864);
            fillGradient(matrix4f, bufferBuilder, x + maxLineWidth + 3, y - 3, x + maxLineWidth + 4, y + n + 3, 400, -267386864, -267386864);
            fillGradient(matrix4f, bufferBuilder, x - 3, y - 3 + 1, x - 3 + 1, y + n + 3 - 1, 400, 1347420415, 1344798847);
            fillGradient(matrix4f, bufferBuilder, x + maxLineWidth + 2, y - 3 + 1, x + maxLineWidth + 3, y + n + 3 - 1, 400, 1347420415, 1344798847);
            fillGradient(matrix4f, bufferBuilder, x - 3, y - 3, x + maxLineWidth + 3, y - 3 + 1, 400, 1347420415, 1347420415);
            fillGradient(matrix4f, bufferBuilder, x - 3, y + n + 2, x + maxLineWidth + 3, y + n + 3, 400, 1344798847, 1344798847);
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

            for (OrderedText orderedText2 : lines) {
                if (orderedText2 != null) {
                    this.textRenderer.draw(orderedText2, (float) x, (float) y, -1, true, matrix4f, immediate, false, 0, 15728880);
                }

                y += 10;
            }

            immediate.draw();
            matrices.pop();
        }
    }

    public int getScrollAmount() {
        return this.scrollAmount;
    }

    public void setScrollAmount(int scrollAmount) {
        this.scrollAmount = scrollAmount;
    }
}
