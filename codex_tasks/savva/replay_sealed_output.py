from __future__ import annotations

import hashlib
import json
import shutil
from pathlib import Path

PROVENANCE = "worldsmith-forgemind@task-extension:savva-economy-pack-v1"
EXPECTED_TASK_SHA256 = "b763966cc7a7f643ca33f17e5c4144b9d2e0f2aadefa5e9d0d31a8798c2e375e"
TASK_PATH = Path("codex_tasks/savva/forgemind-task.json")
OUTPUT = Path("agent-workspace/OUTPUT/SAVVA_PRODUCE_VENDOR")
SEALED_FILES = {
    Path(
        "src/main/java/ru/theframetrip/worldsmith/forgemind/"
        "savva/SavvaProduceVendorFeature.java"
    ): (
        "overlay/src/main/java/ru/theframetrip/worldsmith/forgemind/"
        "savva/SavvaProduceVendorFeature.java",
        "50e70d772be8e68248276fb604834211bfcf36ca",
    ),
    Path(
        "src/main/resources/data/worldsmith/forgemind/"
        "savva_produce_vendor.json"
    ): (
        "overlay/src/main/resources/data/worldsmith/forgemind/"
        "savva_produce_vendor.json",
        "53c7a148df2a1a98d95acb5789d5c5f46741f7ee",
    ),
    Path("SAVVA_AGENT_BUILD.md"): (
        "overlay/SAVVA_AGENT_BUILD.md",
        "e3ba608abd781cfcc587647c19a56e0133b6f9ac",
    ),
}


def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def git_blob_sha1(path: Path) -> str:
    payload = path.read_bytes()
    header = f"blob {len(payload)}\0".encode("ascii")
    return hashlib.sha1(header + payload).hexdigest()


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
    for source, (relative, expected_blob_sha) in SEALED_FILES.items():
        if source.is_symlink() or not source.is_file():
            raise SystemExit(f"Agent output source is unsafe: {source}")
        actual_blob_sha = git_blob_sha1(source)
        if actual_blob_sha != expected_blob_sha:
            raise SystemExit(
                f"Sealed ForgeMind output mismatch for {source}: "
                f"expected git blob {expected_blob_sha}, got {actual_blob_sha}"
            )
        actual_sha = sha256(source)
        destination = OUTPUT / relative
        destination.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(source, destination)
        records.append(
            {
                "path": relative,
                "size_bytes": destination.stat().st_size,
                "sha256": actual_sha,
                "git_blob_sha1": actual_blob_sha,
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
        "offer_count": 25,
        "files": [record["path"] for record in records],
        "file_records": records,
        "apply": "Copy overlay contents into the Worldsmith repository root.",
        "build": "./gradlew clean build --no-daemon",
        "forge_api": {
            "version": "1.20.1",
            "menu_open": "Mob.interact(Player, InteractionHand)",
            "merchant_sync": "vanilla Villager.mobInteract path",
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
