package io.github.tweakeroooverlay.client;

import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import io.github.tweakeroooverlay.TweakerooOverlayClient;

/**
 * MaLiLib initialization callback.
 *
 * <p>MaLiLib calls {@link #registerModHandlers()} after all mods have been initialized. Registering
 * the HUD renderer only at that point means the MaLiLib render dispatcher is ready and other mods
 * have registered their own renderers.</p>
 */
public final class MalilibInitHandler implements IInitializationHandler {
	private final OverlayController controller;

	public MalilibInitHandler(OverlayController controller) {
		this.controller = controller;
	}

	@Override
	public void registerModHandlers() {
		try {
			this.controller.registerMalilibHandlers();
			TweakerooOverlayClient.LOGGER.info("[Tweakeroo Overlay] Registered MaLiLib render handler");
		} catch (Throwable t) {
			TweakerooOverlayClient.LOGGER.error("[Tweakeroo Overlay] Failed to register the MaLiLib render handler", t);
		}
	}
}
