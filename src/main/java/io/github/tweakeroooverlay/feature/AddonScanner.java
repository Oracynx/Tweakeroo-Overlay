package io.github.tweakeroooverlay.feature;

import io.github.tweakeroooverlay.config.OverlayConfig;
import io.github.tweakeroooverlay.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Best-effort reflective discovery of the boolean feature toggles of a Tweakeroo add-on.
 *
 * <p>Sources are tried in this order, all results are merged and de-duplicated by config object
 * identity:</p>
 * <ol>
 *   <li>the mod's registered MaLiLib config screen ({@link ConfigScreenScanner});</li>
 *   <li>the mod's MaLiLib config handler, if the add-on registers one;</li>
 *   <li>the static config fields of the add-on's config holder classes, found by
 *       {@link ModClassLister} (covers add-ons without an "All configs" tab, e.g. TweakerMore
 *       and Eclipse's Tweakeroo Additions).</li>
 * </ol>
 *
 * <p>Everything is duck-typed: any object with the malilib config style methods
 * {@code getName()} / {@code getBooleanValue()} is accepted, independent of the actual
 * Tweakeroo/MaLiLib version. Objects like TweakerMore's {@code TweakerMoreOption} that wrap a
 * config object are unwrapped via a no-argument {@code getConfig()}-style method.</p>
 */
public final class AddonScanner {
	private static final int MAX_DEPTH = 3;
	private static final String[] UNWRAP_METHODS = {"getConfig", "getConfigOption", "getConfigBase", "getOption"};

	private AddonScanner() {
	}

	/**
	 * @return all discovered boolean configs of the mod, in discovery order
	 */
	public static List<ReflectiveBooleanConfig> scanMod(String modId, OverlayConfig overlayConfig) {
		List<ReflectiveBooleanConfig> result = new ArrayList<>();
		Set<Object> seen = Collections.newSetFromMap(new IdentityHashMap<>());

		// 1) The MaLiLib config screen(s) of the mod.
		ConfigScreenScanner.scan(modId, overlayConfig.addonRequireHotkey, seen, result);

		// 2) The MaLiLib registered config handler, if the add-on registers one.
		Object handler = MalilibConfigAccess.configHandlers().get(modId);

		if (handler != null) {
			collectFromValue(handler, modId, "Configs", overlayConfig, result, seen, 0);
		}

		// 3) The static config fields of the add-on's config holder classes.
		ClassLoader classLoader = AddonScanner.class.getClassLoader();

		for (String className : ModClassLister.listCandidateConfigClasses(modId)) {
			try {
				Class<?> clazz = Class.forName(className, true, classLoader);
				collectFromClass(clazz, modId, overlayConfig, result, seen, 0);
			} catch (Throwable ignored) {
				// A single broken/obfuscated add-on class must not break the whole scan.
			}
		}

		return result;
	}

	private static void collectFromClass(Class<?> clazz, String modId, OverlayConfig overlayConfig,
			List<ReflectiveBooleanConfig> result, Set<Object> seen, int depth) {
		String category = categoryName(clazz);

		for (Field field : clazz.getDeclaredFields()) {
			if (Modifier.isStatic(field.getModifiers()) == false || field.isSynthetic()) {
				continue;
			}

			try {
				if (field.canAccess(null) == false) {
					field.setAccessible(true);
				}

				collectFromValue(field.get(null), modId, category, overlayConfig, result, seen, depth);
			} catch (Throwable ignored) {
				// Ignore unreadable fields.
			}
		}

		if (depth >= MAX_DEPTH) {
			return;
		}

		try {
			for (Class<?> nested : clazz.getDeclaredClasses()) {
				collectFromClass(nested, modId, overlayConfig, result, seen, depth + 1);
			}
		} catch (Throwable ignored) {
		}
	}

	private static void collectFromValue(Object value, String modId, String category, OverlayConfig overlayConfig,
			List<ReflectiveBooleanConfig> result, Set<Object> seen, int depth) {
		if (value == null || depth > MAX_DEPTH) {
			return;
		}

		if (ReflectiveBooleanConfig.looksLikeBooleanConfig(value)) {
			ReflectiveBooleanConfig config = ReflectiveBooleanConfig.of(value, modId, category);

			if (config != null &&
				(overlayConfig.addonRequireHotkey == false || config.hasKeybind()) &&
				seen.add(value)) {
				result.add(config);
			}

			return;
		}

		// A wrapper object (e.g. TweakerMoreOption) that holds a config object.
		Object wrapped = unwrap(value);

		if (wrapped != null && wrapped != value && ReflectiveBooleanConfig.looksLikeBooleanConfig(wrapped)) {
			ReflectiveBooleanConfig config = ReflectiveBooleanConfig.of(wrapped, modId, category);

			if (config != null &&
				(overlayConfig.addonRequireHotkey == false || config.hasKeybind()) &&
				seen.add(wrapped)) {
				result.add(config);
			}

			return;
		}

		if (ReflectionUtils.isCollectionLike(value)) {
			for (Object element : ReflectionUtils.asList(value)) {
				collectFromValue(element, modId, category, overlayConfig, result, seen, depth + 1);
			}

			return;
		}

		if (value instanceof Map<?, ?> map) {
			for (Object element : map.values()) {
				collectFromValue(element, modId, category, overlayConfig, result, seen, depth + 1);
			}
		}
	}

	private static Object unwrap(Object value) {
		for (String methodName : UNWRAP_METHODS) {
			var method = ReflectionUtils.findMethod(value.getClass(), methodName);

			if (method != null) {
				Object result = ReflectionUtils.invoke(method, value);

				if (result != null && result != value) {
					return result;
				}
			}
		}

		return null;
	}

	private static String categoryName(Class<?> clazz) {
		String simpleName = clazz.getSimpleName();

		if (simpleName.isBlank()) {
			String name = clazz.getName();
			int dollar = name.lastIndexOf('$');
			return dollar >= 0 ? name.substring(dollar + 1) : name;
		}

		return simpleName;
	}
}
