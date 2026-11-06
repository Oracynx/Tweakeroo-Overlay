package io.github.tweakeroooverlay.feature;

import io.github.tweakeroooverlay.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reflective access to MaLiLib's per-mod config handler registry.
 *
 * <p>MaLiLib only exposes {@code IConfigManager.registerConfigHandler(modId, handler)} and
 * {@code onConfigsChanged(modId)} publicly; there is no public getter for the registered handlers.
 * The handlers are kept in a private map of the {@code ConfigManager} singleton. This class reads
 * that map (with a few fallbacks for other MaLiLib versions) so that the overlay can enumerate the
 * configs of other Tweakeroo add-ons.</p>
 *
 * <p>All failures are swallowed: if the layout of MaLiLib changes, auto-discovery of add-on
 * configs simply returns an empty map instead of breaking the overlay or the game.</p>
 */
public final class MalilibConfigAccess {
	private static final Class<?> CONFIG_MANAGER_CLASS = ReflectionUtils.findClass("fi.dy.masa.malilib.config.ConfigManager");

	private static Map<String, Object> cachedHandlers;

	private MalilibConfigAccess() {
	}

	/** @return modId to {@code IConfigHandler} instance map, never {@code null} */
	public static synchronized Map<String, Object> configHandlers() {
		if (cachedHandlers != null) {
			return cachedHandlers;
		}

		Map<String, Object> result = new LinkedHashMap<>();

		try {
			Object manager = ReflectionUtils.invokeStatic(CONFIG_MANAGER_CLASS, "getInstance");

			if (manager != null) {
				// Newer/other MaLiLib versions may have a public getter; try that first.
				readHandlersFromPublicGetter(manager, result);

				if (result.isEmpty()) {
					readHandlersFromFields(manager, result);
				}
			}
		} catch (Throwable ignored) {
		}

		cachedHandlers = result;
		return cachedHandlers;
	}

	private static void readHandlersFromPublicGetter(Object manager, Map<String, Object> result) {
		Method getter = ReflectionUtils.findMethod(manager.getClass(), "getConfigHandlers");

		if (getter == null) {
			getter = ReflectionUtils.findMethod(manager.getClass(), "getHandlers");
		}

		if (getter == null) {
			return;
		}

		Object value = ReflectionUtils.invoke(getter, manager);
		readHandlerMap(value, result);
	}

	private static void readHandlersFromFields(Object manager, Map<String, Object> result) {
		for (Class<?> clazz = manager.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
			for (Field field : clazz.getDeclaredFields()) {
				if (Modifier.isStatic(field.getModifiers()) || field.getType().isPrimitive()) {
					continue;
				}

				String name = field.getName().toLowerCase(java.util.Locale.ROOT);

				if (name.contains("handler") == false && name.contains("config") == false) {
					continue;
				}

				try {
					field.setAccessible(true);
					Object value = field.get(manager);

					if (value instanceof Map<?, ?>) {
						readHandlerMap(value, result);

						if (result.isEmpty() == false) {
							return;
						}
					}
				} catch (Throwable ignored) {
				}
			}
		}
	}

	private static void readHandlerMap(Object mapValue, Map<String, Object> result) {
		if (!(mapValue instanceof Map<?, ?> map)) {
			return;
		}

		for (Map.Entry<?, ?> entry : map.entrySet()) {
			if (entry.getKey() instanceof String modId && entry.getValue() != null) {
				result.put(modId, entry.getValue());
			}
		}
	}

	/** Forgets the cached handler map; useful for tests / reload. */
	public static synchronized void invalidate() {
		cachedHandlers = null;
	}
}
