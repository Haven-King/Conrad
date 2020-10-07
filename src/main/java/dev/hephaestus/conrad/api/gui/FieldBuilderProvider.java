package dev.hephaestus.conrad.api.gui;

import dev.hephaestus.clothy.api.ConfigBuilder;
import dev.hephaestus.clothy.impl.builders.FieldBuilder;
import dev.hephaestus.conrad.api.Conrad;
import dev.hephaestus.conrad.impl.common.config.ValueContainer;
import dev.hephaestus.conrad.impl.common.keys.KeyRing;
import dev.hephaestus.conrad.impl.common.keys.ValueKey;
import dev.hephaestus.conrad.impl.common.util.ConradUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface FieldBuilderProvider<T> {
	/**
	 * Returns a {@link FieldBuilder} that will build a widget for the given value.
	 * @param configBuilder the parent of the {@link FieldBuilder} that is returned
	 * @param valueContainer the {@link ValueContainer} that stores the current value of the {@link ValueKey}
	 * @param valueKey the {@link ValueKey} that points to the value this {@link FieldBuilder} will be for
	 * @return a {@link FieldBuilder} that can be used to make a new widget
	 */
	FieldBuilder<T, ?> getBuilder(ConfigBuilder configBuilder, ValueContainer valueContainer, ValueKey valueKey);

}
