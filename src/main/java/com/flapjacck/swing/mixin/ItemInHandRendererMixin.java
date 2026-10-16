package com.flapjacck.swing.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import com.flapjacck.swing.config.SwingConfig;

/**
 * FIRST-PERSON HELD ITEM TRANSFORM (item model only).
 * Applies offset (translate), tilt (pitch + roll rotation), and scale.
 * Order: translate → rotate → scale, so offset is in unscaled/unrotated space.
 * Push at HEAD and pop at RETURN always keeps the matrix stack balanced.
 */
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
	@Inject(method = "renderItem", at = @At("HEAD"))
	private void swing$pushAndTransform(LivingEntity entity, ItemStack stack, ItemDisplayContext ctx,
			PoseStack poseStack, SubmitNodeCollector collector, int light, CallbackInfo ci) {
		poseStack.pushPose();

		// 1. Positional offset — applied first, in vanilla model space.
		float ox = SwingConfig.HELD_ITEM_OFFSET_X;
		float oy = SwingConfig.HELD_ITEM_OFFSET_Y;
		float oz = SwingConfig.HELD_ITEM_OFFSET_Z;
		if (ox != 0.0f || oy != 0.0f || oz != 0.0f) {
			poseStack.translate(ox, oy, oz);
		}

		// 2. Tilt — pitch (X-axis) tips the item up/down; roll (Z-axis) slants it sideways.
		float pitch = SwingConfig.ITEM_TILT_PITCH;
		float roll  = SwingConfig.ITEM_TILT_ROLL;
		if (pitch != 0.0f) poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
		if (roll  != 0.0f) poseStack.mulPose(Axis.ZP.rotationDegrees(roll));

		// 3. Scale.
		float s = SwingConfig.HELD_ITEM_SCALE;
		if (s != 1.0f) {
			poseStack.scale(s, s, s);
		}
	}

	@Inject(method = "renderItem", at = @At("RETURN"))
	private void swing$pop(LivingEntity entity, ItemStack stack, ItemDisplayContext ctx,
			PoseStack poseStack, SubmitNodeCollector collector, int light, CallbackInfo ci) {
		poseStack.popPose();
	}
}
