from __future__ import annotations

import hashlib
import json
from pathlib import Path
from typing import Any

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
TASK_ROOT = ROOT / "codex_tasks/savva_food_textures"
TEXTURE_ROOT = ROOT / "src/main/resources/assets/worldsmith/textures/item"
PROFILE_ID = "worldsmith_food_icon_preferences_v1"
EXPECTED = {
    "tomato": ("daea304d6d68a929205db878cce0875a75bd8617cef7507d1c26dd82b840503f", "931bfabbb27e7643ff4b3b8db68efd642d4849af45306e01bb8ab498ce5bd1ea", 104, 9, 1),
    "lettuce": ("fddd671da5f7a4eae645197415ef8621f8ba7e49af2a5bc9cb2655b14b9e0c81", "3a264f7bb8ad9498706687691b9d7c5e4672691da7b5376ea09095655ef441c2", 133, 6, 1),
    "cucumber": ("17cd26131d4e73fe1f7567a1c4cd862b1225be93d25c494d438ff7f97998f876", "e1011f820c6a1b130c65cc17173aa76ae67ddf3df305e116109330dcdd38d106", 92, 6, 2),
    "strawberry": ("82b7280d5cc0bf4383c649c2efdbeba30564385807a4d7dc3f3c6f0ec9e8f516", "087048e577e1c40391fdf208d7de8669bc36da6d9d5c4eda0e2fc57b1e8b465d", 114, 8, 2),
    "blueberry": ("88152298fefca89465a57e12c827d488b058c30b743255e98c1862a0e11718cb", "573c334e2d7622084af72c2edb66a3404dd2ba6bde69b8139d3ce44656f4e555", 121, 8, 2),
    "raspberry": ("d75fa008b694796e082fb9cba81d2d759b95919c73f16d6dc11a1003433bdad4", "ca95279c19b7d6d9f75678b97d4f0c0e59860b8a71b022bd80442272de79c844", 113, 8, 2),
    "pear": ("564cab97ce33256f852fa9dd9d723b9fa53de0d9dd2a2c724145a093e37ccfcb", "225223f6144d17c7e0e74880e4a599bb54e36bfb3b74a7f9bbed410b53d0f8bc", 95, 7, 2),
    "peach": ("7e79eba4f52627cdf4e7a37e8991dbf1fb7d598ac0d75fbdea6634ad4eaa9e01", "97a1d716bd6f23dc4d892f05a620ef3fe005eb886aa8dfa3b10cdceb33c9524a", 100, 9, 2),
    "orange": ("22d47dd5fbbe35bb9b0049d43ded8c1486ad12655860d48c9a648e0cf87164ca", "c5e9121ecd5bf86b54e96198fd5315b63b9861aec8f3a0f76ae894bf285d9b1a", 102, 8, 2),
}
REJECTED = {
    "cucumber": "335152268f17ec0ccbd1ee37bf501157f4706baab59fbea7ceaf1bffd0638dd2",
    "strawberry": "00421f13eed3468b94046d4a62e17518f83124fe366b20b2cfa244f3dd99bb25",
    "blueberry": "1768f403c97b649d34613f3997609495f69a674330cd6044323394757d3cfdee",
    "raspberry": "47ca5bb7afa911d6e26ea0118b3ed1cd62c61e9ba106236d480d1f6d4b23a376",
    "pear": "9733aa8275eef8ba394aadeb54c6d07829656d6d63f6c5abce80c7a761de84af",
    "peach": "74cef7b000f59e24b0d102caba3de1a64c55a587defdee157a78b280ecc67d6e",
    "orange": "097b012f90ae64e8287ef6cbd009ddfe32f076ee731033eefc4190b6a8287526",
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
    gate_report = load_json(TASK_ROOT / "preference-gate-report.json")
    provenance = load_json(TASK_ROOT / "agent-run.json")

    if task.get("feature") != "minecraft_food_item_texture_pack_v1":
        raise VerificationError("Unexpected ForgeMind feature")
    style = task.get("style")
    if not isinstance(style, dict) or style.get("profile") != "vanilla_plus_food_mod":
        raise VerificationError("Unexpected style profile")
    if style.get("preference_profile") != PROFILE_ID or style.get("feedback_revision") != 1:
        raise VerificationError("Task is not bound to the learned preference profile")
    if report.get("preference_profile") != PROFILE_ID or report.get("feedback_revision") != 1:
        raise VerificationError("Texture report lost preference provenance")
    if gate_report.get("profile_id") != PROFILE_ID or gate_report.get("revision") != 1:
        raise VerificationError("Preference gate report is stale")
    if gate_report.get("approved_anchor_ids") != ["tomato", "lettuce"]:
        raise VerificationError("Approved visual anchors changed")
    if provenance.get("agent_version") != "1.7.0":
        raise VerificationError("Unexpected ForgeMind version")
    if provenance.get("preference_profile") != PROFILE_ID or provenance.get("feedback_revision") != 1:
        raise VerificationError("Agent provenance lost feedback binding")
    if provenance.get("verdict") != "passed" or provenance.get("source_unchanged") is not True:
        raise VerificationError("Agent run did not pass immutably")

    records = report.get("textures")
    if not isinstance(records, list):
        raise VerificationError("Texture report has no records")
    by_id = {record.get("id"): record for record in records if isinstance(record, dict)}
    if set(by_id) != set(EXPECTED):
        raise VerificationError(f"Unexpected texture report IDs: {sorted(by_id)}")
    gate_results = gate_report.get("results")
    if not isinstance(gate_results, list):
        raise VerificationError("Preference gate report has no results")
    gate_by_id = {entry.get("id"): entry for entry in gate_results if isinstance(entry, dict)}
    if set(gate_by_id) != set(EXPECTED):
        raise VerificationError("Preference gate does not cover all food icons")

    for item_id, (file_hash, pixel_hash, opaque_count, palette_count, revision) in EXPECTED.items():
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
        actual_pixel_hash = hashlib.sha256(image.tobytes()).hexdigest()
        if actual_pixel_hash != pixel_hash:
            raise VerificationError(f"Pixel hash changed: {item_id}")
        if item_id in REJECTED and actual_pixel_hash == REJECTED[item_id]:
            raise VerificationError(f"Rejected visual layout returned: {item_id}")

        record = by_id[item_id]
        gate = record.get("quality_gate")
        if record.get("pixel_sha256") != pixel_hash or record.get("template_revision") != revision:
            raise VerificationError(f"Report mismatch: {item_id}")
        if not isinstance(gate, dict) or gate.get("status") != "passed" or gate.get("score", 0) < 0.82:
            raise VerificationError(f"Preference quality gate failed: {item_id}")
        gate_entry = gate_by_id[item_id]
        if gate_entry.get("status") != "passed" or gate_entry.get("template_revision") != revision:
            raise VerificationError(f"Preference gate receipt mismatch: {item_id}")

    if by_id["tomato"]["template_revision"] != 1 or by_id["lettuce"]["template_revision"] != 1:
        raise VerificationError("Approved anchors were revised unexpectedly")
    for item_id in REJECTED:
        if by_id[item_id]["template_revision"] < 2:
            raise VerificationError(f"Negative feedback was not learned for {item_id}")

    print(json.dumps({
        "feature": task["feature"],
        "status": "passed",
        "textures": sorted(EXPECTED),
        "approved_anchors": ["tomato", "lettuce"],
        "revised_items": sorted(REJECTED),
        "preference_profile": PROFILE_ID,
        "feedback_revision": 1,
        "agent_version": provenance["agent_version"],
        "artifact_digest": provenance["artifact_digest"],
    }, ensure_ascii=False, sort_keys=True, indent=2))


if __name__ == "__main__":
    main()
