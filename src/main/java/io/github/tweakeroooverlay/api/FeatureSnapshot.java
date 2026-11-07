package io.github.tweakeroooverlay.api;

import java.util.List;
import java.util.Map;

/**
 * The result of one feature collection pass.
 *
 * @param entries    the filtered/sorted entries that should be rendered
 * @param errors     non-fatal provider errors (a broken add-on must never crash the HUD)
 * @param diffStates change state of the collected entries relative to the captured baseline;
 *                   entries that are not in the map are {@link DiffState#UNCHANGED}
 */
public record FeatureSnapshot(List<FeatureEntry> entries, List<String> errors, Map<String, DiffState> diffStates) {
	public FeatureSnapshot {
		entries = List.copyOf(entries);
		errors = List.copyOf(errors);
		diffStates = Map.copyOf(diffStates);
	}

	public FeatureSnapshot(List<FeatureEntry> entries, List<String> errors) {
		this(entries, errors, Map.of());
	}

	public DiffState diffState(FeatureEntry entry) {
		return this.diffStates.getOrDefault(entry.id(), DiffState.UNCHANGED);
	}

	public boolean isEmpty() {
		return entries.isEmpty();
	}
}
