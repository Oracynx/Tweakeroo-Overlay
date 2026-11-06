package io.github.tweakeroooverlay.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

/**
 * The mod's own key binding (registered through the Fabric key mapping API).
 */
public final class KeyBindings {
	public static final KeyMapping.Category CATEGORY =
		KeyMapping.Category.register(Identifier.fromNamespaceAndPath("tweakeroo_overlay", "main"));

	private KeyBindings() {
	}

	/** @return the key binding that toggles the whole overlay, defaulting to F6 */
	public static KeyMapping createToggleKey() {
		return new KeyMapping("key.tweakeroo_overlay.toggle",
			InputConstants.Type.KEYBOARD, InputConstants.KEY_F6, CATEGORY);
	}
}
