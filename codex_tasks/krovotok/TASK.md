# Задача Codex: завершение ассетов меча «Кровоток»

Работай в репозитории `Kadifjw1/KitchenLegacy`, в существующей ветке `fix/krovotok-complete-integration`, и обновляй PR #24. Не создавай новую ветку и не сливай PR самостоятельно.

## Что уже находится в `main`

Повторно не реализовывать:

- `worldsmith:krovotok` и `KrovotokItem`;
- регистрацию в `ModItems` и вкладке Worldsmith;
- NBT заряда и механику «Багрового ритма»;
- item property `worldsmith:krovotok_charge`;
- модели состояний заряда 0–5;
- пять particle JSON и регистрацию частиц;
- русскую и английскую локализацию.

## Цель PR #24

Оставить только недостающий asset pipeline:

1. полный набор текстовых Base64-фрагментов;
2. детерминированное исправление повреждённых фрагментов;
3. строгую проверку SHA, ZIP и 170 элементов `.bbmodel`;
4. материализацию настоящей модели, item-текстур и 30 PNG-кадров частиц;
5. CI, который материализует ресурсы до Gradle-сборки и проверяет их внутри JAR.

Пять particle JSON уже находятся в `src/main/resources` и не должны повторно появляться в `src/generated/resources`.

## Подготовка ассетов

```bash
python3 codex_tasks/krovotok/verify_asset_archive.py
python3 codex_tasks/krovotok/materialize_krovotok_resources.py
```

Проверка обязана подтвердить:

- длину итогового Base64: `199932`;
- SHA-256 ZIP: `3f31f68727da3a6e9d40b0e25e7cc26c6868c7317ca7fbdb516b7ea1e22bf902`;
- целостность ZIP;
- обязательные модели, текстуры и particle JSON в архиве;
- ровно 170 элементов в `krovotok_JE_1.20.1.bbmodel`.

Материализация создаёт в `src/generated/resources/`:

- `krovotok_base.json`;
- анимированную item-текстуру и `.mcmeta`;
- шесть текстур заряда;
- 30 PNG-кадров частиц;
- manifest с SHA-256 результатов.

## Ограничения

- Не создавать заглушки 1×1 и искусственную геометрию.
- Не менять исходную геометрию, UV и display transforms.
- Не коммитить ZIP, PNG, распакованный архив и содержимое `src/generated/resources`.
- Не генерировать копии particle JSON, уже находящихся в `src/main/resources`.
- Не трогать код Кровотока, Предел/Разлом и остальные мечи без подтверждённой ошибки.
- Редактировать только текстовые файлы.

## Проверки

```bash
python3 codex_tasks/krovotok/verify_asset_archive.py
python3 codex_tasks/krovotok/materialize_krovotok_resources.py
./gradlew clean build
git status --short --untracked-files=all
```

Проверь в JAR базовую модель, item-текстуры, пять committed particle JSON и 30 сгенерированных кадров. После CI обязательны ручные проверки dev client, dedicated server, отображения модели и игровой механики.
