package io.github.tweakeroooverlay.client;

import fi.dy.masa.malilib.interfaces.IRenderer;
import fi.dy.masa.malilib.render.GuiContext;
import fi.dy.masa.malilib.util.GuiUtils;
import io.github.tweakeroooverlay.TweakerooOverlayClient;
import io.github.tweakeroooverlay.api.DiffState;
import io.github.tweakeroooverlay.api.FeatureEntry;
import io.github.tweakeroooverlay.api.FeatureSnapshot;
import io.github.tweakeroooverlay.config.OverlayConfig;
import io.github.tweakeroooverlay.feature.AddonDetector;
import io.github.tweakeroooverlay.util.FeatureColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.util.profiling.ProfilerFiller;
import org.joml.Matrix3x2fStack;

import java.util.List;

/**
 * Renders the feature list as a right-aligned HUD overlay.
 *
 * <p>The renderer is registered with MaLiLib's {@code RenderEventHandler}, which calls it while the
 * vanilla in-game GUI is being extracted, i.e. after all vanilla HUD elements have been added.
 * Drawing is done through MaLiLib's {@link GuiContext} wrapper, which keeps the code independent
 * of the detailed Minecraft GUI rendering pipeline.</p>
 */
public final class OverlayRenderer implements IRenderer {
	private final OverlayController controller;

	public OverlayRenderer(OverlayController controller) {
		this.controller = controller;
	}

	@Override
	public void onExtractGuiOverlayPost(GuiContext ctx, float partialTicks, ProfilerFiller profiler) {
		OverlayConfig config = this.controller.config();

		if (shouldRender(ctx, config) == false) {
			return;
		}

		FeatureSnapshot snapshot = this.controller.snapshot();

		if (snapshot.entries().isEmpty()) {
			return;
		}

		try {
			this.render(ctx, config, snapshot);
		} catch (Throwable t) {
			// A rendering error must never take down the game.
			TweakerooOverlayClient.LOGGER.error("[Tweakeroo Overlay] Failed to render the overlay", t);
			config.enabled = false;
		}
	}

	private boolean shouldRender(GuiContext ctx, OverlayConfig config) {
		if (config.enabled == false) {
			return false;
		}

		Minecraft mc = ctx.mc();

		if (mc.player == null || mc.level == null) {
			return false;
		}

		if (config.hideInScreens && GuiUtils.getCurrentScreen() != null) {
			return false;
		}

		// Respect the vanilla F1 "hide HUD" state.
		if (mc.gui == null || mc.gui.hud.isHidden()) {
			return false;
		}

		if (config.hideWithDebugScreen) {
			DebugScreenOverlay debugOverlay = mc.getDebugOverlay();

			if (debugOverlay != null && debugOverlay.showDebugScreen()) {
				return false;
			}
		}

		return true;
	}

	private void render(GuiContext ctx, OverlayConfig config, FeatureSnapshot snapshot) {
		List<FeatureEntry> entries = snapshot.entries();
		Font font = ctx.fontRenderer();
		int fontHeight = font.lineHeight;
		int lineHeight = Math.max(1, fontHeight + config.lineSpacing);

		int maxWidth = 0;
		String[] labels = new String[entries.size()];

		for (int i = 0; i < entries.size(); i++) {
			labels[i] = formatLabel(config, entries.get(i));
			maxWidth = Math.max(maxWidth, font.width(labels[i]));
		}

		int padding = config.showBackground ? Math.max(0, config.backgroundPadding) : 0;
		int contentHeight = fontHeight + (entries.size() - 1) * lineHeight;
		int boxWidth = maxWidth + padding * 2;
		int boxHeight = contentHeight + padding * 2;

		int screenWidth = GuiUtils.getScaledWindowWidth();
		int screenHeight = GuiUtils.getScaledWindowHeight();
		boolean rightAligned = config.corner.isRightSide();
		boolean bottom = config.corner.isBottomSide();

		int anchorX = rightAligned ? screenWidth - config.offsetX : config.offsetX;
		int anchorY = bottom ? screenHeight - config.offsetY : config.offsetY;

		int boxLeft = rightAligned ? anchorX - boxWidth : anchorX;
		int boxTop = bottom ? anchorY - boxHeight : anchorY;

		// Keep the list on screen even if it grows wide.
		boxLeft = Math.max(0, boxLeft);
		boxTop = Math.max(0, boxTop);

		float scale = config.scale;
		boolean scaled = Math.abs(scale - 1.0F) > 0.001F;
		Matrix3x2fStack pose = ctx.pose();

		if (scaled) {
			pose.pushMatrix();
			pose.translate(anchorX, anchorY);
			pose.scale(scale, scale);
			pose.translate(-anchorX, -anchorY);
		}

		try {
			if (config.showBackground) {
				ctx.fill(boxLeft, boxTop, boxLeft + boxWidth, boxTop + boxHeight, config.backgroundColor);
			}

			int textY = boxTop + padding;

			for (int i = 0; i < labels.length; i++) {
				FeatureEntry entry = entries.get(i);
				DiffState diffState = snapshot.diffState(entry);
				int color = FeatureColors.colorFor(config, diffState, entry.enabled());
				int textX = rightAligned
					? boxLeft + boxWidth - padding - font.width(labels[i])
					: boxLeft + padding;

				ctx.drawString(font, labels[i], textX, textY, color, config.showShadow);
				textY += lineHeight;
			}
		} finally {
			if (scaled) {
				pose.popMatrix();
			}
		}
	}

	private static String formatLabel(OverlayConfig config, FeatureEntry entry) {
		String label = entry.label();

		if (config.showProviderName) {
			label = "[" + AddonDetector.modDisplayName(entry.modId()) + "] " + label;
		}

		return label;
	}
}
