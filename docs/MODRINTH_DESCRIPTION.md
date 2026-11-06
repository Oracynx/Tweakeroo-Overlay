# Tweakeroo Overlay

**Tweakeroo Overlay** is an unofficial, client-side add-on for [Tweakeroo](https://modrinth.com/mod/tweakeroo) that shows the feature toggles you currently have enabled in a compact HUD list in the top-right corner of the screen — one feature per line, right-aligned, and green by default.

Tweakeroo has around ninety feature toggles, many of which are switched on and off with hotkeys during gameplay. It is easy to lose track of which ones are actually active. Tweakeroo Overlay answers that question at a glance, without opening the config menu or pressing every hotkey to check.

Built for **Minecraft 26.3** on **Fabric**.

## Features

- **Always up to date** — reads Tweakeroo's `FeatureToggle` values directly, so a feature toggled by its hotkey appears (or disappears) in the overlay immediately.
- **Clean default look** — top-right corner, right-aligned, one feature per line in Minecraft green, with a subtle background box.
- **Fully configurable** — corner and offset, scale, background and colours, line spacing, text shadow, sort order, maximum line count, showing disabled features, per-mod name prefixes, and more.
- **Works with other Tweakeroo add-ons** — automatically discovers feature toggles of add-ons that use MaLiLib's config system, such as [TweakerMore](https://modrinth.com/mod/tweakermore) and [Eclipse's Tweakeroo Additions](https://modrinth.com/mod/eclipses-tweakeroo-additions). A small public API lets any add-on provide its own lines as well.
- **Safe and read-only** — never writes Tweakeroo or MaLiLib configs and never takes over hotkey callbacks; it only polls values. Every provider and render error is caught and degrades gracefully instead of crashing the game.
- **Out of the way** — hides automatically while a GUI screen or the F3 debug screen is open (both configurable), and respects the vanilla F1 "hide HUD" state.
- **Toggle on the fly** — press **F6** (rebindable in Controls) to hide or show the overlay; the state is saved.
- **Localized** — English and Simplified Chinese included.

## Requirements

| Component | Version |
| --- | --- |
| Minecraft | 26.3.x |
| Fabric Loader | 0.20.1 or newer |
| Fabric API | any 26.3 build (tested with 0.165.0+26.3) |
| [MaLiLib](https://modrinth.com/mod/malilib) | 0.30.0 or newer (tested with 0.30.2) |
| [Tweakeroo](https://modrinth.com/mod/tweakeroo) | 0.30.0 or newer (tested with 0.30.1) |
| Java | 25 or newer |

This mod is **client-side only**. Do not install it on a server.

## Installation

1. Install Fabric Loader 0.20.1+ for Minecraft 26.3 and put Fabric API in your `mods/` folder.
2. Install MaLiLib and Tweakeroo (Tweakeroo Overlay does not bundle them).
3. Put `tweakeroo-overlay-<version>.jar` into your `mods/` folder.
4. Launch the game. The overlay appears in the top-right corner; press **F6** to toggle it.

## Configuration

The config file is created on first launch at `config/tweakeroo-overlay.json`. It is a plain JSON file; edit it and restart the game for changes to apply. Colours are signed 32-bit ARGB integers in decimal form (for example, `-11141291` is opaque green `#55FF55`).

```json
{
  "schemaVersion": 1,
  "enabled": true,
  "corner": "TOP_RIGHT",
  "offsetX": 4,
  "offsetY": 4,
  "scale": 1.0,
  "showBackground": true,
  "backgroundColor": 1711276032,
  "backgroundPadding": 2,
  "lineSpacing": 1,
  "showShadow": true,
  "showDisabled": false,
  "enabledColor": -11141291,
  "disabledColor": -5592406,
  "showProviderName": false,
  "sortMode": "CONFIG_ORDER",
  "maxLines": 0,
  "includeTweakeroo": true,
  "includeTweakerooDisableToggles": false,
  "includeTweakerooFixes": false,
  "includeAddonMods": true,
  "includeCustomProviders": true,
  "addonRequireHotkey": true,
  "extraAddonModIds": [],
  "hiddenFeatures": [],
  "hiddenMods": [],
  "visibleMods": [],
  "hideWithDebugScreen": true,
  "hideInScreens": true
}
```

| Option | Description |
| --- | --- |
| `enabled` | Master switch of the overlay (also toggled by the F6 key bind and saved). |
| `corner` | Anchor corner: `TOP_LEFT`, `TOP_RIGHT`, `BOTTOM_LEFT` or `BOTTOM_RIGHT`. The right-side corners render right-aligned. |
| `offsetX` / `offsetY` | Distance from the anchored screen edges, in GUI pixels. |
| `scale` | HUD scale multiplier (0.25–4.0, default 1.0). |
| `showBackground` / `backgroundColor` / `backgroundPadding` | Background box toggle, ARGB colour and inner padding. |
| `lineSpacing` | Extra pixels between two lines. |
| `showShadow` | Draw the vanilla text shadow. |
| `showDisabled` | Also list features that are currently disabled, using `disabledColor`. |
| `enabledColor` / `disabledColor` | Text colours (ARGB). Enabled defaults to green `-11141291`. |
| `showProviderName` | Prefix each line with `[mod name]`. |
| `sortMode` | `CONFIG_ORDER` (Tweakeroo config order, default), `ALPHABETICAL`, or `MOD_THEN_ALPHABETICAL`. |
| `maxLines` | Maximum number of rendered lines; `0` means unlimited. |
| `includeTweakeroo` | Include the feature toggles of Tweakeroo itself. |
| `includeTweakerooDisableToggles` | Also include the hotkeyed toggles from Tweakeroo's "Disable" category. |
| `includeTweakerooFixes` | Also include boolean options from Tweakeroo's "Fixes" category. |
| `includeAddonMods` | Automatically discover feature toggles from other Tweakeroo add-ons. |
| `includeCustomProviders` | Load providers registered through the custom entrypoint (see below). |
| `addonRequireHotkey` | When auto-discovering, only keep toggles that have a key bind. This filters out ordinary settings and is recommended. |
| `extraAddonModIds` | Extra mod IDs to scan, for add-ons that do not declare a Tweakeroo relation in their metadata. |
| `hiddenFeatures` | Feature IDs that are never shown. Both `tweakeroo:tweakFastBlockPlacement` and `tweakFastBlockPlacement` are accepted. |
| `hiddenMods` / `visibleMods` | Mod ID blacklist / whitelist. When `visibleMods` is non-empty, only those mods are shown. |
| `hideWithDebugScreen` / `hideInScreens` | Hide the overlay while the F3 debug screen / any GUI screen is open. |

## Works with other Tweakeroo add-ons

A mod is treated as a Tweakeroo add-on if its Fabric metadata declares a relation to `tweakeroo` in its `depends`, `recommends`, `suggests` or `breaks` section (a plain conflict is ignored). You can also force additional mod IDs through `extraAddonModIds`.

The overlay discovers their toggles by trying, in order:

1. the mod's registered MaLiLib config screen (the complete config list is used when the mod implements the "All" config tab, like Tweakeroo does);
2. the mod's MaLiLib `IConfigHandler`, if it registers one;
3. the static config fields of the mod's config classes, which covers add-ons without an "All" tab (for example TweakerMore and Eclipse's Tweakeroo Additions).

By default only boolean configs with a key bind are shown (`addonRequireHotkey`), so ordinary settings do not flood the overlay. All of this can be limited with `includeAddonMods`, `addonRequireHotkey`, `extraAddonModIds`, `visibleMods`, `hiddenMods` and `hiddenFeatures`.

## For add-on developers

If your mod does not use MaLiLib's config system, or you want full control over labels and categories, implement the small `FeatureProvider` API and register it through the custom Fabric entrypoint `tweakeroo_overlay`.

In your `fabric.mod.json`:

```json
{
  "entrypoints": {
    "tweakeroo_overlay": [
      "com.example.myaddon.MyOverlayFeatureProvider"
    ]
  }
}
```

And implement the interface (add Tweakeroo Overlay as a `compileOnly` dependency):

```java
public final class MyOverlayFeatureProvider implements FeatureProvider {
    @Override
    public String modId() {
        return "myaddon";
    }

    @Override
    public List<FeatureEntry> collectFeatures() {
        // Return enabled and disabled entries; the overlay filters and sorts them.
        // Values are re-read on every refresh, so return live values here.
        return List.of(
            FeatureEntry.of("myaddon:myToggle", "myaddon", "My Toggle", "Tweaks",
                MyConfigs.MY_TOGGLE.getBooleanValue())
        );
    }
}
```

`FeatureEntry` fields: `id` (globally unique, recommended format `"<modid>:<name>"`), `modId`, `label` (already-localized HUD text), `category` (optional grouping), and `enabled`.

- Providers are called on the client thread only.
- Custom providers have the highest priority: an entry with the same ID overrides auto-discovered entries from other providers.
- Providers must be read-only. Do not modify other mods' configs or steal their hotkey callbacks.
- A provider that throws is skipped and logged; it will never crash the game.

## Compatibility and reliability

- Built against Minecraft 26.3 with Fabric Loom 1.19.3 and Java 25. Minecraft 26.x jars are no longer obfuscated, so the mod ships as a regular (non-remapped) jar.
- Tweakeroo and MaLiLib internals are accessed **reflectively and duck-typed** (`FeatureToggle.VALUES`, `Configs$Disable/OPTIONS`, the MaLiLib config screen registry, config class static fields). If a future version renames or removes one of those members, only the affected provider is disabled and a warning is logged.
- The dependency ranges for Tweakeroo and MaLiLib have no upper bound (`>=0.30.0-`), so newer builds are allowed; if a large future rewrite changes the rendering or config APIs, an update may still be required.
- Tested with Tweakeroo 0.30.1 and MaLiLib 0.30.2 on Minecraft 26.3.
- The mod is read-only with respect to Tweakeroo and MaLiLib and does not interfere with their key handling.

## Known limitations

- There is no in-game configuration GUI yet; edit `config/tweakeroo-overlay.json` (the F6 toggle is saved immediately).
- Add-ons that inject toggles directly into Tweakeroo's config list with Mixins (for example the Slime & Honey add-on) are shown as Tweakeroo features, since their true origin cannot be detected.
- Auto-discovery only covers mods that go through MaLiLib's config system; other mods should use the provider API.
- Vanilla status-effect icons share the top-right corner. If you play with many active effects, increase `offsetY` or choose a different `corner`.
- This is an unofficial add-on. It is not affiliated with Tweakeroo's authors, and a future major Tweakeroo rewrite could require an update here as well.

## Credits

- **masa** and **Sakura-Ryoko** for Tweakeroo and MaLiLib. Both are separate downloads and are not bundled with this mod.
- The **Fabric** team for Fabric Loader, Fabric API and Loom.
- Not affiliated with Mojang or Microsoft. "Tweakeroo" is used only to describe compatibility.

## License

MIT. See the LICENSE file in the source repository.
