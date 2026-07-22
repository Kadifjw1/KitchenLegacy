# Technical specification: Worldsmith project migration

Use `PARAMETERS.json` as the source of truth. Canonical identity is display name `Worldsmith`, mod ID and resource/data namespace `worldsmith`, archive name `worldsmith`, and Java package/group `ru.theframetrip.worldsmith`.

Update Gradle metadata, Forge metadata, Java package declarations and imports, registries, networking channel IDs, saved-data identifiers, resource namespaces, language keys, tags, recipes, loot tables and documentation. The GitHub repository name may remain `KitchenLegacy`.

Delete old food/kitchen implementation and resources completely. Preserve Forge 1.20.1 / Forge 47.4.20 / Java 17 setup, `AGENTS.md`, `codex_tasks/`, Predel/Rift specifications, `predel_void.json`, `tex_predel.png`, and approved Predel particles/resources when present. Do not add music or sounds.

After editing, search for `kitchenlegacy`, `KitchenLegacy`, `Наследие кухни`, and `ru.theframetrip.kitchenlegacy`; no active code/resource/build references may remain outside migration documentation. Run `./gradlew clean build` and report results.
