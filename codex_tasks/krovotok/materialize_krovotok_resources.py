#!/usr/bin/env python3
from __future__ import annotations

import argparse
import base64
import binascii
import hashlib
import json
import math
import random
import shutil
import struct
import subprocess
import sys
import uuid
import zlib
from pathlib import Path

TASK_DIR = Path(__file__).resolve().parent
REPO_ROOT = TASK_DIR.parent.parent
UNPACKED_DIR = TASK_DIR / "unpacked"
SOURCE_ASSETS = UNPACKED_DIR / "resourcepack" / "assets" / "worldsmith"
SOURCE_BBMODEL = UNPACKED_DIR / "krovotok_JE_1.20.1.bbmodel"
DEFAULT_OUTPUT = REPO_ROOT / "src" / "generated" / "resources"
GENERATED_BBMODEL = REPO_ROOT / "build" / "generated" / "krovotok" / "krovotok_stages_master.bbmodel"
MAIN_PARTICLES = REPO_ROOT / "src" / "main" / "resources" / "assets" / "worldsmith" / "particles"
EXPECTED_GEOMETRY_SHA256 = "414a5ecdf02b9ded8183e2b02e0c9eb88fa6d9f93e60b15c5573f63e491f2429"
ATLAS_SIZE = 256
TILE_SIZE = 16
STAGE_COUNT = 6
PARTICLE_TEXTURES = {
    "krovotok_blood_mist": [f"worldsmith:krovotok/blood_mist_{frame}" for frame in range(6)],
    "krovotok_blood_spark": [f"worldsmith:krovotok/blood_spark_{frame}" for frame in range(4)],
    "krovotok_blood_pulse": [f"worldsmith:krovotok/blood_pulse_{frame}" for frame in range(6)],
    "krovotok_blood_burst": [f"worldsmith:krovotok/blood_burst_{frame}" for frame in range(8)],
    "krovotok_life_drain": [f"worldsmith:krovotok/life_drain_{frame}" for frame in range(6)],
}

MAIN_PATH = [
    (8.0, 2.4), (8.55, 5.0), (7.45, 8.0), (8.35, 11.0),
    (7.65, 14.0), (8.55, 17.0), (7.35, 20.0), (8.35, 23.0), (7.8, 27.2),
]
BRANCH_PATHS = [
    [(8.25, 4.0), (6.8, 5.0), (5.1, 6.8)],
    [(8.15, 5.6), (9.3, 6.3), (10.8, 8.0)],
    [(7.65, 8.5), (6.6, 9.2), (5.2, 10.8)],
    [(8.15, 10.8), (9.35, 11.8), (10.9, 13.4)],
    [(7.8, 13.5), (6.6, 14.2), (5.2, 16.0)],
    [(8.2, 15.8), (9.5, 16.8), (10.9, 18.6)],
    [(7.65, 18.5), (6.55, 19.2), (5.1, 21.0)],
    [(8.05, 20.6), (9.35, 21.5), (10.8, 23.2)],
    [(7.75, 22.8), (6.7, 23.8), (5.8, 25.7)],
    [(8.1, 24.4), (9.1, 25.0), (10.0, 26.2)],
]
SMALL_BRANCHES = [
    [(6.3, 5.7), (5.6, 5.3)], [(9.8, 7.1), (10.5, 6.7)],
    [(6.3, 9.8), (5.8, 9.2)], [(9.6, 12.2), (10.3, 11.8)],
    [(6.3, 14.8), (5.7, 14.2)], [(9.7, 17.4), (10.4, 17.0)],
    [(6.2, 19.8), (5.6, 19.3)], [(9.6, 22.0), (10.2, 21.5)],
]


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def geometry_sha256(model: dict) -> str:
    rows = [{key: element.get(key) for key in ("from", "to", "origin", "rotation", "inflate")}
            for element in model.get("elements", [])]
    return hashlib.sha256(json.dumps(rows, sort_keys=True, separators=(",", ":")).encode("utf-8")).hexdigest()


def read_source_model() -> dict:
    if not SOURCE_BBMODEL.is_file():
        candidates = sorted(UNPACKED_DIR.rglob("krovotok_JE_1.20.1.bbmodel"))
        if not candidates:
            raise FileNotFoundError(f"Missing approved Krovotok BBModel under {UNPACKED_DIR}")
        source = candidates[0]
    else:
        source = SOURCE_BBMODEL
    model = json.loads(source.read_text(encoding="utf-8"))
    if len(model.get("elements", [])) != 170:
        raise ValueError("Krovotok source must contain exactly 170 elements")
    actual_hash = geometry_sha256(model)
    if actual_hash != EXPECTED_GEOMETRY_SHA256:
        raise ValueError(f"Krovotok geometry changed: expected {EXPECTED_GEOMETRY_SHA256}, got {actual_hash}")
    return model


def png_chunk(kind: bytes, payload: bytes) -> bytes:
    return struct.pack(">I", len(payload)) + kind + payload + struct.pack(">I", binascii.crc32(kind + payload) & 0xFFFFFFFF)


def encode_png_rgba(width: int, height: int, pixels: bytearray) -> bytes:
    if len(pixels) != width * height * 4:
        raise ValueError("Invalid RGBA buffer size")
    scanlines = bytearray()
    stride = width * 4
    for y in range(height):
        scanlines.append(0)
        scanlines.extend(pixels[y * stride:(y + 1) * stride])
    return (b"\x89PNG\r\n\x1a\n"
            + png_chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
            + png_chunk(b"IDAT", zlib.compress(bytes(scanlines), 9))
            + png_chunk(b"IEND", b""))


def blend_pixel(pixels: bytearray, x: int, y: int, color: tuple[int, int, int, int]) -> None:
    if x < 0 or y < 0 or x >= ATLAS_SIZE or y >= ATLAS_SIZE:
        return
    index = (y * ATLAS_SIZE + x) * 4
    sr, sg, sb, sa = color
    if sa <= 0:
        return
    da = pixels[index + 3]
    alpha = sa / 255.0
    pixels[index] = min(255, round(sr * alpha + pixels[index] * (1.0 - alpha)))
    pixels[index + 1] = min(255, round(sg * alpha + pixels[index + 1] * (1.0 - alpha)))
    pixels[index + 2] = min(255, round(sb * alpha + pixels[index + 2] * (1.0 - alpha)))
    pixels[index + 3] = min(255, round(sa + da * (1.0 - alpha)))


def distance_to_segment(px: float, py: float, a: tuple[float, float], b: tuple[float, float]) -> float:
    ax, ay = a
    bx, by = b
    dx, dy = bx - ax, by - ay
    length_sq = dx * dx + dy * dy
    if length_sq <= 1e-12:
        return math.hypot(px - ax, py - ay)
    t = max(0.0, min(1.0, ((px - ax) * dx + (py - ay) * dy) / length_sq))
    return math.hypot(px - (ax + t * dx), py - (ay + t * dy))


def distance_to_path(px: float, py: float, path: list[tuple[float, float]]) -> float:
    return min(distance_to_segment(px, py, path[index], path[index + 1]) for index in range(len(path) - 1))


def material_for(element: dict) -> str:
    name = element.get("name", "").lower()
    if name.startswith("blade"):
        return "blade"
    if "handle" in name or "grip" in name:
        return "handle"
    if "pommel" in name:
        return "pommel"
    return "guard"


def base_color(material: str) -> tuple[int, int, int]:
    return {
        "blade": (29, 32, 40),
        "guard": (25, 28, 35),
        "handle": (55, 30, 23),
        "pommel": (23, 26, 33),
    }[material]


def paint_material_tile(pixels: bytearray, tile_index: int, material: str, seed: int) -> None:
    tx = (tile_index % 16) * TILE_SIZE
    ty = (tile_index // 16) * TILE_SIZE
    rng = random.Random(seed)
    br, bg, bb = base_color(material)
    for y in range(TILE_SIZE):
        for x in range(TILE_SIZE):
            edge = 1 if x in (0, 15) or y in (0, 15) else 0
            noise = rng.randint(-4, 4) - edge * 3
            index = ((ty + y) * ATLAS_SIZE + tx + x) * 4
            pixels[index:index + 4] = bytes((max(0, br + noise), max(0, bg + noise), max(0, bb + noise), 255))


def vein_sample(model_x: float, model_y: float, stage: int) -> tuple[float, float]:
    main_distance = distance_to_path(model_x, model_y, MAIN_PATH)
    branch_distance = min(distance_to_path(model_x, model_y, path) for path in BRANCH_PATHS)
    small_distance = min(distance_to_path(model_x, model_y, path) for path in SMALL_BRANCHES)
    stage_factor = stage / 5.0
    main = max(0.0, 1.0 - main_distance / (0.34 + stage_factor * 0.04))
    branch = max(0.0, 1.0 - branch_distance / (0.20 + stage_factor * 0.04))
    small = max(0.0, 1.0 - small_distance / (0.13 + stage_factor * 0.03))
    base_strength = 0.38 + stage_factor * 0.62
    branch_strength = 0.20 + stage_factor * 0.80
    combined = max(main * base_strength, branch * branch_strength, small * branch_strength * 0.78)
    core = max(
        max(0.0, 1.0 - main_distance / 0.075) * (0.55 + stage_factor * 0.45),
        max(0.0, 1.0 - branch_distance / 0.045) * (0.25 + stage_factor * 0.75),
        max(0.0, 1.0 - small_distance / 0.032) * (0.15 + stage_factor * 0.75),
    )
    return min(1.0, combined), min(1.0, core)


def paint_element_tile(base: bytearray, glow: bytearray, tile_index: int, element: dict, stage: int) -> None:
    material = material_for(element)
    paint_material_tile(base, tile_index, material, 9917 + tile_index * 37)
    if material != "blade":
        return
    tx = (tile_index % 16) * TILE_SIZE
    ty = (tile_index // 16) * TILE_SIZE
    x0, y0 = element["from"][0], element["from"][1]
    x1, y1 = element["to"][0], element["to"][1]
    for py in range(TILE_SIZE):
        for px in range(TILE_SIZE):
            model_x = x0 + (px + 0.5) / TILE_SIZE * (x1 - x0)
            model_y = y1 - (py + 0.5) / TILE_SIZE * (y1 - y0)
            halo, core = vein_sample(model_x, model_y, stage)
            if halo <= 0.0:
                continue
            red = round(120 + 135 * core)
            green = round(8 + 62 * core)
            blue = round(10 + 30 * core)
            alpha = round(50 + 205 * max(halo, core))
            blend_pixel(base, tx + px, ty + py, (red, green, blue, alpha))
            glow_alpha = round(255 * max(core, halo * (0.38 + stage * 0.08)))
            glow_red = 255
            glow_green = round(18 + 65 * core)
            glow_blue = round(20 + 28 * core)
            blend_pixel(glow, tx + px, ty + py, (glow_red, glow_green, glow_blue, glow_alpha))


def stage_textures(model: dict, stage: int) -> tuple[bytes, bytes]:
    base = bytearray(ATLAS_SIZE * ATLAS_SIZE * 4)
    glow = bytearray(ATLAS_SIZE * ATLAS_SIZE * 4)
    for palette_index, material in enumerate(("blade", "guard", "handle", "pommel")):
        paint_material_tile(base, palette_index, material, 100 + palette_index)
    for index, element in enumerate(model["elements"]):
        paint_element_tile(base, glow, 16 + index, element, stage)
    return encode_png_rgba(ATLAS_SIZE, ATLAS_SIZE, base), encode_png_rgba(ATLAS_SIZE, ATLAS_SIZE, glow)


def remap_uv(model: dict) -> dict:
    remapped = json.loads(json.dumps(model))
    remapped["resolution"] = {"width": ATLAS_SIZE, "height": ATLAS_SIZE}
    for index, element in enumerate(remapped["elements"]):
        material = material_for(element)
        material_tile = {"blade": 0, "guard": 1, "handle": 2, "pommel": 3}[material]
        unique_tile = 16 + index
        ux, uy = (unique_tile % 16) * TILE_SIZE, (unique_tile // 16) * TILE_SIZE
        mx, my = (material_tile % 16) * TILE_SIZE, (material_tile // 16) * TILE_SIZE
        for side, face in element.get("faces", {}).items():
            if material == "blade" and side in ("north", "south"):
                face["uv"] = [ux, uy, ux + TILE_SIZE, uy + TILE_SIZE]
            else:
                face["uv"] = [mx, my, mx + TILE_SIZE, my + TILE_SIZE]
            face["texture"] = 0
    return remapped


def game_model(model: dict) -> dict:
    result = {
        "credit": "Worldsmith — Krovotok / approved Colossus geometry",
        "ambientocclusion": model.get("ambientocclusion", False),
        "gui_light": model.get("gui_light", "front"),
        "textures": {"0": "worldsmith:item/krovotok_stage_0", "particle": "worldsmith:item/krovotok_stage_0"},
        "display": model.get("display", {}),
        "elements": [],
    }
    for element in model.get("elements", []):
        converted = {"from": element["from"], "to": element["to"], "faces": {}}
        if element.get("rotation"):
            converted["rotation"] = element["rotation"]
        for side, source_face in element.get("faces", {}).items():
            face = {"texture": "#0"}
            uv = source_face.get("uv")
            if isinstance(uv, list) and len(uv) == 4:
                face["uv"] = [round(float(value) / 16.0, 6) for value in uv]
            converted["faces"][side] = face
        result["elements"].append(converted)
    return result


def make_master_bbmodel(model: dict, stages: list[tuple[bytes, bytes]]) -> dict:
    master = json.loads(json.dumps(model))
    master["name"] = "Кровоток — стадии 0–5"
    master["model_identifier"] = "krovotok_stages"
    master["textures"] = []
    texture_id = 0
    for stage, (base_png, glow_png) in enumerate(stages):
        for kind, payload in (("stage", base_png), ("glow", glow_png)):
            name = f"krovotok_{kind}_{stage}.png"
            master["textures"].append({
                "name": name,
                "id": str(texture_id),
                "width": ATLAS_SIZE,
                "height": ATLAS_SIZE,
                "uv_width": ATLAS_SIZE,
                "uv_height": ATLAS_SIZE,
                "particle": False,
                "use_as_default": texture_id == 0,
                "file_format": "png",
                "render_mode": "default",
                "visible": True,
                "internal": True,
                "saved": False,
                "uuid": str(uuid.uuid5(uuid.NAMESPACE_URL, f"worldsmith:krovotok:{name}")),
                "source": "data:image/png;base64," + base64.b64encode(payload).decode("ascii"),
            })
            texture_id += 1
    return master


def verify_committed_particle_jsons() -> None:
    for name, expected_textures in PARTICLE_TEXTURES.items():
        particle_path = MAIN_PARTICLES / f"{name}.json"
        data = json.loads(particle_path.read_text(encoding="utf-8"))
        if data.get("textures") != expected_textures:
            raise ValueError(f"{particle_path}: particle frames do not match expected list")


def materialize(output_root: Path) -> dict[str, str]:
    subprocess.run([sys.executable, str(TASK_DIR / "verify_asset_archive.py"), "--extract"], cwd=REPO_ROOT, check=True)
    verify_committed_particle_jsons()
    source = read_source_model()
    model = remap_uv(source)
    stages = [stage_textures(model, stage) for stage in range(STAGE_COUNT)]

    worldsmith = output_root / "assets" / "worldsmith"
    generated = worldsmith / "textures" / "item"
    particle_destination = worldsmith / "textures" / "particle" / "krovotok"
    for target in (worldsmith / "models" / "item" / "krovotok_base.json", worldsmith / "krovotok_generated_assets.json", particle_destination):
        if target.is_dir():
            shutil.rmtree(target)
        elif target.exists():
            target.unlink()
    generated.mkdir(parents=True, exist_ok=True)
    particle_destination.mkdir(parents=True, exist_ok=True)

    manifest: dict[str, str] = {}
    base_model_path = worldsmith / "models" / "item" / "krovotok_base.json"
    base_model_path.parent.mkdir(parents=True, exist_ok=True)
    base_model_path.write_text(json.dumps(game_model(model), ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    manifest[base_model_path.relative_to(output_root).as_posix()] = sha256(base_model_path)

    for stage, (base_png, glow_png) in enumerate(stages):
        for kind, payload in (("stage", base_png), ("glow", glow_png)):
            path = generated / f"krovotok_{kind}_{stage}.png"
            path.write_bytes(payload)
            manifest[path.relative_to(output_root).as_posix()] = sha256(path)

    particle_frames = sorted((SOURCE_ASSETS / "textures" / "particle" / "krovotok").glob("*.png"))
    if len(particle_frames) != 30:
        raise RuntimeError(f"Expected 30 Krovotok particle PNG files, found {len(particle_frames)}")
    for source_path in particle_frames:
        destination = particle_destination / source_path.name
        shutil.copy2(source_path, destination)
        manifest[destination.relative_to(output_root).as_posix()] = sha256(destination)

    GENERATED_BBMODEL.parent.mkdir(parents=True, exist_ok=True)
    GENERATED_BBMODEL.write_text(json.dumps(make_master_bbmodel(model, stages), ensure_ascii=False, separators=(",", ":")), encoding="utf-8")

    manifest_path = worldsmith / "krovotok_generated_assets.json"
    manifest_path.write_text(json.dumps({
        "generated": True,
        "source_bbmodel": "verified Krovotok archive",
        "generated_bbmodel": str(GENERATED_BBMODEL.relative_to(REPO_ROOT)),
        "geometry_sha256": EXPECTED_GEOMETRY_SHA256,
        "element_count": 170,
        "stage_count": 6,
        "emissive_masks": 6,
        "particle_frames": 30,
        "files": manifest,
    }, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    return manifest


def main() -> int:
    parser = argparse.ArgumentParser(description="Generate Krovotok stages and full-bright vein masks from the approved BBModel.")
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT)
    args = parser.parse_args()
    manifest = materialize(args.output.resolve())
    print(f"Krovotok generated resources: {args.output.resolve()}")
    print(f"Generated BBModel: {GENERATED_BBMODEL}")
    print(f"Materialized files: {len(manifest)}")
    print("Geometry: 170 elements, unchanged")
    print("Stages: 0..5 with bright main channel and branching full-bright veins")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
