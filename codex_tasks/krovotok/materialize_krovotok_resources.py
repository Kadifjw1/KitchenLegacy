#!/usr/bin/env python3
from __future__ import annotations

import argparse
import base64
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
SOURCE_BBMODEL = TASK_DIR / "source" / "krovotok_stages_master.bbmodel"
SOURCE_BBMODEL_PARTS = TASK_DIR / "source" / "krovotok_stages_master.parts"
DEFAULT_OUTPUT = REPO_ROOT / "src" / "generated" / "resources"
MAIN_PARTICLES = REPO_ROOT / "src" / "main" / "resources" / "assets" / "worldsmith" / "particles"
EXPECTED_GEOMETRY_SHA256 = "414a5ecdf02b9ded8183e2b02e0c9eb88fa6d9f93e60b15c5573f63e491f2429"
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


def geometry_sha256(model: dict) -> str:
    rows = []
    for element in model.get("elements", []):
        rows.append({
            key: element.get(key)
            for key in ("from", "to", "origin", "rotation", "inflate")
        })
    payload = json.dumps(rows, sort_keys=True, separators=(",", ":")).encode("utf-8")
    return hashlib.sha256(payload).hexdigest()


def read_master() -> dict:
    try:
        if SOURCE_BBMODEL.is_file():
            payload = SOURCE_BBMODEL.read_text(encoding="utf-8")
        else:
            parts = sorted(SOURCE_BBMODEL_PARTS.glob("part*.txt"))
            if not parts:
                raise FileNotFoundError(f"Missing Krovotok BBModel and shards: {SOURCE_BBMODEL_PARTS}")
            payload = "".join(part.read_text(encoding="utf-8") for part in parts)
        model = json.loads(payload)
    except (OSError, json.JSONDecodeError) as error:
        raise ValueError(f"Invalid reconstructed Krovotok master BBModel: {error}") from error
    if len(model.get("elements", [])) != 170:
        raise ValueError("Krovotok master must contain exactly 170 elements")
    actual_hash = geometry_sha256(model)
    if actual_hash != EXPECTED_GEOMETRY_SHA256:
        raise ValueError(f"Krovotok geometry changed: expected {EXPECTED_GEOMETRY_SHA256}, got {actual_hash}")
    return model


def embedded_png(model: dict, name: str) -> bytes:
    for texture in model.get("textures", []):
        if texture.get("name") != name:
            continue
        source = texture.get("source", "")
        prefix = "data:image/png;base64,"
        if not source.startswith(prefix):
            raise ValueError(f"Texture {name} is not an embedded PNG")
        return base64.b64decode(source[len(prefix):], validate=True)
    raise FileNotFoundError(f"Missing embedded texture {name} in reconstructed master BBModel")


def game_model(model: dict) -> dict:
    result = {
        "credit": "Worldsmith — Krovotok / approved Colossus geometry",
        "ambientocclusion": model.get("ambientocclusion", False),
        "gui_light": model.get("gui_light", "front"),
        "textures": {
            "0": "worldsmith:item/krovotok_stage_0",
            "particle": "worldsmith:item/krovotok_stage_0",
        },
        "display": model.get("display", {}),
        "elements": [],
    }
    for element in model.get("elements", []):
        converted = {
            "from": element["from"],
            "to": element["to"],
            "faces": {},
        }
        if element.get("rotation"):
            converted["rotation"] = element["rotation"]
        for side, source_face in element.get("faces", {}).items():
            face = {"texture": "#0"}
            uv = source_face.get("uv")
            if isinstance(uv, list) and len(uv) == 4:
                face["uv"] = [round(float(value) / 16.0, 6) for value in uv]
            for key in ("rotation", "cullface", "tintindex"):
                if key in source_face:
                    face[key] = source_face[key]
            converted["faces"][side] = face
        result["elements"].append(converted)
    return result


def verify_committed_particle_jsons() -> None:
    for name, expected_textures in PARTICLE_TEXTURES.items():
        particle_path = MAIN_PARTICLES / f"{name}.json"
        if not particle_path.is_file():
            raise FileNotFoundError(f"Missing committed particle JSON: {particle_path}")
        data = json.loads(particle_path.read_text(encoding="utf-8"))
        if data.get("textures") != expected_textures:
            raise ValueError(f"{particle_path}: particle frames do not match expected list")


def remove_target(path: Path) -> None:
    if path.is_file() or path.is_symlink():
        path.unlink()
    elif path.is_dir():
        shutil.rmtree(path)


def materialize(output_root: Path) -> dict[str, str]:
    subprocess.run(
        [sys.executable, str(TASK_DIR / "verify_asset_archive.py"), "--extract"],
        cwd=REPO_ROOT,
        check=True,
    )
    verify_committed_particle_jsons()
    master = read_master()

    worldsmith = output_root / "assets" / "worldsmith"
    generated_targets = [
        worldsmith / "models" / "item" / "krovotok_base.json",
        worldsmith / "krovotok_generated_assets.json",
        worldsmith / "textures" / "particle" / "krovotok",
    ]
    generated_targets.extend(worldsmith / "textures" / "item" / f"krovotok_stage_{stage}.png" for stage in range(6))
    generated_targets.extend(worldsmith / "textures" / "item" / f"krovotok_glow_{stage}.png" for stage in range(6))
    generated_targets.extend(worldsmith / "textures" / "item" / f"krovotok_charge_{stage}.png" for stage in range(6))
    generated_targets.extend([
        worldsmith / "textures" / "item" / "krovotok.png",
        worldsmith / "textures" / "item" / "krovotok.png.mcmeta",
        worldsmith / "textures" / "item" / "krovotok_static.png",
    ])
    for target in generated_targets:
        remove_target(target)

    manifest: dict[str, str] = {}
    base_model_path = worldsmith / "models" / "item" / "krovotok_base.json"
    base_model_path.parent.mkdir(parents=True, exist_ok=True)
    base_model_path.write_text(json.dumps(game_model(master), ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    manifest[base_model_path.relative_to(output_root).as_posix()] = sha256(base_model_path)

    item_texture_dir = worldsmith / "textures" / "item"
    item_texture_dir.mkdir(parents=True, exist_ok=True)
    for stage in range(6):
        for kind in ("stage", "glow"):
            name = f"krovotok_{kind}_{stage}.png"
            destination = item_texture_dir / name
            destination.write_bytes(embedded_png(master, name))
            manifest[destination.relative_to(output_root).as_posix()] = sha256(destination)

    particle_source = SOURCE_ASSETS / "textures" / "particle" / "krovotok"
    particle_destination = worldsmith / "textures" / "particle" / "krovotok"
    particle_destination.mkdir(parents=True, exist_ok=True)
    particle_frames = sorted(particle_source.glob("*.png"))
    if len(particle_frames) != 30:
        raise RuntimeError(f"Expected 30 Krovotok particle PNG files, found {len(particle_frames)}")
    for source in particle_frames:
        destination = particle_destination / source.name
        shutil.copy2(source, destination)
        manifest[destination.relative_to(output_root).as_posix()] = sha256(destination)

    manifest_path = worldsmith / "krovotok_generated_assets.json"
    manifest_path.parent.mkdir(parents=True, exist_ok=True)
    manifest_path.write_text(json.dumps({
        "generated": True,
        "source_bbmodel": "codex_tasks/krovotok/source/krovotok_stages_master.parts/",
        "geometry_sha256": EXPECTED_GEOMETRY_SHA256,
        "element_count": 170,
        "stage_count": 6,
        "emissive_masks": 6,
        "particle_frames": 30,
        "files": manifest,
    }, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    manifest[manifest_path.relative_to(output_root).as_posix()] = sha256(manifest_path)
    return manifest


def main() -> int:
    parser = argparse.ArgumentParser(description="Materialize Krovotok stages and emissive masks from a text BBModel.")
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT)
    args = parser.parse_args()
    manifest = materialize(args.output.resolve())
    print(f"Krovotok generated resources: {args.output.resolve()}")
    print(f"Materialized files: {len(manifest)}")
    print("Geometry: 170 elements, unchanged")
    print("Stages: 0..5 with separate full-bright crimson vein masks")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
