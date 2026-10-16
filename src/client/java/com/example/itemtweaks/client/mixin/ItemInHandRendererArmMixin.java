package com.example.itemtweaks.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;

import com.example.itemtweaks.ItemTweaksConfig;

/**
 * ARM MESH SCALING — bare hand only.
 *
 * AvatarRenderer#renderRightHand / renderLeftHand are the only two methods
 * that render the actual arm mesh geometry in first-person.  They are called
 * from ItemInHandRenderer#renderPlayerArm, which itself is only invoked when
 * the hand is EMPTY (no item held).  When holding an item, the arm geometry
 * is rendered by the entity-renderer pipeline, which is entirely separate
 * from ItemInHandRenderer and cannot be intercepted here.
 *
 * Result: "Apply Hand Scale" visually scales the bare arm when empty-handed.
 * The scale has no effect while holding an item (the entity renderer controls
 * that arm and is out of scope for this mod).
 */
@Mixin(AvatarRenderer.class)
public abstract class ItemInHandRendererArmMixin {

	@Inject(method = "renderRightHand", at = @At("HEAD"))
	private void itemtweaks$scaleRightArm(PoseStack poseStack, SubmitNodeCollector collector,
			int light, Identifier skinTexture, boolean slim, CallbackInfo ci) {
		if (!ItemTweaksConfig.APPLY_HAND_SCALE) return;
		poseStack.pushPose();
		float s = ItemTweaksConfig.HELD_ITEM_SCALE;
		if (s != 1.0f) poseStack.scale(s, s, s);
	}

	@Inject(method = "renderRightHand", at = @At("RETURN"))
	private void itemtweaks$unscaleRightArm(PoseStack poseStack, SubmitNodeCollector collector,
			int light, Identifier skinTexture, boolean slim, CallbackInfo ci) {
		if (!ItemTweaksConfig.APPLY_HAND_SCALE) return;
		poseStack.popPose();
	}

	@Inject(method = "renderLeftHand", at = @At("HEAD"))
	private void itemtweaks$scaleLeftArm(PoseStack poseStack, SubmitNodeCollector collector,
			int light, Identifier skinTexture, boolean slim, CallbackInfo ci) {
		if (!ItemTweaksConfig.APPLY_HAND_SCALE) return;
		poseStack.pushPose();
		float s = ItemTweaksConfig.HELD_ITEM_SCALE;
		if (s != 1.0f) poseStack.scale(s, s, s);
	}

	@Inject(method = "renderLeftHand", at = @At("RETURN"))
	private void itemtweaks$unscaleLeftArm(PoseStack poseStack, SubmitNodeCollector collector,
			int light, Identifier skinTexture, boolean slim, CallbackInfo ci) {
		if (!ItemTweaksConfig.APPLY_HAND_SCALE) return;
		poseStack.popPose();
	}
}
