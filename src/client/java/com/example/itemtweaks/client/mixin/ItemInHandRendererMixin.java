package com.example.itemtweaks.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import com.example.itemtweaks.ItemTweaksConfig;

/**
 * FIRST-PERSON HELD ITEM SCALE.
 * Hook: ItemInHandRenderer#renderItem. Scaling here resizes the item model, not the arm.
 * Push at HEAD and pop at RETURN so the matrix stack stays balanced.
 */
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
	@Inject(method = "renderItem", at = @At("HEAD"))
	private void itemtweaks$pushAndScale(LivingEntity entity, ItemStack stack, ItemDisplayContext ctx,
			PoseStack poseStack, SubmitNodeCollector collector, int light, CallbackInfo ci) {
		poseStack.pushPose();
		float s = ItemTweaksConfig.HELD_ITEM_SCALE;
		if (s != 1.0f) {
			poseStack.scale(s, s, s);
		}
	}

	@Inject(method = "renderItem", at = @At("RETURN"))
	private void itemtweaks$pop(LivingEntity entity, ItemStack stack, ItemDisplayContext ctx,
			PoseStack poseStack, SubmitNodeCollector collector, int light, CallbackInfo ci) {
		poseStack.popPose();
	}
}
