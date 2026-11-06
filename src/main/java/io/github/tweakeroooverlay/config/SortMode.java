package io.github.tweakeroooverlay.config;

/**
 * How the feature lines are ordered in the overlay.
 */
public enum SortMode {
	/** The order in which the providers / config system report the features (default). */
	CONFIG_ORDER,
	/** Case-insensitive alphabetical order by display name. */
	ALPHABETICAL,
	/** Grouped by mod id, then alphabetical by display name. */
	MOD_THEN_ALPHABETICAL
}
