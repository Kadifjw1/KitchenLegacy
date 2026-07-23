# Кровоток: работа без бинарных файлов в Codex

Codex не должен получать, загружать или патчить ZIP, PNG, GIF и другие бинарные файлы. Утверждённые ассеты Кровотока хранятся в репозитории как текстовые Base64-фрагменты в `codex_tasks/krovotok/assets/`.

Повреждения старой передачи исправляются детерминированно через:

`codex_tasks/krovotok/assets/ARCHIVE_REPAIR_PATCHES.json`

## Команды

Проверка и распаковка:

```bash
python3 codex_tasks/krovotok/verify_asset_archive.py
```

Материализация недостающих бинарных ресурсов:

```bash
python3 codex_tasks/krovotok/materialize_krovotok_resources.py
```

Скрипт создаёт ресурсы в `src/generated/resources/`, который уже подключён в `build.gradle` через `sourceSets.main.resources`.

## Что создаётся автоматически

- объёмная базовая item-модель `krovotok_base.json`;
- анимированная item-текстура и `.mcmeta`;
- шесть PNG-состояний заряда;
- 30 PNG-кадров частиц;
- manifest с SHA-256 каждого созданного файла.

Пять particle JSON уже находятся в `src/main/resources/assets/worldsmith/particles/` и повторно не генерируются. Это исключает duplicate-resource ошибки Gradle.

## Что разрешено редактировать

Только текстовые файлы:

- Base64-фрагменты;
- JSON исправлений и item-моделей;
- Java-классы при отдельной подтверждённой ошибке;
- workflow, скрипты, конфигурацию и документацию.

## Что нельзя коммитить

- `codex_tasks/krovotok/unpacked/`;
- восстановленный ZIP и объединённый Base64;
- сгенерированные PNG;
- `krovotok_base.json` из `src/generated/resources`;
- generated manifest.

## Проверка

```bash
python3 codex_tasks/krovotok/verify_asset_archive.py
python3 codex_tasks/krovotok/materialize_krovotok_resources.py
./gradlew clean build
git status --short --untracked-files=all
```

Сгенерированные файлы должны отсутствовать в Git status, но присутствовать внутри итогового JAR. Основной GitHub Actions workflow выполняет материализацию автоматически перед сборкой.
