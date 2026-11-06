package io.github.tweakeroooverlay.feature;

import io.github.tweakeroooverlay.api.FeatureEntry;
import io.github.tweakeroooverlay.api.FeatureProvider;
import io.github.tweakeroooverlay.config.OverlayConfig;
import io.github.tweakeroooverlay.util.ReflectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The built-in provider for Tweakeroo's own feature toggles.
 *
 * <p>Tweakeroo stores its user-facing feature toggles in the {@code FeatureToggle} enum
 * ({@code fi.dy.masa.tweakeroo.config.FeatureToggle}). The names of that class and of its
 * {@code VALUES} list have been stable for many Tweakeroo versions, and every constant exposes
 * the generic malilib config methods ({@code getBooleanValue()}, {@code getName()},
 * {@code getPrettyName()}), which is why this provider accesses them reflectively.</p>
 *
 * <p>Optionally the toggles from the {@code Disable} and {@code Fixes} config categories can be
 * included as well (both are disabled by default, since they are not the "tweaks" users usually
 * have enabled).</p>
 */
public final class TweakerooFeatureProvider implements FeatureProvider {
	public static final String TWEAKEROO_MOD_ID = "tweakeroo";
	private static final String FEATURE_TOGGLE_CLASS = "fi.dy.masa.tweakeroo.config.FeatureToggle";
	private static final String CONFIGS_CLASS = "fi.dy.masa.tweakeroo.config.Configs";

	private final OverlayConfig config;
	private List<ReflectiveBooleanConfig> tweaks;
	private List<ReflectiveBooleanConfig> disableToggles;
	private List<ReflectiveBooleanConfig> fixes;

	public TweakerooFeatureProvider(OverlayConfig config) {
		this.config = config;
	}

	@Override
	public String modId() {
		return TWEAKEROO_MOD_ID;
	}

	@Override
	public int priority() {
		return TWEAKEROO_PRIORITY;
	}

	@Override
	public ProviderKind kind() {
		return ProviderKind.TWEAKEROO;
	}

	@Override
	public List<FeatureEntry> collectFeatures() {
		if (this.config.includeTweakeroo == false) {
			return List.of();
		}

		List<FeatureEntry> result = new ArrayList<>();
		addAll(result, tweaks());

		if (this.config.includeTweakerooDisableToggles) {
			addAll(result, disableToggles());
		}

		if (this.config.includeTweakerooFixes) {
			addAll(result, fixes());
		}

		return result;
	}

	private static void addAll(List<FeatureEntry> result, List<ReflectiveBooleanConfig> configs) {
		for (ReflectiveBooleanConfig config : configs) {
			result.add(config.toEntry());
		}
	}

	/** The main "Tweaks" tab of Tweakeroo. */
	private List<ReflectiveBooleanConfig> tweaks() {
		if (this.tweaks != null) {
			return this.tweaks;
		}

		List<ReflectiveBooleanConfig> result = new ArrayList<>();
		Class<?> featureToggle = ReflectionUtils.findClass(FEATURE_TOGGLE_CLASS);
		Object values = ReflectionUtils.getStaticField(featureToggle, "VALUES");

		if (values == null) {
			values = ReflectionUtils.invokeStatic(featureToggle, "values");
		}

		for (Object element : ReflectionUtils.asList(values)) {
			ReflectiveBooleanConfig config = ReflectiveBooleanConfig.of(element, TWEAKEROO_MOD_ID, "Tweaks");

			if (config != null) {
				result.add(config);
			}
		}

		this.tweaks = Collections.unmodifiableList(result);
		return this.tweaks;
	}

	/** The "Disable ..." toggles from Tweakeroo's Disable tab. */
	private List<ReflectiveBooleanConfig> disableToggles() {
		if (this.disableToggles != null) {
			return this.disableToggles;
		}

		this.disableToggles = Collections.unmodifiableList(scanOptionsList("Disable", "Disable", true));
		return this.disableToggles;
	}

	/** The boolean options from Tweakeroo's Fixes tab. */
	private List<ReflectiveBooleanConfig> fixes() {
		if (this.fixes != null) {
			return this.fixes;
		}

		this.fixes = Collections.unmodifiableList(scanOptionsList("Fixes", "Fixes", false));
		return this.fixes;
	}

	/**
	 * Reads one of the {@code Configs.<Category>.OPTIONS} lists. Tweakeroo used both
	 * {@code OPTIONS} and {@code VALUES} field names over its history, so both are tried.
	 */
	private static List<ReflectiveBooleanConfig> scanOptionsList(String nestedClass, String category, boolean requireKeybind) {
		List<ReflectiveBooleanConfig> result = new ArrayList<>();
		Class<?> clazz = ReflectionUtils.findClass(CONFIGS_CLASS + "$" + nestedClass);

		if (clazz == null) {
			return result;
		}

		Object options = ReflectionUtils.getStaticField(clazz, "OPTIONS");

		if (options == null) {
			options = ReflectionUtils.getStaticField(clazz, "VALUES");
		}

		for (Object element : ReflectionUtils.asList(options)) {
			ReflectiveBooleanConfig config = ReflectiveBooleanConfig.of(element, TWEAKEROO_MOD_ID, category);

			if (config != null && (requireKeybind == false || config.hasKeybind())) {
				result.add(config);
			}
		}

		return result;
	}
}
