# Codex instructions for Worldsmith migration

## Mandatory project migration

Before implementing new Worldsmith features, inspect the repository for the legacy KitchenLegacy identity and food content.

When the user asks to clean the old project or migrate it to Worldsmith, open:

`codex_tasks/project_migration_worldsmith/`

Read files in this order:

1. `TASK.md`
2. `PARAMETERS.json`
3. `TECH_SPEC.md`
4. `ACCEPTANCE.md`

Treat `PARAMETERS.json` as the source of truth for the new project identity.

Migration rules:

- Remove legacy food, fruit, vegetable, crop, cooking, recipe, and kitchen content completely.
- Rename the project identity from KitchenLegacy to Worldsmith everywhere required by code, resources, Gradle, Forge metadata, packages, namespaces, configs, and generated data.
- Preserve Minecraft 1.20.1 and the current Forge toolchain unless the task explicitly says otherwise.
- Preserve Worldsmith task specifications and approved sword resources, including the Predel/Rift work.
- Do not keep compatibility aliases for the unreleased KitchenLegacy identity unless compilation requires a temporary transition, and remove any temporary alias before completion.
- Run repository-wide searches for stale legacy identifiers after editing.
- Run the available Gradle build and tests before reporting completion.
- Create a dedicated branch and Pull Request. Do not merge automatically.

## Predel / Rift workflow

After the project migration is complete, when implementing the sword **Предел** or the active ability **Разлом**, open:

`codex_tasks/predel_rift/`

Read files in this order:

1. `TASK.md`
2. `PARAMETERS.json`
3. `TECH_SPEC.md`
4. `ACCEPTANCE.md`

Rules:

- Treat `PARAMETERS.json` as the source of truth for numeric values and resource IDs.
- Do not redesign the approved mechanic or visual style.
- Do not add music or sound effects.
- Keep all authoritative world changes on the logical server.
- Preserve existing project architecture where practical instead of blindly creating the suggested package layout.
- Reuse existing registrations, networking, config, and event infrastructure when present.
- Before editing, inspect the repository and locate the actual mod ID, package root, registries, network setup, and resource paths.
- Implement the complete task, then run the available Gradle build and tests.
- Report changed files, build result, and any unresolved compatibility issue.

## Krovotok asset workflow

The Java implementation of **Кровоток** and **Багровый ритм** is already present in `main`. PR #24 is only for completing the approved binary asset pipeline and build verification.

Open:

`codex_tasks/krovotok/`

Read files in this order:

1. `TASK.md`
2. `BINARY_ASSETS.md`
3. `PARAMETERS.json`
4. `TECH_SPEC.md`
5. `ACCEPTANCE.md`

Before every local build or game launch, run:

```bash
python3 codex_tasks/krovotok/verify_asset_archive.py
python3 codex_tasks/krovotok/materialize_krovotok_resources.py
```

Rules:

- Work only in `fix/krovotok-complete-integration` and update PR #24. Do not create another branch and do not merge automatically.
- Do not reimplement item registration, creative-tab registration, localization, NBT charge logic, item-property registration or particle registration already present in `main` unless a verified bug requires a focused fix.
- Approved ZIP and PNG assets remain stored as text-only Base64 fragments. Edit only text files.
- Never manually upload, patch or commit PNG, GIF, ZIP or generated resources.
- Preserve the approved 170-element geometry and display transforms.
- Generated resources belong in `src/generated/resources` and must be present in the built JAR but absent from Git status.
- Preserve Predel/Rift and all other swords.
- Run archive verification, materialization, `./gradlew clean build`, JAR-content checks, dev-client checks and dedicated-server checks before declaring completion.
