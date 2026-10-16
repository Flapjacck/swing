package com.flapjacck.swing.screen;

import java.util.Locale;
import java.util.function.DoubleConsumer;

import com.flapjacck.swing.config.SwingConfig;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * Settings panel for swing speed, held-item scale, per-item/arm offsets, and tilt.
 * Non-pausing so the player sees changes live; content is scrollable.
 *
 * NOTE: This file intentionally exceeds the 250-line guideline. The screen and its
 * custom FloatSlider inner class are tightly coupled to each other's static fields
 * and cannot be cleanly split without introducing significant indirection.
 */
public class SwingSettingsScreen extends Screen {

	// ── Panel dimensions ────────────────────────────────────────────────────────
	private static final int PANEL_W = 320;
	private static final int PANEL_H = 214;
	private static final int PAD     = 18;
	private static final int SLIDER_W = 224;
	private static final int BOX_W   = 52;
	private static final int CTRL_H  = 20;
	private static final int BTN_W   = 84;
	private static final int BTN_GAP = 8;

	// ── Scroll area ──────────────────────────────────────────────────────────────
	private static final int SCROLL_AREA_TOP = 30;
	private static final int SCROLL_AREA_H   = 132;
	private static final int SCROLL_SPEED    = 10;

	// ── Scrollable content Y offsets (relative to scroll area top) ───────────────
	// Section 1: Swing speed
	private static final int ROW1_LABEL_Y = 6;
	private static final int ROW1_CTRL_Y  = 16;
	private static final int TOGGLE_Y     = 42;
	// Section 2: Held item scale
	private static final int ROW2_LABEL_Y   = 68;
	private static final int ROW2_CTRL_Y    = 78;
	private static final int HAND_TOGGLE_Y  = 104;
	// Section 3: Item offsets (X, Y, Z)
	private static final int ITEM_OFFSET_LABEL_Y  = HAND_TOGGLE_Y + CTRL_H + 6;      // 130
	private static final int ITEM_OFFSET_X_CTRL_Y = ITEM_OFFSET_LABEL_Y + 10;         // 140
	private static final int ITEM_OFFSET_Y_CTRL_Y = ITEM_OFFSET_X_CTRL_Y + CTRL_H + 6; // 166
	private static final int ITEM_OFFSET_Z_CTRL_Y = ITEM_OFFSET_Y_CTRL_Y + CTRL_H + 6; // 192
	// Section 4: Item tilt (Pitch, Roll)
	private static final int ITEM_TILT_LABEL_Y    = ITEM_OFFSET_Z_CTRL_Y + CTRL_H + 6; // 218
	private static final int ITEM_TILT_PITCH_Y    = ITEM_TILT_LABEL_Y + 10;             // 228
	private static final int ITEM_TILT_ROLL_Y     = ITEM_TILT_PITCH_Y + CTRL_H + 6;    // 254
	// Section 5: Arm offsets (X, Y, Z) — active when Apply Hand Scale is ON
	private static final int ARM_OFFSET_LABEL_Y   = ITEM_TILT_ROLL_Y + CTRL_H + 6;     // 280
	private static final int ARM_OFFSET_X_CTRL_Y  = ARM_OFFSET_LABEL_Y + 10;            // 290
	private static final int ARM_OFFSET_Y_CTRL_Y  = ARM_OFFSET_X_CTRL_Y + CTRL_H + 6;  // 316
	private static final int ARM_OFFSET_Z_CTRL_Y  = ARM_OFFSET_Y_CTRL_Y + CTRL_H + 6;  // 342
	// Section 6: Arm tilt (Pitch, Roll) — active when Apply Hand Scale is ON
	private static final int ARM_TILT_LABEL_Y     = ARM_OFFSET_Z_CTRL_Y + CTRL_H + 6;  // 368
	private static final int ARM_TILT_PITCH_Y     = ARM_TILT_LABEL_Y + 10;              // 378
	private static final int ARM_TILT_ROLL_Y      = ARM_TILT_PITCH_Y + CTRL_H + 6;     // 404
	private static final int CONTENT_BOTTOM       = ARM_TILT_ROLL_Y + CTRL_H;           // 424
	private static final int MAX_SCROLL = Math.max(0, CONTENT_BOTTOM - SCROLL_AREA_H);  // 292

	// ── Fixed footer ─────────────────────────────────────────────────────────────
	private static final int TITLE_Y     = 14;
	private static final int SEPARATOR_Y = 163;
	private static final int BTN_Y       = 175;

	// ── Colors (ARGB) ─────────────────────────────────────────────────────────────
	static final int COL_BACKDROP    = 0xB0060609;
	static final int COL_PANEL       = 0xFF0F0F14;
	static final int COL_PANEL_INNER = 0xFF18181E;
	static final int COL_ACCENT      = 0xFF8BDDFF;
	static final int COL_SEPARATOR   = 0xFF262630;
	static final int COL_TITLE       = 0xFFFFFFFF;
	static final int COL_LABEL       = 0xFF8888A0;
	static final int COL_TEXT        = 0xFFE8E8EE;
	static final int COL_TRACK_BG    = 0xFF1A1A22;
	static final int COL_TRACK_FILL  = 0xFF2C6A8A;
	static final int COL_HANDLE      = 0xFF8BDDFF;
	static final int COL_HANDLE_HOV  = 0xFFBDEEFF;
	static final int COL_SLIDER_TEXT = 0xFFD0D0DC;
	static final int HANDLE_W        = 6;

	// ── State ────────────────────────────────────────────────────────────────────
	private boolean syncing;
	private boolean resetArmed;
	private int scrollOffset;

	// ── Widget references ────────────────────────────────────────────────────────
	private FloatSlider swingSlider, scaleSlider;
	private EditBox swingBox, scaleBox;
	private Button hasteToggleBtn, handScaleToggleBtn;
	private Button resetBtn;
	// Item offsets
	private FloatSlider itemOffsetXSlider, itemOffsetYSlider, itemOffsetZSlider;
	private EditBox itemOffsetXBox, itemOffsetYBox, itemOffsetZBox;
	// Item tilt
	private FloatSlider itemTiltPitchSlider, itemTiltRollSlider;
	private EditBox itemTiltPitchBox, itemTiltRollBox;
	// Arm offsets
	private FloatSlider armOffsetXSlider, armOffsetYSlider, armOffsetZSlider;
	private EditBox armOffsetXBox, armOffsetYBox, armOffsetZBox;
	// Arm tilt
	private FloatSlider armTiltPitchSlider, armTiltRollSlider;
	private EditBox armTiltPitchBox, armTiltRollBox;

	public SwingSettingsScreen() {
		super(Component.literal("Swing Settings"));
	}

	@Override
	protected void init() {
		int px = panelX(), py = panelY();
		int inner = px + PAD;
		float omin = SwingConfig.OFFSET_MIN, omax = SwingConfig.OFFSET_MAX;
		float tmin = SwingConfig.TILT_MIN,   tmax = SwingConfig.TILT_MAX;

		// — Section 1: Swing speed —
		this.swingSlider = addRenderableWidget(new FloatSlider(inner, 0, SLIDER_W, CTRL_H,
				"Swing speed", SwingConfig.SWING_SPEED_MULTIPLIER, this::applySwing, this.font));
		this.swingBox = addBox(inner + SLIDER_W + 8, 0, SwingConfig.SWING_SPEED_MULTIPLIER, this::applySwing);
		this.hasteToggleBtn = addRenderableWidget(Button.builder(
				Component.literal(hasteToggleLabel()), b -> toggleIgnoreHaste())
				.bounds(inner, 0, SLIDER_W + 8 + BOX_W, CTRL_H).build());

		// — Section 2: Held item scale —
		this.scaleSlider = addRenderableWidget(new FloatSlider(inner, 0, SLIDER_W, CTRL_H,
				"Held item scale", SwingConfig.HELD_ITEM_SCALE, this::applyScale, this.font));
		this.scaleBox = addBox(inner + SLIDER_W + 8, 0, SwingConfig.HELD_ITEM_SCALE, this::applyScale);
		this.handScaleToggleBtn = addRenderableWidget(Button.builder(
				Component.literal(handScaleToggleLabel()), b -> toggleApplyHandScale())
				.bounds(inner, 0, SLIDER_W + 8 + BOX_W, CTRL_H).build());

		// — Section 3: Item offsets —
		this.itemOffsetXSlider = addRenderableWidget(new FloatSlider(inner, 0, SLIDER_W, CTRL_H,
				"Item X", SwingConfig.HELD_ITEM_OFFSET_X, this::applyItemOffsetX, this.font, omin, omax));
		this.itemOffsetXBox = addBox(inner + SLIDER_W + 8, 0, SwingConfig.HELD_ITEM_OFFSET_X, this::applyItemOffsetX);
		this.itemOffsetYSlider = addRenderableWidget(new FloatSlider(inner, 0, SLIDER_W, CTRL_H,
				"Item Y", SwingConfig.HELD_ITEM_OFFSET_Y, this::applyItemOffsetY, this.font, omin, omax));
		this.itemOffsetYBox = addBox(inner + SLIDER_W + 8, 0, SwingConfig.HELD_ITEM_OFFSET_Y, this::applyItemOffsetY);
		this.itemOffsetZSlider = addRenderableWidget(new FloatSlider(inner, 0, SLIDER_W, CTRL_H,
				"Item Z", SwingConfig.HELD_ITEM_OFFSET_Z, this::applyItemOffsetZ, this.font, omin, omax));
		this.itemOffsetZBox = addBox(inner + SLIDER_W + 8, 0, SwingConfig.HELD_ITEM_OFFSET_Z, this::applyItemOffsetZ);

		// — Section 4: Item tilt —
		this.itemTiltPitchSlider = addRenderableWidget(new FloatSlider(inner, 0, SLIDER_W, CTRL_H,
				"Item Pitch", SwingConfig.ITEM_TILT_PITCH, this::applyItemTiltPitch, this.font, tmin, tmax));
		this.itemTiltPitchBox = addBox(inner + SLIDER_W + 8, 0, SwingConfig.ITEM_TILT_PITCH, this::applyItemTiltPitch);
		this.itemTiltRollSlider = addRenderableWidget(new FloatSlider(inner, 0, SLIDER_W, CTRL_H,
				"Item Roll", SwingConfig.ITEM_TILT_ROLL, this::applyItemTiltRoll, this.font, tmin, tmax));
		this.itemTiltRollBox = addBox(inner + SLIDER_W + 8, 0, SwingConfig.ITEM_TILT_ROLL, this::applyItemTiltRoll);

		// — Section 5: Arm offsets —
		this.armOffsetXSlider = addRenderableWidget(new FloatSlider(inner, 0, SLIDER_W, CTRL_H,
				"Arm X", SwingConfig.ARM_OFFSET_X, this::applyArmOffsetX, this.font, omin, omax));
		this.armOffsetXBox = addBox(inner + SLIDER_W + 8, 0, SwingConfig.ARM_OFFSET_X, this::applyArmOffsetX);
		this.armOffsetYSlider = addRenderableWidget(new FloatSlider(inner, 0, SLIDER_W, CTRL_H,
				"Arm Y", SwingConfig.ARM_OFFSET_Y, this::applyArmOffsetY, this.font, omin, omax));
		this.armOffsetYBox = addBox(inner + SLIDER_W + 8, 0, SwingConfig.ARM_OFFSET_Y, this::applyArmOffsetY);
		this.armOffsetZSlider = addRenderableWidget(new FloatSlider(inner, 0, SLIDER_W, CTRL_H,
				"Arm Z", SwingConfig.ARM_OFFSET_Z, this::applyArmOffsetZ, this.font, omin, omax));
		this.armOffsetZBox = addBox(inner + SLIDER_W + 8, 0, SwingConfig.ARM_OFFSET_Z, this::applyArmOffsetZ);

		// — Section 6: Arm tilt —
		this.armTiltPitchSlider = addRenderableWidget(new FloatSlider(inner, 0, SLIDER_W, CTRL_H,
				"Arm Pitch", SwingConfig.ARM_TILT_PITCH, this::applyArmTiltPitch, this.font, tmin, tmax));
		this.armTiltPitchBox = addBox(inner + SLIDER_W + 8, 0, SwingConfig.ARM_TILT_PITCH, this::applyArmTiltPitch);
		this.armTiltRollSlider = addRenderableWidget(new FloatSlider(inner, 0, SLIDER_W, CTRL_H,
				"Arm Roll", SwingConfig.ARM_TILT_ROLL, this::applyArmTiltRoll, this.font, tmin, tmax));
		this.armTiltRollBox = addBox(inner + SLIDER_W + 8, 0, SwingConfig.ARM_TILT_ROLL, this::applyArmTiltRoll);

		repositionScrollableWidgets();

		// — Fixed footer: Reset + Close —
		int totalBtnW = BTN_W * 2 + BTN_GAP;
		int btnLeft   = px + (PANEL_W - totalBtnW) / 2;
		int by        = py + BTN_Y;
		this.resetBtn = addRenderableWidget(Button.builder(Component.literal("Reset"), b -> onResetClick())
				.bounds(btnLeft, by, BTN_W, CTRL_H).build());
		addRenderableWidget(Button.builder(Component.literal("Close"), b -> this.onClose())
				.bounds(btnLeft + BTN_W + BTN_GAP, by, BTN_W, CTRL_H).build());
	}

	/** Reposition all scrollable widgets; hides any that fall outside the clip area. */
	private void repositionScrollableWidgets() {
		int sb  = panelY() + SCROLL_AREA_TOP; // scroll base Y
		int top = sb;
		int bot = sb + SCROLL_AREA_H;
		int in  = panelX() + PAD;
		int bx  = in + SLIDER_W + 8; // box x

		placeWidget(swingSlider,        in, sb + ROW1_CTRL_Y         - scrollOffset, top, bot);
		placeWidget(swingBox,           bx, sb + ROW1_CTRL_Y         - scrollOffset, top, bot);
		placeWidget(hasteToggleBtn,     in, sb + TOGGLE_Y            - scrollOffset, top, bot);
		placeWidget(scaleSlider,        in, sb + ROW2_CTRL_Y         - scrollOffset, top, bot);
		placeWidget(scaleBox,           bx, sb + ROW2_CTRL_Y         - scrollOffset, top, bot);
		placeWidget(handScaleToggleBtn, in, sb + HAND_TOGGLE_Y       - scrollOffset, top, bot);
		placeWidget(itemOffsetXSlider,  in, sb + ITEM_OFFSET_X_CTRL_Y - scrollOffset, top, bot);
		placeWidget(itemOffsetXBox,     bx, sb + ITEM_OFFSET_X_CTRL_Y - scrollOffset, top, bot);
		placeWidget(itemOffsetYSlider,  in, sb + ITEM_OFFSET_Y_CTRL_Y - scrollOffset, top, bot);
		placeWidget(itemOffsetYBox,     bx, sb + ITEM_OFFSET_Y_CTRL_Y - scrollOffset, top, bot);
		placeWidget(itemOffsetZSlider,  in, sb + ITEM_OFFSET_Z_CTRL_Y - scrollOffset, top, bot);
		placeWidget(itemOffsetZBox,     bx, sb + ITEM_OFFSET_Z_CTRL_Y - scrollOffset, top, bot);
		placeWidget(itemTiltPitchSlider, in, sb + ITEM_TILT_PITCH_Y  - scrollOffset, top, bot);
		placeWidget(itemTiltPitchBox,   bx, sb + ITEM_TILT_PITCH_Y   - scrollOffset, top, bot);
		placeWidget(itemTiltRollSlider, in, sb + ITEM_TILT_ROLL_Y    - scrollOffset, top, bot);
		placeWidget(itemTiltRollBox,    bx, sb + ITEM_TILT_ROLL_Y    - scrollOffset, top, bot);
		placeWidget(armOffsetXSlider,   in, sb + ARM_OFFSET_X_CTRL_Y - scrollOffset, top, bot);
		placeWidget(armOffsetXBox,      bx, sb + ARM_OFFSET_X_CTRL_Y - scrollOffset, top, bot);
		placeWidget(armOffsetYSlider,   in, sb + ARM_OFFSET_Y_CTRL_Y - scrollOffset, top, bot);
		placeWidget(armOffsetYBox,      bx, sb + ARM_OFFSET_Y_CTRL_Y - scrollOffset, top, bot);
		placeWidget(armOffsetZSlider,   in, sb + ARM_OFFSET_Z_CTRL_Y - scrollOffset, top, bot);
		placeWidget(armOffsetZBox,      bx, sb + ARM_OFFSET_Z_CTRL_Y - scrollOffset, top, bot);
		placeWidget(armTiltPitchSlider, in, sb + ARM_TILT_PITCH_Y    - scrollOffset, top, bot);
		placeWidget(armTiltPitchBox,    bx, sb + ARM_TILT_PITCH_Y    - scrollOffset, top, bot);
		placeWidget(armTiltRollSlider,  in, sb + ARM_TILT_ROLL_Y     - scrollOffset, top, bot);
		placeWidget(armTiltRollBox,     bx, sb + ARM_TILT_ROLL_Y     - scrollOffset, top, bot);
	}

	private void placeWidget(AbstractWidget w, int x, int y, int clipTop, int clipBot) {
		if (w == null) return;
		w.setX(x); w.setY(y);
		w.visible = (y + CTRL_H > clipTop) && (y < clipBot);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		int px = panelX(), py = panelY();
		int clipTop = py + SCROLL_AREA_TOP, clipBot = clipTop + SCROLL_AREA_H;
		if (mouseX >= px && mouseX < px + PANEL_W && mouseY >= clipTop && mouseY < clipBot) {
			scrollOffset = Mth.clamp(scrollOffset - (int)(scrollY * SCROLL_SPEED), 0, MAX_SCROLL);
			repositionScrollableWidgets();
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	/** Create a numeric text box bound to an apply callback. maxLength=6 handles "-90.00". */
	private EditBox addBox(int x, int y, float value, DoubleConsumer apply) {
		EditBox box = new EditBox(this.font, x, y, BOX_W, CTRL_H, Component.literal("value"));
		box.setMaxLength(6);
		box.setTextColor(COL_TEXT);
		this.syncing = true;
		box.setValue(fmt(value));
		this.syncing = false;
		box.setResponder(text -> {
			if (this.syncing) return;
			try { apply.accept(Float.parseFloat(text)); }
			catch (NumberFormatException ignored) {}
		});
		return addRenderableWidget(box);
	}

	// ── Toggle labels & handlers ─────────────────────────────────────────────────

	private static String hasteToggleLabel() {
		return SwingConfig.IGNORE_HASTE ? "Ignore Haste: ON" : "Ignore Haste: OFF";
	}
	private void toggleIgnoreHaste() {
		SwingConfig.IGNORE_HASTE = !SwingConfig.IGNORE_HASTE;
		if (this.hasteToggleBtn != null)
			this.hasteToggleBtn.setMessage(Component.literal(hasteToggleLabel()));
	}

	private static String handScaleToggleLabel() {
		return SwingConfig.APPLY_HAND_SCALE ? "Apply Hand Scale: ON" : "Apply Hand Scale: OFF";
	}
	private void toggleApplyHandScale() {
		SwingConfig.APPLY_HAND_SCALE = !SwingConfig.APPLY_HAND_SCALE;
		if (this.handScaleToggleBtn != null)
			this.handScaleToggleBtn.setMessage(Component.literal(handScaleToggleLabel()));
	}

	// ── Apply callbacks ──────────────────────────────────────────────────────────

	private void applySwing(double raw) {
		float v = SwingConfig.clamp((float) raw);
		SwingConfig.SWING_SPEED_MULTIPLIER = v;
		sync(this.swingSlider, this.swingBox, v);
	}
	private void applyScale(double raw) {
		float v = SwingConfig.clamp((float) raw);
		SwingConfig.HELD_ITEM_SCALE = v;
		sync(this.scaleSlider, this.scaleBox, v);
	}
	private void applyItemOffsetX(double raw) {
		float v = SwingConfig.clampOffset((float) raw);
		SwingConfig.HELD_ITEM_OFFSET_X = v;
		sync(this.itemOffsetXSlider, this.itemOffsetXBox, v);
	}
	private void applyItemOffsetY(double raw) {
		float v = SwingConfig.clampOffset((float) raw);
		SwingConfig.HELD_ITEM_OFFSET_Y = v;
		sync(this.itemOffsetYSlider, this.itemOffsetYBox, v);
	}
	private void applyItemOffsetZ(double raw) {
		float v = SwingConfig.clampOffset((float) raw);
		SwingConfig.HELD_ITEM_OFFSET_Z = v;
		sync(this.itemOffsetZSlider, this.itemOffsetZBox, v);
	}
	private void applyItemTiltPitch(double raw) {
		float v = SwingConfig.clampTilt((float) raw);
		SwingConfig.ITEM_TILT_PITCH = v;
		sync(this.itemTiltPitchSlider, this.itemTiltPitchBox, v);
	}
	private void applyItemTiltRoll(double raw) {
		float v = SwingConfig.clampTilt((float) raw);
		SwingConfig.ITEM_TILT_ROLL = v;
		sync(this.itemTiltRollSlider, this.itemTiltRollBox, v);
	}
	private void applyArmOffsetX(double raw) {
		float v = SwingConfig.clampOffset((float) raw);
		SwingConfig.ARM_OFFSET_X = v;
		sync(this.armOffsetXSlider, this.armOffsetXBox, v);
	}
	private void applyArmOffsetY(double raw) {
		float v = SwingConfig.clampOffset((float) raw);
		SwingConfig.ARM_OFFSET_Y = v;
		sync(this.armOffsetYSlider, this.armOffsetYBox, v);
	}
	private void applyArmOffsetZ(double raw) {
		float v = SwingConfig.clampOffset((float) raw);
		SwingConfig.ARM_OFFSET_Z = v;
		sync(this.armOffsetZSlider, this.armOffsetZBox, v);
	}
	private void applyArmTiltPitch(double raw) {
		float v = SwingConfig.clampTilt((float) raw);
		SwingConfig.ARM_TILT_PITCH = v;
		sync(this.armTiltPitchSlider, this.armTiltPitchBox, v);
	}
	private void applyArmTiltRoll(double raw) {
		float v = SwingConfig.clampTilt((float) raw);
		SwingConfig.ARM_TILT_ROLL = v;
		sync(this.armTiltRollSlider, this.armTiltRollBox, v);
	}

	private void sync(FloatSlider slider, EditBox box, float v) {
		if (this.syncing) return;
		this.syncing = true;
		if (slider != null) slider.setFromFloat(v);
		if (box != null && !box.isFocused()) box.setValue(fmt(v));
		this.syncing = false;
	}

	// ── Reset ────────────────────────────────────────────────────────────────────

	/** First click arms; second click confirms reset to vanilla. */
	private void onResetClick() {
		if (!this.resetArmed) {
			this.resetArmed = true;
			this.resetBtn.setMessage(Component.literal("Confirm?"));
			return;
		}
		this.resetArmed = false;
		this.resetBtn.setMessage(Component.literal("Reset"));
		SwingConfig.resetToVanilla();
		this.syncing = true;
		// Section 1
		this.swingSlider.setFromFloat(SwingConfig.SWING_SPEED_MULTIPLIER);
		this.swingBox.setValue(fmt(SwingConfig.SWING_SPEED_MULTIPLIER));
		this.hasteToggleBtn.setMessage(Component.literal(hasteToggleLabel()));
		// Section 2
		this.scaleSlider.setFromFloat(SwingConfig.HELD_ITEM_SCALE);
		this.scaleBox.setValue(fmt(SwingConfig.HELD_ITEM_SCALE));
		this.handScaleToggleBtn.setMessage(Component.literal(handScaleToggleLabel()));
		// Section 3
		this.itemOffsetXSlider.setFromFloat(SwingConfig.HELD_ITEM_OFFSET_X);
		this.itemOffsetXBox.setValue(fmt(SwingConfig.HELD_ITEM_OFFSET_X));
		this.itemOffsetYSlider.setFromFloat(SwingConfig.HELD_ITEM_OFFSET_Y);
		this.itemOffsetYBox.setValue(fmt(SwingConfig.HELD_ITEM_OFFSET_Y));
		this.itemOffsetZSlider.setFromFloat(SwingConfig.HELD_ITEM_OFFSET_Z);
		this.itemOffsetZBox.setValue(fmt(SwingConfig.HELD_ITEM_OFFSET_Z));
		// Section 4
		this.itemTiltPitchSlider.setFromFloat(SwingConfig.ITEM_TILT_PITCH);
		this.itemTiltPitchBox.setValue(fmt(SwingConfig.ITEM_TILT_PITCH));
		this.itemTiltRollSlider.setFromFloat(SwingConfig.ITEM_TILT_ROLL);
		this.itemTiltRollBox.setValue(fmt(SwingConfig.ITEM_TILT_ROLL));
		// Section 5
		this.armOffsetXSlider.setFromFloat(SwingConfig.ARM_OFFSET_X);
		this.armOffsetXBox.setValue(fmt(SwingConfig.ARM_OFFSET_X));
		this.armOffsetYSlider.setFromFloat(SwingConfig.ARM_OFFSET_Y);
		this.armOffsetYBox.setValue(fmt(SwingConfig.ARM_OFFSET_Y));
		this.armOffsetZSlider.setFromFloat(SwingConfig.ARM_OFFSET_Z);
		this.armOffsetZBox.setValue(fmt(SwingConfig.ARM_OFFSET_Z));
		// Section 6
		this.armTiltPitchSlider.setFromFloat(SwingConfig.ARM_TILT_PITCH);
		this.armTiltPitchBox.setValue(fmt(SwingConfig.ARM_TILT_PITCH));
		this.armTiltRollSlider.setFromFloat(SwingConfig.ARM_TILT_ROLL);
		this.armTiltRollBox.setValue(fmt(SwingConfig.ARM_TILT_ROLL));
		this.syncing = false;
	}

	// ── Screen lifecycle ─────────────────────────────────────────────────────────

	@Override
	public void onClose() {
		SwingConfig.save();
		super.onClose();
	}

	@Override
	public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean b) {
		if (this.resetArmed && !this.resetBtn.isMouseOver(event.x(), event.y())) {
			this.resetArmed = false;
			this.resetBtn.setMessage(Component.literal("Reset"));
		}
		return super.mouseClicked(event, b);
	}

	@Override public boolean isPauseScreen() { return false; }

	// ── Rendering ────────────────────────────────────────────────────────────────

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		if (this.minecraft.level != null) extractBlurredBackground(graphics);
		graphics.fill(0, 0, this.width, this.height, COL_BACKDROP);
		renderPanel(graphics);
		renderLabels(graphics);
		this.minecraft.gui.hud.extractDeferredSubtitles();
	}

	/** Three-layer panel: dark shell → lighter inner zone → 2px cyan accent stripe → separator. */
	private void renderPanel(GuiGraphicsExtractor graphics) {
		int px = panelX(), py = panelY();
		int pr = px + PANEL_W, pb = py + PANEL_H;
		graphics.fill(px, py, pr, pb, COL_PANEL);
		graphics.fill(px + 1, py + 3, pr - 1, pb - 1, COL_PANEL_INNER);
		graphics.fill(px, py, pr, py + 2, COL_ACCENT);
		graphics.fill(px + PAD, py + SEPARATOR_Y, pr - PAD, py + SEPARATOR_Y + 1, COL_SEPARATOR);
	}

	/**
	 * Section labels rendered in the scroll area; each is clipped so it only
	 * appears when scrolled into view, matching the widget clip behaviour.
	 */
	private void renderLabels(GuiGraphicsExtractor graphics) {
		int px = panelX(), py = panelY();
		int sb  = py + SCROLL_AREA_TOP;
		int top = sb, bot = sb + SCROLL_AREA_H;

		String title = "Swing Settings";
		graphics.text(this.font, title,
				px + (PANEL_W - this.font.width(title)) / 2, py + TITLE_Y, COL_TITLE, true);

		drawLabel(graphics, "Swing Speed",        sb + ROW1_LABEL_Y       - scrollOffset, px, top, bot);
		drawLabel(graphics, "Held Item Scale",     sb + ROW2_LABEL_Y       - scrollOffset, px, top, bot);
		drawLabel(graphics, "Item Offsets",        sb + ITEM_OFFSET_LABEL_Y - scrollOffset, px, top, bot);
		drawLabel(graphics, "Item Tilt",           sb + ITEM_TILT_LABEL_Y  - scrollOffset, px, top, bot);
		drawLabel(graphics, "Arm Offsets",         sb + ARM_OFFSET_LABEL_Y - scrollOffset, px, top, bot);
		drawLabel(graphics, "Arm Tilt",            sb + ARM_TILT_LABEL_Y   - scrollOffset, px, top, bot);
	}

	private void drawLabel(GuiGraphicsExtractor g, String text, int y, int px, int top, int bot) {
		if (y >= top && y < bot) g.text(this.font, text, px + PAD, y, COL_LABEL, false);
	}

	private int panelX() { return (this.width  - PANEL_W) / 2; }
	private int panelY() { return (this.height - PANEL_H) / 2; }
	static String fmt(float v) { return String.format(Locale.US, "%.2f", v); }

	// ── FloatSlider inner class ───────────────────────────────────────────────────

	/**
	 * Custom flat slider with dark track, cyan fill, and an inline value label.
	 * Accepts explicit min/max so it works for both 0.1–1.0 scale and -1.0 to 1.0
	 * offset and -90 to 90 tilt ranges without calling the wrong clamp method.
	 */
	static final class FloatSlider extends AbstractSliderButton {
		private final String        label;
		private final DoubleConsumer onChange;
		private final Font          font;
		private final float         min;
		private final float         max;

		/** Convenience constructor for the standard scale range (0.1 – 1.0). */
		FloatSlider(int x, int y, int w, int h, String label, float current,
				DoubleConsumer onChange, Font font) {
			this(x, y, w, h, label, current, onChange, font,
					SwingConfig.MIN, SwingConfig.MAX);
		}

		/** Full constructor — use for offset or tilt sliders with a custom range. */
		FloatSlider(int x, int y, int w, int h, String label, float current,
				DoubleConsumer onChange, Font font, float min, float max) {
			super(x, y, w, h, Component.literal(label + ": " + fmt(current)),
					Mth.clamp((double)(current - min) / (max - min), 0.0, 1.0));
			this.label = label; this.onChange = onChange; this.font = font;
			this.min = min; this.max = max;
		}

		private float toFloat() { return min + (float)(this.value * (max - min)); }

		void setFromFloat(float v) {
			setValue(Mth.clamp((double)(v - min) / (max - min), 0.0, 1.0));
		}

		/** Use instance min/max for clamping so negative values display correctly. */
		@Override protected void updateMessage() {
			setMessage(Component.literal(label + ": " + fmt(Mth.clamp(toFloat(), min, max))));
		}

		@Override protected void applyValue() { this.onChange.accept(toFloat()); }

		@Override
		public void extractWidgetRenderState(GuiGraphicsExtractor graphics,
				int mouseX, int mouseY, float pt) {
			int x = getX(), y = getY(), w = getWidth(), h = getHeight();
			int handleX = x + (int) Mth.clamp(this.value * (w - HANDLE_W), 0, w - HANDLE_W);

			graphics.fill(x, y, x + w, y + h, COL_TRACK_BG);
			if (handleX > x) graphics.fill(x, y, handleX, y + h, COL_TRACK_FILL);
			graphics.fill(handleX, y - 1, handleX + HANDLE_W, y + h + 1,
					isHovered() ? COL_HANDLE_HOV : COL_HANDLE);

			// Use instance min/max so negative offset/tilt values display correctly.
			String text = label + ": " + fmt(Mth.clamp(toFloat(), min, max));
			graphics.text(this.font, text,
					x + (w - this.font.width(text)) / 2,
					y + (h - 8) / 2,
					COL_SLIDER_TEXT, true);
		}
	}
}
