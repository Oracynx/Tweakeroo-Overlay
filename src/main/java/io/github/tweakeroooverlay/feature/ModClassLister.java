package io.github.tweakeroooverlay.feature;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Enumerates the class names of a mod that could contain config objects.
 *
 * <p>Tweakeroo add-ons are usually structured like Tweakeroo itself: one or more "config holder"
 * classes (with names like {@code *Configs}, {@code *Config}, {@code *Options}, ...) that have
 * static fields with the config objects. Only those classes are loaded and inspected, which keeps
 * the scan fast and avoids initializing random unrelated classes of the add-on.</p>
 */
public final class ModClassLister {
	/** Upper bound for the number of classes that are loaded and scanned per mod. */
	private static final int MAX_CANDIDATES = 100;

	private ModClassLister() {
	}

	public static List<String> listCandidateConfigClasses(String modId) {
		Set<String> classNames = new TreeSet<>();

		try {
			var container = FabricLoader.getInstance().getModContainer(modId);

			if (container.isEmpty()) {
				return List.of();
			}

			for (Path root : container.get().getRootPaths()) {
				collectFromRoot(root, classNames);
			}
		} catch (Throwable ignored) {
			return List.of();
		}

		List<String> result = new ArrayList<>();

		for (String className : classNames) {
			if (looksLikeConfigHolder(className)) {
				result.add(className);

				if (result.size() >= MAX_CANDIDATES) {
					break;
				}
			}
		}

		return result;
	}

	private static void collectFromRoot(Path root, Set<String> classNames) {
		try {
			if (Files.isDirectory(root)) {
				try (Stream<Path> stream = Files.walk(root, 8)) {
					stream.filter(path -> path.toString().endsWith(".class")).forEach(path -> {
						String relative = root.relativize(path).toString().replace('\\', '/');
						addClassName(relative, classNames);
					});
				}
			} else if (Files.isRegularFile(root) && root.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar")) {
				try (ZipFile zip = new ZipFile(root.toFile())) {
					Enumeration<? extends ZipEntry> entries = zip.entries();

					while (entries.hasMoreElements()) {
						ZipEntry entry = entries.nextElement();

						if (entry.isDirectory() == false) {
							addClassName(entry.getName(), classNames);
						}
					}
				}
			}
		} catch (Throwable ignored) {
		}
	}

	private static void addClassName(String path, Set<String> classNames) {
		if (path.startsWith("META-INF/") || path.endsWith(".class") == false || path.contains("module-info")) {
			return;
		}

		String className = path.substring(0, path.length() - ".class".length()).replace('/', '.');

		// Nested classes are inspected via their outer class.
		if (className.contains("$") == false) {
			classNames.add(className);
		}
	}

	/**
	 * @return true if the class name looks like a top-level config holder class
	 */
	static boolean looksLikeConfigHolder(String className) {
		int lastDot = className.lastIndexOf('.');

		if (lastDot <= 0) {
			return false;
		}

		String packageName = className.substring(0, lastDot);
		String simpleName = className.substring(lastDot + 1);

		if (simpleName.startsWith("package-info") || simpleName.equals("module-info")) {
			return false;
		}

		boolean configPackage = packageName.contains(".config") || packageName.endsWith(".config");
		boolean configName = simpleName.matches(".*(Config|Configs|Options|Toggles|Tweaks|Fixes|Disable|Generic|Hotkeys)$");

		return configPackage && configName;
	}
}
