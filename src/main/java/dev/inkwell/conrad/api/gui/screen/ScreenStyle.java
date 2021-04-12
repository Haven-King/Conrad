package dev.inkwell.conrad.api.gui.screen;

import net.minecraft.util.Identifier;

import java.util.Optional;

public class ScreenStyle {
    public static final ScreenStyle DEFAULT = new ScreenStyle();

    private boolean renderBackgroundTexture = true;
    private Identifier backgroundTexture = new Identifier("textures/block/dirt.png");
    private int backgroundColor = 0xFF404040;

    public ScreenStyle() {

    }

    public ScreenStyle(Identifier backgroundTexture) {
        this.backgroundTexture = backgroundTexture;
    }

    public Optional<Identifier> getBackgroundTexture() {
        return this.renderBackgroundTexture ? Optional.of(backgroundTexture) : Optional.empty();
    }

    public int getBackgroundColor() {
        return 0xFF404040;
    }

    public ScreenStyle backgroundTexture(Identifier texture) {
        this.backgroundTexture = texture;
        return this;
    }

    public ScreenStyle renderBackground(boolean bl) {
        this.renderBackgroundTexture = bl;
        return this;
    }

    public ScreenStyle backgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }
}
