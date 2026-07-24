#!/usr/bin/env python3
from __future__ import annotations

import json
from pathlib import Path

TASK_DIR = Path(__file__).resolve().parent
REPO_ROOT = TASK_DIR.parent.parent
GAME_MODEL = REPO_ROOT / "src" / "generated" / "resources" / "assets" / "worldsmith" / "models" / "item" / "krovotok_base.json"
MASTER_BBMODEL = REPO_ROOT / "build" / "generated" / "krovotok" / "krovotok_stages_master.bbmodel"
MANIFEST = REPO_ROOT / "src" / "generated" / "resources" / "assets" / "worldsmith" / "krovotok_generated_assets.json"

PROFILE = "koloss_approved_display_v1"
Y_OFFSET = 4.0
SOURCE_MIN_Y = -11.5
CANONICAL_MIN_Y = -7.5
CANONICAL_MAX_Y = 31.5
EPSILON = 1.0e-6

CANONICAL_DISPLAY = {
    "thirdperson_righthand": {
        "rotation": [14, -88, 21],
        "translation": [0, 3, 0.5],
        "scale": [0.65, 0.65, 0.65],
    },
    "thirdperson_lefthand": {
        "rotation": [0, 90, -55],
        "translation": [0, 3.5, 1],
        "scale": [0.42, 0.42, 0.42],
    },
    "firstperson_righthand": {
        "rotation": [0, -90, 25],
        "translation": [1.1, 3.4, 1.1],
        "scale": [0.65, 0.65, 0.65],
    },
    "firstperson_lefthand": {
        "rotation": [0, 90, -25],
        "translation": [1.1, 3.4, 1.1],
        "scale": [0.52, 0.52, 0.52],
    },
    "ground": {
        "rotation": [94, 42, -180],
        "translation": [0, 1, 0],
        "scale": [0.28, 0.28, 0.28],
    },
    "gui": {
        "rotation": [16, -180, 41],
        "translation": [-0.75, -0.75, -1.5],
        "scale": [0.48, 0.48, 0.48],
    },
    "head": {
        "rotation": [0, -180, 0],
        "translation": [0, 10, 0],
        "scale": [0.6, 0.6, 0.6],
    },
    "fixed": {
        "rotation": [0, -180, -36],
        "translation": [0.25, -1.5, 0],
        "scale": [0.55, 0.55, 0.55],
    },
    "on_shelf": {
        "scale": [0.75, 0.75, 0.75],
    },
}


def load_json(path: Path) -> dict:
    if not path.is_file():
        raise FileNotFoundError(f"Missing generated Krovotok file: {path}")
    return json.loads(path.read_text(encoding="utf-8"))


def write_json(path: Path, data: dict, *, compact: bool = False) -> None:
    if compact:
        payload = json.dumps(data, ensure_ascii=False, separators=(",", ":"))
    else:
        payload = json.dumps(data, ensure_ascii=False, indent=2) + "\n"
    path.write_text(payload, encoding="utf-8")


def shift_vector_y(value: object, offset: float) -> None:
    if isinstance(value, list) and len(value) >= 2 and isinstance(value[1], (int, float)):
        value[1] = round(float(value[1]) + offset, 6)


def min_y(model: dict) -> float:
    elements = model.get("elements", [])
    if not elements:
        raise ValueError("Krovotok model contains no elements")
    return min(float(element["from"][1]) for element in elements)


def max_y(model: dict) -> float:
    return max(float(element["to"][1]) for element in model.get("elements", []))


def needs_shift(model: dict) -> bool:
    current = min_y(model)
    if abs(current - SOURCE_MIN_Y) <= EPSILON:
        return True
    if abs(current - CANONICAL_MIN_Y) <= EPSILON:
        return False
    raise ValueError(
        f"Unexpected Krovotok Y bounds: min={current}; expected {SOURCE_MIN_Y} or {CANONICAL_MIN_Y}"
    )


def shift_elements(model: dict) -> None:
    for element in model.get("elements", []):
        shift_vector_y(element.get("from"), Y_OFFSET)
        shift_vector_y(element.get("to"), Y_OFFSET)
        shift_vector_y(element.get("origin"), Y_OFFSET)
        rotation = element.get("rotation")
        if isinstance(rotation, dict):
            shift_vector_y(rotation.get("origin"), Y_OFFSET)


def shift_outliner(nodes: object) -> None:
    if not isinstance(nodes, list):
        return
    for node in nodes:
        if isinstance(node, dict):
            shift_vector_y(node.get("origin"), Y_OFFSET)
            shift_outliner(node.get("children"))


def apply_profile(model: dict, *, include_outliner: bool) -> None:
    if len(model.get("elements", [])) != 170:
        raise ValueError("Krovotok must contain exactly 170 elements")
    if needs_shift(model):
        shift_elements(model)
        if include_outliner:
            shift_outliner(model.get("outliner"))
    model["display"] = json.loads(json.dumps(CANONICAL_DISPLAY))
    model["worldsmith_position_profile"] = PROFILE

    if abs(min_y(model) - CANONICAL_MIN_Y) > EPSILON or abs(max_y(model) - CANONICAL_MAX_Y) > EPSILON:
        raise ValueError(
            f"Incorrect canonical Krovotok bounds: minY={min_y(model)}, maxY={max_y(model)}"
        )


def main() -> int:
    game_model = load_json(GAME_MODEL)
    apply_profile(game_model, include_outliner=False)
    write_json(GAME_MODEL, game_model)

    master = load_json(MASTER_BBMODEL)
    apply_profile(master, include_outliner=True)
    write_json(MASTER_BBMODEL, master, compact=True)

    if MANIFEST.is_file():
        manifest = load_json(MANIFEST)
        manifest["position_profile"] = PROFILE
        manifest["model_y_offset"] = Y_OFFSET
        manifest["canonical_bounds_y"] = [CANONICAL_MIN_Y, CANONICAL_MAX_Y]
        manifest["display"] = CANONICAL_DISPLAY
        write_json(MANIFEST, manifest)

    print(f"Krovotok position profile applied: {PROFILE}")
    print(f"Canonical Y bounds: {CANONICAL_MIN_Y}..{CANONICAL_MAX_Y}")
    print("Right-hand scale: third person 0.65, first person 0.65")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
