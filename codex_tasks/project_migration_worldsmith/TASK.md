# Task: migrate KitchenLegacy to Worldsmith

Clean the existing Forge 1.20.1 project so it becomes the base repository for **Worldsmith**.

## Required result

1. Remove all implementation and resources related to food, fruits, vegetables, crops, seeds, cooking, recipes, and kitchen systems.
2. Rename the project identity everywhere from the legacy KitchenLegacy identity to the values in `PARAMETERS.json`.
3. Preserve the approved Worldsmith sword work and all Codex task specifications.
4. Keep the project compiling on Minecraft 1.20.1, Forge 47.4.20, Java 17.
5. Run the acceptance checks in `ACCEPTANCE.md`.
6. Commit the implementation on a dedicated branch and create a Pull Request. Do not merge it automatically.

## Work order

1. Audit the repository before deleting anything.
2. Produce a list of legacy food systems and identity references.
3. Remove food-related code and resources completely, including registrations and generated data.
4. Rename packages, imports, resource namespaces, Forge metadata, Gradle metadata, config names, network identifiers, saved-data identifiers, localization keys, and retained resource references.
5. Remove stale empty files/directories and dead dependencies.
6. Run repository-wide searches for legacy identifiers and food content.
7. Run Gradle build/tests and fix all failures caused by the migration.
8. Report deleted, renamed, preserved, and unresolved items in the PR description.

## Important constraints

- Do not delete `codex_tasks/`, `AGENTS.md`, or approved Predel/Rift resources.
- Do not redesign the Predel sword or Rift ability.
- Do not add music or sound effects.
- Do not change Minecraft, Forge, or Java versions.
- Do not leave compatibility aliases for the unreleased KitchenLegacy identity.
- Do not preserve food content merely to make compilation easier; remove it and repair references correctly.
