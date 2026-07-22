# Technical specification: Worldsmith project migration

## 1. New identity

Use `PARAMETERS.json` as the source of truth.

Required canonical values:

- Display name: `Worldsmith`
- Mod ID: `worldsmith`
- Archive name: `worldsmith`
- Group ID and base package: `ru.theframetrip.worldsmith`
- Resource/data namespace: `worldsmith`

Update every relevant location, including:

- `gradle.properties`
- `build.gradle` / Gradle archive name
- `settings.gradle` project name when applicable
- `META-INF/mods.toml`
- `@Mod` annotation and mod ID constants
- Java package declarations, folder paths and imports
- registry helper classes and deferred registers
- networking channel identifiers
- config file names and config translation keys
- saved-data names and capability identifiers
- `assets/<namespace>` and `data/<namespace>` directories
- item/block/entity model texture references
- language keys, tags, recipes, loot tables, advancements and generated resources
- tests, datagen, run configuration namespace references
- README/documentation that describes the active project

The source repository name may remain `KitchenLegacy` on GitHub for now. Do not attempt to rename the GitHub repository through code.

## 2. Legacy content removal

Delete all code and resources whose purpose belongs to the old food/kitchen project. Audit registrations first so removal is complete and compilation-safe.

The removal includes, when present:

- edible items and `FoodProperties`
- fruits, vegetables, berries and related item registries
- seeds, crop blocks, crop growth logic and farmland interaction
- trees/bushes/worldgen created only to provide food ingredients
- kitchen blocks, machines, cookware and kitchen tools
- cooking containers, menus, screens and block entities
- recipe types, serializers, ingredients and recipe JSON files
- food creative-mode tabs
- food-related events, capabilities, networking and configuration
- loot tables, tags, advancements and language entries for deleted content
- item/block models, blockstates, textures, particles and sounds used only by deleted content
- data-generator providers used only by deleted content
- tests and demo code for deleted content
- stale comments and descriptions presenting the mod as a food mod

Do not leave registered placeholders, missing-model entries, empty registries, dangling suppliers, dead event listeners, orphaned translation keys, or references to deleted resources.

## 3. Preservation rules

Preserve:

- Forge project structure, Gradle wrapper and Minecraft 1.20.1 setup
- reusable infrastructure that is not food-specific and remains useful for Worldsmith
- `AGENTS.md`
- all folders under `codex_tasks/`
- `codex_tasks/predel_rift/`
- existing approved Predel resources, including `predel_void.json` and `tex_predel.png` when present
- existing approved particle resources for Predel/Rift when present

For retained Worldsmith resources, move or rewrite namespace references from `kitchenlegacy` to `worldsmith`.

Do not delete unrelated non-food features solely because they are not yet part of Predel. However, remove demo/test scaffolding that has no real project purpose or depends entirely on deleted food systems.

## 4. Predel resource validation

The retained item model must live under the Worldsmith namespace, normally:

`src/main/resources/assets/worldsmith/models/item/predel_void.json`

Its texture reference must resolve to the retained texture, normally:

`worldsmith:item/tex_predel`

The texture should live at:

`src/main/resources/assets/worldsmith/textures/item/tex_predel.png`

If the current approved files use a slightly different valid retained layout, preserve their content and normalize only the namespace/path references needed for Minecraft 1.20.1.

## 5. Dependency cleanup

Inspect dependencies after deleting legacy systems.

- Keep GeckoLib only if retained Worldsmith content currently requires it or the upcoming approved implementation requires it.
- Remove imports, comments, repositories, or dependencies used exclusively by deleted legacy food/demo content.
- Do not upgrade dependency versions as part of this migration.

## 6. Search and cleanup requirements

After editing, search the repository for these legacy identity strings:

- `kitchenlegacy`
- `KitchenLegacy`
- `Наследие кухни`
- `ru.theframetrip.kitchenlegacy`

Outside migration documentation and Git history, there must be no active code/resource/build references to them.

Also search for names of deleted registered food, fruit, vegetable, crop, seed, recipe and kitchen entries discovered during the audit. No active references may remain.

## 7. Build and reporting

Run the available Gradle verification, at minimum:

- `./gradlew clean build` on Unix-like environments, or the platform-equivalent wrapper command

Run additional tests/datagen checks when present and practical.

The Pull Request must include:

- old identity to new identity mapping
- list of deleted content groups
- list of moved/renamed packages and resource namespaces
- preserved Worldsmith assets/tasks
- exact commands run and results
- any known limitation that could not be resolved
