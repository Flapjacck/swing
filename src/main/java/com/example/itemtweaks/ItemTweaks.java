package com.example.itemtweaks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Shared id / logger. Mixins and the client entrypoint do the work. */
public final class ItemTweaks {
	public static final String MOD_ID = "swing";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private ItemTweaks() {}
}
