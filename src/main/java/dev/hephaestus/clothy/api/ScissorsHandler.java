package dev.hephaestus.clothy.api;

import dev.hephaestus.clothy.impl.ScissorsHandlerImpl;
import dev.hephaestus.math.impl.Rectangle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;

@Environment(EnvType.CLIENT)
public interface ScissorsHandler {
    ScissorsHandler INSTANCE = ScissorsHandlerImpl.INSTANCE;
    
    void clearScissors();
    
    List<Rectangle> getScissorsAreas();
    
    void scissor(Rectangle rectangle);
    
    void removeLastScissor();
    
    void applyScissors();
}
