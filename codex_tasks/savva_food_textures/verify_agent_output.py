from __future__ import annotations

import hashlib
import json
from pathlib import Path
from typing import Any

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
TASK_ROOT = ROOT / "codex_tasks/savva_food_textures"
TEXTURE_ROOT = ROOT / "src/main/resources/assets/worldsmith/textures/item"
EXPECTED = {
    "tomato": {
        "file_sha256": "daea304d6d68a929205db878cce0875a75bd8617cef7507d1c26dd82b840503f",
        "pixel_sha256": "931bfabbb27e7643ff4b3b8db68efd642d4849af45306e01bb8ab498ce5bd1ea",
        "palette_colors": 9,
        "opaque_pixels": 104,
    },
    "lettuce": {
        "file_sha256": "fddd671da5f7a4eae645197415ef8621f8ba7e49af2a5bc9cb2655b14b9e0c81",
        "pixel_sha256": "3a264f7bb8ad9498706687691b9d7c5e4672691da7b5376ea09095655ef441c2",
        "palette_colors": 6,
        "opaque_pixels": 133,
    },
    "cucumber": {
        "file_sha256": "cbec4f1190c5a2e61512cadf32597141feb1092f70a0a8bf1d892d44da2c7376",
        "pixel_sha256": "335152268f17ec0ccbd1ee37bf501157f4706baab59fbea7ceaf1bffd0638dd2",
        "palette_colors": 6,
        "opaque_pixels": 90,
    },
}


class VerificationError(RuntimeError):
    pass


def reject_duplicates(pairs: list[tuple[str, Any]]) -> dict[str, Any]:
    result: dict[str, Any] = {}
    for key, value in pairs:
        if key in result:
            raise VerificationError(f"Duplicate JSON key: {key}")
        result[key] = value
    return result


def load_json(path: Path) -> dict[str, Any]:
    if path.is_symlink() or not path.is_file():
        raise VerificationError(f"Missing or unsafe JSON file: {path}")
    value = json.loads(
        path.read_text(encoding="utf-8"),
        object_pairs_hook=reject_duplicates,
    )
    if not isinstance(value, dict):
        raise VerificationError(f"Expected JSON object: {path}")
    return value


def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main() -> None:
    task = load_json(TASK_ROOT / "forgemind-task.json")
    report = load_json(TASK_ROOT / "texture-report.json")
    provenance = load_json(TASK_ROOT / "agent-run.json")

    if task.get("feature") != "minecraft_food_item_texture_pack_v1":
        raise VerificationError("Unexpected ForgeMind feature")
    if task.get("style", {}).get("profile") != "vanilla_plus_food_mod":
        raise VerificationError("Unexpected style profile")
    if provenance.get("verdict") != "passed" or provenance.get("source_unchanged") is not True:
        raise VerificationError("Agent run did not pass immutably")

    records = report.get("textures")
    if not isinstance(records, list):
        raise VerificationError("Texture report has no records")
    by_id = {record.get("id"): record for record in records if isinstance(record, dict)}
    if set(by_id) != set(EXPECTED):
        raise VerificationError(f"Unexpected texture report IDs: {sorted(by_id)}")

    for item_id, expected in EXPECTED.items():
        path = TEXTURE_ROOT / f"{item_id}.png"
        if path.is_symlink() or not path.is_file():
            raise VerificationError(f"Missing texture: {path}")
        if sha256(path) != expected["file_sha256"]:
            raise VerificationError(f"Agent file hash changed: {item_id}")

        image = Image.open(path).convert("RGBA")
        if image.size != (16, 16):
            raise VerificationError(f"Texture is not 16x16: {item_id}")
        pixels = list(image.getdata())
        if any(pixel[3] not in (0, 255) for pixel in pixels):
            raise VerificationError(f"Semi-transparent pixels found: {item_id}")
        opaque = [pixel for pixel in pixels if pixel[3] == 255]
        palette = set(opaque)
        if len(opaque) != expected["opaque_pixels"]:
            raise VerificationError(f"Opaque pixel count changed: {item_id}")
        if len(palette) != expected["palette_colors"]:
            raise VerificationError(f"Palette changed: {item_id}")
        pixel_hash = hashlib.sha256(image.tobytes()).hexdigest()
        if pixel_hash != expected["pixel_sha256"]:
            raise VerificationError(f"Pixel hash changed: {item_id}")

        record = by_id[item_id]
        if record.get("pixel_sha256") != expected["pixel_sha256"]:
            raise VerificationError(f"Report pixel hash mismatch: {item_id}")
        if record.get("semi_transparent_pixels") != 0:
            raise VerificationError(f"Report transparency mismatch: {item_id}")

    print(
        json.dumps(
            {
                "feature": task["feature"],
                "status": "passed",
                "textures": sorted(EXPECTED),
                "agent_version": provenance["agent_version"],
                "artifact_digest": provenance["artifact_digest"],
            },
            ensure_ascii=False,
            sort_keys=True,
            indent=2,
        )
    )


if __name__ == "__main__":
    main()
