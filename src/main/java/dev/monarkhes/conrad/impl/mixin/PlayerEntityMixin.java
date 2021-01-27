/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.monarkhes.conrad.impl.mixin;

import dev.monarkhes.conrad.test.Color;
import dev.monarkhes.conrad.test.TestConfig;
import net.fabricmc.loader.api.config.util.Table;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.*;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
	private void setNameColor(CallbackInfoReturnable<Text> cir) {
		if (this.getScoreboardTeam() == null) {
			MutableText text = ((MutableText) cir.getReturnValue()).styled(style -> style.withColor(TextColor.fromRgb(
					TestConfig.MY_FAVORITE_COLOR.getValue(this.getUuid()).value
			)));

			MutableText tags = new LiteralText("");

			for (Iterator<Table.Entry<String, Color>> iterator = TestConfig.TAGS.getValue(this.getUuid()).iterator(); iterator.hasNext(); ) {
				Table.Entry<String, Color> entry = iterator.next();
				tags.append(
						new LiteralText(entry.getKey())
								.styled(style -> style.withColor(TextColor.fromRgb(entry.getValue().value)))
				);

				if (iterator.hasNext()) {
					tags.append(new LiteralText("\n").styled(style -> Style.EMPTY));
				}
			}

			text.styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tags)));
		}
	}
}
