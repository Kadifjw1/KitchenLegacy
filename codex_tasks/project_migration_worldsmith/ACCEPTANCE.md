# Acceptance criteria: Worldsmith migration

The migration is complete only when all applicable checks pass.

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
- [ ] No dead imports, dangling registry references, orphaned suppliers, or missing resource references remain.
- [ ] Repository search finds no active legacy identity references to `kitchenlegacy`, `KitchenLegacy`, `Наследие кухни`, or `ru.theframetrip.kitchenlegacy`, excluding the migration task documents themselves.

## Preservation

- [ ] `AGENTS.md` remains present.
- [ ] `codex_tasks/project_migration_worldsmith/` remains present.
- [ ] `codex_tasks/predel_rift/` remains present and unchanged except for necessary namespace corrections.
- [ ] `predel_void.json` remains present under the Worldsmith resource namespace.
- [ ] `tex_predel.png` remains present when it existed before migration.
- [ ] Predel model texture references resolve correctly under the Worldsmith namespace.
- [ ] No approved Predel/Rift mechanic or visual specification was redesigned.

## Technical quality

- [ ] Java package directory structure matches package declarations.
- [ ] All retained imports and registry references compile.
- [ ] No empty legacy package/resource trees remain.
- [ ] Dependencies used only by deleted legacy content are removed; required dependencies are preserved.
- [ ] Minecraft remains 1.20.1, Forge remains 47.4.20, Java remains 17.
- [ ] `clean build` succeeds with the Gradle wrapper.
- [ ] Any available relevant tests also pass.

## Pull Request report

- [ ] PR lists deleted content categories.
- [ ] PR lists identity/package/resource renames.
- [ ] PR lists preserved Worldsmith files.
- [ ] PR contains the exact build/test commands and their results.
- [ ] PR clearly states any unresolved issue; otherwise it states that no known issues remain.
