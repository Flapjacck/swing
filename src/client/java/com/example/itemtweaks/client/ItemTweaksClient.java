package com.example.itemtweaks.client;

import com.example.itemtweaks.ItemTweaks;
import com.example.itemtweaks.ItemTweaksConfig;

import net.fabricmc.api.ClientModInitializer;

public class ItemTweaksClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ItemTweaksConfig.load();
		SwingCommandHandler.register();
		ItemTweaks.LOGGER.info("Swing loaded (scale {}, swing x{})",
				ItemTweaksConfig.HELD_ITEM_SCALE,
				ItemTweaksConfig.SWING_SPEED_MULTIPLIER);
	}
}
