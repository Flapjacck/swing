package com.example.itemtweaks.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.LivingEntity;

import com.example.itemtweaks.ItemTweaksConfig;

/**
 * VISUAL SWING ANIMATION SPEED (client only).
 * LivingEntity#getCurrentSwingDuration() is used to advance the arm swing.
 * Dividing it speeds the animation up. Does not change server-side combat.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntitySwingMixin {
	@Inject(method = "getCurrentSwingDuration", at = @At("RETURN"), cancellable = true)
	private void itemtweaks$scaleSwingDuration(CallbackInfoReturnable<Integer> cir) {
		float mult = ItemTweaksConfig.SWING_SPEED_MULTIPLIER;
		if (mult == 1.0f || mult <= 0f) return;
		// Must stay >= 1 tick or the swing never completes properly.
		cir.setReturnValue(Math.max(1, Math.round(cir.getReturnValueI() / mult)));
	}
}
