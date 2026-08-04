# ForgeMind — текстуры овощей Саввы

Feature: `minecraft_food_item_texture_pack_v1`

Task: `SAVVA_FOOD_TEXTURES`

## Что изменено

ForgeMind 1.5.0 самостоятельно сгенерировал и проверил новые оригинальные item-текстуры 16×16:

- `worldsmith:tomato`;
- `worldsmith:lettuce`;
- `worldsmith:cucumber`.

В репозиторий перенесены PNG непосредственно из каталога
`OUTPUT/SAVVA_FOOD_TEXTURES/overlay`, созданного командой:

```bash
forgemind-agent run SAVVA_FOOD_TEXTURES --local-root agent-workspace
```

## Визуальный профиль

Использован профиль `vanilla_plus_food_mod`:

- компактные и сразу читаемые силуэты;
- цветной, а не чисто чёрный контур;
- ограниченная палитра;
- сдержанные блики;
- различимая форма каждого овоща;
- прозрачный фон и отсутствие полупрозрачных пикселей.

Farmer's Delight, Croptopia и Pam's HarvestCraft 2 использовались только для анализа общих визуальных принципов. Пиксельные раскладки и палитры сторонних модов не копировались.

## Агентский результат

- workflow run: `30898446399`;
- artifact: `forgemind-savva-food-textures`;
- artifact digest: `sha256:e1594139f21f8cfb43c620f46c09ec5267f918bb61e70087966d2606238171e7`;
- verdict: `passed`;
- source unchanged: `true`.

## Проверка

```bash
python codex_tasks/savva_food_textures/verify_agent_output.py
./gradlew clean build --no-daemon
```

CI проверяет точные file SHA-256 и pixel SHA-256, размер 16×16, RGBA,
отсутствие полупрозрачности, размер палитры, число непрозрачных пикселей и
наличие неизменённых PNG внутри итогового JAR.

## Честная граница

Эта версия улучшает только инвентарные иконки предметов. Растения в мире,
семена, стадии роста и 3D-модели урожая остаются отдельным этапом.
