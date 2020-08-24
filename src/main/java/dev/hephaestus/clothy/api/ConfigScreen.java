package dev.hephaestus.clothy.api;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;
import dev.hephaestus.conrad.annotations.Nullable;

import java.util.function.Consumer;

public interface ConfigScreen {
    void setSavingRunnable(@Nullable Runnable savingRunnable);
    
    void setAfterInitConsumer(@Nullable Consumer<Screen> afterInitConsumer);
    
    Identifier getBackgroundLocation();
    
    boolean isRequiresRestart();
    
    boolean isEdited();

    void saveAll(boolean openOtherScreens);
    
    void addTooltip(Tooltip tooltip);
}
