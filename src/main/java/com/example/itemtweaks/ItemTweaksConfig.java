package com.example.itemtweaks;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Mth;

/**
 * Live tweak values. Mixins read these every frame; GUI writes them in place.
 * Persisted to {@code config/swing.json}.
 */
public final class ItemTweaksConfig {
	public static final float MIN = 0.1f;
	public static final float MAX = 1.0f;
	public static final float DEFAULT_HELD_ITEM_SCALE = 1.0f;
	public static final float DEFAULT_SWING_SPEED = 1.0f;
	public static final boolean DEFAULT_IGNORE_HASTE = false;
	public static final boolean DEFAULT_APPLY_HAND_SCALE = false;

	/** First-person held item size. 1.0 = vanilla. */
	public static float HELD_ITEM_SCALE = DEFAULT_HELD_ITEM_SCALE;

	/** Arm-swing animation speed. 1.0 = vanilla; lower is slower. Visual only. */
	public static float SWING_SPEED_MULTIPLIER = DEFAULT_SWING_SPEED;

	/**
	 * When true, the swing-duration mixin ignores haste / mining-fatigue and
	 * always starts from the vanilla base of 6 ticks before applying the
	 * multiplier.  Keeps animation speed constant across islands and gear sets.
	 */
	public static boolean IGNORE_HASTE = DEFAULT_IGNORE_HASTE;

	/** When true, applies HELD_ITEM_SCALE to the player's hand/arm as well. */
	public static boolean APPLY_HAND_SCALE = DEFAULT_APPLY_HAND_SCALE;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private ItemTweaksConfig() {}

	public static float clamp(float value) {
		return Math.round(Mth.clamp(value, MIN, MAX) * 100f) / 100f;
	}

	public static void resetToVanilla() {
		HELD_ITEM_SCALE = DEFAULT_HELD_ITEM_SCALE;
		SWING_SPEED_MULTIPLIER = DEFAULT_SWING_SPEED;
		IGNORE_HASTE = DEFAULT_IGNORE_HASTE;
		APPLY_HAND_SCALE = DEFAULT_APPLY_HAND_SCALE;
	}

	public static void load() {
		Path file = file();
		if (!Files.isRegularFile(file)) {
			save();
			return;
		}
		try (Reader reader = Files.newBufferedReader(file)) {
			FileData data = GSON.fromJson(reader, FileData.class);
			if (data == null) {
				return;
			}
			HELD_ITEM_SCALE = clamp(data.heldItemScale);
			SWING_SPEED_MULTIPLIER = clamp(data.swingSpeedMultiplier);
			IGNORE_HASTE = data.ignoreHaste;
			APPLY_HAND_SCALE = data.applyHandScale;
		} catch (Exception e) {
			ItemTweaks.LOGGER.warn("Could not read {}, using vanilla defaults", file, e);
			resetToVanilla();
		}
	}

	public static void save() {
		Path file = file();
		try {
			Files.createDirectories(file.getParent());
			try (Writer writer = Files.newBufferedWriter(file)) {
				FileData data = new FileData();
				data.heldItemScale = clamp(HELD_ITEM_SCALE);
				data.swingSpeedMultiplier = clamp(SWING_SPEED_MULTIPLIER);
				data.ignoreHaste = IGNORE_HASTE;
				data.applyHandScale = APPLY_HAND_SCALE;
				GSON.toJson(data, writer);
			}
		} catch (IOException e) {
			ItemTweaks.LOGGER.warn("Could not write {}", file, e);
		}
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("swing.json");
	}

	private static final class FileData {
		float heldItemScale = DEFAULT_HELD_ITEM_SCALE;
		float swingSpeedMultiplier = DEFAULT_SWING_SPEED;
		boolean ignoreHaste = DEFAULT_IGNORE_HASTE;
		boolean applyHandScale = DEFAULT_APPLY_HAND_SCALE;
	}
}
