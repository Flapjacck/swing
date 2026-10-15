package com.example.itemtweaks;

/**
 * All tweakable values live here. Edit, rebuild, done (no config file / GUI on purpose).
 */
public final class ItemTweaksConfig {
	private ItemTweaksConfig() {}

	/** First-person held item size. 1.0 = vanilla, 0.5 = half, 1.5 = 50% bigger. */
	public static final float HELD_ITEM_SCALE = 0.75f;

	/**
	 * Arm-swing animation speed. 1.0 = vanilla, 2.0 = twice as fast, 0.5 = half.
	 * Visual only: shortens/lengthens LivingEntity swing duration (ticks) on this client.
	 */
	public static final float SWING_SPEED_MULTIPLIER = 1.0f;

}
