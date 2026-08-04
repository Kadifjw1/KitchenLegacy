from __future__ import annotations

import json
import struct
from pathlib import Path
from typing import Any

ROOT = Path(__file__).resolve().parents[2]
TASK = ROOT / "codex_tasks/savva_economy/forgemind-task.json"
ITEMS_JAVA = ROOT / "src/main/java/ru/theframetrip/worldsmith/registry/ModItems.java"
TABS_JAVA = ROOT / "src/main/java/ru/theframetrip/worldsmith/registry/ModCreativeModeTabs.java"
SPAWNER_JAVA = ROOT / "src/main/java/ru/theframetrip/worldsmith/item/SavvaSpawnerItem.java"
VENDOR_JAVA = ROOT / (
    "src/main/java/ru/theframetrip/worldsmith/forgemind/savva/"
    "SavvaProduceVendorFeature.java"
)
CATALOG_JAVA = ROOT / (
    "src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/"
    "SavvaShopCatalog.java"
)
VENDOR_PROFILE = ROOT / (
    "src/main/resources/data/worldsmith/forgemind/savva_produce_vendor.json"
)
SHOP_PROFILE = ROOT / (
    "src/main/resources/data/worldsmith/forgemind/savva_custom_shop.json"
)
ASSET_ROOT = ROOT / "src/main/resources/assets/worldsmith"


class VerificationError(RuntimeError):
    pass


def reject_duplicate_keys(pairs: list[tuple[str, Any]]) -> dict[str, Any]:
    result: dict[str, Any] = {}
    for key, value in pairs:
        if key in result:
            raise VerificationError(f"Duplicate JSON key: {key}")
        result[key] = value
    return result


def read_json(path: Path) -> dict[str, Any]:
    if path.is_symlink() or not path.is_file():
        raise VerificationError(f"Required JSON is missing or unsafe: {path}")
    payload = json.loads(
        path.read_text(encoding="utf-8"),
        object_pairs_hook=reject_duplicate_keys,
    )
    if not isinstance(payload, dict):
        raise VerificationError(f"Expected JSON object: {path}")
    return payload


def read_text(path: Path) -> str:
    if path.is_symlink() or not path.is_file():
        raise VerificationError(f"Required text file is missing or unsafe: {path}")
    return path.read_text(encoding="utf-8")


def require(text: str, marker: str, label: str) -> None:
    if marker not in text:
        raise VerificationError(f"Missing {label}: {marker}")


def png_dimensions(path: Path) -> tuple[int, int]:
    if path.is_symlink() or not path.is_file():
        raise VerificationError(f"PNG is missing or unsafe: {path}")
    data = path.read_bytes()
    if len(data) < 24 or data[:8] != b"\x89PNG\r\n\x1a\n":
        raise VerificationError(f"Invalid PNG signature: {path}")
    if data[12:16] != b"IHDR":
        raise VerificationError(f"PNG has no leading IHDR: {path}")
    return struct.unpack(">II", data[16:24])


def main() -> None:
    task = read_json(TASK)
    if task.get("task_type") != "worldsmith_feature_build":
        raise VerificationError("Unexpected task type")
    if task.get("feature") != "savva_economy_pack_v1":
        raise VerificationError("Unexpected feature ID")

    items = read_text(ITEMS_JAVA)
    tabs = read_text(TABS_JAVA)
    spawner = read_text(SPAWNER_JAVA)
    vendor = read_text(VENDOR_JAVA)
    catalog = read_text(CATALOG_JAVA)

    registrations = {
        "savva_coin": 'ITEMS.register("savva_coin"',
        "tomato": 'ITEMS.register("tomato"',
        "lettuce": 'ITEMS.register("lettuce"',
        "cucumber": 'ITEMS.register("cucumber"',
        "savva_spawner": 'ITEMS.register("savva_spawner"',
    }
    for item_id, marker in registrations.items():
        require(items, marker, f"item registration {item_id}")

    for marker in (
        ".nutrition(3).saturationMod(0.3F)",
        ".nutrition(1).saturationMod(0.2F)",
        ".nutrition(2).saturationMod(0.25F)",
        "new Item.Properties().stacksTo(16)",
    ):
        require(items, marker, "item property")

    tab_marker = "public static final RegistryObject<CreativeModeTab> SAVVA_TAB"
    tab_start = tabs.find(tab_marker)
    if tab_start < 0:
        raise VerificationError("Savva creative tab is missing")
    display_start = tabs.find(".displayItems(", tab_start)
    tab_end = tabs.find(".build());", display_start)
    if display_start < 0 or tab_end < 0:
        raise VerificationError("Savva creative tab displayItems block is malformed")
    savva_display = tabs[display_start:tab_end]
    expected_order = [
        "ModItems.SAVVA_SPAWNER.get()",
        "ModItems.SAVVA_COIN.get()",
        "ModItems.TOMATO.get()",
        "ModItems.LETTUCE.get()",
        "ModItems.CUCUMBER.get()",
    ]
    positions = [savva_display.find(marker) for marker in expected_order]
    if any(position < 0 for position in positions) or positions != sorted(positions):
        raise VerificationError("Savva creative tab entries are missing or out of order")

    for marker in (
        "SavvaProduceVendorFeature.spawnSavva(",
        "Vec3.atBottomCenterOf(spawnPos)",
        "context.getItemInHand().shrink(1)",
        "getAbilities().instabuild",
    ):
        require(spawner, marker, "Savva spawner behavior")

    require(vendor, 'CURRENCY_ID = "worldsmith:savva_coin"', "vendor currency")
    require(catalog, 'CURRENCY_ID = "worldsmith:savva_coin"', "shop currency")
    if catalog.count("new Offer(") != 25:
        raise VerificationError("Savva shop catalog must contain exactly 25 offers")
    for item_id in ("worldsmith:tomato", "worldsmith:lettuce", "worldsmith:cucumber"):
        if vendor.count(f'"{item_id}"') < 2:
            raise VerificationError(f"Vendor must buy and sell {item_id}")
        if catalog.count(f'new ResourceLocation("{item_id}")') < 2:
            raise VerificationError(f"Custom shop must buy and sell {item_id}")

    for profile_path in (VENDOR_PROFILE, SHOP_PROFILE):
        profile = read_json(profile_path)
        if profile.get("currency_id") != "worldsmith:savva_coin":
            raise VerificationError(f"Wrong profile currency: {profile_path}")
        offers = profile.get("offers")
        if not isinstance(offers, list) or len(offers) != 25:
            raise VerificationError(f"Profile must contain 25 offers: {profile_path}")

    model_ids = ["savva_coin", "tomato", "lettuce", "cucumber", "savva_spawner"]
    for model_id in model_ids:
        model_path = ASSET_ROOT / "models/item" / f"{model_id}.json"
        model = read_json(model_path)
        expected_texture = f"worldsmith:item/{model_id}"
        if model.get("parent") != "minecraft:item/generated":
            raise VerificationError(f"Wrong model parent: {model_path}")
        textures = model.get("textures")
        if not isinstance(textures, dict) or textures.get("layer0") != expected_texture:
            raise VerificationError(f"Wrong model texture: {model_path}")
        texture_path = ASSET_ROOT / "textures/item" / f"{model_id}.png"
        if png_dimensions(texture_path) != (16, 16):
            raise VerificationError(f"Texture must be 16x16: {texture_path}")

    required_language_keys = {
        "itemGroup.worldsmith.savva",
        "item.worldsmith.savva_coin",
        "item.worldsmith.tomato",
        "item.worldsmith.lettuce",
        "item.worldsmith.cucumber",
        "item.worldsmith.savva_spawner",
    }
    for locale in ("ru_ru", "en_us"):
        language = read_json(ASSET_ROOT / "lang" / f"{locale}.json")
        missing = sorted(required_language_keys - set(language))
        if missing:
            raise VerificationError(f"Missing {locale} language keys: {missing}")

    result = {
        "feature": "savva_economy_pack_v1",
        "status": "passed",
        "items": model_ids,
        "offer_count": 25,
        "texture_size": [16, 16],
        "creative_tab": "worldsmith:savva",
        "currency": "worldsmith:savva_coin",
    }
    print(json.dumps(result, ensure_ascii=False, sort_keys=True, indent=2))


if __name__ == "__main__":
    main()
