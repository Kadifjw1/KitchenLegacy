#!/usr/bin/env python3
from __future__ import annotations

import argparse
import base64
import hashlib
import json
import shutil
import sys
import zipfile
from pathlib import Path

TASK_DIR = Path(__file__).resolve().parent
ASSET_DIR = TASK_DIR / "assets"
PATCH_FILE = ASSET_DIR / "ARCHIVE_REPAIR_PATCHES.json"
EXPECTED_SHA256 = "3f31f68727da3a6e9d40b0e25e7cc26c6868c7317ca7fbdb516b7ea1e22bf902"
EXPECTED_B64_LENGTH = 199_932
EXPECTED_PIECE_SHAS = {
    "archive.part00.b64": "9359a27c70a1db0d88a981ab2d4a920c4eae52bc",
    "archive.part01.b64": "38a23a3dbd6211caeccf1708413864e7b242f6bc",
    "archive.rem00.b64": "b26d4b0038dca84f4fa6999d7b0f38fe937f0581",
    "archive.rem01.b64": "4885bf1583067385a8c4eb4518f3b68260c052ab",
    "archive.rem02.b64": "57b9f6ceb4dedf310c18c9d7eacdc3299ae8dabc",
    "archive.rem03.b64": "79a7026bbabdcb1f14f7775205046386eb6bff1c",
    "archive.rem04.b64": "0e0efa5dfd68f2c61c794d3c75ba8732af3723e4",
    "archive.rem05.b64": "d2525f113434bd3f9464da5b2011dce42a1a24f7",
    "archive.rem06.b64": "f8a0d02b51743d80fc6be2322134d211366e40dc",
    "archive.rem07.b64": "f89128dbe0be61a813fcfb87922f5e45e88f83ae",
    "archive.rem08.b64": "62ba338b775a07b961e25eb1c8c7dbd5420f7d9f",
}
ORDERED_PIECES = list(EXPECTED_PIECE_SHAS)


def git_blob_sha(value: str) -> str:
    raw = value.encode("utf-8")
    return hashlib.sha1(f"blob {len(raw)}\0".encode("ascii") + raw).hexdigest()


def read_piece(name: str) -> str:
    path = ASSET_DIR / name
    if not path.is_file():
        raise FileNotFoundError(f"Missing asset piece: {path}")
    return "".join(path.read_text(encoding="utf-8").split())


def repair_piece(name: str, value: str, patch_data: dict[str, object]) -> str:
    specification = patch_data.get(name)
    if specification is None:
        return value

    operations = specification["operations"]
    for operation in operations:
        start = int(operation["start"])
        end = int(operation["end"])
        expected_old = str(operation["old"])
        actual_old = value[start:end]
        if actual_old != expected_old:
            raise ValueError(
                f"{name}: source changed at {start}:{end}; "
                f"expected {expected_old!r}, got {actual_old!r}"
            )

    repaired = value
    for operation in reversed(operations):
        start = int(operation["start"])
        end = int(operation["end"])
        repaired = repaired[:start] + str(operation["replacement"]) + repaired[end:]

    expected_length = int(specification["expected_length"])
    if len(repaired) != expected_length:
        raise ValueError(f"{name}: repaired length {len(repaired)} != {expected_length}")
    return repaired


def verify_zip(zip_path: Path) -> None:
    required = {
        "krovotok_JE_1.20.1.bbmodel",
        "resourcepack/assets/worldsmith/models/item/krovotok.json",
        "resourcepack/assets/worldsmith/textures/item/krovotok.png",
        "resourcepack/assets/worldsmith/textures/item/krovotok.png.mcmeta",
        "resourcepack/assets/worldsmith/particles/krovotok_blood_mist.json",
        "resourcepack/assets/worldsmith/particles/krovotok_blood_spark.json",
        "resourcepack/assets/worldsmith/particles/krovotok_blood_pulse.json",
        "resourcepack/assets/worldsmith/particles/krovotok_blood_burst.json",
        "resourcepack/assets/worldsmith/particles/krovotok_life_drain.json",
    }
    with zipfile.ZipFile(zip_path) as archive:
        bad_member = archive.testzip()
        if bad_member is not None:
            raise ValueError(f"Corrupt ZIP member: {bad_member}")
        members = set(archive.namelist())
        missing = sorted(required - members)
        if missing:
            raise ValueError("Missing required ZIP members: " + ", ".join(missing))
        model = json.loads(archive.read("krovotok_JE_1.20.1.bbmodel").decode("utf-8"))
        elements = model.get("elements", [])
        if len(elements) != 170:
            raise ValueError(f"BBModel contains {len(elements)} elements instead of 170")
        print(f"ZIP members: {len(members)}; BBModel elements: {len(elements)}")


def main() -> int:
    parser = argparse.ArgumentParser(description="Repair and verify the Krovotok source archive")
    parser.add_argument("--extract", action="store_true", help="extract verified archive to unpacked/")
    arguments = parser.parse_args()

    patch_data = json.loads(PATCH_FILE.read_text(encoding="utf-8"))
    pieces: list[str] = []
    for name in ORDERED_PIECES:
        source = read_piece(name)
        repaired = repair_piece(name, source, patch_data)
        actual_sha = git_blob_sha(repaired)
        expected_sha = EXPECTED_PIECE_SHAS[name]
        print(f"{name}: source={len(source)}, repaired={len(repaired)}, sha={actual_sha}")
        if actual_sha != expected_sha:
            raise ValueError(f"{name}: repaired Git blob SHA mismatch; expected {expected_sha}")
        pieces.append(repaired)

    encoded = "".join(pieces)
    if len(encoded) != EXPECTED_B64_LENGTH:
        raise ValueError(f"Base64 length {len(encoded)} != {EXPECTED_B64_LENGTH}")
    archive_bytes = base64.b64decode(encoded, validate=True)
    actual_sha256 = hashlib.sha256(archive_bytes).hexdigest()
    if actual_sha256 != EXPECTED_SHA256:
        raise ValueError(f"ZIP SHA-256 {actual_sha256} != {EXPECTED_SHA256}")

    b64_path = TASK_DIR / "Krovotok_complete_v2_with_particles.zip.b64"
    zip_path = TASK_DIR / "Krovotok_complete_v2_with_particles.zip"
    b64_path.write_text(encoded, encoding="utf-8")
    zip_path.write_bytes(archive_bytes)
    verify_zip(zip_path)

    if arguments.extract:
        output = TASK_DIR / "unpacked"
        shutil.rmtree(output, ignore_errors=True)
        output.mkdir(parents=True)
        with zipfile.ZipFile(zip_path) as archive:
            archive.extractall(output)
        print(f"Extracted verified assets to: {output}")

    print(f"Krovotok archive verified: {actual_sha256}")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as error:
        print(f"ERROR: {error}", file=sys.stderr)
        raise SystemExit(1)
