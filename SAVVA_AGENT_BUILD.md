# Савва Урожайник — ForgeMind agent build

Generated feature: `savva_produce_vendor_v1`

## Build

```bash
./gradlew clean build --no-daemon
```

## Spawn

```mcfunction
/worldsmith savva spawn
```

Савву также можно создать предметом `worldsmith:savva_spawner` из отдельной творческой вкладки «Worldsmith: Лавка Саввы».

Савва — постоянный фермер-житель с ролью `savva_produce_vendor`. Лавка работает между тиками `1000` и `12000`.

Основная валюта: `worldsmith:savva_coin`. Изумруд остаётся только аварийным fallback, если реестр предметов повреждён.

Профиль содержит 25 предложений. В торговлю добавлены собственные предметы Worldsmith:

- `worldsmith:tomato`;
- `worldsmith:lettuce`;
- `worldsmith:cucumber`.

Предложения хранятся на жителе и восстанавливаются раз в игровой день. Все сделки выполняются сервером с проверкой инвентаря, цены, остатка и свободного места.

## Honest boundary

В этой версии Савва использует ванильную модель фермера. Отдельный внешний вид NPC потребует валидного скина 64×64 или GeckoLib-модели.
