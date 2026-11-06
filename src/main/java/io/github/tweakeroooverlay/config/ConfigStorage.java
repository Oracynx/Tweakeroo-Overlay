package io.github.tweakeroooverlay.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Loads and saves {@link OverlayConfig} as a pretty-printed JSON file.
 *
 * <p>Loading is deliberately forgiving: a missing file, an unparsable file or a hand-edited file
 * with wrong value types must never prevent the game from starting. Corrupt files are moved aside
 * (with a timestamp) and defaults are used instead.</p>
 */
public final class ConfigStorage {
	private static final DateTimeFormatter BACKUP_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
	private static final Gson GSON = new GsonBuilder()
		.setPrettyPrinting()
		.disableHtmlEscaping()
		.create();

	private final Path file;

	public ConfigStorage(Path file) {
		this.file = file;
	}

	public Path file() {
		return this.file;
	}

	/**
	 * @param warning the non-fatal problem that was hit while loading, or {@code null} if none
	 */
	public record LoadResult(OverlayConfig config, String warning) {
	}

	public LoadResult load() {
		if (!Files.exists(this.file)) {
			return new LoadResult(new OverlayConfig().sanitize(), null);
		}

		try (Reader reader = Files.newBufferedReader(this.file, StandardCharsets.UTF_8)) {
			OverlayConfig config = GSON.fromJson(reader, OverlayConfig.class);

			if (config == null) {
				throw new JsonParseException("Config file is empty");
			}

			return new LoadResult(config.sanitize(), null);
		} catch (IOException | RuntimeException e) {
			String warning = "Failed to load config from " + this.file + ", using defaults: " + e;
			backupCorruptFile();
			return new LoadResult(new OverlayConfig().sanitize(), warning);
		}
	}

	/**
	 * Writes the config atomically (write to a temporary file, then move it into place), so a
	 * crash during saving can never leave a half-written config file behind.
	 */
	public void save(OverlayConfig config) throws IOException {
		OverlayConfig safeConfig = (config == null ? new OverlayConfig() : config).sanitize();
		Path parent = this.file.getParent();

		if (parent != null) {
			Files.createDirectories(parent);
		}

		Path temp = this.file.resolveSibling(this.file.getFileName() + ".tmp");

		try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
			GSON.toJson(safeConfig, writer);
		}

		try {
			Files.move(temp, this.file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		} catch (IOException atomicMoveFailed) {
			Files.move(temp, this.file, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private void backupCorruptFile() {
		try {
			String suffix = BACKUP_TIMESTAMP.format(LocalDateTime.now());
			Path backup = this.file.resolveSibling(this.file.getFileName() + ".corrupt-" + suffix);
			Files.move(this.file, backup, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException ignored) {
			// Nothing sensible we can do here; the defaults will be used for this session.
		}
	}
}
