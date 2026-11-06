package io.github.tweakeroooverlay.feature;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModDependency;
import net.fabricmc.loader.api.metadata.ModMetadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Finds the Tweakeroo add-on mods whose feature toggles should be considered for the overlay.
 *
 * <p>An add-on is detected from its Fabric metadata: a mod is treated as a Tweakeroo add-on if it
 * declares a relation to {@code tweakeroo} other than a conflict (TweakerMore, for example, only
 * declares {@code "breaks": {"tweakeroo": "<0.29.4"}} instead of a hard dependency). Mods can also
 * be listed explicitly via the {@code extraAddonModIds} config option.</p>
 */
public final class AddonDetector {
	private static final String TWEAKEROO = "tweakeroo";
	private static final String MALILIB = "malilib";
	private static final String SELF = "tweakeroo_overlay";

	private AddonDetector() {
	}

	/** @return the mod ids to scan, Tweakeroo itself and MaLiLib excluded */
	public static List<String> detectAddonModIds(Collection<String> extraModIds) {
		Set<String> result = new LinkedHashSet<>();

		if (extraModIds != null) {
			for (String modId : extraModIds) {
				if (modId != null && !modId.isBlank() && FabricLoader.getInstance().isModLoaded(modId)) {
					result.add(modId);
				}
			}
		}

		for (ModContainer container : FabricLoader.getInstance().getAllMods()) {
			Optional<ModMetadata> metadata = Optional.ofNullable(container.getMetadata());

			if (metadata.isEmpty()) {
				continue;
			}

			String modId = metadata.get().getId();

			if (isIgnoredMod(modId)) {
				continue;
			}

			if (isTweakerooAddon(metadata.get())) {
				result.add(modId);
			}
		}

		return new ArrayList<>(result);
	}

	private static boolean isIgnoredMod(String modId) {
		return TWEAKEROO.equals(modId) || MALILIB.equals(modId) || SELF.equals(modId);
	}

	private static boolean isTweakerooAddon(ModMetadata metadata) {
		try {
			for (ModDependency dependency : metadata.getDependencies()) {
				if (TWEAKEROO.equals(dependency.getModId()) && dependency.getKind() != ModDependency.Kind.CONFLICTS) {
					return true;
				}
			}
		} catch (Throwable ignored) {
			// Malformed metadata: treat as not an add-on.
		}

		return false;
	}

	/** @return the display name of a mod id, falling back to the id itself */
	public static String modDisplayName(String modId) {
		try {
			return FabricLoader.getInstance()
				.getModContainer(modId)
				.map(container -> container.getMetadata().getName())
				.filter(name -> name != null && !name.isBlank())
				.orElse(modId);
		} catch (Throwable ignored) {
			return modId;
		}
	}
}
