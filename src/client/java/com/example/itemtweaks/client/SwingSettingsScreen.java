package com.example.itemtweaks.client;

import java.util.Locale;
import java.util.function.DoubleConsumer;

import com.example.itemtweaks.ItemTweaksConfig;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/** Dark in-game panel: sliders + typed values for swing speed and held-item scale. */
public class SwingSettingsScreen extends Screen {
	private static final int PANEL_W = 320;
	private static final int PANEL_H = 196;
	private static final int SLIDER_W = 200;
	private static final int BOX_W = 52;
	private static final int ROW_H = 20;
	private static final int BACKDROP = 0xC8080810;
	private static final int PANEL = 0xFF16161C;
	private static final int PANEL_EDGE = 0xFF3D3D4A;

	private boolean syncing;
	private FloatSlider swingSlider;
	private FloatSlider scaleSlider;
	private EditBox swingBox;
	private EditBox scaleBox;

	public SwingSettingsScreen() {
		super(Component.literal("Swing"));
	}

	@Override
	protected void init() {
		int x = panelX();
		int y = panelY();
		int inner = x + 16;

		addRenderableWidget(new StringWidget(x, y + 12, PANEL_W, 12,
				Component.literal("Swing"), this.font));

		int row1 = y + 44;
		addRenderableWidget(new StringWidget(inner, row1 - 14, SLIDER_W, 12,
				Component.literal("Swing speed"), this.font));
		this.swingSlider = addRenderableWidget(new FloatSlider(inner, row1, SLIDER_W, ROW_H,
				"Swing speed", ItemTweaksConfig.SWING_SPEED_MULTIPLIER, this::applySwing));
		this.swingBox = addBox(inner + SLIDER_W + 8, row1, ItemTweaksConfig.SWING_SPEED_MULTIPLIER, this::applySwing);

		int row2 = y + 96;
		addRenderableWidget(new StringWidget(inner, row2 - 14, SLIDER_W, 12,
				Component.literal("Held item scale"), this.font));
		this.scaleSlider = addRenderableWidget(new FloatSlider(inner, row2, SLIDER_W, ROW_H,
				"Held item scale", ItemTweaksConfig.HELD_ITEM_SCALE, this::applyScale));
		this.scaleBox = addBox(inner + SLIDER_W + 8, row2, ItemTweaksConfig.HELD_ITEM_SCALE, this::applyScale);

		int by = y + PANEL_H - 36;
		int bw = 88;
		addRenderableWidget(Button.builder(Component.literal("Reset"), b -> reset())
				.bounds(x + 16, by, bw, ROW_H).build());
		addRenderableWidget(Button.builder(Component.literal("Apply"), b -> ItemTweaksConfig.save())
				.bounds(x + (PANEL_W - bw) / 2, by, bw, ROW_H).build());
		addRenderableWidget(Button.builder(Component.literal("Close"), b -> this.onClose())
				.bounds(x + PANEL_W - 16 - bw, by, bw, ROW_H).build());
	}

	private EditBox addBox(int x, int y, float value, DoubleConsumer apply) {
		EditBox box = new EditBox(this.font, x, y, BOX_W, ROW_H, Component.literal("value"));
		box.setMaxLength(4);
		box.setTextColor(0xFFE8E8EE);
		this.syncing = true;
		box.setValue(fmt(value));
		this.syncing = false;
		box.setResponder(text -> {
			if (this.syncing) {
				return;
			}
			try {
				apply.accept(Float.parseFloat(text));
			} catch (NumberFormatException ignored) {
				// Keep typing; last valid value stays applied.
			}
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
		if (this.syncing) {
			return;
		}
		this.syncing = true;
		if (slider != null) {
			slider.setFromFloat(v);
		}
		if (box != null && !box.isFocused()) {
			box.setValue(fmt(v));
		}
		this.syncing = false;
	}

	private void reset() {
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
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		if (this.minecraft.level != null) {
			extractBlurredBackground(graphics);
		}
		graphics.fill(0, 0, this.width, this.height, BACKDROP);
		int x = panelX();
		int y = panelY();
		graphics.fill(x, y, x + PANEL_W, y + PANEL_H, PANEL);
		graphics.fill(x, y, x + PANEL_W, y + 1, PANEL_EDGE);
		this.minecraft.gui.hud.extractDeferredSubtitles();
	}

	private int panelX() {
		return (this.width - PANEL_W) / 2;
	}

	private int panelY() {
		return (this.height - PANEL_H) / 2;
	}

	private static String fmt(float v) {
		return String.format(Locale.US, "%.2f", v);
	}

	private static final class FloatSlider extends AbstractSliderButton {
		private final String label;
		private final DoubleConsumer onChange;

		FloatSlider(int x, int y, int w, int h, String label, float current, DoubleConsumer onChange) {
			super(x, y, w, h, Component.literal(label + ": " + fmt(current)),
					Mth.clamp((current - ItemTweaksConfig.MIN) / (ItemTweaksConfig.MAX - ItemTweaksConfig.MIN), 0.0, 1.0));
			this.label = label;
			this.onChange = onChange;
		}

		void setFromFloat(float v) {
			setValue((v - ItemTweaksConfig.MIN) / (ItemTweaksConfig.MAX - ItemTweaksConfig.MIN));
		}

		@Override
		protected void updateMessage() {
			setMessage(Component.literal(this.label + ": " + fmt(ItemTweaksConfig.clamp(toFloat()))));
		}

		@Override
		protected void applyValue() {
			this.onChange.accept(toFloat());
		}

		private float toFloat() {
			return ItemTweaksConfig.MIN + (float) (this.value * (ItemTweaksConfig.MAX - ItemTweaksConfig.MIN));
		}
	}
}
