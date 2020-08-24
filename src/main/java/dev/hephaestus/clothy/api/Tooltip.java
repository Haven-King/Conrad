package dev.hephaestus.clothy.api;

import dev.hephaestus.math.impl.Point;
import net.minecraft.text.Text;

import java.util.List;

public interface Tooltip {
    static Tooltip of(Point location, List<Text> text) {
        return QueuedTooltip.create(location, text);
    }

    Point getPoint();
    
    default int getX() {
        return getPoint().getX();
    }
    
    default int getY() {
        return getPoint().getY();
    }
    
    List<Text> getText();
}
