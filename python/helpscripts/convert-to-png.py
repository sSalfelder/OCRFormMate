from pdf2image import convert_from_path

# Optional: Poppler Pfad unter Windows
POPPLER_PATH = r"C:\poppler-24.08.0\Library\bin"

# Konvertierung
pages = convert_from_path(
    "../Hauptantrag_Buergergeld.pdf",
    dpi=300,
    poppler_path=POPPLER_PATH  # nur auf Windows nötig
)

# Speichern der ersten Seite
pages[0].save("buergergeld_formular_blanko.png", "PNG")
