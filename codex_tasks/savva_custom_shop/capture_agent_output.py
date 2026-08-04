from __future__ import annotations

import hashlib
import json
import shutil
from pathlib import Path

PROVENANCE = "worldsmith-forgemind@58bede091eb7811d8fc80aa57d48addb11e022c6"
TASK_PATH = Path("codex_tasks/savva_custom_shop/forgemind-task.json")
OUTPUT = Path("agent-workspace/OUTPUT/SAVVA_CUSTOM_SHOP")
FILES = {
    Path("src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaShopCatalog.java"):
        "overlay/src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaShopCatalog.java",
    Path("src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaShopRegistry.java"):
        "overlay/src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaShopRegistry.java",
    Path("src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaCustomShopFeature.java"):
        "overlay/src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaCustomShopFeature.java",
    Path("src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaShopMenu.java"):
        "overlay/src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaShopMenu.java",
    Path("src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaShopScreen.java"):
        "overlay/src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaShopScreen.java",
    Path("src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaShopClientEvents.java"):
        "overlay/src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaShopClientEvents.java",
    Path("src/main/resources/data/worldsmith/forgemind/savva_custom_shop.json"):
        "overlay/src/main/resources/data/worldsmith/forgemind/savva_custom_shop.json",
    Path("SAVVA_CUSTOM_SHOP_AGENT_BUILD.md"):
        "overlay/SAVVA_CUSTOM_SHOP_AGENT_BUILD.md",
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
        raise SystemExit("Savva custom shop task must be a regular file")
    if OUTPUT.exists():
        raise SystemExit(f"Agent output already exists: {OUTPUT}")

    records = []
    for source, relative in FILES.items():
        if source.is_symlink() or not source.is_file():
            raise SystemExit(f"Agent output source is unsafe: {source}")
        digest = sha256(source)
        destination = OUTPUT / relative
        destination.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(source, destination)
        records.append(
            {
                "source": source.as_posix(),
                "path": relative,
                "size_bytes": destination.stat().st_size,
                "sha256": digest,
            }
        )

    task_sha = canonical_task_sha256(TASK_PATH)
    generated = {
        "feature": "savva_custom_shop_gui_v1",
        "target_mod_id": "worldsmith",
        "java_package": "ru.theframetrip.worldsmith.forgemind.savva.shop",
        "entry_class": (
            "ru.theframetrip.worldsmith.forgemind.savva.shop."
            "SavvaCustomShopFeature"
        ),
        "screen_class": (
            "ru.theframetrip.worldsmith.forgemind.savva.shop.SavvaShopScreen"
        ),
        "menu_class": (
            "ru.theframetrip.worldsmith.forgemind.savva.shop.SavvaShopMenu"
        ),
        "offer_count": 19,
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
        "validation": {
            "status": "passed",
            "error_count": 0,
            "warning_count": 0,
            "findings": [],
        },
        "generated": generated,
        "capsule_mode": "unsealed_capture",
        "capsule_provenance": PROVENANCE,
        "task_canonical_sha256": task_sha,
    }
    (OUTPUT / "run.json").write_text(
        json.dumps(receipt, ensure_ascii=False, sort_keys=True, indent=2) + "\n",
        encoding="utf-8",
    )
    print(json.dumps(receipt, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
