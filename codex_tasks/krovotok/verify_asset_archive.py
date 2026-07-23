#!/usr/bin/env python3
from pathlib import Path
import sys
root = Path(__file__).resolve().parents[2]
required = [
 'models/item/krovotok_base.json','textures/item/krovotok.png','textures/item/krovotok.png.mcmeta',
 *[f'textures/item/krovotok_charge_{i}.png' for i in range(6)],
 *[f'textures/particle/krovotok/{p}.png' for p in ['krovotok_blood_mist','krovotok_blood_spark','krovotok_blood_pulse','krovotok_blood_burst','krovotok_life_drain']],
]
base = root/'src/generated/resources/assets/worldsmith'
missing = [p for p in required if not (base/p).exists()]
if missing:
    print('Missing generated Krovotok assets:', *missing, sep='\n- ', file=sys.stderr)
    sys.exit(1)
print('Krovotok generated assets are present.')
