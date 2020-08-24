package dev.hephaestus.clothy.impl;

import dev.hephaestus.clothy.api.Modifier;
import dev.hephaestus.clothy.api.ModifierKey;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

@Environment(EnvType.CLIENT)
public class ModifierKeyImpl implements ModifierKey {
    private InputUtil.Key keyCode;
    private Modifier modifier;
    
    public ModifierKeyImpl() {
    }
    
    @Override
    public InputUtil.Key getCode() {
        return keyCode;
    }
    
    @Override
    public Modifier getModifier() {
        return modifier;
    }
    
    @Override
    public ModifierKey setKey(InputUtil.Key keyCode) {
        this.keyCode = keyCode.getCategory().createFromCode(keyCode.getCode());
        if (keyCode.equals(InputUtil.UNKNOWN_KEY))
            setModifier(Modifier.none());
        return this;
    }
    
    @Override
    public ModifierKey setModifier(Modifier modifier) {
        this.modifier = Modifier.of(modifier.getValue());
        return this;
    }
    
    @Override
    public String toString() {
        return getLocalizedName().getString();
    }
    
    @Override
    public Text getLocalizedName() {
        Text base = this.keyCode.getLocalizedText();
        if (modifier.hasShift())
            base = new TranslatableText("modifier.cloth-config.shift", base);
        if (modifier.hasControl())
            base = new TranslatableText("modifier.cloth-config.ctrl", base);
        if (modifier.hasAlt())
            base = new TranslatableText("modifier.cloth-config.alt", base);
        return base;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ModifierKey))
            return false;
        ModifierKey that = (ModifierKey) o;
        return keyCode.equals(that.getCode()) && modifier.equals(that.getModifier());
    }
    
    @Override
    public int hashCode() {
        int result = keyCode != null ? keyCode.hashCode() : 0;
        result = 31 * result + (modifier != null ? modifier.hashCode() : 0);
        return result;
    }
}
