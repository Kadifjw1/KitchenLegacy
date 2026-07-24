#!/usr/bin/env python3
from __future__ import annotations

import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
TASK_DIR = Path(__file__).resolve().parent
LOCK_PATH = TASK_DIR / "MODEL_LOCK.json"
GAME_MODEL = ROOT / "src" / "generated" / "resources" / "assets" / "worldsmith" / "models" / "item" / "krovotok_base.json"
MASTER_MODEL = ROOT / "build" / "generated" / "krovotok" / "krovotok_stages_master.bbmodel"

ALLOWED_STATUSES = {
    "approved_locked",
    "candidate_pending_ingame_validation",
}


def geometry_sha256(model: dict) -> str:
    rows = [
        {
            key: element.get(key)
            for key in ("from", "to", "origin", "rotation", "inflate")
        }
        for element in model.get("elements", [])
    ]
    payload = json.dumps(rows, sort_keys=True, separators=(",", ":")).encode("utf-8")
    return hashlib.sha256(payload).hexdigest()


def verify(path: Path, lock: dict, strict: bool) -> None:
    if not path.is_file():
        raise FileNotFoundError(f"Missing generated model: {path}")

    model = json.loads(path.read_text(encoding="utf-8"))
    elements = model.get("elements", [])

    if len(elements) != lock["element_count"]:
        raise AssertionError(
            f"{path}: expected {lock['element_count']} elements, got {len(elements)}"
        )

    bounds_y = [
        min(float(element["from"][1]) for element in elements),
        max(float(element["to"][1]) for element in elements),
    ]
    if bounds_y != lock["bounds_y"]:
        raise AssertionError(
            f"{path}: expected Y bounds {lock['bounds_y']}, got {bounds_y}"
        )

    if model.get("worldsmith_position_profile") != lock["position_profile"]:
        raise AssertionError(f"{path}: position profile changed")

    # While the model is a placement candidate, display transforms and the
    # renderer correction must remain editable. Once the user approves the
    # in-game result, status becomes approved_locked and all strict hashes are
    # enforced again.
    if strict:
        actual_geometry = geometry_sha256(model)
        if actual_geometry != lock["geometry_sha256"]:
            raise AssertionError(
                f"{path}: geometry lock mismatch: {actual_geometry}"
            )
        if model.get("display") != lock["display"]:
            raise AssertionError(f"{path}: display transforms changed")


def main() -> int:
    lock = json.loads(LOCK_PATH.read_text(encoding="utf-8"))
    status = lock.get("status")
    if status not in ALLOWED_STATUSES:
        raise AssertionError(f"Unsupported Krovotok model status: {status}")

    strict = status == "approved_locked"
    verify(GAME_MODEL, lock, strict)
    verify(MASTER_MODEL, lock, strict)

    print("Krovotok model candidate verified:")
    print(f"- status: {status}")
    print(f"- elements: {lock['element_count']}")
    print(f"- Y bounds: {lock['bounds_y']}")
    print(f"- position profile: {lock['position_profile']}")
    if strict:
        print(f"- geometry: {lock['geometry_sha256']}")
        print("- display transforms: locked")
    else:
        print("- geometry/display strict lock: deferred until in-game approval")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
