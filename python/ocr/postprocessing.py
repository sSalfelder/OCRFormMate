import json
import os
from functools import lru_cache
from rapidfuzz import process, fuzz

DICTIONARY_DIR = "../dictionaries"

@lru_cache(maxsize=32)
def load_dictionary(form_type: str) -> dict:
    path = os.path.join(DICTIONARY_DIR, f"{form_type}.json")
    try:
        with open(path, "r", encoding="utf-8") as f:
            return json.load(f)
    except FileNotFoundError:
        return {}

def fuzzy_correct(value, choices):
    result = process.extractOne(value, choices, scorer=fuzz.ratio)
    if result and len(result) >= 2:
        match, score = result[0], result[1]
        return match if score > 80 else value
    return value


def postprocess_all(results: dict, form_type: str = "default") -> dict:
    dictionary = load_dictionary(form_type)
    corrected = {}

    for field_name, value in results.items():
        values = dictionary.get(field_name)
        if isinstance(value, str) and isinstance(values, list):
            corrected[field_name] = fuzzy_correct(value, values)
        else:
            corrected[field_name] = value

    return corrected
