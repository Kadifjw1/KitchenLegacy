# Готовый промпт для Codex

Работай в репозитории `Kadifjw1/KitchenLegacy` и только в существующей ветке `fix/krovotok-complete-integration`. Обновляй PR #24, не создавай новую ветку и не сливай PR.

Прочитай `AGENTS.md` и документы в `codex_tasks/krovotok/`.

Java-интеграция Кровотока уже находится в `main`. Не добавляй повторно `KrovotokItem`, регистрации, creative tab, локализацию, item property, charge-модели и пять particle JSON.

Задача PR #24:

- восстановить утверждённый ZIP из текстовых Base64-фрагментов;
- применить только зафиксированные исправления;
- проверить SHA-256, ZIP и 170 элементов `.bbmodel`;
- материализовать настоящую базовую модель, item-текстуры и 30 PNG-кадров частиц;
- использовать пять particle JSON из `src/main/resources`, не создавая дубликаты;
- обеспечить материализацию перед Gradle-сборкой и проверку ресурсов внутри JAR.

Команды:

```bash
python3 codex_tasks/krovotok/verify_asset_archive.py
python3 codex_tasks/krovotok/materialize_krovotok_resources.py
./gradlew clean build
git status --short --untracked-files=all
```

Не используй PNG 1×1, искусственные 170 кубиков и fallback-заглушки. Не загружай, не патчь и не коммить бинарные файлы. Сгенерированные ресурсы должны отсутствовать в Git status, но присутствовать в JAR.

После CI проверь dev client, dedicated server, модель, состояния заряда и сохранность Предела/Разлома. PR не сливать до подтверждения ручных критериев.
