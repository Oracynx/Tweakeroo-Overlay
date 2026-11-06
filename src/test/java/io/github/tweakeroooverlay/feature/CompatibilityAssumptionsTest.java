package io.github.tweakeroooverlay.feature;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Compile-classpath compatibility checks against the real Tweakeroo / MaLiLib artifacts.
 *
 * <p>All classes are loaded with {@code initialize = false}, so no Minecraft client instance or
 * mod init is needed. These tests guard the reflective access points of the compatibility layer:
 * if a dependency version rename/moves one of these members, the corresponding provider needs to
 * be updated, and this test fails early instead of only at runtime.</p>
 */
class CompatibilityAssumptionsTest {
	private static final ClassLoader LOADER = CompatibilityAssumptionsTest.class.getClassLoader();

	@Test
	void tweakerooFeatureToggleApiIsPresent() throws Exception {
		Class<?> featureToggle = Class.forName("fi.dy.masa.tweakeroo.config.FeatureToggle", false, LOADER);

		Field values = featureToggle.getField("VALUES");
		assertTrue(Modifier.isStatic(values.getModifiers()), "FeatureToggle.VALUES must be static");
		assertNotNull(featureToggle.getMethod("getBooleanValue"));
		assertNotNull(featureToggle.getMethod("getName"));
		assertNotNull(featureToggle.getMethod("getPrettyName"));
		assertTrue(featureToggle.isEnum(), "FeatureToggle must still be an enum");
	}

	@Test
	void tweakerooConfigCategoriesArePresent() throws Exception {
		for (String className : new String[]{
			"fi.dy.masa.tweakeroo.config.Configs$Disable",
			"fi.dy.masa.tweakeroo.config.Configs$Fixes"}) {
			Class<?> configs = Class.forName(className, false, LOADER);
			assertNotNull(configs.getField("OPTIONS"), className + ".OPTIONS is required");
		}
	}

	@Test
	void malilibRenderingApiIsPresent() throws Exception {
		Class<?> guiContext = Class.forName("fi.dy.masa.malilib.render.GuiContext", false, LOADER);
		Class<?> font = Class.forName("net.minecraft.client.gui.Font", false, LOADER);
		Class<?> renderEventHandler = Class.forName("fi.dy.masa.malilib.event.RenderEventHandler", false, LOADER);
		Class<?> renderer = Class.forName("fi.dy.masa.malilib.interfaces.IRenderer", false, LOADER);

		assertNotNull(guiContext.getMethod("drawString", font, String.class, int.class, int.class, int.class, boolean.class));
		assertNotNull(renderEventHandler.getMethod("registerInGameGuiRenderer", renderer));
	}

	@Test
	void malilibConfigScreenApiIsPresent() throws Exception {
		Class<?> registry = Class.forName("fi.dy.masa.malilib.registry.Registry", false, LOADER);
		Class<?> configScreenRegistry = Class.forName("fi.dy.masa.malilib.gui.config.registry.ConfigScreenRegistry", false, LOADER);

		assertNotNull(registry.getField("CONFIG_SCREEN"));
		assertNotNull(configScreenRegistry.getMethod("getAllModsWithConfigScreens"));
		assertDoesNotThrow(() -> Class.forName("fi.dy.masa.malilib.gui.interfaces.IConfigGuiAllTab", false, LOADER));
	}

	@Test
	void reflectiveBooleanConfigAcceptsRealConfigShape() {
		// Sanity check of the duck typing helper with a real malilib class already on the
		// classpath: BooleanHotkeyGuiWrapper exposes getName()/getBooleanValue().
		Class<?> wrapper = assertDoesNotThrow(
			() -> Class.forName("fi.dy.masa.malilib.config.options.BooleanHotkeyGuiWrapper", false, LOADER));
		Method getName = assertDoesNotThrow(() -> wrapper.getMethod("getName"));
		Method getBooleanValue = assertDoesNotThrow(() -> wrapper.getMethod("getBooleanValue"));

		assertNotNull(getName);
		assertNotNull(getBooleanValue);
	}
}
