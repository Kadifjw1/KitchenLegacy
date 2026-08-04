# ForgeMind — овощи, ягоды и фрукты Саввы

Feature: `minecraft_food_item_texture_pack_v1`

Task: `SAVVA_FOOD_TEXTURES`

ForgeMind 1.6.0 самостоятельно сгенерировал и проверил девять оригинальных item-текстур 16×16.

## Овощи

- `worldsmith:tomato`;
- `worldsmith:lettuce`;
- `worldsmith:cucumber`.

## Ягоды

- `worldsmith:strawberry`;
- `worldsmith:blueberry`;
- `worldsmith:raspberry`.

## Фрукты

- `worldsmith:pear`;
- `worldsmith:peach`;
- `worldsmith:orange`.

PNG перенесены непосредственно из `OUTPUT/SAVVA_FOOD_TEXTURES/overlay`, созданного командой:

```bash
forgemind-agent run SAVVA_FOOD_TEXTURES --local-root agent-workspace
```

## Визуальный профиль

Использован `vanilla_plus_food_mod`: компактный читаемый силуэт, цветной контур, ограниченная палитра, сдержанные блики, прозрачный фон и отсутствие полупрозрачных пикселей. Сторонние food-моды использовались только для анализа общих принципов; их пиксели и палитры не копировались.

## Агентский результат

- ForgeMind: `1.6.0`;
- workflow run: `30907916695`;
- artifact: `forgemind-savva-food-textures`;
- artifact digest: `sha256:4bf6b662ff403c8ce3e4e6ad833813171bd5046d78506f6ebf6db2010463686f`;
- verdict: `passed`;
- source unchanged: `true`.

## Проверка

```bash
python codex_tasks/savva_food_textures/verify_agent_output.py
python codex_tasks/savva_economy/verify_economy_pack.py
./gradlew clean build --no-daemon
```

CI проверяет file SHA-256 и pixel SHA-256 всех девяти PNG, размер 16×16, RGBA, прозрачность, палитру, метрики силуэта, модели предметов, локализацию, вкладку Саввы, 37 торговых предложений и наличие ресурсов внутри итогового JAR.

## Честная граница

Сейчас это съедобные предметы и торговые товары. Семена, кусты, деревья и стадии выращивания остаются отдельным следующим этапом.
