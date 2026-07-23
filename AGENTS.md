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

## Krovotok workflow

The Java implementation of **Кровоток / Багровый ритм** remains in the repository. The approved model, item textures, charge textures, particle frames and particle JSON are maintained outside Git as a user-owned media package.

Rules:

- Create a dedicated branch and Pull Request for focused Krovotok code changes. Do not merge automatically.
- Do not recreate, regenerate, upload or commit Krovotok PNG, GIF, ZIP, Base64 fragments, particle JSON or item-model JSON unless the user explicitly asks to restore the external package.
- Do not add a materialization script or CI step that reconstructs Krovotok media inside the repository.
- Preserve the existing server-authoritative combo logic and the dedicated crimson particle provider.
- Preserve Predel/Rift and all other swords.
- A repository-only JAR intentionally does not contain Krovotok rendering media. Runtime visual testing requires the separately supplied media/resource package.
- Run `./gradlew clean build` before reporting code changes.
