package io.github.tweakeroooverlay.config;

import java.util.ArrayList;
import java.util.List;

/**
 * The persisted configuration of the overlay.
 *
 * <p>This class is intentionally plain data (public fields + Gson), with no Minecraft
 * dependencies, so it can be unit tested. {@link #sanitize()} must be called after loading to
 * guard against hand-edited / outdated config files.</p>
 */
public final class OverlayConfig {
	public static final int CURRENT_SCHEMA = 1;

	/** Schema version, used for future config migrations. */
	public int schemaVersion = CURRENT_SCHEMA;

	/** Master switch of the overlay. Also toggled by the key binding. */
	public boolean enabled = true;

	/** Screen corner to anchor the overlay to. Right side corners render right-aligned. */
	public Corner corner = Corner.TOP_RIGHT;

	/** Offset from the anchored screen edge, in GUI pixels. */
	public int offsetX = 4;
	public int offsetY = 4;

	/** HUD scale multiplier. 1.0 = normal text size. */
	public float scale = 1.0F;

	/** Draw a dark background box behind the lines. */
	public boolean showBackground = true;
	public int backgroundColor = 0x66000000;
	public int backgroundPadding = 2;

	/** Extra pixels between two lines. */
	public int lineSpacing = 1;

	/** Draw the vanilla text shadow. */
	public boolean showShadow = true;

	/** Also render feature lines for disabled features, using {@link #disabledColor}. */
	public boolean showDisabled = false;

	/** Colour for enabled features (ARGB). Defaults to the vanilla chat green. */
	public int enabledColor = 0xFF55FF55;

	/** Colour for disabled features (ARGB), only used when {@link #showDisabled} is true. */
	public int disabledColor = 0xFFAAAAAA;

	/** Colour for features that were disabled when the baseline was captured and are enabled now (ARGB). */
	public int newlyEnabledColor = 0xFF00FF00;

	/** Colour for features that were enabled when the baseline was captured and are disabled now (ARGB). */
	public int newlyDisabledColor = 0xFFFF5555;

	// ---- Change tracking ("capture / clear baseline" key binding) ----

	/** Whether a captured baseline is active and the change highlighting is enabled. */
	public boolean baselineActive = false;

	/** Feature ids that were enabled when the baseline was captured (managed automatically). */
	public List<String> baselineFeatures = new ArrayList<>();

	/**
	 * When a baseline is active, only render features whose state changed since the baseline.
	 * This is the default: the change tracking is meant to answer "what changed since my
	 * snapshot?". Set to {@code false} to keep showing all features and only highlight the
	 * changed ones instead.
	 */
	public boolean diffOnlyChanged = true;

	/** Prefix every line with the owning mod name, e.g. {@code [Tweakeroo] Fast Placement}. */
	public boolean showProviderName = false;

	/** Line ordering. */
	public SortMode sortMode = SortMode.CONFIG_ORDER;

	/** Maximum number of rendered lines; 0 or negative means unlimited. */
	public int maxLines = 0;

	/** Include the feature toggles of Tweakeroo itself. */
	public boolean includeTweakeroo = true;

	/** Also include the "Disable ..." toggles from Tweakeroo's Disable config category. */
	public boolean includeTweakerooDisableToggles = false;

	/** Also include the (usually enabled by default) boolean options from Tweakeroo's Fixes category. */
	public boolean includeTweakerooFixes = false;

	/** Include features auto-discovered from other Tweakeroo add-on mods. */
	public boolean includeAddonMods = true;

	/** Include third-party providers registered through the custom entrypoint. */
	public boolean includeCustomProviders = true;

	/**
	 * Only auto-discover boolean configs that have a key bind. Feature toggles of masa's mods and
	 * most add-ons are hotkey-toggleable, while plain boolean settings usually are not, so this
	 * keeps the overlay free of non-feature settings. Explicit providers are not affected.
	 */
	public boolean addonRequireHotkey = true;

	/** Extra mod ids to scan for add-on features, even if their metadata does not hint at Tweakeroo. */
	public List<String> extraAddonModIds = new ArrayList<>();

	/** Features ids that are never shown. Entries match id or id without mod prefix. */
	public List<String> hiddenFeatures = new ArrayList<>();

	/** Mod ids whose features are never shown. */
	public List<String> hiddenMods = new ArrayList<>();

	/** If non-empty, only these mod ids are considered. */
	public List<String> visibleMods = new ArrayList<>();

	/** Hide the overlay while the F3 debug screen is open. */
	public boolean hideWithDebugScreen = true;

	/** Hide the overlay while any GUI screen is open. */
	public boolean hideInScreens = true;

	/**
	 * Normalizes all values into valid ranges and replaces null collections created by bad
	 * hand-edited config files. Returns {@code this} for chaining.
	 */
	public OverlayConfig sanitize() {
		if (schemaVersion <= 0) {
			schemaVersion = CURRENT_SCHEMA;
		}

		if (corner == null) {
			corner = Corner.TOP_RIGHT;
		}

		if (sortMode == null) {
			sortMode = SortMode.CONFIG_ORDER;
		}

		offsetX = clamp(offsetX, -10000, 10000);
		offsetY = clamp(offsetY, -10000, 10000);
		scale = clamp(scale, 0.25F, 4.0F);
		backgroundPadding = clamp(backgroundPadding, 0, 64);
		lineSpacing = clamp(lineSpacing, -8, 64);

		if (maxLines < 0) {
			maxLines = 0;
		}

		hiddenFeatures = sanitizeList(hiddenFeatures);
		hiddenMods = sanitizeList(hiddenMods);
		visibleMods = sanitizeList(visibleMods);
		extraAddonModIds = sanitizeList(extraAddonModIds);
		baselineFeatures = sanitizeList(baselineFeatures);

		return this;
	}

	public OverlayConfig copy() {
		OverlayConfig copy = new OverlayConfig();
		copy.schemaVersion = this.schemaVersion;
		copy.enabled = this.enabled;
		copy.corner = this.corner;
		copy.offsetX = this.offsetX;
		copy.offsetY = this.offsetY;
		copy.scale = this.scale;
		copy.showBackground = this.showBackground;
		copy.backgroundColor = this.backgroundColor;
		copy.backgroundPadding = this.backgroundPadding;
		copy.lineSpacing = this.lineSpacing;
		copy.showShadow = this.showShadow;
		copy.showDisabled = this.showDisabled;
		copy.enabledColor = this.enabledColor;
		copy.disabledColor = this.disabledColor;
		copy.newlyEnabledColor = this.newlyEnabledColor;
		copy.newlyDisabledColor = this.newlyDisabledColor;
		copy.baselineActive = this.baselineActive;
		copy.baselineFeatures = new ArrayList<>(this.baselineFeatures);
		copy.diffOnlyChanged = this.diffOnlyChanged;
		copy.showProviderName = this.showProviderName;
		copy.sortMode = this.sortMode;
		copy.maxLines = this.maxLines;
		copy.includeTweakeroo = this.includeTweakeroo;
		copy.includeTweakerooDisableToggles = this.includeTweakerooDisableToggles;
		copy.includeTweakerooFixes = this.includeTweakerooFixes;
		copy.includeAddonMods = this.includeAddonMods;
		copy.includeCustomProviders = this.includeCustomProviders;
		copy.addonRequireHotkey = this.addonRequireHotkey;
		copy.hiddenFeatures = new ArrayList<>(this.hiddenFeatures);
		copy.hiddenMods = new ArrayList<>(this.hiddenMods);
		copy.visibleMods = new ArrayList<>(this.visibleMods);
		copy.extraAddonModIds = new ArrayList<>(this.extraAddonModIds);
		copy.hideWithDebugScreen = this.hideWithDebugScreen;
		copy.hideInScreens = this.hideInScreens;
		return copy;
	}

	private static List<String> sanitizeList(List<String> list) {
		List<String> result = new ArrayList<>();

		if (list != null) {
			for (String value : list) {
				if (value != null && !value.isBlank()) {
					result.add(value.trim());
				}
			}
		}

		return result;
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	private static float clamp(float value, float min, float max) {
		if (Float.isNaN(value)) {
			return 1.0F;
		}

		return Math.max(min, Math.min(max, value));
	}
}
