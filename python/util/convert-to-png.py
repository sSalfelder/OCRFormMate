from pdf2image import convert_from_path

POPPLER_PATH = r"C:\poppler-24.08.0\Library\bin"

pages = convert_from_path(
    "../Hauptantrag_Buergergeld.pdf",
    dpi=300,
    poppler_path=POPPLER_PATH
)

pages[0].save("buergergeld_formular_blanko.png", "PNG")
