package io.github.tweakeroooverlay.feature;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigBoolean;
import fi.dy.masa.malilib.config.IHotkeyTogglable;
import fi.dy.masa.malilib.config.options.BooleanHotkeyGuiWrapper;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.interfaces.IConfigGui;
import fi.dy.masa.malilib.gui.interfaces.IConfigGuiAllTab;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.data.ModInfo;

import java.util.List;
import java.util.Set;

/**
 * Enumerates the boolean configs of a mod through MaLiLib's config screen registry.
 *
 * <p>Every mod that has a MaLiLib config GUI is registered in {@code Registry.CONFIG_SCREEN}.
 * Tweakeroo (and some other mods) implement {@link IConfigGuiAllTab}, which exposes the configs of
 * <em>all</em> tabs; for other mods only the currently selected (default) tab is available, which
 * is still a useful best-effort fallback. This is the most version-tolerant way to discover the
 * feature toggles of Tweakeroo add-ons that use MaLiLib's config system.</p>
 *
 * <p>Instantiating config screens is done lazily (on the first HUD refresh) and every mod is
 * isolated with a {@code try/catch}, so a broken add-on GUI can never break the overlay.</p>
 */
public final class ConfigScreenScanner {
	private ConfigScreenScanner() {
	}

	/**
	 * Collects the boolean configs of one mod from its registered config screen(s).
	 *
	 * @param seen identity set used to avoid collecting the same config object twice
	 */
	public static void scan(String modId, boolean requireHotkey, Set<Object> seen,
			List<ReflectiveBooleanConfig> result) {
		try {
			for (ModInfo modInfo : Registry.CONFIG_SCREEN.getAllModsWithConfigScreens()) {
				if (modId.equals(modInfo.modId()) == false) {
					continue;
				}

				try {
					scanScreen(modId, modInfo, requireHotkey, seen, result);
				} catch (Throwable ignored) {
					// A broken add-on config screen must not break the scan.
				}
			}
		} catch (Throwable ignored) {
			// MaLiLib versions without a config screen registry: ignore this source.
		}
	}

	private static void scanScreen(String modId, ModInfo modInfo, boolean requireHotkey, Set<Object> seen,
			List<ReflectiveBooleanConfig> result) {
		GuiBase screen = modInfo.configScreenSupplier().get();

		if (screen == null) {
			return;
		}

		List<GuiConfigsBase.ConfigOptionWrapper> wrappers = null;

		if (screen instanceof IConfigGuiAllTab allTab) {
			wrappers = allTab.getAllConfigs();
		} else if (screen instanceof IConfigGui configGui) {
			wrappers = configGui.getConfigs();
		}

		if (wrappers == null) {
			return;
		}

		for (GuiConfigsBase.ConfigOptionWrapper wrapper : wrappers) {
			if (wrapper == null || wrapper.getType() != GuiConfigsBase.ConfigOptionWrapper.Type.CONFIG) {
				continue;
			}

			IConfigBase config = wrapper.getConfig();

			// Tweakeroo (and others) wrap their enum based toggles so the GUI can show the
			// key bind next to them; the wrapped config holds the real name/value.
			if (config instanceof BooleanHotkeyGuiWrapper hotkeyWrapper) {
				config = hotkeyWrapper.getBooleanConfig();
			}

			if (!(config instanceof IConfigBoolean)) {
				continue;
			}

			if (requireHotkey && !(config instanceof IHotkeyTogglable)) {
				continue;
			}

			ReflectiveBooleanConfig reflective = ReflectiveBooleanConfig.of(config, modId, modInfo.modName());

			if (reflective != null && seen.add(config)) {
				result.add(reflective);
			}
		}
	}
}
