package com.flapjacck.swing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flapjacck.swing.command.SwingCommandHandler;
import com.flapjacck.swing.config.SwingConfig;

import net.fabricmc.api.ClientModInitializer;

/** Client entrypoint — holds the mod ID, logger, and initialization. */
public final class Swing implements ClientModInitializer {
	public static final String MOD_ID = "swing";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		SwingConfig.load();
		SwingCommandHandler.register();
		LOGGER.info("Swing loaded (scale {}, swing x{})",
				SwingConfig.HELD_ITEM_SCALE,
				SwingConfig.SWING_SPEED_MULTIPLIER);
	}
}
