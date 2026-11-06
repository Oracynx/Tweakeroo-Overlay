package io.github.tweakeroooverlay.api;

import java.util.List;

/**
 * The result of one feature collection pass.
 *
 * @param entries the filtered/sorted entries that should be rendered
 * @param errors  non-fatal provider errors (a broken add-on must never crash the HUD)
 */
public record FeatureSnapshot(List<FeatureEntry> entries, List<String> errors) {
	public FeatureSnapshot {
		entries = List.copyOf(entries);
		errors = List.copyOf(errors);
	}

	public boolean isEmpty() {
		return entries.isEmpty();
	}
}
