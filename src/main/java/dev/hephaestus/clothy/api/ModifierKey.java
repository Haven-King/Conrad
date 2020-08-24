package dev.hephaestus.clothy.api;

import dev.hephaestus.clothy.impl.ModifierKeyImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public interface ModifierKey {
    static ModifierKey of(InputUtil.Key keyCode, Modifier modifier) {
        return new ModifierKeyImpl().setKeyAndModifier(keyCode, modifier);
    }
    
    static ModifierKey copyOf(ModifierKey code) {
        return of(code.getCode(), code.getModifier());
    }
    
    static ModifierKey unknown() {
        return of(InputUtil.UNKNOWN_KEY, Modifier.none());
    }
    
    InputUtil.Key getCode();
    
    ModifierKey setKey(InputUtil.Key keyCode);
    
    default InputUtil.Type getType() {
        return getCode().getCategory();
    }
    
    Modifier getModifier();
    
    ModifierKey setModifier(Modifier modifier);
    
    default ModifierKey copy() {
        return copyOf(this);
    }
    
    default boolean matchesMouse(int button) {
        return !isUnknown() && getType() == InputUtil.Type.MOUSE && getCode().getCode() == button && getModifier().matchesCurrent();
    }
    
    default boolean matchesKey(int keyCode, int scanCode) {
        if (isUnknown())
            return false;
        if (keyCode == InputUtil.UNKNOWN_KEY.getCode()) {
            return getType() == InputUtil.Type.SCANCODE && getCode().getCode() == scanCode && getModifier().matchesCurrent();
        } else {
            return getType() == InputUtil.Type.KEYSYM && getCode().getCode() == keyCode && getModifier().matchesCurrent();
        }
    }
    
    default boolean matchesCurrentMouse() {
        if (!isUnknown() && getType() == InputUtil.Type.MOUSE && getModifier().matchesCurrent()) {
            return GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), getCode().getCode()) == GLFW.GLFW_PRESS;
        }
        return false;
    }
    
    default boolean matchesCurrentKey() {
        return !isUnknown() && getType() == InputUtil.Type.KEYSYM && getModifier().matchesCurrent() && InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), getCode().getCode());
    }
    
    default ModifierKey setKeyAndModifier(InputUtil.Key keyCode, Modifier modifier) {
        setKey(keyCode);
        setModifier(modifier);
        return this;
    }
    
    default ModifierKey clearModifier() {
        return setModifier(Modifier.none());
    }
    
    String toString();
    
    Text getLocalizedName();
    
    default boolean isUnknown() {
        return getCode().equals(InputUtil.UNKNOWN_KEY);
    }
}
