package dev.hephaestus.clothy;

import dev.hephaestus.clothy.impl.EasingMethod;
import dev.hephaestus.clothy.impl.EasingMethod.EasingMethodImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class ClothConfigInitializer {
    public static final Logger LOGGER = LogManager.getFormatterLogger("ClothConfig");

    public static EasingMethod getEasingMethod() {
        return EasingMethodImpl.NONE;
    }
    
    public static long getScrollDuration() {
        return 0;
    }
    
    public static double getScrollStep() {
        return 16.0;
    }
    
    public static double getBounceBackMultiplier() {
        return -10;
    }
}
