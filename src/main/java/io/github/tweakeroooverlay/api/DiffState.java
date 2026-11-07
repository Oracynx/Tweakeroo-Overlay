package io.github.tweakeroooverlay.api;

/**
 * The change state of one feature relative to a captured baseline snapshot.
 *
 * <p>A baseline is captured with the "capture baseline" key binding: it remembers which features
 * were enabled at that moment. Afterwards the overlay can highlight what changed since then.</p>
 */
public enum DiffState {
	/** The feature had the same state when the baseline was captured. */
	UNCHANGED,
	/** The feature was disabled (or unknown) when the baseline was captured and is enabled now. */
	NEWLY_ENABLED,
	/** The feature was enabled when the baseline was captured and is disabled now. */
	NEWLY_DISABLED;

	public boolean isChanged() {
		return this != UNCHANGED;
	}
}
