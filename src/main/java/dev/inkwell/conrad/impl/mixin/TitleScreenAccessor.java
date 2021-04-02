package dev.inkwell.conrad.impl.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(TitleScreen.class)
public interface TitleScreenAccessor {
    @Accessor long getBackgroundFadeStart();
    @Accessor void setBackgroundFadeStart(long l);
    @Accessor boolean getDoBackgroundFade();
    @Accessor RotatingCubeMapRenderer getBackgroundRenderer();
}
