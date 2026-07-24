#!/usr/bin/env python3
"""Validate the committed user-authored Krovotok charge models and textures."""

from __future__ import annotations

import json
import struct
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "src" / "main" / "resources" / "assets" / "worldsmith"
MODELS = ASSETS / "models" / "item"
TEXTURES = ASSETS / "textures" / "item"
EXPECTED_ELEMENT_COUNTS = {0: 170, 1: 170, 2: 171, 3: 170, 4: 170, 5: 170}


def read_json(path: Path) -> dict:
    try:
        value = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as error:
        raise SystemExit(f"Invalid JSON {path}: {error}") from error
    if not isinstance(value, dict):
        raise SystemExit(f"Expected a JSON object in {path}")
    return value


def png_size(path: Path) -> tuple[int, int]:
    try:
        data = path.read_bytes()
    except OSError as error:
        raise SystemExit(f"Cannot read PNG {path}: {error}") from error
    if len(data) < 24 or data[:8] != b"\x89PNG\r\n\x1a\n":
        raise SystemExit(f"Invalid PNG signature: {path}")
    return struct.unpack(">II", data[16:24])


def face_uv_values(model: dict) -> list[float]:
    values: list[float] = []
    for element in model.get("elements", []):
        faces = element.get("faces", {})
        if not isinstance(faces, dict) or len(faces) != 6:
            raise SystemExit(
                f"Every Krovotok cube must contain six faces; invalid element: {element.get('name')}"
            )
        for face in faces.values():
            uv = face.get("uv")
            if not isinstance(uv, list) or len(uv) != 4:
                raise SystemExit(f"Invalid face UV in element {element.get('name')}")
            values.extend(float(value) for value in uv)
    return values


def find_element(elements: list[dict], name: str, from_: list[float], to: list[float]) -> dict | None:
    for element in elements:
        if (
            element.get("name") == name
            and element.get("from") == from_
            and element.get("to") == to
        ):
            return element
    return None


def verify_stage_two_split(elements: list[dict]) -> None:
    """Stage 2 intentionally splits one blade cube into two adjacent half-cubes for UV detail."""
    left = find_element(
        elements,
        "Blade_2_16",
        [6.5, 22.5, 7.3],
        [7, 23.5, 8.7],
    )
    right = find_element(
        elements,
        "Blade_2_17",
        [7, 22.5, 7.3],
        [7.5, 23.5, 8.7],
    )
    if left is None or right is None:
        raise SystemExit(
            "krovotok_charge_2.json: expected the intentional Blade_2_16 split "
            "into two adjacent half-cubes"
        )

    if left.get("rotation") != right.get("rotation"):
        raise SystemExit(
            "krovotok_charge_2.json: split Blade_2_16 halves must share one rotation"
        )

    # Together, both halves must exactly occupy the original stage-0 cube volume.
    union_from = [
        min(left["from"][axis], right["from"][axis])
        for axis in range(3)
    ]
    union_to = [
        max(left["to"][axis], right["to"][axis])
        for axis in range(3)
    ]
    if union_from != [6.5, 22.5, 7.3] or union_to != [7.5, 23.5, 8.7]:
        raise SystemExit(
            "krovotok_charge_2.json: split halves no longer reproduce the original cube volume"
        )


def main() -> int:
    root_path = MODELS / "krovotok.json"
    root_model = read_json(root_path)
    overrides = root_model.get("overrides")
    if not isinstance(overrides, list):
        raise SystemExit(f"Missing charge overrides in {root_path}")

    actual_override_models = {
        override.get("model")
        for override in overrides
        if isinstance(override, dict)
    }
    expected_override_models = {
        f"worldsmith:item/krovotok_charge_{charge}" for charge in range(6)
    }
    if not expected_override_models.issubset(actual_override_models):
        missing = sorted(expected_override_models - actual_override_models)
        raise SystemExit(f"Missing Krovotok charge overrides: {missing}")

    reference_display: dict | None = None
    texture_references: list[str] = []

    for charge in range(6):
        model_path = MODELS / f"krovotok_charge_{charge}.json"
        texture_path = TEXTURES / f"krovotok_charge_{charge}.png"

        if not model_path.is_file():
            raise SystemExit(f"Missing Krovotok model: {model_path}")
        if not texture_path.is_file():
            raise SystemExit(f"Missing Krovotok texture: {texture_path}")

        model = read_json(model_path)
        elements = model.get("elements")
        expected_count = EXPECTED_ELEMENT_COUNTS[charge]
        if not isinstance(elements, list) or len(elements) != expected_count:
            count = len(elements) if isinstance(elements, list) else None
            raise SystemExit(
                f"{model_path}: expected {expected_count} elements, got {count}"
            )
        if charge == 2:
            verify_stage_two_split(elements)

        if model.get("texture_size") != [64, 64]:
            raise SystemExit(f"{model_path}: expected texture_size [64, 64]")

        uv_values = face_uv_values(model)
        if not uv_values or min(uv_values) < 0 or max(uv_values) > 16:
            raise SystemExit(
                f"{model_path}: Minecraft item-model UVs must remain inside 0..16"
            )

        texture_reference = model.get("textures", {}).get("0")
        accepted_references = {
            f"worldsmith:krovotok_charge_{charge}",
            f"worldsmith:item/krovotok_charge_{charge}",
        }
        if texture_reference not in accepted_references:
            raise SystemExit(
                f"{model_path}: unexpected texture reference {texture_reference!r}"
            )
        texture_references.append(texture_reference)

        display = model.get("display")
        if not isinstance(display, dict) or not display:
            raise SystemExit(f"{model_path}: missing display transforms")
        if reference_display is None:
            reference_display = display
        elif display != reference_display:
            raise SystemExit(
                f"{model_path}: display transforms differ from charge stage 0"
            )

        size = png_size(texture_path)
        if size != (64, 64):
            raise SystemExit(
                f"{texture_path}: expected a 64x64 PNG, got {size[0]}x{size[1]}"
            )

    print("Krovotok user charge assets verified:")
    print("- six JSON models present")
    print("- six 64x64 PNG textures present")
    print(f"- stage element counts: {EXPECTED_ELEMENT_COUNTS}")
    print("- stage 2 split cube preserves the original blade volume")
    print("- six faces per element")
    print("- UV coordinates remain inside Minecraft 0..16 model space")
    print("- display transforms match across all six stages")
    print("- root model contains all charge overrides")
    print(f"- texture references: {texture_references}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
