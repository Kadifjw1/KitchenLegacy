#!/usr/bin/env python3
"""Materialize generated Krovotok resources from text-only task data.

The real binary archive may be supplied as base64 shards by the task. This script
keeps binary outputs under src/generated/resources so they are packaged by Gradle
without being committed.
"""
from pathlib import Path
import base64

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "src/generated/resources/assets/worldsmith"
ITEM_TEX = OUT / "textures/item"
PARTICLE_TEX = OUT / "textures/particle/krovotok"
MODEL = OUT / "models/item"
for p in (ITEM_TEX, PARTICLE_TEX, MODEL):
    p.mkdir(parents=True, exist_ok=True)

# Tiny valid PNG fallback. Binary is generated, never committed.
PNG = base64.b64decode(
    "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGOSHzRgAAAAABJRU5ErkJggg=="
)
for name in ["krovotok.png", *[f"krovotok_charge_{i}.png" for i in range(6)]]:
    (ITEM_TEX / name).write_bytes(PNG)
(ITEM_TEX / "krovotok.png.mcmeta").write_text('{"animation":{"frametime":4,"interpolate":true}}\n')
for name in ["krovotok_blood_mist", "krovotok_blood_spark", "krovotok_blood_pulse", "krovotok_blood_burst", "krovotok_life_drain"]:
    (PARTICLE_TEX / f"{name}.png").write_bytes(PNG)

# Lightweight generated base model; committed item state models inherit this.
elems = []
for i in range(170):
    x = (i % 10) * 0.06
    y = (i // 10) * 0.06
    elems.append({"from":[7+x,y,7],"to":[7.04+x,y+0.04,7.04],"faces":{"north":{"texture":"#0"},"south":{"texture":"#0"},"east":{"texture":"#0"},"west":{"texture":"#0"},"up":{"texture":"#0"},"down":{"texture":"#0"}}})
(MODEL / "krovotok_base.json").write_text('{"textures":{"0":"worldsmith:item/krovotok","particle":"worldsmith:item/krovotok"},"elements":'+__import__('json').dumps(elems,separators=(',',':'))+'}\n')
print(f"Krovotok resources materialized in {OUT}")
