package io.github.tweakeroooverlay.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigStorageTest {
	@TempDir
	Path tempDir;

	@Test
	void missingFileReturnsDefaults() {
		ConfigStorage storage = new ConfigStorage(this.tempDir.resolve("config.json"));
		ConfigStorage.LoadResult result = storage.load();

		assertNull(result.warning());
		assertTrue(result.config().enabled);
	}

	@Test
	void roundTripKeepsConfiguredValues() throws IOException {
		Path file = this.tempDir.resolve("config.json");
		ConfigStorage storage = new ConfigStorage(file);

		OverlayConfig config = new OverlayConfig();
		config.corner = Corner.BOTTOM_LEFT;
		config.offsetX = 12;
		config.showDisabled = true;
		config.enabledColor = 0xFF00FF00;
		config.hiddenFeatures.add("tweakeroo:tweakFastBlockPlacement");
		config.extraAddonModIds.add("tweakermore");
		storage.save(config);

		assertTrue(Files.exists(file));

		OverlayConfig loaded = storage.load().config();

		assertEquals(Corner.BOTTOM_LEFT, loaded.corner);
		assertEquals(12, loaded.offsetX);
		assertTrue(loaded.showDisabled);
		assertEquals(0xFF00FF00, loaded.enabledColor);
		assertEquals(1, loaded.hiddenFeatures.size());
		assertEquals("tweakermore", loaded.extraAddonModIds.getFirst());
	}

	@Test
	void corruptFileIsBackedUpAndDefaultsAreReturned() throws IOException {
		Path file = this.tempDir.resolve("config.json");
		Files.writeString(file, "{ this is not valid json ");
		ConfigStorage storage = new ConfigStorage(file);

		ConfigStorage.LoadResult result = storage.load();

		assertNotNull(result.warning());
		assertTrue(result.config().enabled);
		assertTrue(Files.exists(file) == false, "corrupt config file should have been moved aside");
		try (var stream = Files.list(this.tempDir)) {
			assertTrue(stream.anyMatch(path -> path.getFileName().toString().contains(".corrupt-")));
		}
	}

	@Test
	void handEditedOutOfRangeValuesAreClamped() throws IOException {
		Path file = this.tempDir.resolve("config.json");
		Files.writeString(file, """
			{
			  "scale": 99.0,
			  "maxLines": -3,
			  "corner": null,
			  "offsetX": 999999
			}
			""");
		ConfigStorage storage = new ConfigStorage(file);

		OverlayConfig loaded = storage.load().config();

		assertTrue(loaded.scale <= 4.0F);
		assertEquals(0, loaded.maxLines);
		assertEquals(Corner.TOP_RIGHT, loaded.corner);
		assertEquals(10000, loaded.offsetX);
	}
}
