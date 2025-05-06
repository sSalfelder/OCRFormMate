
import os
from pathlib import Path

INCLUDED_JAVA_SUBFOLDER = "src/main/java/com/github/ssalfelder/ocrformmate"
INCLUDED_PYTHON_SUBFOLDERS = [
    "python/ocr",
    "python/util",
    "python/handwriting_dataset/scripts"
]

EXCLUDED_FILES = {"__init__.py", "__main__.py"}

def generate_code_overview_markdown(java_root: Path, python_root: Path, output_path: Path):
    markdown_lines = []

    def add_code_section_markdown(header, file_path):
        markdown_lines.append(f"**{header}**\n")
        markdown_lines.append("```")
        with open(file_path, 'r', encoding='utf-8', errors='ignore') as file:
            markdown_lines.extend([line.rstrip() for line in file])
        markdown_lines.append("```")
        markdown_lines.append("")

    def collect_files(base_path: Path, extension: str, allowed_paths: list):
        collected = []
        for rel in allowed_paths:
            target_dir = base_path / Path(rel)
            for foldername, _, filenames in os.walk(target_dir):
                for filename in filenames:
                    if filename.endswith(extension) and filename not in EXCLUDED_FILES:
                        full_path = os.path.join(foldername, filename)
                        rel_path = os.path.relpath(full_path, base_path)
                        collected.append((rel_path, full_path))
        return collected

    java_files = collect_files(base_path=project_root, extension='.java', allowed_paths=[INCLUDED_JAVA_SUBFOLDER])
    python_files = collect_files(base_path=project_root, extension='.py', allowed_paths=INCLUDED_PYTHON_SUBFOLDERS)

    markdown_lines.append("# Java-Klassen")
    for rel_path, full_path in java_files:
        add_code_section_markdown(rel_path, full_path)

    markdown_lines.append("# Python-Skripte")
    for rel_path, full_path in python_files:
        add_code_section_markdown(rel_path, full_path)

    output_path.parent.mkdir(parents=True, exist_ok=True)
    with open(output_path, 'w', encoding='utf-8') as f:
        f.write("\n".join(markdown_lines))


if __name__ == "__main__":
    script_dir = Path(__file__).resolve().parent
    project_root = script_dir.parent.parent  # OCRFormMate/
    java_root = project_root / "src"
    python_root = project_root / "python"
    output_file = python_root / "output" / "OCRFormMate_Code_Uebersicht.md"

    generate_code_overview_markdown(java_root, python_root, output_file)
    print(f"Markdown-Datei erstellt unter: {output_file}")
