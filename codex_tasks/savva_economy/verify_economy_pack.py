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
VENDOR_JAVA = ROOT / "src/main/java/ru/theframetrip/worldsmith/forgemind/savva/SavvaProduceVendorFeature.java"
CATALOG_JAVA = ROOT / "src/main/java/ru/theframetrip/worldsmith/forgemind/savva/shop/SavvaShopCatalog.java"
VENDOR_PROFILE = ROOT / "src/main/resources/data/worldsmith/forgemind/savva_produce_vendor.json"
SHOP_PROFILE = ROOT / "src/main/resources/data/worldsmith/forgemind/savva_custom_shop.json"
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
    payload = json.loads(path.read_text(encoding="utf-8"), object_pairs_hook=reject_duplicate_keys)
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
    data = path.read_bytes()
    if len(data) < 24 or data[:8] != b"\x89PNG\r\n\x1a\n" or data[12:16] != b"IHDR":
        raise VerificationError(f"Invalid PNG: {path}")
    return struct.unpack(">II", data[16:24])


def main() -> None:
    task = read_json(TASK)
    if task.get("task_type") != "worldsmith_feature_build" or task.get("feature") != "savva_economy_pack_v1":
        raise VerificationError("Unexpected task contract")
    produce = task.get("produce")
    if not isinstance(produce, list) or len(produce) != 9:
        raise VerificationError("Savva economy must define nine custom foods")
    food_ids = [str(item["item_id"]).split(":", 1)[1] for item in produce]
    expected_food_ids = ["tomato", "lettuce", "cucumber", "strawberry", "blueberry", "raspberry", "pear", "peach", "orange"]
    if food_ids != expected_food_ids:
        raise VerificationError(f"Unexpected food order: {food_ids}")
    offer_count = int(task["vendor_integration"]["offer_count"])
    if offer_count != 37:
        raise VerificationError("Savva economy must define 37 offers")

    items = read_text(ITEMS_JAVA)
    tabs = read_text(TABS_JAVA)
    spawner = read_text(SPAWNER_JAVA)
    vendor = read_text(VENDOR_JAVA)
    catalog = read_text(CATALOG_JAVA)
    require(items, 'ITEMS.register("savva_coin"', "currency registration")
    require(items, 'ITEMS.register("savva_spawner"', "spawner registration")
    for spec in produce:
        item_id = str(spec["item_id"]).split(":", 1)[1]
        saturation = str(spec["saturation"])
        if "." not in saturation:
            saturation += ".0"
        marker = f'registerFood("{item_id}", {spec["nutrition"]}, {saturation}F)'
        require(items, marker, f"food registration {item_id}")

    tab_marker = "public static final RegistryObject<CreativeModeTab> SAVVA_TAB"
    tab_start = tabs.find(tab_marker)
    display_start = tabs.find(".displayItems(", tab_start)
    tab_end = tabs.find(".build());", display_start)
    if min(tab_start, display_start, tab_end) < 0:
        raise VerificationError("Savva creative tab is malformed")
    display = tabs[display_start:tab_end]
    expected_constants = ["SAVVA_SPAWNER", "SAVVA_COIN", *[item.upper() for item in food_ids]]
    positions = [display.find(f"ModItems.{constant}.get()") for constant in expected_constants]
    if any(position < 0 for position in positions) or positions != sorted(positions):
        raise VerificationError("Savva creative tab entries are missing or out of order")

    for marker in ("SavvaProduceVendorFeature.spawnSavva(", "context.getItemInHand().shrink(1)", "getAbilities().instabuild"):
        require(spawner, marker, "Savva spawner behavior")
    require(vendor, "PROFILE_VERSION = 3", "vendor profile version")
    require(vendor, 'CURRENCY_ID = "worldsmith:savva_coin"', "vendor currency")
    require(catalog, 'CURRENCY_ID = "worldsmith:savva_coin"', "shop currency")
    if catalog.count("new Offer(") != offer_count:
        raise VerificationError(f"Savva shop catalog must contain {offer_count} offers")
    for item_id in food_ids:
        resource = f"worldsmith:{item_id}"
        if vendor.count(f'"{resource}"') < 2:
            raise VerificationError(f"Vendor must buy and sell {resource}")
        if catalog.count(f'new ResourceLocation("{resource}")') < 2:
            raise VerificationError(f"Custom shop must buy and sell {resource}")

    for profile_path in (VENDOR_PROFILE, SHOP_PROFILE):
        profile = read_json(profile_path)
        if profile.get("currency_id") != "worldsmith:savva_coin":
            raise VerificationError(f"Wrong profile currency: {profile_path}")
        offers = profile.get("offers")
        if not isinstance(offers, list) or len(offers) != offer_count:
            raise VerificationError(f"Profile must contain {offer_count} offers: {profile_path}")

    model_ids = ["savva_coin", *food_ids, "savva_spawner"]
    for model_id in model_ids:
        model_path = ASSET_ROOT / "models/item" / f"{model_id}.json"
        model = read_json(model_path)
        if model.get("parent") != "minecraft:item/generated":
            raise VerificationError(f"Wrong model parent: {model_path}")
        textures = model.get("textures")
        if not isinstance(textures, dict) or textures.get("layer0") != f"worldsmith:item/{model_id}":
            raise VerificationError(f"Wrong model texture: {model_path}")
        texture_path = ASSET_ROOT / "textures/item" / f"{model_id}.png"
        if png_dimensions(texture_path) != (16, 16):
            raise VerificationError(f"Texture must be 16x16: {texture_path}")

    required_keys = {"itemGroup.worldsmith.savva", "item.worldsmith.savva_coin", "item.worldsmith.savva_spawner", *[f"item.worldsmith.{item}" for item in food_ids]}
    for locale in ("ru_ru", "en_us"):
        language = read_json(ASSET_ROOT / "lang" / f"{locale}.json")
        missing = sorted(required_keys - set(language))
        if missing:
            raise VerificationError(f"Missing {locale} language keys: {missing}")

    print(json.dumps({
        "feature": "savva_economy_pack_v1",
        "status": "passed",
        "custom_food_count": len(food_ids),
        "offer_count": offer_count,
        "items": model_ids,
        "creative_tab": "worldsmith:savva",
        "currency": "worldsmith:savva_coin",
    }, ensure_ascii=False, sort_keys=True, indent=2))


if __name__ == "__main__":
    main()
