package com.example.itemtweaks.client;

import java.util.Locale;
import java.util.function.DoubleConsumer;

import com.example.itemtweaks.ItemTweaksConfig;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/** Settings panel for swing speed and held-item scale. Non-pausing so the player sees changes live. */
public class SwingSettingsScreen extends Screen {

	// --- Layout ---
	private static final int PANEL_W = 320;
	private static final int PANEL_H = 178;
	private static final int PAD     = 18;   // horizontal inset on both sides
	// Slider + 8px gap + box fills the inner width exactly: 320 - 2*18 = 284 = 224 + 8 + 52
	private static final int SLIDER_W = 224;
	private static final int BOX_W    = 52;
	private static final int CTRL_H   = 20;
	private static final int BTN_W    = 84;
	private static final int BTN_GAP  = 8;

	// Vertical offsets from panel top
	private static final int TITLE_Y      = 14;
	private static final int ROW1_LABEL_Y = 38;
	private static final int ROW1_CTRL_Y  = 52;
	private static final int ROW2_LABEL_Y = 88;
	private static final int ROW2_CTRL_Y  = 102;
	private static final int SEPARATOR_Y  = 138;
	private static final int BTN_Y        = 148;

	// --- Colors (ARGB) ---
	private static final int COL_BACKDROP    = 0xB0060609;
	private static final int COL_PANEL       = 0xFF0F0F14;
	private static final int COL_PANEL_INNER = 0xFF18181E; // 1-shade lighter inset, gives depth
	private static final int COL_ACCENT      = 0xFF8BDDFF; // soft cyan — the only hue on the panel
	private static final int COL_SEPARATOR   = 0xFF262630;
	private static final int COL_TITLE       = 0xFFFFFFFF;
	private static final int COL_LABEL       = 0xFF8888A0; // muted so labels step back from controls
	private static final int COL_TEXT        = 0xFFE8E8EE;
	private static final int COL_TRACK_BG    = 0xFF1A1A22;
	private static final int COL_TRACK_FILL  = 0xFF2C6A8A;
	private static final int COL_HANDLE      = 0xFF8BDDFF;
	private static final int COL_HANDLE_HOV  = 0xFFBDEEFF;
	private static final int COL_SLIDER_TEXT = 0xFFD0D0DC;
	private static final int HANDLE_W        = 6;

	// --- State ---
	private boolean syncing;      // prevents slider <-> box feedback loops
	private boolean resetArmed;   // true after first Reset click; second click confirms
	private FloatSlider swingSlider, scaleSlider;
	private EditBox     swingBox,    scaleBox;
	private Button      resetBtn;

	public SwingSettingsScreen() {
		super(Component.literal("Swing Settings"));
	}

	@Override
	protected void init() {
		int px = panelX(), py = panelY();
		int inner = px + PAD;

		int r1y = py + ROW1_CTRL_Y;
		this.swingSlider = addRenderableWidget(new FloatSlider(inner, r1y, SLIDER_W, CTRL_H,
				"Swing speed", ItemTweaksConfig.SWING_SPEED_MULTIPLIER, this::applySwing, this.font));
		this.swingBox = addBox(inner + SLIDER_W + 8, r1y, ItemTweaksConfig.SWING_SPEED_MULTIPLIER, this::applySwing);

		int r2y = py + ROW2_CTRL_Y;
		this.scaleSlider = addRenderableWidget(new FloatSlider(inner, r2y, SLIDER_W, CTRL_H,
				"Held item scale", ItemTweaksConfig.HELD_ITEM_SCALE, this::applyScale, this.font));
		this.scaleBox = addBox(inner + SLIDER_W + 8, r2y, ItemTweaksConfig.HELD_ITEM_SCALE, this::applyScale);

		// Two buttons centered: Reset and Close (Apply removed — onClose auto-saves)
		int totalBtnW = BTN_W * 2 + BTN_GAP;
		int btnLeft   = px + (PANEL_W - totalBtnW) / 2;
		int by        = py + BTN_Y;
		this.resetBtn = addRenderableWidget(Button.builder(Component.literal("Reset"), b -> onResetClick())
				.bounds(btnLeft, by, BTN_W, CTRL_H).build());
		addRenderableWidget(Button.builder(Component.literal("Close"), b -> this.onClose())
				.bounds(btnLeft + BTN_W + BTN_GAP, by, BTN_W, CTRL_H).build());
	}

	/** Creates a numeric text box that feeds into {@code apply} on every keystroke. */
	private EditBox addBox(int x, int y, float value, DoubleConsumer apply) {
		EditBox box = new EditBox(this.font, x, y, BOX_W, CTRL_H, Component.literal("value"));
		box.setMaxLength(4);
		box.setTextColor(COL_TEXT);
		this.syncing = true;
		box.setValue(fmt(value));
		this.syncing = false;
		box.setResponder(text -> {
			if (this.syncing) return;
			try { apply.accept(Float.parseFloat(text)); }
			catch (NumberFormatException ignored) {} // user still typing, wait it out
		});
		return addRenderableWidget(box);
	}

	private void applySwing(double raw) {
		float v = ItemTweaksConfig.clamp((float) raw);
		ItemTweaksConfig.SWING_SPEED_MULTIPLIER = v;
		sync(this.swingSlider, this.swingBox, v);
	}

	private void applyScale(double raw) {
		float v = ItemTweaksConfig.clamp((float) raw);
		ItemTweaksConfig.HELD_ITEM_SCALE = v;
		sync(this.scaleSlider, this.scaleBox, v);
	}

	private void sync(FloatSlider slider, EditBox box, float v) {
		if (this.syncing) return;
		this.syncing = true;
		if (slider != null) slider.setFromFloat(v);
		if (box != null && !box.isFocused()) box.setValue(fmt(v)); // don't interrupt active typing
		this.syncing = false;
	}

	/** First click arms Reset ("Confirm?"); second click within the same session resets. */
	private void onResetClick() {
		if (!this.resetArmed) {
			this.resetArmed = true;
			this.resetBtn.setMessage(Component.literal("Confirm?"));
			return;
		}
		// Second click — do the actual reset
		this.resetArmed = false;
		this.resetBtn.setMessage(Component.literal("Reset"));
		ItemTweaksConfig.resetToVanilla();
		this.syncing = true;
		this.swingSlider.setFromFloat(ItemTweaksConfig.SWING_SPEED_MULTIPLIER);
		this.scaleSlider.setFromFloat(ItemTweaksConfig.HELD_ITEM_SCALE);
		this.swingBox.setValue(fmt(ItemTweaksConfig.SWING_SPEED_MULTIPLIER));
		this.scaleBox.setValue(fmt(ItemTweaksConfig.HELD_ITEM_SCALE));
		this.syncing = false;
	}

	@Override
	public void onClose() {
		ItemTweaksConfig.save();
		super.onClose();
	}

	@Override
	public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean b) {
		// If the player clicks anywhere other than the reset button while it's armed, disarm it.
		if (this.resetArmed && !this.resetBtn.isMouseOver(event.x(), event.y())) {
			this.resetArmed = false;
			this.resetBtn.setMessage(Component.literal("Reset"));
		}
		return super.mouseClicked(event, b);
	}

	@Override
	public boolean isPauseScreen() {
		return false; // game keeps ticking so the player sees changes live
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		if (this.minecraft.level != null) extractBlurredBackground(graphics);
		// Translucent veil — dark enough to frame the panel, light enough to see the world.
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
		// Inner zone inset 1px; top starts after accent stripe so it isn't overdrawn.
		graphics.fill(px + 1, py + 3, pr - 1, pb - 1, COL_PANEL_INNER);
		graphics.fill(px, py, pr, py + 2, COL_ACCENT);
		// Thin separator between the settings area and the button row.
		graphics.fill(px + PAD, py + SEPARATOR_Y, pr - PAD, py + SEPARATOR_Y + 1, COL_SEPARATOR);
	}

	/** Title (centered) and the two muted section labels drawn directly in the background pass. */
	private void renderLabels(GuiGraphicsExtractor graphics) {
		int px = panelX(), py = panelY();
		String title = "Swing Settings";
		graphics.text(this.font, title, px + (PANEL_W - this.font.width(title)) / 2,
				py + TITLE_Y, COL_TITLE, true);
		graphics.text(this.font, "Swing Speed",     px + PAD, py + ROW1_LABEL_Y, COL_LABEL, false);
		graphics.text(this.font, "Held Item Scale",  px + PAD, py + ROW2_LABEL_Y, COL_LABEL, false);
	}

	private int panelX() { return (this.width  - PANEL_W) / 2; }
	private int panelY() { return (this.height - PANEL_H) / 2; }

	private static String fmt(float v) { return String.format(Locale.US, "%.2f", v); }


	/**
	 * Flat custom-rendered slider
	 * Track is three rects: dark background, cyan active fill, bright handle knob.
	 * Handle brightens on hover. Value is drawn centered on the track.
	 */
	private static final class FloatSlider extends AbstractSliderButton {
		private final String       label;
		private final DoubleConsumer onChange;
		private final Font         font; // needed for text width + rendering inside extractWidgetRenderState

		FloatSlider(int x, int y, int w, int h, String label, float current,
				DoubleConsumer onChange, Font font) {
			super(x, y, w, h, Component.literal(label + ": " + fmt(current)),
					Mth.clamp((double)(current - ItemTweaksConfig.MIN)
							/ (ItemTweaksConfig.MAX - ItemTweaksConfig.MIN), 0.0, 1.0));
			this.label = label; this.onChange = onChange; this.font = font;
		}

		private float toFloat() {
			return ItemTweaksConfig.MIN + (float)(this.value * (ItemTweaksConfig.MAX - ItemTweaksConfig.MIN));
		}

		void setFromFloat(float v) {
			setValue((double)(v - ItemTweaksConfig.MIN) / (ItemTweaksConfig.MAX - ItemTweaksConfig.MIN));
		}

		@Override protected void updateMessage() {
			setMessage(Component.literal(this.label + ": " + fmt(ItemTweaksConfig.clamp(toFloat()))));
		}

		@Override protected void applyValue() { this.onChange.accept(toFloat()); }

		@Override
		public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float pt) {
			int x = getX(), y = getY(), w = getWidth(), h = getHeight();
			int handleX = x + (int) Mth.clamp(this.value * (w - HANDLE_W), 0, w - HANDLE_W);

			graphics.fill(x, y, x + w, y + h, COL_TRACK_BG);
			if (handleX > x) graphics.fill(x, y, handleX, y + h, COL_TRACK_FILL);
			// Handle is 1px taller than the track so it looks like it's lifted above the surface.
			graphics.fill(handleX, y - 1, handleX + HANDLE_W, y + h + 1,
					isHovered() ? COL_HANDLE_HOV : COL_HANDLE);

			String text = this.label + ": " + fmt(ItemTweaksConfig.clamp(toFloat()));
			graphics.text(this.font, text,
					x + (w - this.font.width(text)) / 2,
					y + (h - 8) / 2, // vertically center the 8px-tall MC font
					COL_SLIDER_TEXT, true);
		}
	}
}
