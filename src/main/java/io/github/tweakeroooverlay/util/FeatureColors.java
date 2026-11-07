package io.github.tweakeroooverlay.util;

import io.github.tweakeroooverlay.api.DiffState;
import io.github.tweakeroooverlay.config.OverlayConfig;

/**
 * Pure colour selection for one overlay line, kept separate from the renderer so it can be tested
 * without Minecraft.
 */
public final class FeatureColors {
	private FeatureColors() {
	}

	public static int colorFor(OverlayConfig config, DiffState diffState, boolean enabled) {
		if (diffState == DiffState.NEWLY_ENABLED) {
			return config.newlyEnabledColor;
		}

		if (diffState == DiffState.NEWLY_DISABLED) {
			return config.newlyDisabledColor;
		}

		return enabled ? config.enabledColor : config.disabledColor;
	}
}
