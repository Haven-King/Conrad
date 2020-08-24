package dev.hephaestus.clothy.impl;

import dev.hephaestus.conrad.annotations.Nullable;
import dev.hephaestus.clothy.api.AbstractConfigListEntry;
import dev.hephaestus.clothy.api.ConfigCategory;
import dev.hephaestus.clothy.api.EntryContainer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class ConfigCategoryImpl implements ConfigCategory {
    
    private final Supplier<List<Object>> listSupplier;
    private final Consumer<Identifier> backgroundConsumer;
    private final Runnable destroyCategory;
    private final Text categoryKey;

    private @Nullable Supplier<Optional<List<Text>>> tooltipSupplier;
    
    ConfigCategoryImpl(Text categoryKey, Consumer<Identifier> backgroundConsumer, Supplier<List<Object>> listSupplier, Runnable destroyCategory) {
        this.listSupplier = listSupplier;
        this.backgroundConsumer = backgroundConsumer;
        this.categoryKey = categoryKey;
        this.destroyCategory = destroyCategory;
    }
    
    @Override
    public Text getCategoryKey() {
        return categoryKey;
    }

    @Override
    public EntryContainer addEntry(AbstractConfigListEntry<?> entry) {
        listSupplier.get().add(entry);
        return this;
    }
    
    @Override
    public ConfigCategory setCategoryBackground(Identifier identifier) {
        backgroundConsumer.accept(identifier);
        return this;
    }
    
    @Override
    public void removeCategory() {
        destroyCategory.run();
    }

    public @Nullable Supplier<Optional<List<Text>>> getTooltipSupplier() {
        return tooltipSupplier;
    }

    public ConfigCategory setTooltipSupplier(@Nullable Supplier<Optional<List<Text>>> tooltipSupplier) {
        this.tooltipSupplier = tooltipSupplier;
        return this;
    }

}
