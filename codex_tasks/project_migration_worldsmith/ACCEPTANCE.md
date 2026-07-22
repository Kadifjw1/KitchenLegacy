# Acceptance criteria: Worldsmith migration

## Identity
- [ ] `mod_id` is `worldsmith`.
- [ ] Display name is `Worldsmith`.
- [ ] Archive/base name is `worldsmith`.
- [ ] Group ID and Java base package are `ru.theframetrip.worldsmith`.
- [ ] Active resource and data namespaces use `worldsmith`.
- [ ] Forge dependency sections in `mods.toml` use the new mod ID.
- [ ] The built JAR and manifest identify Worldsmith rather than KitchenLegacy.

## Legacy cleanup
- [ ] No food, fruit, vegetable, crop, seed, cooking, recipe, or kitchen item/block is registered.
- [ ] No food-specific event, menu, screen, block entity, serializer, worldgen, config, or network handler remains.
- [ ] No models, textures, blockstates, recipes, loot tables, tags, advancements, language entries, or generated files remain for deleted food content.
- [ ] No active legacy identity references to `kitchenlegacy`, `KitchenLegacy`, `Наследие кухни`, or `ru.theframetrip.kitchenlegacy`, excluding migration docs.

## Preservation
- [ ] `AGENTS.md`, `codex_tasks/project_migration_worldsmith/`, and `codex_tasks/predel_rift/` remain present.
- [ ] `predel_void.json` and `tex_predel.png` are preserved under `assets/worldsmith` and model texture references resolve.
- [ ] Predel/Rift mechanics and visual specification are not redesigned.

## Technical quality
- [ ] Java package directory structure matches package declarations.
- [ ] Dependencies used only by deleted legacy content are removed.
- [ ] Minecraft remains 1.20.1, Forge remains 47.4.20, Java remains 17.
- [ ] `clean build` succeeds with the Gradle wrapper.
