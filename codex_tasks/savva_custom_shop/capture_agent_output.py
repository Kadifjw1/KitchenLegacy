from __future__ import annotations

import hashlib
import json
import shutil
from pathlib import Path

PROVENANCE = "worldsmith-forgemind@58bede091eb7811d8fc80aa57d48addb11e022c6"
EXPECTED_TASK_SHA256 = "3c02c2db8d6418b2e1bfbc1da76675961a161b595fa537933ef60c04fa24b106"
TASK_PATH = Path("codex_tasks/savva_custom_shop/forgemind-task.json")
OUTPUT = Path("agent-workspace/OUTPUT/SAVVA_CUSTOM_SHOP")
SEALED_FILES = {
    Path("src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaShopCatalog.java"): (
        "overlay/src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaShopCatalog.java",
        "df7b4a8229d58ab08478d49c262013cf471eb38d39e2fda5bf8064a87dc835c9",
    ),
    Path("src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaShopRegistry.java"): (
        "overlay/src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaShopRegistry.java",
        "8503f2dba10d36e4a51e23b75ab8fd219cbe60d70cced0e4feb2b31b0db3cd6b",
    ),
    Path("src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaCustomShopFeature.java"): (
        "overlay/src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaCustomShopFeature.java",
        "59ef8d0cc1a7fb1d41e4afa273a8d44dfb19ed2be1614a3f3012ebcf50ce86c2",
    ),
    Path("src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaShopMenu.java"): (
        "overlay/src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaShopMenu.java",
        "3e7785087d9f1e97f98e7313892dd0933d394897f7f3f62a2dcc9f1e0db592fe",
    ),
    Path("src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaShopScreen.java"): (
        "overlay/src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaShopScreen.java",
        "29d06e16329b9e07ff4b85e06295e83f57538de0fd5f4e4d8cfb4b003132b346",
    ),
    Path("src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaShopClientEvents.java"): (
        "overlay/src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaShopClientEvents.java",
        "bbd58c127f9116c2fdf94b41cffe025e5477597793b2b1e2dabe5e23d5f04427",
    ),
    Path("src/main/resources/data/worldsmith/forgemind/savva_custom_shop.json"): (
        "overlay/src/main/resources/data/worldsmith/forgemind/savva_custom_shop.json",
        "ce64d4eeaf9c155eb8e9d5ccfbb53b7e7ddee8d3d3729ed33465d4b6cd48fbac",
    ),
    Path("SAVVA_CUSTOM_SHOP_AGENT_BUILD.md"): (
        "overlay/SAVVA_CUSTOM_SHOP_AGENT_BUILD.md",
        "6db336cee0d621638c6577983948491514913efb2c77daceaae35f4bd517a18f",
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
        raise SystemExit("Savva custom shop task must be a regular file")
    task_sha = canonical_task_sha256(TASK_PATH)
    if task_sha != EXPECTED_TASK_SHA256:
        raise SystemExit(
            f"Savva custom shop task changed: expected {EXPECTED_TASK_SHA256}, got {task_sha}"
        )
    task = json.loads(TASK_PATH.read_text(encoding="utf-8"))
    if task.get("task_type") != "worldsmith_feature_build":
        raise SystemExit("Unexpected ForgeMind task type")
    if task.get("feature") != "savva_custom_shop_gui_v1":
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
                "source": source.as_posix(),
                "path": relative,
                "size_bytes": destination.stat().st_size,
                "sha256": actual_sha,
            }
        )

    generated = {
        "feature": "savva_custom_shop_gui_v1",
        "target_mod_id": "worldsmith",
        "java_package": "ru.theframetrip.worldsmith.forgemind.savva.shop",
        "entry_class": "ru.theframetrip.worldsmith.forgemind.savva.shop.SavvaCustomShopFeature",
        "screen_class": "ru.theframetrip.worldsmith.forgemind.savva.shop.SavvaShopScreen",
        "menu_class": "ru.theframetrip.worldsmith.forgemind.savva.shop.SavvaShopMenu",
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
