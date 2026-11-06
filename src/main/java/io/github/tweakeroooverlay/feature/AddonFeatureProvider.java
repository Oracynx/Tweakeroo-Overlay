package io.github.tweakeroooverlay.feature;

import io.github.tweakeroooverlay.api.FeatureEntry;
import io.github.tweakeroooverlay.api.FeatureProvider;
import io.github.tweakeroooverlay.config.OverlayConfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Auto-discovery provider for the feature toggles of other Tweakeroo add-on mods.
 *
 * <p>The set of add-on mods and their config objects is discovered lazily on the first HUD frame
 * (all mods are fully initialized by then) and cached; only the config values are re-read on every
 * collection pass, so toggling a feature is reflected immediately.</p>
 */
public final class AddonFeatureProvider implements FeatureProvider {
	private static final String PROVIDER_ID = "tweakeroo_overlay:addons";

	private final OverlayConfig config;
	private final Map<String, List<ReflectiveBooleanConfig>> cacheByModId = new LinkedHashMap<>();

	public AddonFeatureProvider(OverlayConfig config) {
		this.config = config;
	}

	@Override
	public String modId() {
		return PROVIDER_ID;
	}

	@Override
	public int priority() {
		return GENERIC_DISCOVERY_PRIORITY;
	}

	@Override
	public ProviderKind kind() {
		return ProviderKind.ADDON;
	}

	@Override
	public List<FeatureEntry> collectFeatures() {
		if (this.config.includeAddonMods == false) {
			return List.of();
		}

		List<FeatureEntry> result = new ArrayList<>();

		for (String addonModId : AddonDetector.detectAddonModIds(this.config.extraAddonModIds)) {
			List<ReflectiveBooleanConfig> configs;

			synchronized (this.cacheByModId) {
				configs = this.cacheByModId.computeIfAbsent(addonModId, modId -> AddonScanner.scanMod(modId, this.config));
			}

			for (ReflectiveBooleanConfig config : configs) {
				result.add(config.toEntry());
			}
		}

		return result;
	}

	/** @return the add-on mod ids discovered so far, for debug output */
	public List<String> discoveredModIds() {
		synchronized (this.cacheByModId) {
			return List.copyOf(this.cacheByModId.keySet());
		}
	}
}
