# Кровоток — техническое задание для Codex

## Контекст проекта

- Репозиторий: `Kadifjw1/KitchenLegacy`
- Minecraft: Java Edition 1.20.1
- Forge: 47.4.20
- Java: 17
- Mod ID / namespace: `worldsmith`
- Основной Java package: `ru.theframetrip.worldsmith`
- Работать поверх текущей ветки задачи, не удаляя и не ломая меч `Предел` и способность `Разлом`.

## Цель

Полностью интегрировать новый меч `worldsmith:krovotok` («Кровоток»): модель, анимированную текстуру, шесть состояний заряда, пять типов частиц и пассивную способность «Багровый ритм».

Исходные материалы находятся в:

- `codex_tasks/krovotok/assets/Krovotok_complete_v2_with_particles.zip.b64`
- `codex_tasks/krovotok/assets/krovotok_JE_1.20.1.bbmodel`

Перед работой выполнить:

```bash
bash codex_tasks/krovotok/decode_assets.sh
```

Архив будет распакован в `codex_tasks/krovotok/unpacked/`.

## Важные ограничения

1. Не менять геометрию меча. Исходный `.bbmodel` содержит 170 элементов.
2. Не использовать package `com.worldsmith.*` из черновых файлов архива. Всё адаптировать под `ru.theframetrip.worldsmith.*`.
3. Не создавать второй независимый реестр предметов или частиц. Расширять существующие:
   - `ru.theframetrip.worldsmith.registry.ModItems`
   - `ru.theframetrip.worldsmith.registry.ModParticleTypes`
   - `ru.theframetrip.worldsmith.client.ClientModEvents`
4. Механика урона и зарядов должна быть серверной. Клиент отвечает только за рендер и визуальные эффекты.
5. Не использовать GeckoLib для меча: это обычная Java item model.
6. Не оставлять namespace `kitchenlegacy` в новых ресурсах Кровотока.

## Ресурсы меча

Скопировать и привести к рабочей структуре:

- `resourcepack/assets/worldsmith/models/item/krovotok.json`
  → `src/main/resources/assets/worldsmith/models/item/krovotok_base.json`
- `resourcepack/assets/worldsmith/textures/item/krovotok.png`
  → `src/main/resources/assets/worldsmith/textures/item/krovotok.png`
- `resourcepack/assets/worldsmith/textures/item/krovotok.png.mcmeta`
  → рядом с текстурой
- `krovotok_charge_0.png` … `krovotok_charge_5.png`
  → `src/main/resources/assets/worldsmith/textures/item/`

Создать основную модель `assets/worldsmith/models/item/krovotok.json` с parent `worldsmith:item/krovotok_base` и overrides для шести состояний заряда. Зарегистрировать item property:

- ResourceLocation: `worldsmith:krovotok_charge`
- Значения: `0.0`, `0.2`, `0.4`, `0.6`, `0.8`, `1.0`

Создать модели `krovotok_charge_0.json` … `krovotok_charge_5.json`, наследующие `krovotok_base` и подменяющие texture `0` и `particle` на соответствующую текстуру.

## Регистрация предмета

В `ModItems` добавить:

```java
public static final RegistryObject<Item> KROVOTOK = ITEMS.register("krovotok", ...);
```

Создать `ru.theframetrip.worldsmith.item.KrovotokItem`.

Базовые параметры:

- Tier: `Tiers.IRON`
- attackDamageModifier: `7`
- attackSpeedModifier: `-2.6F`
- durability берётся из tier, без отдельной кастомной прочности

Добавить меч в существующую вкладку Worldsmith.

## Способность «Багровый ритм»

### Данные ItemStack

Хранить в NBT самого меча:

- `KrovotokCharge`: int, диапазон 0–5
- `KrovotokLastHitGameTime`: long

### Накопление

- Заряд выдаётся только при успешном ударе живой сущности этим мечом.
- Максимум: 5.
- Окно серии: 60 тиков (3 секунды) между успешными ударами.
- Если следующий удар сделан позже 60 тиков, перед обработкой сбросить заряд до 0.
- Если до удара было 0–4 зарядов: нанести бонус текущего заряда, затем увеличить заряд на 1.
- Если до удара было 5 зарядов: выполнить разрядку и сбросить заряд до 0.

### Бонусы зарядов

За каждый накопленный заряд:

- дополнительный урон при попадании: `+0.6` единицы;
- скорость атаки: `+0.06` к attack speed modifier.

Для динамической скорости атаки переопределить Forge-метод атрибутов ItemStack либо использовать безопасный transient modifier. Не накапливать дубликаты UUID. После смены предмета/заряда старый модификатор должен корректно исчезать.

### Разрядка

При ударе с 5 зарядами:

- дополнительный урон: `6.0`;
- лечение владельца: `min(4.0, фактически нанесённый дополнительный урон * 0.5)`;
- сбросить заряд в 0;
- создать `krovotok_blood_burst` в точке цели;
- создать поток `krovotok_life_drain` от цели к владельцу.

Не лечить владельца при ударе по неуязвимой цели, если дополнительный урон фактически не прошёл.

## Частицы

Зарегистрировать в существующем `ModParticleTypes`:

- `krovotok_blood_mist`
- `krovotok_blood_spark`
- `krovotok_blood_pulse`
- `krovotok_blood_burst`
- `krovotok_life_drain`

Скопировать JSON из:

`unpacked/resourcepack/assets/worldsmith/particles/`

Скопировать PNG-кадры из:

`unpacked/resourcepack/assets/worldsmith/textures/particle/krovotok/`

Черновые Java-классы в архиве использовать только как визуальный/алгоритмический референс. Переписать их под архитектуру репозитория.

### Визуальное поведение

- В руке, 0 зарядов: очень редкая дымка у гарды.
- 1–4 заряда: редкие искры; частота немного растёт с зарядом.
- 5 зарядов: пульс вокруг руки/гарды раз в 8–12 тиков и дополнительные искры.
- Обычное получение заряда: короткий `blood_pulse` у поражённой цели.
- Разрядка: `blood_burst` у цели.
- Лечение: 8–14 частиц `life_drain` по интерполированной линии цель → игрок.

Не создавать плотное постоянное облако. Частицы должны быть приглушёнными, тёмно-багровыми, без неонового эффекта.

## Локализация

Добавить в `assets/worldsmith/lang/ru_ru.json`:

```json
"item.worldsmith.krovotok": "Кровоток"
```

Добавить в `en_us.json`:

```json
"item.worldsmith.krovotok": "Bloodflow"
```

Tooltip на русском/английском:

- название способности: «Багровый ритм» / “Crimson Rhythm”
- строка заряда: `Заряд: X/5`
- краткое описание разрядки и лечения

## Проверки

Выполнить:

```bash
./gradlew clean build
```

Дополнительно проверить запуск клиента и dedicated server. В dedicated server не должно быть загрузки client-only классов.

## Что не делать

- Не менять существующие мечи и их ID.
- Не удалять ресурсы `Предела`.
- Не переносить проект обратно в `kitchenlegacy`.
- Не коммитить распакованную временную папку `codex_tasks/krovotok/unpacked/`; она должна быть в `.gitignore` или удалена после интеграции.
- Не добавлять огромные кровавые облака, брызги на экран или постоянный яркий glow.
