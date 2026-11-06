package io.github.tweakeroooverlay.feature;

import io.github.tweakeroooverlay.api.FeatureEntry;
import io.github.tweakeroooverlay.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * Reflective view of one boolean config object of Tweakeroo / one of its add-ons.
 *
 * <p>No Tweakeroo/MaLiLib type is referenced at compile time. A config object is accepted if it
 * looks like a malilib config (has {@code getName()} and {@code getBooleanValue()}). All method
 * handles are cached; values are read on every collection pass so hotkey toggles show up
 * immediately.</p>
 */
public final class ReflectiveBooleanConfig {
	private final Object target;
	private final String modId;
	private final String category;
	private final Method getName;
	private final Method getBooleanValue;
	private final Method getPrettyName;
	private final Method getConfigGuiDisplayName;
	private final Method getKeybind;

	private ReflectiveBooleanConfig(Object target, String modId, String category,
			Method getName, Method getBooleanValue, Method getPrettyName,
			Method getConfigGuiDisplayName, Method getKeybind) {
		this.target = target;
		this.modId = modId;
		this.category = category == null ? "" : category;
		this.getName = getName;
		this.getBooleanValue = getBooleanValue;
		this.getPrettyName = getPrettyName;
		this.getConfigGuiDisplayName = getConfigGuiDisplayName;
		this.getKeybind = getKeybind;
	}

	/**
	 * @return true if the object duck-types as a malilib boolean config, i.e. has public
	 *         no-argument {@code getName()} and {@code getBooleanValue()} methods
	 */
	public static boolean looksLikeBooleanConfig(Object target) {
		if (target == null) {
			return false;
		}

		Class<?> clazz = target.getClass();

		return ReflectionUtils.findMethod(clazz, "getName") != null &&
			ReflectionUtils.findMethod(clazz, "getBooleanValue") != null;
	}

	/**
	 * Tries to wrap the given object as a boolean config.
	 *
	 * @return the wrapper, or {@code null} if the object is not a boolean config
	 */
	public static ReflectiveBooleanConfig of(Object target, String modId, String category) {
		if (target == null) {
			return null;
		}

		Class<?> clazz = target.getClass();
		Method getName = ReflectionUtils.findMethod(clazz, "getName");
		Method getBooleanValue = ReflectionUtils.findMethod(clazz, "getBooleanValue");

		if (getName == null || getBooleanValue == null || getBooleanValue.getReturnType() != boolean.class) {
			return null;
		}

		return new ReflectiveBooleanConfig(target, modId, category, getName, getBooleanValue,
			ReflectionUtils.findMethod(clazz, "getPrettyName"),
			ReflectionUtils.findMethod(clazz, "getConfigGuiDisplayName"),
			ReflectionUtils.findMethod(clazz, "getKeybind"));
	}

	/** @return true if this config has an associated key bind, i.e. is a user-toggleable feature */
	public boolean hasKeybind() {
		return this.getKeybind != null;
	}

	/** @return the raw config name, e.g. {@code "tweakFastBlockPlacement"} */
	public String name() {
		Object value = ReflectionUtils.invoke(this.getName, this.target);
		return value instanceof String str && !str.isBlank() ? str : this.target.getClass().getSimpleName();
	}

	/** @return the display name used in the HUD (localized, without formatting codes) */
	public String label() {
		String label = readString(this.getPrettyName);

		if (label == null || label.isBlank()) {
			label = readString(this.getConfigGuiDisplayName);
		}

		if (label == null || label.isBlank()) {
			label = name();
		}

		String clean = ReflectionUtils.stripFormatting(label);
		return clean == null || clean.isBlank() ? name() : clean.trim();
	}

	private String readString(Method method) {
		Object value = ReflectionUtils.invoke(method, this.target);

		return value instanceof String str ? str : null;
	}

	public boolean enabled() {
		Object value = ReflectionUtils.invoke(this.getBooleanValue, this.target);

		return value instanceof Boolean bool && bool;
	}

	public String id() {
		return this.modId + ":" + name();
	}

	public FeatureEntry toEntry() {
		return FeatureEntry.of(id(), this.modId, label(), this.category, enabled());
	}

	@Override
	public String toString() {
		return "ReflectiveBooleanConfig[" + id() + "]";
	}
}
