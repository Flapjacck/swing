package com.flapjacck.swing.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;

import com.flapjacck.swing.config.SwingConfig;

/**
 * ARM MESH TRANSFORM — bare hand only (empty-handed first-person).
 *
 * AvatarRenderer#renderRightHand / renderLeftHand render the actual arm mesh
 * geometry in first-person. They are called from ItemInHandRenderer#renderPlayerArm
 * only when the hand is EMPTY. All transforms here (offset, tilt, scale) are
 * gated behind APPLY_HAND_SCALE so they only fire when the user has opted in.
 *
 * Order: translate (offset) → rotate (tilt) → scale.
 */
@Mixin(AvatarRenderer.class)
public abstract class ItemInHandRendererArmMixin {

	@Inject(method = "renderRightHand", at = @At("HEAD"))
	private void swing$transformRightArm(PoseStack poseStack, SubmitNodeCollector collector,
			int light, Identifier skinTexture, boolean slim, CallbackInfo ci) {
		if (!SwingConfig.APPLY_HAND_SCALE) return;
		poseStack.pushPose();

		// 1. Positional offset — separate values from the held item offsets.
		float ox = SwingConfig.ARM_OFFSET_X;
		float oy = SwingConfig.ARM_OFFSET_Y;
		float oz = SwingConfig.ARM_OFFSET_Z;
		if (ox != 0.0f || oy != 0.0f || oz != 0.0f) poseStack.translate(ox, oy, oz);

		// 2. Tilt — pitch (X) tips up/down; roll (Z) slants sideways.
		float pitch = SwingConfig.ARM_TILT_PITCH;
		float roll  = SwingConfig.ARM_TILT_ROLL;
		if (pitch != 0.0f) poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
		if (roll  != 0.0f) poseStack.mulPose(Axis.ZP.rotationDegrees(roll));

		// 3. Scale.
		float s = SwingConfig.HELD_ITEM_SCALE;
		if (s != 1.0f) poseStack.scale(s, s, s);
	}

	@Inject(method = "renderRightHand", at = @At("RETURN"))
	private void swing$unscaleRightArm(PoseStack poseStack, SubmitNodeCollector collector,
			int light, Identifier skinTexture, boolean slim, CallbackInfo ci) {
		if (!SwingConfig.APPLY_HAND_SCALE) return;
		poseStack.popPose();
	}

	@Inject(method = "renderLeftHand", at = @At("HEAD"))
	private void swing$transformLeftArm(PoseStack poseStack, SubmitNodeCollector collector,
			int light, Identifier skinTexture, boolean slim, CallbackInfo ci) {
		if (!SwingConfig.APPLY_HAND_SCALE) return;
		poseStack.pushPose();

		// 1. Positional offset.
		float ox = SwingConfig.ARM_OFFSET_X;
		float oy = SwingConfig.ARM_OFFSET_Y;
		float oz = SwingConfig.ARM_OFFSET_Z;
		if (ox != 0.0f || oy != 0.0f || oz != 0.0f) poseStack.translate(ox, oy, oz);

		// 2. Tilt.
		float pitch = SwingConfig.ARM_TILT_PITCH;
		float roll  = SwingConfig.ARM_TILT_ROLL;
		if (pitch != 0.0f) poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
		if (roll  != 0.0f) poseStack.mulPose(Axis.ZP.rotationDegrees(roll));

		// 3. Scale.
		float s = SwingConfig.HELD_ITEM_SCALE;
		if (s != 1.0f) poseStack.scale(s, s, s);
	}

	@Inject(method = "renderLeftHand", at = @At("RETURN"))
	private void swing$unscaleLeftArm(PoseStack poseStack, SubmitNodeCollector collector,
			int light, Identifier skinTexture, boolean slim, CallbackInfo ci) {
		if (!SwingConfig.APPLY_HAND_SCALE) return;
		poseStack.popPose();
	}
}
