package io.github.tweakeroooverlay.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A simple, insertion-ordered, thread-safe registry of feature providers.
 */
public final class FeatureProviderRegistry {
	private final List<FeatureProvider> providers = new ArrayList<>();

	public void register(FeatureProvider provider) {
		Objects.requireNonNull(provider, "provider");

		synchronized (this.providers) {
			this.providers.add(provider);
		}
	}

	public void registerAll(Collection<? extends FeatureProvider> providers) {
		Objects.requireNonNull(providers, "providers");

		synchronized (this.providers) {
			for (FeatureProvider provider : providers) {
				register(provider);
			}
		}
	}

	/** @return an immutable snapshot of the registered providers, in registration order. */
	public List<FeatureProvider> providers() {
		synchronized (this.providers) {
			return List.copyOf(this.providers);
		}
	}

	public int size() {
		synchronized (this.providers) {
			return this.providers.size();
		}
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public void clear() {
		synchronized (this.providers) {
			this.providers.clear();
		}
	}

	/** An empty, immutable registry; useful for tests and as a fallback. */
	public static FeatureProviderRegistry empty() {
		return new FeatureProviderRegistry();
	}

	/** @return an unmodifiable view of the providers, for debugging. */
	public List<FeatureProvider> view() {
		return Collections.unmodifiableList(providers());
	}
}
