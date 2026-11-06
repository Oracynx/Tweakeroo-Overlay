package io.github.tweakeroooverlay;

import fi.dy.masa.malilib.event.InitializationHandler;
import io.github.tweakeroooverlay.client.MalilibInitHandler;
import io.github.tweakeroooverlay.client.OverlayController;
import net.fabricmc.api.ModInitializer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Tweakeroo Overlay - shows the currently enabled Tweakeroo (and Tweakeroo add-on) feature toggles
 * in a right-aligned list in a screen corner.
 *
 * <p>Initialization follows the usual MaLiLib mod pattern: the Fabric entrypoint only registers an
 * {@link fi.dy.masa.malilib.interfaces.IInitializationHandler} with MaLiLib, and the actual
 * (MaLiLib facing) registration is done later in {@code registerModHandlers()}, once all mods have
 * been initialized.</p>
 */
public final class TweakerooOverlayClient implements ModInitializer {
	public static final String MOD_ID = "tweakeroo_overlay";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	private static OverlayController controller;

	@Override
	public void onInitialize() {
		try {
			controller = new OverlayController();
			controller.initialize();
			InitializationHandler.getInstance().registerInitializationHandler(new MalilibInitHandler(controller));
			LOGGER.info("[Tweakeroo Overlay] Initialized");
		} catch (Throwable t) {
			// Never crash the game because of an overlay mod: disable it and log the problem.
			LOGGER.error("[Tweakeroo Overlay] Failed to initialize, the overlay will stay disabled", t);
		}
	}

	/** @return the controller, or {@code null} if initialization failed */
	public static OverlayController controller() {
		return controller;
	}
}
