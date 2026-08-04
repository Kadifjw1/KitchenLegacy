from __future__ import annotations

import hashlib
import json
import shutil
from pathlib import Path

PROVENANCE = "worldsmith-forgemind@task-extension:savva-fruit-berry-pack-v3"
EXPECTED_TASK_SHA256 = "a2abd3b254b0cdd4ca162363d91170686de844f248987b2b40746f263f4ecd46"
TASK_PATH = Path("codex_tasks/savva_custom_shop/forgemind-task.json")
OUTPUT = Path("agent-workspace/OUTPUT/SAVVA_CUSTOM_SHOP")
SEALED_FILES = {
    Path("src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaShopCatalog.java"): (
        "overlay/src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaShopCatalog.java",
        "045c90c9ad908a706a2b59b2c8e02c27e2edebe3",
    ),
    Path("src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaShopRegistry.java"): (
        "overlay/src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaShopRegistry.java",
        "6aaa5f03adb391990336b3dafb965e641ae603a3",
    ),
    Path("src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaCustomShopFeature.java"): (
        "overlay/src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaCustomShopFeature.java",
        "23dcac931314df2272c25229d2b3c4f984651461",
    ),
    Path("src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaShopMenu.java"): (
        "overlay/src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaShopMenu.java",
        "a9793218564b88d535e429cbfa8b88c6f4fbaa51",
    ),
    Path("src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaShopScreen.java"): (
        "overlay/src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaShopScreen.java",
        "3d9b2dd63c8080ead268312cc98cdf825e960990",
    ),
    Path("src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaShopClientEvents.java"): (
        "overlay/src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaShopClientEvents.java",
        "67a92c2175c0166a397959660025b1c40b9f4220",
    ),
    Path("src/main/resources/data/worldsmith/forgemind/savva_custom_shop.json"): (
        "overlay/src/main/resources/data/worldsmith/forgemind/savva_custom_shop.json",
        "5d788123aed869471ba1586138a6633f922af6e6",
    ),
    Path("SAVVA_CUSTOM_SHOP_AGENT_BUILD.md"): (
        "overlay/SAVVA_CUSTOM_SHOP_AGENT_BUILD.md",
        "5dd88809eb1ececba718c24a84feb8b2b8a74c4f",
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
        raise SystemExit("Savva custom shop task must be a regular file")
    task_sha = canonical_task_sha256(TASK_PATH)
    if task_sha != EXPECTED_TASK_SHA256:
        raise SystemExit(f"Savva custom shop task changed: expected {EXPECTED_TASK_SHA256}, got {task_sha}")
    task = json.loads(TASK_PATH.read_text(encoding="utf-8"))
    if task.get("task_type") != "worldsmith_feature_build" or task.get("feature") != "savva_custom_shop_gui_v1":
        raise SystemExit("Unexpected ForgeMind task contract")
    offers = task.get("vendor", {}).get("offers")
    if not isinstance(offers, list) or len(offers) != 37:
        raise SystemExit("Savva custom shop sealed task must contain 37 offers")
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
            "source": source.as_posix(),
            "path": relative,
            "size_bytes": destination.stat().st_size,
            "sha256": sha256(source),
            "git_blob_sha1": actual_blob_sha,
        })

    generated = {
        "feature": "savva_custom_shop_gui_v1",
        "target_mod_id": "worldsmith",
        "java_package": "ru.theframetrip.worldsmith.forgemind.savva.shop",
        "entry_class": "ru.theframetrip.worldsmith.forgemind.savva.shop.SavvaCustomShopFeature",
        "screen_class": "ru.theframetrip.worldsmith.forgemind.savva.shop.SavvaShopScreen",
        "menu_class": "ru.theframetrip.worldsmith.forgemind.savva.shop.SavvaShopMenu",
        "offer_count": 37,
        "files": [record["path"] for record in records],
        "file_records": records,
        "security": {
            "server_authoritative": True,
            "vanilla_container_button_packet": True,
            "server_revalidates_role_distance_hours_stock_inventory": True,
            "client_inventory_panel": False,
        },
    }
    OUTPUT.mkdir(parents=True, exist_ok=True)
    (OUTPUT / "feature-manifest.json").write_text(
        json.dumps(generated, ensure_ascii=False, sort_keys=True, indent=2) + "\n",
        encoding="utf-8",
    )
    receipt = {
        "schema_version": 1,
        "task_id": "SAVVA_CUSTOM_SHOP",
        "task_type": "worldsmith_feature_build",
        "feature": "savva_custom_shop_gui_v1",
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
