# Prah Forge 1.20.1 build fix

The ash echo uses the default full-size `ArmorStand` created by its constructor.

Do not call `ArmorStand#setSmall(boolean)` from `PrahAbilityManager`: in the Forge 1.20.1 mappings used by this project, that method is private and causes `compileJava` to fail.

Validation:

```bash
python3 codex_tasks/prah/verify_prah_build_fix.py
./gradlew clean build --no-daemon
```
