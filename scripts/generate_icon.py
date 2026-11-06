#!/usr/bin/env python3
"""Generates the mod icon (128x128 PNG).

Run from the project root:
    python scripts/generate_icon.py

Requires Pillow. The icon is a dark rounded square with a right-aligned list of green
"feature lines", matching what the mod renders in the top-right corner of the HUD.
"""

from PIL import Image, ImageDraw

SIZE = 128
BACKGROUND = (20, 20, 28, 255)
BORDER = (85, 255, 85, 160)
GREEN = (85, 255, 85, 255)
GREEN_DIM = (85, 255, 85, 190)


def rounded_rect(draw, box, radius, fill):
    draw.rounded_rectangle(box, radius=radius, fill=fill)


def main():
    image = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)

    rounded_rect(draw, (4, 4, SIZE - 5, SIZE - 5), 20, BACKGROUND)
    rounded_rect(draw, (8, 8, SIZE - 9, SIZE - 9), 16, (0, 0, 0, 0) if False else BACKGROUND)
    draw.rounded_rectangle((4, 4, SIZE - 5, SIZE - 5), radius=20, outline=BORDER, width=2)

    # Right-aligned "feature lines", one per row.
    right = SIZE - 22
    widths = (76, 58, 66, 44)
    y = 34
    for index, width in enumerate(widths):
        color = GREEN if index % 2 == 0 else GREEN_DIM
        draw.rounded_rectangle((right - width, y, right, y + 9), radius=4, fill=color)
        y += 17

    # A subtle check mark in the lower left corner.
    draw.line((18, SIZE - 26, 26, SIZE - 18), fill=GREEN, width=4)
    draw.line((26, SIZE - 18, 38, SIZE - 36), fill=GREEN, width=4)

    image.save("src/main/resources/assets/tweakeroo_overlay/icon.png")
    print("Wrote src/main/resources/assets/tweakeroo_overlay/icon.png")


if __name__ == "__main__":
    main()
