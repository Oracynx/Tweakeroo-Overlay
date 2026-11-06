package io.github.tweakeroooverlay.api;

import java.util.Objects;

/**
 * One immutable HUD line describing a single feature toggle.
 *
 * <p>A feature is normally a boolean config entry of Tweakeroo or of a Tweakeroo add-on.
 * Providers translate whatever data source they use into this common representation, so the
 * overlay renderer never needs to know anything about the source mod's config system.</p>
 *
 * @param id        globally unique id, by convention {@code "<modId>:<unique path>"},
 *                  e.g. {@code "tweakeroo:generic.fastPlacement"}. Used for de-duplication and
 *                  for the {@code hiddenFeatures} config option.
 * @param modId     the mod that owns the feature, e.g. {@code "tweakeroo"} or {@code "tweakermore"}
 * @param label     the already-localized display name shown in the HUD
 * @param category  optional category/group name, may be empty. Not rendered by default, but
 *                  available to providers and future sort modes.
 * @param enabled   whether the feature is currently enabled
 */
public record FeatureEntry(String id, String modId, String label, String category, boolean enabled) {
	public FeatureEntry {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(modId, "modId");
		Objects.requireNonNull(label, "label");
		category = category == null ? "" : category;
	}

	public static FeatureEntry of(String id, String modId, String label, String category, boolean enabled) {
		return new FeatureEntry(id, modId, label, category, enabled);
	}

	/**
	 * @return true if this entry matches the given id or the given id without its mod prefix.
	 *         This makes the {@code hiddenFeatures} config easier to use, since both
	 *         {@code "tweakeroo:generic.fastPlacement"} and {@code "generic.fastPlacement"} match.
	 */
	public boolean matchesId(String candidate) {
		if (candidate == null || candidate.isBlank()) {
			return false;
		}

		String trimmed = candidate.trim();
		return this.id.equals(trimmed)
			|| this.id.equals(this.modId + ":" + trimmed);
	}
}
