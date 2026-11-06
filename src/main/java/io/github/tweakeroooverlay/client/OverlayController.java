package io.github.tweakeroooverlay.client;

import fi.dy.masa.malilib.event.RenderEventHandler;
import fi.dy.masa.malilib.util.InfoUtils;
import fi.dy.masa.malilib.util.StringUtils;
import io.github.tweakeroooverlay.TweakerooOverlayClient;
import io.github.tweakeroooverlay.api.FeatureProvider;
import io.github.tweakeroooverlay.api.FeatureProviderRegistry;
import io.github.tweakeroooverlay.api.FeatureSnapshot;
import io.github.tweakeroooverlay.config.ConfigStorage;
import io.github.tweakeroooverlay.config.OverlayConfig;
import io.github.tweakeroooverlay.feature.AddonFeatureProvider;
import io.github.tweakeroooverlay.feature.TweakerooFeatureProvider;
import io.github.tweakeroooverlay.util.FeatureListBuilder;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.client.KeyMapping;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Owns the overlay state: the loaded config, the feature providers, the (throttled) feature
 * snapshot and the key binding handling.
 */
public final class OverlayController {
	/** Name of the Fabric custom entrypoint that third-party mods can implement to add providers. */
	public static final String FEATURE_PROVIDER_ENTRYPOINT = "tweakeroo_overlay";

	private static final String CONFIG_FILE_NAME = "tweakeroo-overlay.json";
	private static final long REFRESH_INTERVAL_NANOS = 500_000_000L;
	private static final long ERROR_LOG_INTERVAL_NANOS = 30_000_000_000L;

	private final ConfigStorage storage;
	private final OverlayConfig config;
	private final FeatureProviderRegistry providers = new FeatureProviderRegistry();
	private final KeyMapping toggleKey;
	private final OverlayRenderer renderer = new OverlayRenderer(this);

	private FeatureSnapshot snapshot = new FeatureSnapshot(List.of(), List.of());
	private long lastRefreshNanos;
	private long lastErrorLogNanos;

	public OverlayController() {
		Path configPath = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME);
		this.storage = new ConfigStorage(configPath);

		ConfigStorage.LoadResult loadResult = this.storage.load();
		this.config = loadResult.config();

		if (loadResult.warning() != null) {
			TweakerooOverlayClient.LOGGER.warn("[Tweakeroo Overlay] {}", loadResult.warning());
		}

		this.toggleKey = KeyBindings.createToggleKey();
	}

	/** Called from the Fabric mod initializer. */
	public void initialize() {
		// The key binding has to be registered while the mods are being initialized, so that it
		// shows up in the vanilla controls screen.
		KeyMappingHelper.registerKeyMapping(this.toggleKey);
		ClientTickEvents.END_CLIENT_TICK.register(client -> this.onClientTick());

		this.registerBuiltinProviders();
		this.registerCustomProviders();

		// The first feature snapshot is built on the first client tick instead of right here:
		// by then MaLiLib and the other mods are fully initialized, so config screens and config
		// handlers can be queried safely.
		this.lastRefreshNanos = 0;
	}

	/** Called from MaLiLib's initialization callback, once all mods are ready. */
	public void registerMalilibHandlers() {
		RenderEventHandler.getInstance().registerInGameGuiRenderer(this.renderer);
	}

	private void onClientTick() {
		while (this.toggleKey.consumeClick()) {
			this.toggleOverlay();
		}

		long now = System.nanoTime();

		if (now - this.lastRefreshNanos >= REFRESH_INTERVAL_NANOS) {
			this.refreshFeatures();
		}
	}

	private void registerBuiltinProviders() {
		this.providers.register(new TweakerooFeatureProvider(this.config));
		this.providers.register(new AddonFeatureProvider(this.config));
	}

	private void registerCustomProviders() {
		try {
			List<EntrypointContainer<FeatureProvider>> containers = FabricLoader.getInstance()
				.getEntrypointContainers(FEATURE_PROVIDER_ENTRYPOINT, FeatureProvider.class);

			for (EntrypointContainer<FeatureProvider> container : containers) {
				try {
					FeatureProvider provider = container.getEntrypoint();
					this.providers.register(provider);
					TweakerooOverlayClient.LOGGER.info(
						"[Tweakeroo Overlay] Registered custom feature provider {} from mod {}",
						provider.describe(), container.getProvider().getMetadata().getId());
				} catch (Throwable t) {
					TweakerooOverlayClient.LOGGER.warn(
						"[Tweakeroo Overlay] Failed to load a custom feature provider from mod {}",
						container.getProvider().getMetadata().getId(), t);
				}
			}
		} catch (Throwable t) {
			TweakerooOverlayClient.LOGGER.warn("[Tweakeroo Overlay] Failed to load custom feature providers", t);
		}
	}

	private void refreshFeatures() {
		this.lastRefreshNanos = System.nanoTime();
		this.snapshot = FeatureListBuilder.build(this.providers.providers(), this.config);

		List<String> errors = this.snapshot.errors();

		if (errors.isEmpty() == false && (this.lastRefreshNanos - this.lastErrorLogNanos) >= ERROR_LOG_INTERVAL_NANOS) {
			this.lastErrorLogNanos = this.lastRefreshNanos;

			for (String error : errors) {
				TweakerooOverlayClient.LOGGER.warn("[Tweakeroo Overlay] Feature provider error: {}", error);
			}
		}
	}

	private void toggleOverlay() {
		this.config.enabled = this.config.enabled == false;
		this.saveConfig();
		this.refreshFeatures();

		try {
			InfoUtils.printBooleanConfigToggleMessage(
				StringUtils.translate("tweakeroo_overlay.name"), this.config.enabled);
		} catch (Throwable ignored) {
			// The message is a nicety only.
		}
	}

	private void saveConfig() {
		try {
			this.storage.save(this.config);
		} catch (IOException e) {
			TweakerooOverlayClient.LOGGER.error("[Tweakeroo Overlay] Failed to save the config file {}", this.storage.file(), e);
		}
	}

	public OverlayConfig config() {
		return this.config;
	}

	public FeatureSnapshot snapshot() {
		return this.snapshot;
	}

	public FeatureProviderRegistry providers() {
		return this.providers;
	}

	/** Forces a feature refresh on the next tick / immediately. */
	public void requestRefresh() {
		this.lastRefreshNanos = 0;
	}
}
