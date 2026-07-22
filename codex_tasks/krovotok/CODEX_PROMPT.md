# Инструкция для Codex

Работай в репозитории `Kadifjw1/KitchenLegacy` в текущей ветке.

Твоя задача: полностью интегрировать меч `worldsmith:krovotok` и пассивную способность «Багровый ритм» в существующий Forge 1.20.1 мод Worldsmith.

Сначала прочитай:

1. `codex_tasks/krovotok/TECH_SPEC.md`
2. `codex_tasks/krovotok/PARAMETERS.json`
3. `codex_tasks/krovotok/ACCEPTANCE.md`

Затем выполни:

```bash
bash codex_tasks/krovotok/decode_assets.sh
```

Используй распакованные материалы как источник моделей, текстур, particle JSON и визуальных референсов. Черновой Java-код из архива не копируй вслепую: он использует неправильный package `com.worldsmith`. Интегрируй всё в уже существующие классы и package `ru.theframetrip.worldsmith`.

Обязательно:

- сначала изучи существующие `ModItems`, `ModParticleTypes`, `ClientModEvents`, creative tab и реализацию `PredelItem`;
- не создавай дублирующие реестры;
- сохрани работоспособность Предела и Разлома;
- реализуй механику сервер-авторитетно;
- защити dedicated server от client-only imports;
- после изменений запусти `./gradlew clean build`;
- исправляй ошибки до успешной сборки;
- в итоговом сообщении перечисли изменённые файлы, механику, результаты сборки и оставшиеся риски.

Не ограничивайся написанием плана: внеси все изменения в код и ресурсы.
