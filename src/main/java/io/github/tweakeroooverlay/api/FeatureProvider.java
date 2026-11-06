package io.github.tweakeroooverlay.api;

import java.util.List;

/**
 * A source of feature toggles for the overlay.
 *
 * <p>Providers are added through one of these mechanisms:</p>
 * <ol>
 *   <li>the built-in Tweakeroo provider (see {@code TweakerooFeatureProvider}),</li>
 *   <li>the built-in generic provider, which finds boolean configs of other Tweakeroo add-ons,</li>
 *   <li>the Fabric custom entrypoint {@code "tweakeroo_overlay"} for
 *       explicit, third-party providers. A mod can add, in its own {@code fabric.mod.json}:
 *       <pre>{@code
 *       "entrypoints": {
 *         "tweakeroo_overlay": [ "com.example.MyProvider" ]
 *       }
 *       }</pre>
 *   </li>
 * </ol>
 *
 * <p>Implementations must have a public no-argument constructor when registered as an entrypoint.
 * They are called from the client thread only.</p>
 */
public interface FeatureProvider {
	/** The mod id of the mod that owns the features, e.g. {@code "tweakeroo"}. */
	String modId();

	/**
	 * @return all feature toggles known to this provider. Implementations should return both
	 *         enabled and disabled entries; the overlay filters/sorts them based on its config.
	 */
	List<FeatureEntry> collectFeatures();

	/**
	 * Ordering when two providers return an entry with the same id. Lower values win; the first
	 * collected entry for an id is kept. Explicit third-party providers default to
	 * {@link #DEFAULT_PRIORITY}, the built-in generic add-on discovery uses {@link #GENERIC_DISCOVERY_PRIORITY}.
	 */
	default int priority() {
		return DEFAULT_PRIORITY;
	}

	/** Which config switches (if any) control this provider. */
	default ProviderKind kind() {
		return ProviderKind.CUSTOM;
	}

	/**
	 * @return a short human readable description of the provider, used for debug logging only.
	 */
	default String describe() {
		return getClass().getSimpleName() + "[" + modId() + "]";
	}

	int EXPLICIT_PRIORITY = 0;
	int TWEAKEROO_PRIORITY = 100;
	int GENERIC_DISCOVERY_PRIORITY = 500;
	int DEFAULT_PRIORITY = EXPLICIT_PRIORITY;

	enum ProviderKind {
		/** The features of Tweakeroo itself. */
		TWEAKEROO,
		/** Automatically discovered features of another Tweakeroo add-on. */
		ADDON,
		/** A third-party provider registered through the custom entrypoint. */
		CUSTOM
	}
}
