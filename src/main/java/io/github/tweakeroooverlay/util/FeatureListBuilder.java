package io.github.tweakeroooverlay.util;

import io.github.tweakeroooverlay.api.FeatureEntry;
import io.github.tweakeroooverlay.api.FeatureProvider;
import io.github.tweakeroooverlay.api.FeatureSnapshot;
import io.github.tweakeroooverlay.config.OverlayConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure (Minecraft independent) logic that merges the output of all feature providers, applies the
 * user configuration (visibility, filtering, sorting, line limit) and produces the final list of
 * lines to render.
 *
 * <p>Being pure Java, this class is covered by unit tests and can be freely extended with new
 * sort modes / filters without touching the rendering code.</p>
 */
public final class FeatureListBuilder {
	private FeatureListBuilder() {
	}

	public static FeatureSnapshot build(Collection<FeatureProvider> providers, OverlayConfig config) {
		OverlayConfig safeConfig = config == null ? new OverlayConfig() : config;
		List<FeatureProvider> sortedProviders = new ArrayList<>(providers == null ? List.of() : providers);
		// Lower priority value wins, so explicit third-party providers override auto-discovery.
		sortedProviders.sort(Comparator.comparingInt(FeatureProvider::priority));

		Map<String, FeatureEntry> byId = new LinkedHashMap<>();
		List<String> errors = new ArrayList<>();

		for (FeatureProvider provider : sortedProviders) {
			if (provider == null || !isProviderEnabled(provider, safeConfig)) {
				continue;
			}

			try {
				List<FeatureEntry> features = provider.collectFeatures();

				if (features == null) {
					continue;
				}

				for (FeatureEntry feature : features) {
					if (feature != null) {
						// First provider (lowest priority value) wins for duplicated ids.
						byId.putIfAbsent(feature.id(), feature);
					}
				}
			} catch (Throwable t) {
				// Never let a single broken provider (e.g. a mismatched add-on version) break the HUD.
				errors.add(provider.describe() + ": " + t.getClass().getSimpleName() + ": " + t.getMessage());
			}
		}

		List<FeatureEntry> entries = new ArrayList<>(byId.size());

		for (FeatureEntry entry : byId.values()) {
			if (isEntryVisible(entry, safeConfig)) {
				entries.add(entry);
			}
		}

		sort(entries, safeConfig.sortMode);

		if (safeConfig.maxLines > 0 && entries.size() > safeConfig.maxLines) {
			entries = new ArrayList<>(entries.subList(0, safeConfig.maxLines));
		}

		return new FeatureSnapshot(entries, errors);
	}

	private static boolean isProviderEnabled(FeatureProvider provider, OverlayConfig config) {
		return switch (provider.kind()) {
			case TWEAKEROO -> config.includeTweakeroo;
			case ADDON -> config.includeAddonMods;
			case CUSTOM -> config.includeCustomProviders;
		};
	}

	private static boolean isEntryVisible(FeatureEntry entry, OverlayConfig config) {
		if (!entry.enabled() && !config.showDisabled) {
			return false;
		}

		if (!config.visibleMods.isEmpty() && !config.visibleMods.contains(entry.modId())) {
			return false;
		}

		if (config.hiddenMods.contains(entry.modId())) {
			return false;
		}

		for (String hidden : config.hiddenFeatures) {
			if (entry.matchesId(hidden)) {
				return false;
			}
		}

		return true;
	}

	private static void sort(List<FeatureEntry> entries, io.github.tweakeroooverlay.config.SortMode sortMode) {
		if (sortMode == null) {
			return;
		}

		switch (sortMode) {
			case CONFIG_ORDER -> {
				// Keep provider / config order.
			}
			case ALPHABETICAL -> entries.sort(
				Comparator.comparing(FeatureEntry::label, String.CASE_INSENSITIVE_ORDER)
					.thenComparing(FeatureEntry::id)
			);
			case MOD_THEN_ALPHABETICAL -> entries.sort(
				Comparator.comparing(FeatureEntry::modId, String.CASE_INSENSITIVE_ORDER)
					.thenComparing(FeatureEntry::label, String.CASE_INSENSITIVE_ORDER)
					.thenComparing(FeatureEntry::id)
			);
		}
	}
}
