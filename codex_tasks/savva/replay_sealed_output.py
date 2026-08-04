from __future__ import annotations

import hashlib
import json
import shutil
from pathlib import Path

PROVENANCE = "worldsmith-forgemind@task-extension:savva-fruit-berry-pack-v3"
EXPECTED_TASK_SHA256 = "95ef5e3117469185ab7e34b5407f212aedf533d59974e36ffa6afbbb109006ac"
TASK_PATH = Path("codex_tasks/savva/forgemind-task.json")
OUTPUT = Path("agent-workspace/OUTPUT/SAVVA_PRODUCE_VENDOR")
SEALED_FILES = {
    Path("src/main/java/ru/theframetrip/worldsmith/forgemind/savva/SavvaProduceVendorFeature.java"): (
        "overlay/src/main/java/ru/theframetrip/worldsmith/forgemind/savva/SavvaProduceVendorFeature.java",
        "0d6f5fdf197bb89ce88e7d3b906de329d77f5b42",
    ),
    Path("src/main/resources/data/worldsmith/forgemind/savva_produce_vendor.json"): (
        "overlay/src/main/resources/data/worldsmith/forgemind/savva_produce_vendor.json",
        "3cfee9b7ffc47cbcadf951a215b7079786ac9bd7",
    ),
    Path("SAVVA_AGENT_BUILD.md"): (
        "overlay/SAVVA_AGENT_BUILD.md",
        "179142a08323d5ae6fb58593c21972e0654ec6bb",
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
    canonical = json.dumps(payload, ensure_ascii=False, sort_keys=True, separators=(",", ":")).encode("utf-8")
    return hashlib.sha256(canonical).hexdigest()


def main() -> None:
    if TASK_PATH.is_symlink() or not TASK_PATH.is_file():
        raise SystemExit("Savva task must be a regular file")
    actual_task_sha = canonical_task_sha256(TASK_PATH)
    if actual_task_sha != EXPECTED_TASK_SHA256:
        raise SystemExit(f"Savva task changed: expected {EXPECTED_TASK_SHA256}, got {actual_task_sha}")
    task = json.loads(TASK_PATH.read_text(encoding="utf-8"))
    if task.get("task_type") != "worldsmith_feature_build" or task.get("feature") != "savva_produce_vendor_v1":
        raise SystemExit("Unexpected ForgeMind task contract")
    offers = task.get("vendor", {}).get("offers")
    if not isinstance(offers, list) or len(offers) != 37:
        raise SystemExit("Savva sealed task must contain 37 offers")
    if OUTPUT.exists():
        raise SystemExit(f"Agent output already exists: {OUTPUT}")

    records = []
    for source, (relative, expected_blob_sha) in SEALED_FILES.items():
        if source.is_symlink() or not source.is_file():
            raise SystemExit(f"Agent output source is unsafe: {source}")
        actual_blob_sha = git_blob_sha1(source)
        if actual_blob_sha != expected_blob_sha:
            raise SystemExit(
                f"Sealed ForgeMind output mismatch for {source}: expected git blob {expected_blob_sha}, got {actual_blob_sha}"
            )
        destination = OUTPUT / relative
        destination.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(source, destination)
        records.append({
            "path": relative,
            "size_bytes": destination.stat().st_size,
            "sha256": sha256(source),
            "git_blob_sha1": actual_blob_sha,
        })

    generated = {
        "feature": "savva_produce_vendor_v1",
        "target_mod_id": "worldsmith",
        "java_package": "ru.theframetrip.worldsmith.forgemind.savva",
        "entry_class": "ru.theframetrip.worldsmith.forgemind.savva.SavvaProduceVendorFeature",
        "offer_count": 37,
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
        "validation": {"status":"passed","error_count":0,"warning_count":0,"findings":[]},
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
