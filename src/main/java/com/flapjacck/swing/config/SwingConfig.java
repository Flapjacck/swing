package com.flapjacck.swing.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Mth;

/**
 * Live tweak values. Mixins read these every frame; GUI writes them in place.
 * Persisted to {@code config/swing.json}.
 */
public final class SwingConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(SwingConfig.class);

	public static final float MIN = 0.1f;
	public static final float MAX = 1.0f;
	public static final float OFFSET_MIN = -1.0f;
	public static final float OFFSET_MAX = 1.0f;
	public static final float TILT_MIN = -90.0f;
	public static final float TILT_MAX = 90.0f;
	public static final float DEFAULT_HELD_ITEM_SCALE = 1.0f;
	public static final float DEFAULT_SWING_SPEED = 1.0f;
	public static final boolean DEFAULT_IGNORE_HASTE = false;
	public static final boolean DEFAULT_APPLY_HAND_SCALE = false;
	public static final float DEFAULT_OFFSET = 0.0f;
	public static final float DEFAULT_TILT = 0.0f;

	/** First-person held item size. 1.0 = vanilla. */
	public static float HELD_ITEM_SCALE = DEFAULT_HELD_ITEM_SCALE;

	/** Arm-swing animation speed. 1.0 = vanilla; lower is slower. Visual only. */
	public static float SWING_SPEED_MULTIPLIER = DEFAULT_SWING_SPEED;

	/**
	 * When true, the swing-duration mixin ignores haste / mining-fatigue and
	 * always starts from the vanilla base of 6 ticks before applying the
	 * multiplier. Keeps animation speed constant across islands and gear sets.
	 */
	public static boolean IGNORE_HASTE = DEFAULT_IGNORE_HASTE;

	/** When true, applies HELD_ITEM_SCALE to the player's hand/arm as well. */
	public static boolean APPLY_HAND_SCALE = DEFAULT_APPLY_HAND_SCALE;

	/**
	 * First-person positional offsets for the held item model.
	 * X = left/right, Y = up/down, Z = forward/back. 0.0 = vanilla position.
	 */
	public static float HELD_ITEM_OFFSET_X = DEFAULT_OFFSET;
	public static float HELD_ITEM_OFFSET_Y = DEFAULT_OFFSET;
	public static float HELD_ITEM_OFFSET_Z = DEFAULT_OFFSET;

	/**
	 * First-person tilt (rotation) for the held item model.
	 * PITCH = X-axis rotation (tips item up/down).
	 * ROLL  = Z-axis rotation (slants item left/right). Degrees, 0 = vanilla.
	 */
	public static float ITEM_TILT_PITCH = DEFAULT_TILT;
	public static float ITEM_TILT_ROLL  = DEFAULT_TILT;

	/**
	 * First-person positional offsets for the bare arm (empty hand).
	 * Only applied when APPLY_HAND_SCALE is true (same gate as arm scale).
	 * X = left/right, Y = up/down, Z = forward/back. 0.0 = vanilla position.
	 */
	public static float ARM_OFFSET_X = DEFAULT_OFFSET;
	public static float ARM_OFFSET_Y = DEFAULT_OFFSET;
	public static float ARM_OFFSET_Z = DEFAULT_OFFSET;

	/**
	 * First-person tilt (rotation) for the bare arm (empty hand).
	 * Only applied when APPLY_HAND_SCALE is true.
	 * PITCH = X-axis rotation (tips arm up/down).
	 * ROLL  = Z-axis rotation (slants arm left/right). Degrees, 0 = vanilla.
	 */
	public static float ARM_TILT_PITCH = DEFAULT_TILT;
	public static float ARM_TILT_ROLL  = DEFAULT_TILT;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private SwingConfig() {}

	public static float clamp(float value) {
		return Math.round(Mth.clamp(value, MIN, MAX) * 100f) / 100f;
	}

	/** Clamp an offset value to [OFFSET_MIN, OFFSET_MAX] rounded to 2 dp. */
	public static float clampOffset(float value) {
		return Math.round(Mth.clamp(value, OFFSET_MIN, OFFSET_MAX) * 100f) / 100f;
	}

	/** Clamp a tilt (rotation) value to [TILT_MIN, TILT_MAX] rounded to 2 dp. */
	public static float clampTilt(float value) {
		return Math.round(Mth.clamp(value, TILT_MIN, TILT_MAX) * 100f) / 100f;
	}

	public static void resetToVanilla() {
		HELD_ITEM_SCALE = DEFAULT_HELD_ITEM_SCALE;
		SWING_SPEED_MULTIPLIER = DEFAULT_SWING_SPEED;
		IGNORE_HASTE = DEFAULT_IGNORE_HASTE;
		APPLY_HAND_SCALE = DEFAULT_APPLY_HAND_SCALE;
		HELD_ITEM_OFFSET_X = DEFAULT_OFFSET;
		HELD_ITEM_OFFSET_Y = DEFAULT_OFFSET;
		HELD_ITEM_OFFSET_Z = DEFAULT_OFFSET;
		ITEM_TILT_PITCH    = DEFAULT_TILT;
		ITEM_TILT_ROLL     = DEFAULT_TILT;
		ARM_OFFSET_X       = DEFAULT_OFFSET;
		ARM_OFFSET_Y       = DEFAULT_OFFSET;
		ARM_OFFSET_Z       = DEFAULT_OFFSET;
		ARM_TILT_PITCH     = DEFAULT_TILT;
		ARM_TILT_ROLL      = DEFAULT_TILT;
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
			HELD_ITEM_OFFSET_X = clampOffset(data.heldItemOffsetX);
			HELD_ITEM_OFFSET_Y = clampOffset(data.heldItemOffsetY);
			HELD_ITEM_OFFSET_Z = clampOffset(data.heldItemOffsetZ);
			ITEM_TILT_PITCH    = clampTilt(data.itemTiltPitch);
			ITEM_TILT_ROLL     = clampTilt(data.itemTiltRoll);
			ARM_OFFSET_X       = clampOffset(data.armOffsetX);
			ARM_OFFSET_Y       = clampOffset(data.armOffsetY);
			ARM_OFFSET_Z       = clampOffset(data.armOffsetZ);
			ARM_TILT_PITCH     = clampTilt(data.armTiltPitch);
			ARM_TILT_ROLL      = clampTilt(data.armTiltRoll);
		} catch (Exception e) {
			LOGGER.warn("Could not read {}, using vanilla defaults", file, e);
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
				data.heldItemOffsetX = clampOffset(HELD_ITEM_OFFSET_X);
				data.heldItemOffsetY = clampOffset(HELD_ITEM_OFFSET_Y);
				data.heldItemOffsetZ = clampOffset(HELD_ITEM_OFFSET_Z);
				data.itemTiltPitch   = clampTilt(ITEM_TILT_PITCH);
				data.itemTiltRoll    = clampTilt(ITEM_TILT_ROLL);
				data.armOffsetX      = clampOffset(ARM_OFFSET_X);
				data.armOffsetY      = clampOffset(ARM_OFFSET_Y);
				data.armOffsetZ      = clampOffset(ARM_OFFSET_Z);
				data.armTiltPitch    = clampTilt(ARM_TILT_PITCH);
				data.armTiltRoll     = clampTilt(ARM_TILT_ROLL);
				GSON.toJson(data, writer);
			}
		} catch (IOException e) {
			LOGGER.warn("Could not write {}", file, e);
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
		float heldItemOffsetX = DEFAULT_OFFSET;
		float heldItemOffsetY = DEFAULT_OFFSET;
		float heldItemOffsetZ = DEFAULT_OFFSET;
		float itemTiltPitch   = DEFAULT_TILT;
		float itemTiltRoll    = DEFAULT_TILT;
		float armOffsetX      = DEFAULT_OFFSET;
		float armOffsetY      = DEFAULT_OFFSET;
		float armOffsetZ      = DEFAULT_OFFSET;
		float armTiltPitch    = DEFAULT_TILT;
		float armTiltRoll     = DEFAULT_TILT;
	}
}
