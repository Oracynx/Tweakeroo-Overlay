package io.github.tweakeroooverlay.util;

import io.github.tweakeroooverlay.api.DiffState;
import io.github.tweakeroooverlay.config.OverlayConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FeatureColorsTest {
	private static final int ENABLED = 0xFF55FF55;
	private static final int DISABLED = 0xFFAAAAAA;
	private static final int NEWLY_ENABLED = 0xFF00FF00;
	private static final int NEWLY_DISABLED = 0xFFFF0000;

	private static OverlayConfig config() {
		OverlayConfig config = new OverlayConfig();
		config.enabledColor = ENABLED;
		config.disabledColor = DISABLED;
		config.newlyEnabledColor = NEWLY_ENABLED;
		config.newlyDisabledColor = NEWLY_DISABLED;
		return config.sanitize();
	}

	@Test
	void unchangedEnabledUsesNormalEnabledColour() {
		assertEquals(ENABLED, FeatureColors.colorFor(config(), DiffState.UNCHANGED, true));
	}

	@Test
	void unchangedDisabledUsesNormalDisabledColour() {
		assertEquals(DISABLED, FeatureColors.colorFor(config(), DiffState.UNCHANGED, false));
	}

	@Test
	void newlyEnabledUsesNewlyEnabledColour() {
		assertEquals(NEWLY_ENABLED, FeatureColors.colorFor(config(), DiffState.NEWLY_ENABLED, true));
	}

	@Test
	void newlyDisabledUsesNewlyDisabledColour() {
		assertEquals(NEWLY_DISABLED, FeatureColors.colorFor(config(), DiffState.NEWLY_DISABLED, false));
	}
}
