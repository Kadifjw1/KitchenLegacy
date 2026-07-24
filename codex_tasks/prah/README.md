# Prah development checks

Run the source guard and Gradle build before merging changes to the active ability:

```bash
python3 codex_tasks/prah/verify_prah_build_fix.py
./gradlew clean build --no-daemon
```
