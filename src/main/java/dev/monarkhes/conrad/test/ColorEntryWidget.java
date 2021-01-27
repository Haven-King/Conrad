package dev.monarkhes.conrad.test;

import dev.monarkhes.vivid.screen.ConfigScreen;
import dev.monarkhes.vivid.util.Alignment;
import dev.monarkhes.vivid.widgets.value.entry.TextWidgetComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ColorEntryWidget extends TextWidgetComponent<Color> {
    public ColorEntryWidget(ConfigScreen parent, int x, int y, int width, int height, Alignment alignment, Supplier<@NotNull Color> defaultValueSupplier, Consumer<Color> changedListener, Consumer<Color> saveConsumer, @NotNull Color value) {
        super(parent, x, y, width, height, alignment, defaultValueSupplier, changedListener, saveConsumer, value);
    }

    @Override
    protected String valueOf(Color value) {
        if (value.value == -1) {
            return "0xFFFFFFFF";
        } else {
            return "0x" + Integer.toUnsignedString(value.value, 16).toUpperCase(Locale.ROOT);
        }
    }

    @Override
    protected int getColor() {
        return this.hasError() ? super.getColor() : this.getValue().value;
    }

    @Override
    protected Color emptyValue() {
        return new Color(-1);
    }

    @Override
    protected Optional<Color> parse(String value) {
        try {
            if (value.equalsIgnoreCase("0xFFFFFFFF")) {
                return Optional.of(new Color(-1));
            } else {
                return Optional.of(new Color(Integer.parseUnsignedInt(value.startsWith("0x")
                        ? value.substring(2) : value, 16)));
            }
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
