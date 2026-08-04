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
    "tomato": ("daea304d6d68a929205db878cce0875a75bd8617cef7507d1c26dd82b840503f", "931bfabbb27e7643ff4b3b8db68efd642d4849af45306e01bb8ab498ce5bd1ea", 104, 9),
    "lettuce": ("fddd671da5f7a4eae645197415ef8621f8ba7e49af2a5bc9cb2655b14b9e0c81", "3a264f7bb8ad9498706687691b9d7c5e4672691da7b5376ea09095655ef441c2", 133, 6),
    "cucumber": ("cbec4f1190c5a2e61512cadf32597141feb1092f70a0a8bf1d892d44da2c7376", "335152268f17ec0ccbd1ee37bf501157f4706baab59fbea7ceaf1bffd0638dd2", 90, 6),
    "strawberry": ("983db9036d7c57d4d055fd7af466f8255cc200fe2ee0df71d7bfc69984413f3c", "00421f13eed3468b94046d4a62e17518f83124fe366b20b2cfa244f3dd99bb25", 90, 8),
    "blueberry": ("ef15d898be6540f5e6e82e5a7e5a7a75eb26532b4dc19bf0ed3a84bf2e2f65a4", "1768f403c97b649d34613f3997609495f69a674330cd6044323394757d3cfdee", 69, 8),
    "raspberry": ("85c43bbeb03dfdda10ea329d018f0d62953f03ef8d2d5975c8bf15aa685d58bc", "47ca5bb7afa911d6e26ea0118b3ed1cd62c61e9ba106236d480d1f6d4b23a376", 89, 8),
    "pear": ("82b824af4b0582bbd2b7bdcbd0fa1713015cf416ef8a81ef72fc840568cb14a6", "9733aa8275eef8ba394aadeb54c6d07829656d6d63f6c5abce80c7a761de84af", 86, 7),
    "peach": ("4c9f658d0af4f5a72f597f54f2869205fbc65bfb474fe8fa9f2f0e21f07762bd", "74cef7b000f59e24b0d102caba3de1a64c55a587defdee157a78b280ecc67d6e", 93, 8),
    "orange": ("0df30e240b15e77dd2100756d8ac0afb4b4c50c0f77d0ea9b7652b11e912acbe", "097b012f90ae64e8287ef6cbd009ddfe32f076ee731033eefc4190b6a8287526", 85, 8),
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
    value = json.loads(path.read_text(encoding="utf-8"), object_pairs_hook=reject_duplicates)
    if not isinstance(value, dict):
        raise VerificationError(f"Expected JSON object: {path}")
    return value


def main() -> None:
    task = load_json(TASK_ROOT / "forgemind-task.json")
    report = load_json(TASK_ROOT / "texture-report.json")
    provenance = load_json(TASK_ROOT / "agent-run.json")
    if task.get("feature") != "minecraft_food_item_texture_pack_v1":
        raise VerificationError("Unexpected ForgeMind feature")
    if task.get("style", {}).get("profile") != "vanilla_plus_food_mod":
        raise VerificationError("Unexpected style profile")
    if provenance.get("agent_version") != "1.6.0":
        raise VerificationError("Unexpected ForgeMind version")
    if provenance.get("verdict") != "passed" or provenance.get("source_unchanged") is not True:
        raise VerificationError("Agent run did not pass immutably")
    records = report.get("textures")
    if not isinstance(records, list):
        raise VerificationError("Texture report has no records")
    by_id = {record.get("id"): record for record in records if isinstance(record, dict)}
    if set(by_id) != set(EXPECTED):
        raise VerificationError(f"Unexpected texture report IDs: {sorted(by_id)}")

    for item_id, (file_hash, pixel_hash, opaque_count, palette_count) in EXPECTED.items():
        path = TEXTURE_ROOT / f"{item_id}.png"
        if path.is_symlink() or not path.is_file():
            raise VerificationError(f"Missing texture: {path}")
        if hashlib.sha256(path.read_bytes()).hexdigest() != file_hash:
            raise VerificationError(f"Agent file hash changed: {item_id}")
        image = Image.open(path).convert("RGBA")
        if image.size != (16, 16):
            raise VerificationError(f"Texture is not 16x16: {item_id}")
        pixels = list(image.getdata())
        if any(pixel[3] not in (0, 255) for pixel in pixels):
            raise VerificationError(f"Semi-transparent pixels found: {item_id}")
        opaque = [pixel for pixel in pixels if pixel[3] == 255]
        if len(opaque) != opaque_count or len(set(opaque)) != palette_count:
            raise VerificationError(f"Texture metrics changed: {item_id}")
        if hashlib.sha256(image.tobytes()).hexdigest() != pixel_hash:
            raise VerificationError(f"Pixel hash changed: {item_id}")
        record = by_id[item_id]
        if record.get("pixel_sha256") != pixel_hash or record.get("semi_transparent_pixels") != 0:
            raise VerificationError(f"Report mismatch: {item_id}")

    print(json.dumps({
        "feature": task["feature"],
        "status": "passed",
        "textures": sorted(EXPECTED),
        "agent_version": provenance["agent_version"],
        "artifact_digest": provenance["artifact_digest"],
    }, ensure_ascii=False, sort_keys=True, indent=2))


if __name__ == "__main__":
    main()
