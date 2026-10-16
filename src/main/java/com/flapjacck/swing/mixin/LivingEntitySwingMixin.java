package com.flapjacck.swing.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.LivingEntity;

import com.flapjacck.swing.config.SwingConfig;

/**
 * VISUAL SWING ANIMATION SPEED (client only).
 * LivingEntity#getCurrentSwingDuration() is used to advance the arm swing.
 * Dividing it speeds the animation up. Does not change server-side combat.
 *
 * When IGNORE_HASTE is enabled, the vanilla base of 6 ticks is used instead
 * of the haste-affected value, so animation speed stays constant regardless
 * of potion effects or gear that grants mining speed / haste.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntitySwingMixin {

	/** Vanilla swing duration with no haste or mining fatigue (ticks). */
	private static final int VANILLA_BASE_DURATION = 6;

	@Inject(method = "getCurrentSwingDuration", at = @At("RETURN"), cancellable = true)
	private void swing$scaleSwingDuration(CallbackInfoReturnable<Integer> cir) {
		float mult = SwingConfig.SWING_SPEED_MULTIPLIER;

		// Determine the base duration to scale from.
		// When ignoreHaste is on, always use the no-haste base so the animation
		// is identical everywhere regardless of active status effects or gear.
		int base = SwingConfig.IGNORE_HASTE ? VANILLA_BASE_DURATION : cir.getReturnValueI();

		if (mult == 1.0f && !SwingConfig.IGNORE_HASTE) return; // nothing to change
		if (mult <= 0f) return;

		// Must stay >= 1 tick or the swing never completes properly.
		cir.setReturnValue(Math.max(1, Math.round(base / mult)));
	}
}

