package dev.hephaestus.conrad.impl.mixin.client;

import dev.hephaestus.conrad.impl.ConradUtils;
import dev.hephaestus.conrad.impl.client.screen.ModConfigListScreen;
import dev.hephaestus.conrad.impl.client.widget.TexturedButtonWidget;
import dev.hephaestus.conrad.impl.config.ConfigManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
@Environment(EnvType.CLIENT)
public class GameMenuScreenButton extends Screen {
	@Unique
	private static final Identifier CONFIGURE_BUTTON_LOCATION = new Identifier(ConradUtils.MOD_ID, "textures/gui/configure_button.png");

	protected GameMenuScreenButton(Text title) {
		super(title);
	}

	@Inject(method = "initWidgets", at = @At("TAIL"))
	private void showConfigButton(CallbackInfo ci) {
		if (ConfigManager.keyCount() > 0) {
			ButtonWidget configButton = new TexturedButtonWidget(this.width - 20, 12, 20, 20, 0, 0, CONFIGURE_BUTTON_LOCATION, 32, 64, (button) -> {
				this.client.openScreen(new ModConfigListScreen(this));
			});

			this.addButton(configButton);
		}
	}
}
