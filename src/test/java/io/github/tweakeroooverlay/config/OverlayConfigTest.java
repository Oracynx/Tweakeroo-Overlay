package io.github.tweakeroooverlay.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OverlayConfigTest {
	@Test
	void sanitizeRepairsNullCollections() {
		OverlayConfig config = new OverlayConfig();
		config.hiddenFeatures = null;
		config.hiddenMods = null;
		config.visibleMods = null;

		config.sanitize();

		assertNotNull(config.hiddenFeatures);
		assertNotNull(config.hiddenMods);
		assertNotNull(config.visibleMods);
	}

	@Test
	void sanitizeClampsValues() {
		OverlayConfig config = new OverlayConfig();
		config.scale = 99.0F;
		config.offsetX = Integer.MAX_VALUE;
		config.maxLines = -5;
		config.backgroundPadding = -3;
		config.lineSpacing = 9999;
		config.enabledColor = 0x12345678;

		config.sanitize();

		assertTrue(config.scale <= 4.0F && config.scale >= 0.25F);
		assertEquals(10000, config.offsetX);
		assertEquals(0, config.maxLines);
		assertEquals(0, config.backgroundPadding);
		assertEquals(64, config.lineSpacing);
		// Colours are only validated to be an int; the value itself is passed through.
		assertEquals(0x12345678, config.enabledColor);
	}

	@Test
	void sanitizeFillsDefaultsForNullEnums() {
		OverlayConfig config = new OverlayConfig();
		config.corner = null;
		config.sortMode = null;
		config.schemaVersion = 0;

		config.sanitize();

		assertEquals(Corner.TOP_RIGHT, config.corner);
		assertEquals(SortMode.CONFIG_ORDER, config.sortMode);
		assertEquals(OverlayConfig.CURRENT_SCHEMA, config.schemaVersion);
	}

	@Test
	void sanitizeTrimsAndDropsBlankFilterEntries() {
		OverlayConfig config = new OverlayConfig();
		config.hiddenFeatures.add("  tweakeroo:a  ");
		config.hiddenFeatures.add("   ");
		config.hiddenFeatures.add(null);

		config.sanitize();

		assertEquals(1, config.hiddenFeatures.size());
		assertEquals("tweakeroo:a", config.hiddenFeatures.getFirst());
	}

	@Test
	void copyIsDeepForLists() {
		OverlayConfig config = new OverlayConfig();
		config.hiddenFeatures.add("a");

		OverlayConfig copy = config.copy();
		copy.hiddenFeatures.add("b");

		assertEquals(1, config.hiddenFeatures.size());
		assertEquals(2, copy.hiddenFeatures.size());
	}

	@Test
	void copyIsDeepForBaselineFeatures() {
		OverlayConfig config = new OverlayConfig();
		config.baselineActive = true;
		config.baselineFeatures.add("tweakeroo:tweakFastBlockPlacement");
		config.diffOnlyChanged = true;

		OverlayConfig copy = config.copy();
		copy.baselineFeatures.add("tweakeroo:tweakZoom");

		assertTrue(copy.baselineActive);
		assertTrue(copy.diffOnlyChanged);
		assertEquals(1, config.baselineFeatures.size());
		assertEquals(2, copy.baselineFeatures.size());
	}

	@Test
	void changeTrackingShowsOnlyChangesByDefault() {
		OverlayConfig config = new OverlayConfig();

		assertTrue(config.diffOnlyChanged, "change tracking should only show changed features by default");
		assertTrue(config.baselineActive == false);
		assertTrue(config.showProviderName == false, "no extra labels/markers by default");
		assertTrue(config.showDisabled == false);
	}
}
