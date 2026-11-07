package io.github.tweakeroooverlay.util;

import io.github.tweakeroooverlay.api.DiffState;
import io.github.tweakeroooverlay.api.FeatureEntry;
import io.github.tweakeroooverlay.api.FeatureProvider;
import io.github.tweakeroooverlay.api.FeatureSnapshot;
import io.github.tweakeroooverlay.config.OverlayConfig;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeatureListBuilderTest {
	private static FeatureProvider provider(String modId, FeatureProvider.ProviderKind kind, int priority, FeatureEntry... entries) {
		return new FeatureProvider() {
			@Override
			public String modId() {
				return modId;
			}

			@Override
			public List<FeatureEntry> collectFeatures() {
				return List.of(entries);
			}

			@Override
			public int priority() {
				return priority;
			}

			@Override
			public FeatureProvider.ProviderKind kind() {
				return kind;
			}
		};
	}

	private static FeatureEntry entry(String id, String label, boolean enabled) {
		return FeatureEntry.of(id, id.substring(0, id.indexOf(':')), label, "", enabled);
	}

	@Test
	void collectsAndFiltersDisabledByDefault() {
		OverlayConfig config = new OverlayConfig().sanitize();
		FeatureSnapshot snapshot = FeatureListBuilder.build(
			List.of(provider("tweakeroo", FeatureProvider.ProviderKind.TWEAKEROO, 100,
				entry("tweakeroo:a", "A", true),
				entry("tweakeroo:b", "B", false))),
			config
		);

		assertEquals(1, snapshot.entries().size());
		assertEquals("tweakeroo:a", snapshot.entries().getFirst().id());
	}

	@Test
	void includesDisabledWhenConfigured() {
		OverlayConfig config = new OverlayConfig();
		config.showDisabled = true;
		config.sanitize();

		FeatureSnapshot snapshot = FeatureListBuilder.build(
			List.of(provider("tweakeroo", FeatureProvider.ProviderKind.TWEAKEROO, 100,
				entry("tweakeroo:a", "A", true),
				entry("tweakeroo:b", "B", false))),
			config
		);

		assertEquals(2, snapshot.entries().size());
	}

	@Test
	void sortsAlphabetically() {
		OverlayConfig config = new OverlayConfig();
		config.sortMode = io.github.tweakeroooverlay.config.SortMode.ALPHABETICAL;
		config.sanitize();

		FeatureSnapshot snapshot = FeatureListBuilder.build(
			List.of(provider("tweakeroo", FeatureProvider.ProviderKind.TWEAKEROO, 100,
				entry("tweakeroo:b", "Bravo", true),
				entry("tweakeroo:a", "alpha", true))),
			config
		);

		assertEquals(List.of("alpha", "Bravo"), snapshot.entries().stream().map(FeatureEntry::label).toList());
	}

	@Test
	void lowerPriorityProviderWinsDuplicateIds() {
		FeatureSnapshot snapshot = FeatureListBuilder.build(
			List.of(
				provider("custom", FeatureProvider.ProviderKind.CUSTOM, FeatureProvider.EXPLICIT_PRIORITY,
					entry("tweakeroo:a", "Custom win", true)),
				provider("tweakeroo", FeatureProvider.ProviderKind.TWEAKEROO, FeatureProvider.TWEAKEROO_PRIORITY,
					entry("tweakeroo:a", "Generic lose", true))),
			new OverlayConfig().sanitize()
		);

		assertEquals(1, snapshot.entries().size());
		assertEquals("Custom win", snapshot.entries().getFirst().label());
	}

	@Test
	void hidesFeaturesAndMods() {
		OverlayConfig config = new OverlayConfig();
		config.hiddenFeatures.add("b");
		config.hiddenMods.add("addonmod");
		config.sanitize();

		FeatureSnapshot snapshot = FeatureListBuilder.build(
			List.of(
				provider("tweakeroo", FeatureProvider.ProviderKind.TWEAKEROO, 100,
					entry("tweakeroo:a", "A", true),
					entry("tweakeroo:b", "B", true)),
				provider("addonmod", FeatureProvider.ProviderKind.ADDON, 500,
					entry("addonmod:c", "C", true))),
			config
		);

		assertEquals(List.of("A"), snapshot.entries().stream().map(FeatureEntry::label).toList());
	}

	@Test
	void respectsProviderKindSwitches() {
		OverlayConfig config = new OverlayConfig();
		config.includeTweakeroo = false;
		config.includeCustomProviders = false;
		config.sanitize();

		FeatureSnapshot snapshot = FeatureListBuilder.build(
			List.of(
				provider("tweakeroo", FeatureProvider.ProviderKind.TWEAKEROO, 100, entry("tweakeroo:a", "A", true)),
				provider("custom", FeatureProvider.ProviderKind.CUSTOM, 0, entry("custom:b", "B", true)),
				provider("addonmod", FeatureProvider.ProviderKind.ADDON, 500, entry("addonmod:c", "C", true))),
			config
		);

		assertEquals(List.of("C"), snapshot.entries().stream().map(FeatureEntry::label).toList());
	}

	@Test
	void limitingLines() {
		OverlayConfig config = new OverlayConfig();
		config.maxLines = 2;
		config.sanitize();

		FeatureSnapshot snapshot = FeatureListBuilder.build(
			List.of(provider("tweakeroo", FeatureProvider.ProviderKind.TWEAKEROO, 100,
				entry("tweakeroo:a", "A", true),
				entry("tweakeroo:b", "B", true),
				entry("tweakeroo:c", "C", true))),
			config
		);

		assertEquals(2, snapshot.entries().size());
	}

	@Test
	void brokenProviderIsReportedNotThrown() {
		FeatureProvider broken = new FeatureProvider() {
			@Override
			public String modId() {
				return "broken";
			}

			@Override
			public List<FeatureEntry> collectFeatures() {
				throw new IllegalStateException("boom");
			}
		};

		FeatureSnapshot snapshot = FeatureListBuilder.build(
			List.of(broken, provider("tweakeroo", FeatureProvider.ProviderKind.TWEAKEROO, 100, entry("tweakeroo:a", "A", true))),
			new OverlayConfig().sanitize()
		);

		assertEquals(1, snapshot.entries().size());
		assertEquals(1, snapshot.errors().size());
		assertTrue(snapshot.errors().getFirst().contains("boom"));
	}

	@Test
	void noBaselineMeansNoChangedFeatures() {
		FeatureSnapshot snapshot = FeatureListBuilder.build(
			List.of(provider("tweakeroo", FeatureProvider.ProviderKind.TWEAKEROO, 100,
				entry("tweakeroo:a", "A", true),
				entry("tweakeroo:b", "B", false))),
			new OverlayConfig().sanitize()
		);

		assertEquals(1, snapshot.entries().size());
		assertEquals(DiffState.UNCHANGED, snapshot.diffState(snapshot.entries().getFirst()));
		assertTrue(snapshot.diffStates().values().stream().allMatch(DiffState.UNCHANGED::equals));
	}

	@Test
	void capturesNewlyEnabledAndNewlyDisabledFeatures() {
		OverlayConfig config = new OverlayConfig();
		config.sanitize(); // showDisabled = false

		FeatureSnapshot snapshot = FeatureListBuilder.build(
			List.of(provider("tweakeroo", FeatureProvider.ProviderKind.TWEAKEROO, 100,
				entry("tweakeroo:a", "A", false), // was enabled at baseline -> disabled now
				entry("tweakeroo:b", "B", true))), // was disabled at baseline -> enabled now
			config,
			Set.of("tweakeroo:a")
		);

		// The newly disabled entry has to be shown even though showDisabled is false.
		assertEquals(2, snapshot.entries().size());
		FeatureEntry newlyEnabled = snapshot.entries().stream()
			.filter(entry -> "tweakeroo:b".equals(entry.id()))
			.findFirst()
			.orElseThrow();
		FeatureEntry newlyDisabled = snapshot.entries().stream()
			.filter(entry -> "tweakeroo:a".equals(entry.id()))
			.findFirst()
			.orElseThrow();

		assertEquals(DiffState.NEWLY_ENABLED, snapshot.diffState(newlyEnabled));
		assertEquals(DiffState.NEWLY_DISABLED, snapshot.diffState(newlyDisabled));
	}

	@Test
	void unchangedFeaturesAreShownInShowAllMode() {
		OverlayConfig config = new OverlayConfig();
		config.diffOnlyChanged = false;
		config.sanitize();

		FeatureSnapshot snapshot = FeatureListBuilder.build(
			List.of(provider("tweakeroo", FeatureProvider.ProviderKind.TWEAKEROO, 100,
				entry("tweakeroo:a", "A", true), // enabled at baseline and now
				entry("tweakeroo:c", "C", false), // disabled at baseline and now
				entry("tweakeroo:d", "D", false))),
			config,
			Set.of("tweakeroo:a")
		);

		assertEquals(1, snapshot.entries().size());
		assertEquals("tweakeroo:a", snapshot.entries().getFirst().id());
		assertEquals(DiffState.UNCHANGED, snapshot.diffState(snapshot.entries().getFirst()));
	}

	@Test
	void defaultConfigShowsOnlyChangedFeaturesWhileBaselineIsActive() {
		OverlayConfig config = new OverlayConfig();
		config.baselineActive = true;
		config.sanitize(); // diffOnlyChanged defaults to true

		FeatureSnapshot snapshot = FeatureListBuilder.build(
			List.of(provider("tweakeroo", FeatureProvider.ProviderKind.TWEAKEROO, 100,
				entry("tweakeroo:a", "A", true), // unchanged enabled -> hidden
				entry("tweakeroo:b", "B", true), // newly enabled -> shown
				entry("tweakeroo:c", "C", false))), // newly disabled -> shown
			config,
			Set.of("tweakeroo:a", "tweakeroo:c")
		);

		assertEquals(2, snapshot.entries().size());
		assertTrue(snapshot.entries().stream().noneMatch(entry -> "tweakeroo:a".equals(entry.id())));
	}

	@Test
	void hiddenFiltersStillApplyToChangedFeatures() {
		OverlayConfig config = new OverlayConfig();
		config.hiddenFeatures.add("tweakeroo:a");
		config.sanitize();

		FeatureSnapshot snapshot = FeatureListBuilder.build(
			List.of(provider("tweakeroo", FeatureProvider.ProviderKind.TWEAKEROO, 100,
				entry("tweakeroo:a", "A", false),
				entry("tweakeroo:b", "B", true))),
			config,
			Set.of("tweakeroo:a")
		);

		assertEquals(1, snapshot.entries().size());
		assertEquals("tweakeroo:b", snapshot.entries().getFirst().id());
	}
}
