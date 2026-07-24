#!/usr/bin/env python3
from __future__ import annotations

import argparse
import hashlib
import json
import shutil
import subprocess
import sys
from pathlib import Path

TASK_DIR = Path(__file__).resolve().parent
REPO_ROOT = TASK_DIR.parent.parent
UNPACKED_DIR = TASK_DIR / "unpacked"
SOURCE_ASSETS = UNPACKED_DIR / "resourcepack" / "assets" / "worldsmith"
DEFAULT_OUTPUT = REPO_ROOT / "src" / "generated" / "resources"
MAIN_PARTICLES = REPO_ROOT / "src" / "main" / "resources" / "assets" / "worldsmith" / "particles"
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


def normalize_item_model_uv(model_path: Path) -> None:
    """Convert Blockbench pixel UVs (0..64) to Minecraft model UV units (0..16)."""
    try:
        model = json.loads(model_path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as error:
        raise ValueError(f"Invalid Krovotok item model {model_path}: {error}") from error

    uv_values: list[float] = []
    for element in model.get("elements", []):
        for face in element.get("faces", {}).values():
            uv = face.get("uv")
            if isinstance(uv, list) and len(uv) == 4:
                uv_values.extend(value for value in uv if isinstance(value, (int, float)))

    if not uv_values:
        raise ValueError(f"Krovotok base model has no face UV coordinates: {model_path}")

    max_uv = max(uv_values)
    if max_uv <= 16:
        return

    scale = 16.0 / 64.0
    for element in model.get("elements", []):
        for face in element.get("faces", {}).values():
            uv = face.get("uv")
            if isinstance(uv, list) and len(uv) == 4:
                face["uv"] = [round(float(value) * scale, 6) for value in uv]

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
        try:
            particle_data = json.loads(particle_path.read_text(encoding="utf-8"))
        except (OSError, json.JSONDecodeError) as error:
            raise ValueError(f"Invalid Krovotok particle JSON {particle_path}: {error}") from error

        actual_textures = particle_data.get("textures")
        if actual_textures != expected_textures:
            raise ValueError(
                f"{particle_path}: texture list does not match generated frames; "
                f"expected {expected_textures}, got {actual_textures}"
            )


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

    for charge in range(6):
        name = f"krovotok_charge_{charge}.png"
        mappings.append(
            (
                SOURCE_ASSETS / "textures" / "item" / name,
                worldsmith_output / "textures" / "item" / name,
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
        relative = destination.relative_to(output_root).as_posix()
        manifest[relative] = sha256(destination)

    manifest_path = worldsmith_output / "krovotok_generated_assets.json"
    manifest_path.parent.mkdir(parents=True, exist_ok=True)
    manifest_path.write_text(
        json.dumps(
            {
                "generated": True,
                "source_archive_sha256": "3f31f68727da3a6e9d40b0e25e7cc26c6868c7317ca7fbdb516b7ea1e22bf902",
                "committed_particle_jsons": [f"assets/worldsmith/particles/{name}.json" for name in PARTICLE_NAMES],
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
            "Repair and decode the text-only Krovotok archive, validate committed particle JSON references, "
            "then materialize missing binary game assets under src/generated/resources."
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
    print("Particle JSON texture lists match all generated frames; no duplicate resources were generated.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
