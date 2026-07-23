# Кровоток — техническое задание PR #24

## Контекст

- Репозиторий: `Kadifjw1/KitchenLegacy`
- Ветка: `fix/krovotok-complete-integration`
- PR: `#24`
- Minecraft: `1.20.1`
- Forge: `47.4.20`
- Java: `17`
- Mod ID: `worldsmith`

Java-интеграция `worldsmith:krovotok` и способности «Багровый ритм» уже находится в `main`. Этот PR не повторяет предмет, реестры, локализацию, item property, charge-модели и particle JSON.

## Цель

Заменить заглушечный asset pipeline из `main` на воспроизводимое восстановление утверждённого комплекта Кровотока из текстовых Base64-фрагментов.

После слияния чистый checkout должен:

1. проверить и восстановить исходный ZIP;
2. материализовать настоящую базовую модель, item-текстуры и кадры частиц;
3. использовать пять particle JSON, уже committed в `src/main/resources`;
4. собрать JAR без duplicate-resource ошибок;
5. не добавлять бинарные результаты в Git.

## Источник ассетов

Фрагменты находятся в `codex_tasks/krovotok/assets/`. Исправления задаются только через `ARCHIVE_REPAIR_PATCHES.json`.

Ожидаемые параметры:

- длина Base64: `199932`;
- SHA-256 ZIP: `3f31f68727da3a6e9d40b0e25e7cc26c6868c7317ca7fbdb516b7ea1e22bf902`;
- элементов `.bbmodel`: `170`;
- particle JSON в архиве: `5`;
- particle JSON в `src/main/resources`: `5`;
- generated particle JSON: `0`;
- generated PNG-кадров: `30`.

## Проверка архива

`verify_asset_archive.py` должен:

- читать фрагменты в фиксированном порядке;
- проверять исходные участки перед исправлением;
- применять только описанные операции;
- сверять Git blob SHA восстановленных фрагментов;
- проверять длину Base64 и SHA-256 ZIP;
- выполнять `ZipFile.testzip()`;
- проверять обязательные пути;
- разбирать `.bbmodel` и проверять 170 элементов;
- по `--extract` распаковывать архив в `codex_tasks/krovotok/unpacked/`.

Любая ошибка должна завершать скрипт с ненулевым кодом. Fallback-заглушки запрещены.

## Материализация

`materialize_krovotok_resources.py` сначала запускает строгую проверку с распаковкой, затем проверяет наличие пяти committed particle JSON в:

`src/main/resources/assets/worldsmith/particles/`

После этого копирует в `src/generated/resources` только недостающие ресурсы:

- `krovotok_base.json` из утверждённой модели;
- `krovotok.png`, `.mcmeta`, `krovotok_static.png`;
- `krovotok_charge_0.png` … `krovotok_charge_5.png`;
- 30 PNG-кадров из `textures/particle/krovotok/`;
- manifest с SHA-256 результатов и перечнем reused particle JSON.

Материализатор обязан удалять старые generated-копии particle JSON от прежнего placeholder pipeline, чтобы Gradle не видел дубликаты.

Запрещено:

- генерировать PNG 1×1;
- создавать искусственную геометрию из микрокубов;
- повторно копировать particle JSON в generated resources;
- менять UV, элементы или display transforms;
- использовать частично восстановленный архив.

## Сборка

Основной workflow перед Gradle выполняет:

```bash
python3 codex_tasks/krovotok/verify_asset_archive.py
python3 codex_tasks/krovotok/materialize_krovotok_resources.py
./gradlew clean build
```

После сборки проверяется наличие в JAR:

- `assets/worldsmith/models/item/krovotok_base.json`;
- `assets/worldsmith/textures/item/krovotok.png`;
- committed particle JSON;
- generated PNG-кадра частиц.

Отдельный workflow проверяет полный набор из пяти committed particle JSON и 30 generated PNG-кадров, отсутствие generated particle JSON и чистый Git status.

## Git

Разрешены только текстовые Base64-фрагменты, JSON исправлений, скрипты, workflow, документация и конфигурация.

Не коммитить ZIP, PNG/GIF, `unpacked/`, материализованные ресурсы и generated manifest.

## Ручная приёмка

После CI обязательны dev-client и dedicated-server тесты, проверка модели во всех item-режимах, анимации, состояний заряда и регрессия Предела/Разлома.
