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

package dev.inkwell.conrad.api.gui.widgets.value.entry;

import dev.inkwell.conrad.api.gui.constraints.Matches;
import dev.inkwell.conrad.api.gui.screen.ConfigScreen;
import dev.inkwell.conrad.api.gui.util.Alignment;
import dev.inkwell.conrad.api.gui.widgets.value.ValueWidgetComponent;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class TextWidgetComponent<T> extends ValueWidgetComponent<T> implements Matches {
    private final Alignment alignment;
    protected String text;
    private int maxLength = 32;
    private int firstCharacterIndex;
    private int selectionStart;
    private int selectionEnd;
    private boolean selecting;
    private int focusedTicks;
    private String regex = null;
    private Predicate<String> textPredicate = this::matches;

    public TextWidgetComponent(ConfigScreen parent, int x, int y, int width, int height, Alignment alignment, Supplier<@NotNull T> defaultValueSupplier, Consumer<T> changedListener, Consumer<T> saveConsumer, @NotNull T value) {
        super(parent, x, y, width, height, defaultValueSupplier, changedListener, saveConsumer, value);
        this.alignment = alignment;
        this.text = this.valueOf(value);
    }

    @Override
    public boolean hasError() {
        return !passes();
    }

    @Override
    public void renderBackground(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {

    }

    @Override
    public void renderContents(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        String text = this.text.isEmpty() ? this.valueOf(this.emptyValue()) : this.text;

        float w2 = textRenderer.getWidth(text) * this.parent.getScale() * 0.5F;
        float x = this.x + this.width / 2F
                + ((this.width / 2F - 3) * this.alignment.mod)
                - w2 * this.alignment.mod;

        float y1 = this.textYPos() + 1;
        float y2 = y1 + 2 + textRenderer.fontHeight * parent.getScale();

        if (this.isFocused()) {
            if (selectionStart != selectionEnd) {
                float x1 = x - w2 + textRenderer.getWidth(this.text.substring(0, selectionStart)) * parent.getScale();
                float x2 = x - w2 + textRenderer.getWidth(this.text.substring(0, selectionEnd)) * parent.getScale();

                if (selectionStart == 0) x1 -= 1;
                if (selectionEnd == text.length()) x2 += 1;

                fill(matrixStack, x1, y1, x2, y2, 0xFF0022AA, 0.5F);
            } else {
                if (this.focusedTicks / 6 % 2 == 0) {
                    float x1 = x - w2 + textRenderer.getWidth(this.text.substring(0, selectionStart)) * parent.getScale();
                    fill(matrixStack, x1 - 0.25F, y1, x1 + 0.25F, y2, 0xFFFFFFFF, 1F);
                }
            }
        }

        drawCenteredText(
                matrixStack,
                textRenderer,
                new LiteralText(text),
                x,
                this.textYPos(),
                this.hasError() ? 0xFF5555 : this.getColor(),
                this.parent.getScale()
        );

    }

    protected int getColor() {
        return 0xFFFFFFFF;
    }

    @Override
    public boolean holdsFocus() {
        return true;
    }

    protected abstract String valueOf(T value);

    protected abstract T emptyValue();

    public void write(String string) {
        int i = Math.min(this.selectionStart, this.selectionEnd);
        int j = Math.max(this.selectionStart, this.selectionEnd);
        int k = this.maxLength - this.text.length() - (i - j);
        String string2 = SharedConstants.stripInvalidChars(string);
        int l = string2.length();
        if (k < l) {
            string2 = string2.substring(0, k);
            l = k;
        }

        this.text = (new StringBuilder(this.text)).replace(i, j, string2).toString();
        this.setSelectionStart(i + l);
        this.setSelectionEnd(this.selectionStart);
        this.onChanged(this.text);
    }

    @Override
    public Text getDefaultValueAsText() {
        return new LiteralText(this.valueOf(this.getDefaultValue()));
    }

    public void setSelectionStart(int cursor) {
        this.selectionStart = MathHelper.clamp(cursor, 0, this.text.length());
    }

    public void setSelectionEnd(int i) {
        int j = this.text.length();
        this.selectionEnd = MathHelper.clamp(i, 0, j);
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        if (textRenderer != null) {
            if (this.firstCharacterIndex > j) {
                this.firstCharacterIndex = j;
            }

            int k = this.width;
            String string = textRenderer.trimToWidth(this.text.substring(this.firstCharacterIndex), k);
            int l = string.length() + this.firstCharacterIndex;
            if (this.selectionEnd == this.firstCharacterIndex) {
                this.firstCharacterIndex -= textRenderer.trimToWidth(this.text, k, true).length();
            }

            if (this.selectionEnd > l) {
                this.firstCharacterIndex += this.selectionEnd - l;
            } else if (this.selectionEnd <= this.firstCharacterIndex) {
                this.firstCharacterIndex -= this.firstCharacterIndex - this.selectionEnd;
            }

            this.firstCharacterIndex = MathHelper.clamp(this.firstCharacterIndex, 0, j);
        }
    }

    public String getSelectedText() {
        int i = Math.min(this.selectionStart, this.selectionEnd);
        int j = Math.max(this.selectionStart, this.selectionEnd);
        return this.text.substring(i, j);
    }

    public void setCursorToStart() {
        this.setCursor(0);
    }

    public void setCursorToEnd() {
        this.setCursor(this.text.length());
    }

    public int getCursor() {
        return this.selectionStart;
    }

    public void setCursor(int cursor) {
        this.setSelectionStart(cursor);
        if (!this.selecting) {
            this.setSelectionEnd(this.selectionStart);
        }

        this.onChanged(this.text);
    }

    public int getWordSkipPosition(int wordOffset) {
        return this.getWordSkipPosition(wordOffset, this.getCursor());
    }

    private int getWordSkipPosition(int wordOffset, int cursorPosition) {
        return this.getWordSkipPosition(wordOffset, cursorPosition, true);
    }

    private int getWordSkipPosition(int wordOffset, int cursorPosition, boolean skipOverSpaces) {
        int i = cursorPosition;
        boolean bl = wordOffset < 0;
        int j = Math.abs(wordOffset);

        for (int k = 0; k < j; ++k) {
            if (!bl) {
                int l = this.text.length();
                i = this.text.indexOf(32, i);
                if (i == -1) {
                    i = l;
                } else {
                    while (skipOverSpaces && i < l && this.text.charAt(i) == ' ') {
                        ++i;
                    }
                }
            } else {
                while (skipOverSpaces && i > 0 && this.text.charAt(i - 1) == ' ') {
                    --i;
                }

                while (i > 0 && this.text.charAt(i - 1) != ' ') {
                    --i;
                }
            }
        }

        return i;
    }

    private void moveCursor(int offset) {
        this.setCursor(this.getCursorLocation(offset));
    }

    private int getCursorLocation(int offset) {
        return Util.moveCursor(this.text, this.selectionStart, offset);
    }

    public void eraseWords(int wordOffset) {
        if (!this.text.isEmpty()) {
            if (this.selectionEnd != this.selectionStart) {
                this.write("");
            } else {
                this.eraseCharacters(this.getWordSkipPosition(wordOffset) - this.selectionStart);
            }
        }
    }

    public void eraseCharacters(int characterOffset) {
        if (!this.text.isEmpty()) {
            if (this.selectionEnd != this.selectionStart) {
                this.write("");
            } else {
                int i = this.getCursorLocation(characterOffset);
                int j = Math.min(i, this.selectionStart);
                int k = Math.max(i, this.selectionStart);
                if (j != k) {
                    this.text = (new StringBuilder(this.text)).delete(j, k).toString();
                    this.onChanged(this.text);
                    this.setCursor(j);
                }
            }
        }
    }

    private void erase(int offset) {
        if (Screen.hasControlDown()) {
            this.eraseWords(offset);
        } else {
            this.eraseCharacters(offset);
        }

    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.isFocused()) {
            return false;
        } else {
            this.selecting = Screen.hasShiftDown();
            if (Screen.isSelectAll(keyCode)) {
                this.setCursorToEnd();
                this.setSelectionEnd(0);
                return true;
            } else if (Screen.isCopy(keyCode)) {
                MinecraftClient.getInstance().keyboard.setClipboard(this.getSelectedText());
                return true;
            } else if (Screen.isPaste(keyCode)) {
                this.write(MinecraftClient.getInstance().keyboard.getClipboard());

                return true;
            } else if (Screen.isCut(keyCode)) {
                MinecraftClient.getInstance().keyboard.setClipboard(this.getSelectedText());
                this.write("");

                return true;
            } else {
                switch (keyCode) {
                    case 259:
                        this.selecting = false;
                        this.erase(-1);
                        this.selecting = Screen.hasShiftDown();
                        return true;
                    case 260:
                    case 264:
                    case 265:
                    case 266:
                    case 267:
                    default:
                        return false;
                    case 261:
                        this.selecting = false;
                        this.erase(1);
                        this.selecting = Screen.hasShiftDown();
                        return true;
                    case 262:
                        if (Screen.hasControlDown()) {
                            this.setCursor(this.getWordSkipPosition(1));
                        } else {
                            this.moveCursor(1);
                        }

                        return true;
                    case 263:
                        if (Screen.hasControlDown()) {
                            this.setCursor(this.getWordSkipPosition(-1));
                        } else {
                            this.moveCursor(-1);
                        }

                        return true;
                    case 268:
                        this.setCursorToStart();
                        return true;
                    case 269:
                        this.setCursorToEnd();
                        return true;
                }
            }
        }
    }

    private void onChanged(String newText) {
        if (newText.isEmpty()) {
            this.setValue(this.emptyValue());
        } else {
            Optional<T> value = this.parse(newText);
            value.ifPresent(this::setValue);
        }
    }

    public boolean charTyped(char chr, int keyCode) {
        if (!this.isFocused()) {
            return false;
        } else if (SharedConstants.isValidChar(chr)) {
            this.write(Character.toString(chr));

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean isMouseOver = this.isMouseOver(mouseX, mouseY);
        this.setFocused(isMouseOver);

        if (this.isFocused() && isMouseOver && button == 0) {
            if (this.alignment == Alignment.RIGHT) {
                int i = MathHelper.floor(mouseX) - this.getX();
                if (this.isFocused()) {
                    i -= 4;
                }

                TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
                String string = textRenderer.trimToWidth(this.text.substring(this.firstCharacterIndex), this.width);
                this.setCursor(textRenderer.trimToWidth(string, i).length() + this.firstCharacterIndex);
            }

            return true;
        } else {
            return false;
        }
    }

    protected abstract Optional<T> parse(String value);

    public void setTextPredicate(Predicate<String> predicate) {
        this.textPredicate = predicate;
    }

    public TextWidgetComponent<T> setMaxLength(int length) {
        this.maxLength = length;
        return this;
    }

    @Override
    public boolean matches(String value) {
        return regex == null || value.matches(regex);
    }

    @Override
    public @Nullable String getRegex() {
        return regex;
    }

    @Override
    public void setRegex(String regex) {
        this.regex = regex;
    }

    @Override
    public boolean passes() {
        return matches(this.text) && (this.textPredicate == null || this.textPredicate.test(this.text));
    }

    @Override
    public void tick() {
        super.tick();

        this.focusedTicks++;
    }
}
