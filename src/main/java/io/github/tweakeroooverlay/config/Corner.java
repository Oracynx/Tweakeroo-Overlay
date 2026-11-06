package io.github.tweakeroooverlay.config;

/**
 * The screen corner the overlay is anchored to.
 */
public enum Corner {
	TOP_LEFT,
	TOP_RIGHT,
	BOTTOM_LEFT,
	BOTTOM_RIGHT;

	public boolean isRightSide() {
		return this == TOP_RIGHT || this == BOTTOM_RIGHT;
	}

	public boolean isBottomSide() {
		return this == BOTTOM_LEFT || this == BOTTOM_RIGHT;
	}
}
