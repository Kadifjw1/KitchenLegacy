#!/usr/bin/env python3
from __future__ import annotations

import argparse
import hashlib
import json
import shutil
import struct
import subprocess
import sys
from pathlib import Path

TASK_DIR = Path(__file__).resolve().parent
REPO_ROOT = TASK_DIR.parent.parent
UNPACKED_DIR = TASK_DIR / "unpacked"
SOURCE_ASSETS = UNPACKED_DIR / "resourcepack" / "assets" / "worldsmith"
DEFAULT_OUTPUT = REPO_ROOT / "src" / "generated" / "resources"
MAIN_ASSETS = REPO_ROOT / "src" / "main" / "resources" / "assets" / "worldsmith"
MAIN_MODELS = MAIN_ASSETS / "models" / "item"
MAIN_ITEM_TEXTURES = MAIN_ASSETS / "textures" / "item"
MAIN_PARTICLES = MAIN_ASSETS / "particles"
EXPECTED_ELEMENT_COUNTS = {0: 170, 1: 170, 2: 171, 3: 170, 4: 170, 5: 170}

PARTICLE_TEXTURES = {
    "krovotok_blood_mist": [f"worldsmith:krovotok/blood_mist_{frame}" for frame in range(6)],
    "krovotok_blood_spark": [f"worldsmith:krovotok/blood_spark_{frame}" for frame in range(4)],
    "krovotok_blood_pulse": [f"worldsmith:krovotok/blood_pulse_{frame}" for frame in range(6)],
    "krovotok_blood_burst": [f"worldsmith:krovotok/blood_burst_{frame}" for frame in range(8)],
    "krovotok_life_drain": [f"worldsmith:krovotok/life_drain_{frame}" for frame in range(6)],
}
PARTICLE_NAMES = tuple(PARTICLE_TEXTURES)


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def copy_file(source: Path, destination: Path) -> None:
    if not source.is_file():
        raise FileNotFoundError(f"Missing Krovotok source asset: {source}")
    destination.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(source, destination)


def read_json(path: Path) -> dict:
    try:
        value = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as error:
        raise ValueError(f"Invalid JSON file {path}: {error}") from error
    if not isinstance(value, dict):
        raise ValueError(f"Expected a JSON object in {path}")
    return value


def png_size(path: Path) -> tuple[int, int]:
    data = path.read_bytes()
    if len(data) < 24 or data[:8] != b"\x89PNG\r\n\x1a\n":
        raise ValueError(f"Invalid PNG signature: {path}")
    return struct.unpack(">II", data[16:24])


def normalize_item_model_uv(model_path: Path) -> None:
    """Convert Blockbench pixel UVs (0..64) to Minecraft model UV units (0..16)."""
    model = read_json(model_path)
    uv_values: list[float] = []

    for element in model.get("elements", []):
        for face in element.get("faces", {}).values():
            uv = face.get("uv")
            if isinstance(uv, list) and len(uv) == 4:
                uv_values.extend(value for value in uv if isinstance(value, (int, float)))

    if not uv_values:
        raise ValueError(f"Krovotok base model has no face UV coordinates: {model_path}")
    if max(uv_values) <= 16:
        return

    for element in model.get("elements", []):
        for face in element.get("faces", {}).values():
            uv = face.get("uv")
            if isinstance(uv, list) and len(uv) == 4:
                face["uv"] = [round(float(value) * 0.25, 6) for value in uv]

    model["texture_size"] = [64, 64]
    model_path.write_text(
        json.dumps(model, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )


def verify_committed_particle_jsons() -> None:
    missing = [name for name in PARTICLE_NAMES if not (MAIN_PARTICLES / f"{name}.json").is_file()]
    if missing:
        raise FileNotFoundError(
            "Missing committed Krovotok particle JSON files: " + ", ".join(missing)
        )

    for name, expected_textures in PARTICLE_TEXTURES.items():
        particle_path = MAIN_PARTICLES / f"{name}.json"
        actual_textures = read_json(particle_path).get("textures")
        if actual_textures != expected_textures:
            raise ValueError(
                f"{particle_path}: expected textures {expected_textures}, got {actual_textures}"
            )


def find_element(elements: list[dict], name: str, from_: list[float], to: list[float]) -> dict | None:
    return next(
        (
            element
            for element in elements
            if element.get("name") == name
            and element.get("from") == from_
            and element.get("to") == to
        ),
        None,
    )


def verify_stage_two_split(elements: list[dict], model_path: Path) -> None:
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
        raise ValueError(
            f"{model_path}: intentional split Blade_2_16 half-cubes are missing"
        )
    if left.get("rotation") != right.get("rotation"):
        raise ValueError(f"{model_path}: split half-cubes use different rotations")


def verify_committed_charge_assets() -> dict[int, str]:
    """Validate the six user-authored item models and PNGs committed under src/main/resources."""
    root_model_path = MAIN_MODELS / "krovotok.json"
    root_model = read_json(root_model_path)

    expected_models = {f"worldsmith:item/krovotok_charge_{charge}" for charge in range(6)}
    actual_models = {
        override.get("model")
        for override in root_model.get("overrides", [])
        if isinstance(override, dict)
    }
    if not expected_models.issubset(actual_models):
        missing = sorted(expected_models - actual_models)
        raise ValueError(f"{root_model_path}: missing charge overrides: {missing}")

    texture_references: dict[int, str] = {}
    for charge in range(6):
        model_path = MAIN_MODELS / f"krovotok_charge_{charge}.json"
        texture_path = MAIN_ITEM_TEXTURES / f"krovotok_charge_{charge}.png"

        if not model_path.is_file():
            raise FileNotFoundError(f"Missing user Krovotok model: {model_path}")
        if not texture_path.is_file():
            raise FileNotFoundError(f"Missing user Krovotok texture: {texture_path}")

        model = read_json(model_path)
        elements = model.get("elements")
        expected_count = EXPECTED_ELEMENT_COUNTS[charge]
        if not isinstance(elements, list) or len(elements) != expected_count:
            actual_count = len(elements) if isinstance(elements, list) else None
            raise ValueError(
                f"{model_path}: expected {expected_count} elements, got {actual_count}"
            )
        if charge == 2:
            verify_stage_two_split(elements, model_path)

        if model.get("texture_size") != [64, 64]:
            raise ValueError(f"{model_path}: expected texture_size [64, 64]")

        texture_reference = model.get("textures", {}).get("0")
        accepted = {
            f"worldsmith:krovotok_charge_{charge}",
            f"worldsmith:item/krovotok_charge_{charge}",
        }
        if texture_reference not in accepted:
            raise ValueError(
                f"{model_path}: unexpected texture reference {texture_reference!r}; "
                f"expected one of {sorted(accepted)}"
            )

        size = png_size(texture_path)
        if size != (64, 64):
            raise ValueError(f"{texture_path}: expected 64x64 PNG, got {size[0]}x{size[1]}")

        texture_references[charge] = texture_reference

    return texture_references


def clean_generated_targets(worldsmith_output: Path) -> None:
    targets = [
        worldsmith_output / "models" / "item" / "krovotok_base.json",
        worldsmith_output / "textures" / "item" / "krovotok.png",
        worldsmith_output / "textures" / "item" / "krovotok.png.mcmeta",
        worldsmith_output / "textures" / "item" / "krovotok_static.png",
        worldsmith_output / "krovotok_generated_assets.json",
    ]
    targets.extend(
        worldsmith_output / "textures" / "item" / f"krovotok_charge_{charge}.png"
        for charge in range(6)
    )
    targets.extend(
        worldsmith_output / "textures" / f"krovotok_charge_{charge}.png"
        for charge in range(6)
    )
    targets.extend(
        worldsmith_output / "particles" / f"{name}.json"
        for name in PARTICLE_NAMES
    )

    for target in targets:
        if target.is_file() or target.is_symlink():
            target.unlink()

    particle_directory = worldsmith_output / "textures" / "particle" / "krovotok"
    if particle_directory.exists():
        shutil.rmtree(particle_directory)


def materialize(output_root: Path) -> dict[str, str]:
    subprocess.run(
        [sys.executable, str(TASK_DIR / "verify_asset_archive.py"), "--extract"],
        cwd=REPO_ROOT,
        check=True,
    )
    verify_committed_particle_jsons()
    charge_texture_references = verify_committed_charge_assets()

    worldsmith_output = output_root / "assets" / "worldsmith"
    clean_generated_targets(worldsmith_output)

    mappings: list[tuple[Path, Path]] = [
        (
            SOURCE_ASSETS / "models" / "item" / "krovotok.json",
            worldsmith_output / "models" / "item" / "krovotok_base.json",
        ),
        (
            SOURCE_ASSETS / "textures" / "item" / "krovotok.png",
            worldsmith_output / "textures" / "item" / "krovotok.png",
        ),
        (
            SOURCE_ASSETS / "textures" / "item" / "krovotok.png.mcmeta",
            worldsmith_output / "textures" / "item" / "krovotok.png.mcmeta",
        ),
        (
            SOURCE_ASSETS / "textures" / "item" / "krovotok_static.png",
            worldsmith_output / "textures" / "item" / "krovotok_static.png",
        ),
    ]

    # User PNGs are authoritative. Only create a compatibility alias when an exported JSON
    # references the texture namespace root instead of textures/item.
    for charge, texture_reference in charge_texture_references.items():
        if texture_reference == f"worldsmith:krovotok_charge_{charge}":
            mappings.append(
                (
                    MAIN_ITEM_TEXTURES / f"krovotok_charge_{charge}.png",
                    worldsmith_output / "textures" / f"krovotok_charge_{charge}.png",
                )
            )

    particle_texture_source = SOURCE_ASSETS / "textures" / "particle" / "krovotok"
    for source in sorted(particle_texture_source.glob("*.png")):
        mappings.append(
            (
                source,
                worldsmith_output / "textures" / "particle" / "krovotok" / source.name,
            )
        )

    actual_particle_pngs = sum(
        1
        for source, _ in mappings
        if source.suffix == ".png" and "particle" in source.parts and "krovotok" in source.parts
    )
    if actual_particle_pngs != 30:
        raise RuntimeError(
            f"Expected 30 Krovotok particle PNG files, found {actual_particle_pngs}"
        )

    manifest: dict[str, str] = {}
    for source, destination in mappings:
        copy_file(source, destination)
        if destination.name == "krovotok_base.json":
            normalize_item_model_uv(destination)
        manifest[destination.relative_to(output_root).as_posix()] = sha256(destination)

    manifest_path = worldsmith_output / "krovotok_generated_assets.json"
    manifest_path.parent.mkdir(parents=True, exist_ok=True)
    manifest_path.write_text(
        json.dumps(
            {
                "generated": True,
                "source_archive_sha256": "3f31f68727da3a6e9d40b0e25e7cc26c6868c7317ca7fbdb516b7ea1e22bf902",
                "user_charge_assets_authoritative": True,
                "expected_element_counts": EXPECTED_ELEMENT_COUNTS,
                "stage_2_intentional_split_cube": True,
                "user_charge_models": [
                    f"assets/worldsmith/models/item/krovotok_charge_{charge}.json"
                    for charge in range(6)
                ],
                "user_charge_textures": [
                    f"assets/worldsmith/textures/item/krovotok_charge_{charge}.png"
                    for charge in range(6)
                ],
                "charge_texture_references": charge_texture_references,
                "committed_particle_jsons": [
                    f"assets/worldsmith/particles/{name}.json" for name in PARTICLE_NAMES
                ],
                "particle_textures": PARTICLE_TEXTURES,
                "files": manifest,
            },
            ensure_ascii=False,
            indent=2,
        )
        + "\n",
        encoding="utf-8",
    )

    return manifest


def main() -> int:
    parser = argparse.ArgumentParser(
        description=(
            "Validate committed user Krovotok charge assets, repair the historical text archive, "
            "and materialize only non-authoritative generated resources."
        )
    )
    parser.add_argument(
        "--output",
        type=Path,
        default=DEFAULT_OUTPUT,
        help=f"Generated resource root (default: {DEFAULT_OUTPUT})",
    )
    args = parser.parse_args()

    output = args.output.resolve()
    manifest = materialize(output)
    print(f"Krovotok generated resources: {output}")
    print(f"Materialized files: {len(manifest)}")
    print("Committed charge JSON/PNG files are authoritative; archive charge textures were not generated.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
