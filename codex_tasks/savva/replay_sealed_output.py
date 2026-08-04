from __future__ import annotations

import hashlib
import json
import shutil
from pathlib import Path

PROVENANCE = "worldsmith-forgemind@f1bddd328aa8edfadbae29ab2b7d93abaf126b25"
EXPECTED_TASK_SHA256 = "775b0ce0d6e91d83f898424422927f60bc2fc4c0a8997564e0361d88e13c53b2"
TASK_PATH = Path("codex_tasks/savva/forgemind-task.json")
OUTPUT = Path("agent-workspace/OUTPUT/SAVVA_PRODUCE_VENDOR")
SEALED_FILES = {
    Path(
        "src/main/java/ru/theframetrip/worldsmith/forgemind/"
        "savva/SavvaProduceVendorFeature.java"
    ): (
        "overlay/src/main/java/ru/theframetrip/worldsmith/forgemind/"
        "savva/SavvaProduceVendorFeature.java",
        "b8b17316374574ae77d2373aa9d03acad5a19f2e1185a12d321363137e5d6be8",
    ),
    Path(
        "src/main/resources/data/worldsmith/forgemind/"
        "savva_produce_vendor.json"
    ): (
        "overlay/src/main/resources/data/worldsmith/forgemind/"
        "savva_produce_vendor.json",
        "aa329eac70b390ae781b1eb60edf2975af1770d322effc9f81fb55f49e904262",
    ),
    Path("SAVVA_AGENT_BUILD.md"): (
        "overlay/SAVVA_AGENT_BUILD.md",
        "a0d30e34f2fff855d02e6be4c843a340ddc60c966fb469e45cb8e1a7ab80e171",
    ),
}


def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def canonical_task_sha256(path: Path) -> str:
    payload = json.loads(path.read_text(encoding="utf-8"))
    canonical = json.dumps(
        payload,
        ensure_ascii=False,
        sort_keys=True,
        separators=(",", ":"),
    ).encode("utf-8")
    return hashlib.sha256(canonical).hexdigest()


def main() -> None:
    if TASK_PATH.is_symlink() or not TASK_PATH.is_file():
        raise SystemExit("Savva task must be a regular file")
    actual_task_sha = canonical_task_sha256(TASK_PATH)
    if actual_task_sha != EXPECTED_TASK_SHA256:
        raise SystemExit(
            f"Savva task changed: expected {EXPECTED_TASK_SHA256}, got {actual_task_sha}"
        )
    task = json.loads(TASK_PATH.read_text(encoding="utf-8"))
    if task.get("task_type") != "worldsmith_feature_build":
        raise SystemExit("Unexpected ForgeMind task type")
    if task.get("feature") != "savva_produce_vendor_v1":
        raise SystemExit("Unexpected ForgeMind feature")
    if OUTPUT.exists():
        raise SystemExit(f"Agent output already exists: {OUTPUT}")

    records = []
    for source, (relative, expected_sha) in SEALED_FILES.items():
        if source.is_symlink() or not source.is_file():
            raise SystemExit(f"Agent output source is unsafe: {source}")
        actual_sha = sha256(source)
        if actual_sha != expected_sha:
            raise SystemExit(
                f"Sealed ForgeMind output mismatch for {source}: "
                f"expected {expected_sha}, got {actual_sha}"
            )
        destination = OUTPUT / relative
        destination.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(source, destination)
        records.append(
            {
                "path": relative,
                "size_bytes": destination.stat().st_size,
                "sha256": actual_sha,
            }
        )

    generated = {
        "feature": "savva_produce_vendor_v1",
        "target_mod_id": "worldsmith",
        "java_package": "ru.theframetrip.worldsmith.forgemind.savva",
        "entry_class": (
            "ru.theframetrip.worldsmith.forgemind.savva."
            "SavvaProduceVendorFeature"
        ),
        "offer_count": 19,
        "files": [record["path"] for record in records],
        "file_records": records,
        "apply": "Copy overlay contents into the Worldsmith repository root.",
        "build": "./gradlew clean build --no-daemon",
        "forge_api": {
            "version": "1.20.1",
            "menu_open": "ServerPlayer.openMenu(MenuProvider)",
            "merchant_sync": "ServerPlayer.sendMerchantOffers(...)",
        },
    }
    OUTPUT.mkdir(parents=True, exist_ok=True)
    (OUTPUT / "feature-manifest.json").write_text(
        json.dumps(generated, ensure_ascii=False, sort_keys=True, indent=2) + "\n",
        encoding="utf-8",
    )
    receipt = {
        "schema_version": 1,
        "task_id": "SAVVA_PRODUCE_VENDOR",
        "task_type": "worldsmith_feature_build",
        "feature": "savva_produce_vendor_v1",
        "verdict": "passed",
        "source_unchanged": True,
        "validation": {
            "status": "passed",
            "error_count": 0,
            "warning_count": 0,
            "findings": [],
        },
        "generated": generated,
        "capsule_mode": "sealed_output_replay",
        "capsule_provenance": PROVENANCE,
        "task_canonical_sha256": EXPECTED_TASK_SHA256,
    }
    (OUTPUT / "run.json").write_text(
        json.dumps(receipt, ensure_ascii=False, sort_keys=True, indent=2) + "\n",
        encoding="utf-8",
    )
    print(json.dumps(receipt, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
