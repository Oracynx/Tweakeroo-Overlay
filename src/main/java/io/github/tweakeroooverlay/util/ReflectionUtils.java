package io.github.tweakeroooverlay.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Small reflection helpers shared by the compatibility layer.
 *
 * <p>Everything Tweakeroo / MaLiLib specific is accessed reflectively on purpose: the public API
 * of those mods changes between versions, while the class/field/method names used here have been
 * stable for many releases. Reflection also means a missing class or method only disables the
 * affected provider instead of crashing the game.</p>
 */
public final class ReflectionUtils {
	private ReflectionUtils() {
	}

	/** @return the class, or {@code null} if it cannot be loaded (also handles broken static init). */
	public static Class<?> findClass(String className) {
		try {
			return Class.forName(className, false, ReflectionUtils.class.getClassLoader());
		} catch (Throwable ignored) {
			return null;
		}
	}

	/** @return the value of a public static field of the given class, or {@code null}. */
	public static Object getStaticField(Class<?> clazz, String fieldName) {
		if (clazz == null) {
			return null;
		}

		try {
			Field field = clazz.getField(fieldName);

			if (Modifier.isStatic(field.getModifiers())) {
				return field.get(null);
			}
		} catch (Throwable ignored) {
		}

		return null;
	}

	/** @return the value of any (also private) static field, or {@code null}. */
	public static Object getAnyStaticField(Class<?> clazz, String fieldName) {
		if (clazz == null) {
			return null;
		}

		try {
			Field field = clazz.getDeclaredField(fieldName);
			field.setAccessible(true);

			if (Modifier.isStatic(field.getModifiers())) {
				return field.get(null);
			}
		} catch (Throwable ignored) {
		}

		return null;
	}

	/**
	 * Invokes a static method without arguments.
	 *
	 * @return the result, or {@code null} if the method does not exist / threw
	 */
	public static Object invokeStatic(Class<?> clazz, String methodName) {
		if (clazz == null) {
			return null;
		}

		try {
			Method method = clazz.getMethod(methodName);
			return method.invoke(null);
		} catch (Throwable ignored) {
			return null;
		}
	}

	/** Finds a public no-argument method, but never crashes on a broken/missing method. */
	public static Method findMethod(Class<?> clazz, String methodName) {
		if (clazz == null) {
			return null;
		}

		try {
			Method method = clazz.getMethod(methodName);

			if (method.getParameterCount() == 0) {
				return method;
			}
		} catch (Throwable ignored) {
		}

		return null;
	}

	/** Invokes a cached no-argument method, returning {@code null} on any problem. */
	public static Object invoke(Method method, Object target) {
		if (method == null || target == null) {
			return null;
		}

		try {
			return method.invoke(target);
		} catch (Throwable ignored) {
			return null;
		}
	}

	/**
	 * Removes Minecraft formatting codes like {@code "\u00a7c"} from a string.
	 */
	public static String stripFormatting(String text) {
		if (text == null || text.isEmpty()) {
			return text;
		}

		return text.replaceAll("\u00a7.", "");
	}

	/**
	 * Splits a camelCase / PascalCase identifier into words, e.g. {@code "tweakFastBlockPlacement"}
	 * becomes {@code "tweak Fast Block Placement"}. Used as the last-resort display name fallback.
	 */
	public static String splitCamelCase(String text) {
		if (text == null || text.isEmpty()) {
			return text;
		}

		StringBuilder builder = new StringBuilder(text.length() + 8);

		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);

			if (i > 0 && Character.isUpperCase(c) && !Character.isUpperCase(text.charAt(i - 1))) {
				builder.append(' ');
			}

			builder.append(c);
		}

		return builder.toString();
	}

	/**
	 * @return true if the object is an {@link Iterable} or an array, in which case the caller can
	 *         iterate over it without knowing its exact type
	 */
	public static boolean isCollectionLike(Object value) {
		return value instanceof Iterable<?> || (value != null && value.getClass().isArray());
	}

	/** Converts an {@link Iterable} or an array to a list of elements. */
	public static List<Object> asList(Object value) {
		List<Object> result = new ArrayList<>();

		if (value instanceof Iterable<?> iterable) {
			for (Object element : iterable) {
				result.add(element);
			}
		} else if (value != null && value.getClass().isArray()) {
			int length = java.lang.reflect.Array.getLength(value);

			for (int i = 0; i < length; i++) {
				result.add(java.lang.reflect.Array.get(value, i));
			}
		}

		return result;
	}

	/** @return the lower-case class name, e.g. {@code "configbooleanhotkeyed"} */
	public static String simpleClassName(Object value) {
		return value == null ? "" : value.getClass().getSimpleName().toLowerCase(Locale.ROOT);
	}
}
