# Codex instructions for KitchenLegacy

## Worldsmith task workflow

When the user asks to implement the sword **Предел** or the active ability **Разлом**, open and read the task folder:

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
