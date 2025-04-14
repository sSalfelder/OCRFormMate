import os
import random
import numpy as np
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont, ImageOps, ImageEnhance, ImageFilter
import json
from tqdm import tqdm

CHARACTERS = ["@", "-", "&", "€", "/", ".", ":", ",", "(", ")", "+", "*"]
IMAGES_PER_CHAR_AND_FONT = 500
FIELD_NAME = "Zeichen"
FIELD_SIZE = (128, 128)  # Größe der Leinwand, auf die das Zeichen initial gezeichnet wird
FINAL_SIZE = (384, 384)  # Endgültige Bildgröße nach Padding/Skalierung (wie vom TrOCR Encoder erwartet)

BASE_DIR = Path(__file__).resolve().parent.parent
BASE_OUTPUT_DIR = Path("D:/dataset_augmented_chars")
OUTPUT_DIR = BASE_OUTPUT_DIR / "images"
JSONL_PATH = BASE_OUTPUT_DIR / "dataset.jsonl"
FONT_DIR = BASE_DIR.parent / "fonts"

# Augmentierungsschritte
# Maximale Rotation in Grad
ROTATION_RANGES = {
    "@": (-8, 8),
    "-": (-4, 4),
    "&": (-10, 10),
    "€": (-8, 8),
    "/": (-8, 8),
    ".": (-3, 3),
    ":": (-4, 4),
    ",": (-5, 5),
    "(": (-7, 7),
    ")": (-7, 7),
    "+": (-5, 5),
    "*": (-6, 6),
    "default": (-7, 7)
}

# Verschiebung im Feld
PLACEMENT_JITTER_X = 5  # (max. horizontale Verschiebung in Pixeln)
PLACEMENT_JITTER_Y = 3  # (max. vertikale Verschiebung in Pixeln)

# Linien/Boxen
ADD_LINES_PROB = 0.6  # Wahrscheinlichkeit, eine Grundlinie hinzuzufügen
ADD_BOX_PROB = 0.1  # Wahrscheinlichkeit, eine Box-Ecke hinzuzufügen
LINE_COLOR_RANGE = (190, 235)  # Grauwertbereich für Linien/Boxen
LINE_WIDTH_RANGE = (1, 2)  # Stärke

# Helligkeit & Kontrast
BRIGHTNESS_RANGE = (0.7, 1.3)
CONTRAST_RANGE = (0.8, 1.2)
CONTRAST_PROB = 0.5

# Weichzeichnung (Blur)
GAUSSIAN_BLUR_PROB = 0.2
GAUSSIAN_BLUR_RADIUS_RANGE = (0.5, 1.2)

# Rauschen (Noise)
GAUSSIAN_NOISE_PROB = 0.15
GAUSSIAN_NOISE_STD_RANGE = (3, 15)

# Scherung (Shear)
ADD_SHEAR_PROB = 0.15
MAX_SHEAR_FACTOR = 0.08

os.makedirs(OUTPUT_DIR, exist_ok=True)
os.makedirs(JSONL_PATH.parent, exist_ok=True)



# Padding/Skalierung
def prepare_image(image: Image.Image, size=FINAL_SIZE) -> Image.Image:
    """
    Skaliert das Bild proportional und fügt Ränder hinzu, um die Zielgröße
    zu erreichen. Zentriert das Bild.
    """
    return ImageOps.pad(image, size, color="white", centering=(0.5, 0.5), method=Image.BICUBIC)


# Rauschen hinzufügen
def add_gaussian_noise(image: Image.Image, std_range=GAUSSIAN_NOISE_STD_RANGE) -> Image.Image:
    """ Fügt Gauss'sches Rauschen zum Bild hinzu. """
    if image.mode != 'RGB':
        image = image.convert('RGB')  # Noise braucht typischerweise 3 Kanäle
    np_img = np.array(image).astype(np.float32)
    std = random.uniform(*std_range)
    noise = np.random.normal(0, std, np_img.shape)
    noisy_img = np.clip(np_img + noise, 0, 255).astype(np.uint8)
    return Image.fromarray(noisy_img)


# Scherung
def apply_shear(image: Image.Image, max_shear_factor=MAX_SHEAR_FACTOR) -> Image.Image:
    """ Wendet eine leichte Scherung auf das Bild an. """
    shear_x = random.uniform(-max_shear_factor, max_shear_factor)
    # shear_y = random.uniform(-max_shear_factor, max_shear_factor) # Optionale Y-Scherung
    shear_y = 0
    matrix = (1, shear_x, 0, shear_y, 1, 0)
    return image.transform(image.size, Image.AFFINE, matrix, resample=Image.BICUBIC, fillcolor="white")


def safe_char_name(c):
    """ Erzeugt sichere Dateinamen für Zeichen. """
    return {
        "@": "at", "-": "minus", "&": "and", "€": "euro", "/": "slash",
        ".": "point", ":": "colon", ",": "comma", "(": "left_bracket",
        ")": "right_bracket", "+": "plus", "*": "asterisk"
    }.get(c, f"char_{ord(c)}")


# Fonts
fonts = list(FONT_DIR.glob("*.ttf"))
if not fonts:
    raise RuntimeError(f"Keine Fonts im Ordner gefunden: {FONT_DIR}")
print(f"{len(fonts)} Fonts gefunden.")


# Augmentierungs-Pipeline
def realistic_augment(image: Image.Image, char: str) -> Image.Image:
    """
    Wendet eine Kette von Augmentierungen an, um realistischere
    Handschriftvariationen und Scan-Artefakte zu simulieren.
    """
    augmented_image = image.copy()  # Arbeite auf einer Kopie

    # Rotation
    rotation_range = ROTATION_RANGES.get(char, ROTATION_RANGES["default"])
    angle = random.uniform(*rotation_range)
    if angle != 0:
        augmented_image = augmented_image.rotate(angle, resample=Image.BICUBIC, fillcolor="white", expand=False)

    # Scherung
    if char not in ["-", ".", ",", ":"] and random.random() < ADD_SHEAR_PROB:
        augmented_image = apply_shear(augmented_image)

    # Helligkeit & Kontrast
    augmented_image = ImageEnhance.Brightness(augmented_image).enhance(random.uniform(*BRIGHTNESS_RANGE))
    if random.random() < CONTRAST_PROB:
        # print("Applying Contrast")
        augmented_image = ImageEnhance.Contrast(augmented_image).enhance(random.uniform(*CONTRAST_RANGE))

    # Weichzeichnung (Blur)
    if random.random() < GAUSSIAN_BLUR_PROB:
        radius = random.uniform(*GAUSSIAN_BLUR_RADIUS_RANGE)
        augmented_image = augmented_image.filter(ImageFilter.GaussianBlur(radius=radius))

    # Rauschen (Noise)
    if random.random() < GAUSSIAN_NOISE_PROB:
        augmented_image = add_gaussian_noise(augmented_image)

    return augmented_image


# Generierungsschleife
counter = 0

print("Starte Bildgenerierung...")
with open(JSONL_PATH, "w", encoding="utf-8") as jsonl_file:
    for font_path in tqdm(fonts, desc="Fonts"):
        try:
            # Lade Font
            font_test_size = 50
            font = ImageFont.truetype(str(font_path), font_test_size)
        except OSError as e:
            print(f"WARNUNG: Konnte Font nicht laden: {font_path.name} - {e}. Überspringe.")
            continue

        for char in tqdm(CHARACTERS, desc="Characters", leave=False):
            for i in range(IMAGES_PER_CHAR_AND_FONT):
                try:
                    font_size = random.randint(44, 54)  # Leichte Variation der Schriftgröße
                    font = ImageFont.truetype(str(font_path), font_size)  # Font mit aktueller Größe laden

                    image = Image.new("RGB", FIELD_SIZE, color="white")
                    draw = ImageDraw.Draw(image)

                    try:
                        bbox = draw.textbbox((0, 0), char, font=font, anchor="lt")
                    except ValueError:
                        try:
                            tw = draw.textlength(char, font=font)
                            th = font_size  # Annäherung
                            bbox = (0, 0, tw, th)
                        except Exception:
                            print(
                                f"WARNUNG: Konnte BBox für '{char}' mit Font {font_path.name} nicht ermitteln. Überspringe.")
                            continue

                    text_width = bbox[2] - bbox[0]
                    text_height = bbox[3] - bbox[1]

                    # Position berechnen (Zentriert + Jitter)
                    base_x = (FIELD_SIZE[0] - text_width) // 2
                    base_y = (FIELD_SIZE[1] - text_height) // 2

                    # Platzierungs-Jitter hinzufügen
                    jitter_x = random.randint(-PLACEMENT_JITTER_X, PLACEMENT_JITTER_X)
                    jitter_y = random.randint(-PLACEMENT_JITTER_Y, PLACEMENT_JITTER_Y)

                    # Endgültige Zeichenposition
                    draw_x = base_x + jitter_x
                    draw_y = base_y + jitter_y

                    draw_x = max(0, min(FIELD_SIZE[0] - text_width, draw_x))
                    draw_y = max(0, min(FIELD_SIZE[1] - text_height, draw_y))

                    # Zeichne das Zeichen
                    draw.text((draw_x, draw_y - bbox[1]), char, font=font, fill="black",
                              anchor="lt")  # anchor='lt' + Offset für präzise Platzierung

                    # Kern-Augmentierungen anwenden ---
                    augmented_image = realistic_augment(image, char)

                    draw_bg = ImageDraw.Draw(augmented_image)
                    img_w, img_h = augmented_image.size  # Größe nach Augmentierung (sollte 128x128 sein bei expand=False)

                    # Grundlinie
                    if random.random() < ADD_LINES_PROB:

                        line_y_rel = bbox[3] * 0.9
                        abs_line_y = draw_y + line_y_rel
                        abs_line_y += random.uniform(-1, 1)

                        # Auf augmentiertes Bild übertragen
                        line_y_final = min(img_h - 2, max(0, int(abs_line_y)))

                        line_x_start = max(0, draw_x - 5)  # Etwas links vom Zeichen beginnen
                        line_x_end = min(img_w - 1, draw_x + text_width + 5)  # Etwas rechts vom Zeichen enden
                        line_color_val = random.randint(*LINE_COLOR_RANGE)
                        line_color = (line_color_val,) * 3
                        line_width = random.randint(*LINE_WIDTH_RANGE)
                        if line_x_start < line_x_end:  # Gültigkeitsprüfung
                            draw_bg.line([(line_x_start, line_y_final), (line_x_end, line_y_final)],
                                         fill=line_color, width=line_width)

                        if random.random() < ADD_BOX_PROB:
                            box_margin = 3
                            box_color_val = random.randint(*LINE_COLOR_RANGE)
                            box_color = (box_color_val,) * 3
                            box_width = random.randint(1, 2)
                            corner_len = 5
                            # Oben-Links Ecke
                            x0, y0 = max(0, draw_x - box_margin), max(0, draw_y - bbox[1] - box_margin)
                            draw_bg.line([(x0, y0), (x0 + corner_len, y0)], fill=box_color, width=box_width)
                            draw_bg.line([(x0, y0), (x0, y0 + corner_len)], fill=box_color, width=box_width)
                            # Unten-Rechts Ecke
                            x1, y1 = min(img_w - 1, draw_x + text_width + box_margin), min(img_h - 1,
                                                                                           draw_y + text_height - bbox[
                                                                                               1] + box_margin)
                            draw_bg.line([(x1, y1), (x1 - corner_len, y1)], fill=box_color, width=box_width)
                            draw_bg.line([(x1, y1), (x1, y1 - corner_len)], fill=box_color, width=box_width)

                    # Endgültiges Padding/Skalierung
                    final_image = prepare_image(augmented_image, size=FINAL_SIZE)

                    # Speicherung
                    image_filename = f"{FIELD_NAME}_{counter:06}.png"
                    char_folder_name = safe_char_name(char)
                    char_dir = OUTPUT_DIR / char_folder_name
                    os.makedirs(char_dir, exist_ok=True)
                    image_path = char_dir / image_filename

                    final_image.save(image_path)

                    jsonl_file.write(json.dumps({
                        "image": f"images/{char_folder_name}/{image_filename}",
                        "text": f"FELD:{FIELD_NAME} TEXT:{char}"  # Format beibehalten
                    }, ensure_ascii=False) + "\n")

                    counter += 1

                except Exception as e:
                    print(f"\nFEHLER bei der Verarbeitung von '{char}' mit Font {font_path.name}, Index {i}: {e}")
                    import traceback

                    traceback.print_exc()
                    continue

print(f"\n{counter} Bilder generiert und gespeichert in '{OUTPUT_DIR}'")
print(f"JSONL-Datei gespeichert unter: {JSONL_PATH}")