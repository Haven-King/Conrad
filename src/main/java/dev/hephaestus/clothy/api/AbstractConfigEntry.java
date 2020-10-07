package dev.hephaestus.clothy.api;

import dev.hephaestus.clothy.impl.gui.AbstractConfigScreen;
import dev.hephaestus.clothy.impl.gui.widget.DynamicElementListWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public abstract class AbstractConfigEntry<T> extends DynamicElementListWidget.ElementEntry<AbstractConfigEntry<T>> implements ReferenceProvider<T> {
    @Nullable private final Consumer<T> saveConsumer;
    private final Supplier<T> defaultValue;
    private AbstractConfigScreen screen;
    private Supplier<Optional<Text>> errorSupplier;
    @Nullable private List<ReferenceProvider<?>> referencableEntries = null;

    public AbstractConfigEntry(@Nullable Consumer<T> saveConsumer, Supplier<T> defaultValue) {
        this.saveConsumer = saveConsumer;
        this.defaultValue = defaultValue;
    }
    
    public final void setReferenceProviderEntries(@Nullable List<ReferenceProvider<?>> referencableEntries) {
        this.referencableEntries = referencableEntries;
    }
    
    public void requestReferenceRebuilding() {
        AbstractConfigScreen configScreen = getConfigScreen();
        if (configScreen instanceof ReferenceBuildingConfigScreen) {
            ((ReferenceBuildingConfigScreen) configScreen).requestReferenceRebuilding();
        }
    }
    
    @Override
    public @NotNull
	AbstractConfigEntry<T> provideReferenceEntry() {
        return this;
    }

    @Nullable
    public final List<ReferenceProvider<?>> getReferenceProviderEntries() {
        return referencableEntries;
    }
    
    public abstract boolean isRequiresRestart();
    
    public abstract void setRequiresRestart(boolean requiresRestart);
    
    public abstract Text getFieldName();
    
    public Text getDisplayedFieldName() {
        MutableText text = getFieldName().shallowCopy();
        boolean hasError = getConfigError().isPresent();
        boolean isEdited = isEdited();
        if (hasError)
            text = text.formatted(Formatting.RED);
        if (isEdited)
            text = text.formatted(Formatting.ITALIC);
        if (!hasError && !isEdited)
            text = text.formatted(Formatting.GRAY);
        return text;
    }
    
    public abstract T getValue();
    
    public final Optional<Text> getConfigError() {
        if (errorSupplier != null && errorSupplier.get().isPresent())
            return errorSupplier.get();
        return getError();
    }
    
    public void lateRender(MatrixStack matrices, int mouseX, int mouseY, float delta) {}
    
    public void setErrorSupplier(Supplier<Optional<Text>> errorSupplier) {
        this.errorSupplier = errorSupplier;
    }
    
    public Optional<Text> getError() {
        return Optional.empty();
    }
    
    public final Optional<T> getDefaultValue() {
        return defaultValue == null ? Optional.empty() : Optional.ofNullable(defaultValue.get());
    }
    
    @Nullable
    public final AbstractConfigScreen getConfigScreen() {
        return screen;
    }
    
    public final void addTooltip(@NotNull Tooltip tooltip) {
        screen.addTooltip(tooltip);
    }
    
    public void updateSelected(boolean isSelected) {}
    
    public final void setScreen(AbstractConfigScreen screen) {
        this.screen = screen;
    }
    
    public final void save() {
        this.saveConsumer.accept(this.getValue());
    }
    
    public boolean isEdited() {
        return getConfigError().isPresent();
    }
    
    @Override
    public int getItemHeight() {
        return 24;
    }
    
    public int getInitialReferenceOffset() {
        return 0;
    }
}
