#!/usr/bin/env python3
"""Validate the committed user-authored Krovotok charge models and textures."""

from __future__ import annotations

from collections import Counter
import json
import struct
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "src" / "main" / "resources" / "assets" / "worldsmith"
MODELS = ASSETS / "models" / "item"
TEXTURES = ASSETS / "textures" / "item"


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


def element_signature(element: dict) -> tuple:
    return (
        element.get("name"),
        tuple(element.get("from", [])),
        tuple(element.get("to", [])),
        json.dumps(element.get("rotation"), sort_keys=True, separators=(",", ":")),
    )


def geometry_diagnostic(models: list[dict]) -> str:
    reference = models[0].get("elements", [])
    reference_names = Counter(element.get("name") for element in reference)
    reference_signatures = Counter(element_signature(element) for element in reference)
    lines: list[str] = []

    for charge, model in enumerate(models):
        elements = model.get("elements", [])
        names = Counter(element.get("name") for element in elements)
        signatures = Counter(element_signature(element) for element in elements)

        extra_names = list((names - reference_names).elements())
        missing_names = list((reference_names - names).elements())
        duplicate_names = sorted(name for name, count in names.items() if name and count > 1)
        extra_signatures = list((signatures - reference_signatures).elements())
        missing_signatures = list((reference_signatures - signatures).elements())

        lines.append(f"stage {charge}: elements={len(elements)}")
        if extra_names:
            lines.append(f"  extra names vs stage 0: {extra_names}")
        if missing_names:
            lines.append(f"  missing names vs stage 0: {missing_names}")
        if duplicate_names:
            lines.append(f"  duplicate names: {duplicate_names}")
        if extra_signatures:
            lines.append("  extra geometry signatures:")
            for signature in extra_signatures[:10]:
                lines.append(f"    {signature}")
        if missing_signatures:
            lines.append("  missing geometry signatures:")
            for signature in missing_signatures[:10]:
                lines.append(f"    {signature}")

    return "\n".join(lines)


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

    models: list[dict] = []
    for charge in range(6):
        model_path = MODELS / f"krovotok_charge_{charge}.json"
        if not model_path.is_file():
            raise SystemExit(f"Missing Krovotok model: {model_path}")
        models.append(read_json(model_path))

    invalid_counts = [
        (charge, len(model.get("elements", [])))
        for charge, model in enumerate(models)
        if not isinstance(model.get("elements"), list) or len(model.get("elements", [])) != 170
    ]
    if invalid_counts:
        details = geometry_diagnostic(models)
        raise SystemExit(
            "Krovotok stage element-count mismatch: "
            + ", ".join(f"stage {charge}={count}" for charge, count in invalid_counts)
            + "\n"
            + details
        )

    reference_display: dict | None = None
    texture_references: list[str] = []

    for charge, model in enumerate(models):
        model_path = MODELS / f"krovotok_charge_{charge}.json"
        texture_path = TEXTURES / f"krovotok_charge_{charge}.png"

        if not texture_path.is_file():
            raise SystemExit(f"Missing Krovotok texture: {texture_path}")

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
    print("- 170 elements and six faces per element")
    print("- UV coordinates remain inside Minecraft 0..16 model space")
    print("- display transforms match across all six stages")
    print("- root model contains all charge overrides")
    print(f"- texture references: {texture_references}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
