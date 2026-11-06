# Changelog

All notable changes to this project are documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project uses [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-11-01

### Added

- Right-aligned HUD overlay that lists the currently enabled Tweakeroo feature toggles, one
  feature per line, in the top-right corner of the screen (green by default).
- Optional rendering of disabled features, the extra Tweakeroo "Disable" toggles and the "Fixes"
  options.
- Automatic discovery of the feature toggles of other Tweakeroo add-ons through MaLiLib's config
  screen registry and config holder classes (works for TweakerMore and Eclipse's Tweakeroo
  Additions, among others).
- A small `FeatureProvider` API plus the `tweakeroo_overlay` custom Fabric entrypoint, so third
  party mods can contribute their own feature lines.
- JSON configuration file (`config/tweakeroo-overlay.json`) with options for corner, offset,
  scale, colours, background, line spacing, sorting, filtering, hidden features and mods and more.
- Toggle key binding, defaulting to `F6`.
- English and Simplified Chinese translations.
- Unit tests for the Minecraft independent feature aggregation / configuration logic.
