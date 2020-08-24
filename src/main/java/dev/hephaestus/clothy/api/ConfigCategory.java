package dev.hephaestus.clothy.api;

import dev.hephaestus.conrad.annotations.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public interface ConfigCategory extends EntryContainer {
    
    Text getCategoryKey();

    ConfigCategory setCategoryBackground(Identifier identifier);

    @Nullable
    Supplier<Optional<List<Text>>> getTooltipSupplier();

    ConfigCategory setTooltipSupplier(@Nullable Supplier<Optional<List<Text>>> tooltipSupplier);

    void removeCategory();
    
}
