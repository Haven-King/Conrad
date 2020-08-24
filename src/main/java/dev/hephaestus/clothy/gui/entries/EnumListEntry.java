package dev.hephaestus.clothy.gui.entries;

import dev.hephaestus.conrad.annotations.ApiStatus;
import dev.hephaestus.conrad.annotations.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class EnumListEntry<T extends Enum<?>> extends SelectionListEntry<T> {
    public static final Function<Enum<?>, Text> DEFAULT_NAME_PROVIDER = t -> new TranslatableText(t instanceof Translatable ? ((Translatable) t).getCode() : t.toString());

    @ApiStatus.Internal
    public EnumListEntry(Text fieldName, Class<T> clazz, T value, Text resetButtonKey, Supplier<T> defaultValue, Consumer<T> saveConsumer, Function<Enum<?>, Text> enumNameProvider, @Nullable Supplier<Optional<List<Text>>> tooltipSupplier, boolean requiresRestart) {
        super(fieldName, clazz.getEnumConstants(), value, resetButtonKey, defaultValue, saveConsumer, enumNameProvider::apply, tooltipSupplier, requiresRestart);
    }
}
