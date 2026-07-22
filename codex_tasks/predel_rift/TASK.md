# Task: implement sword «Предел» and ability «Разлом»

Implement the approved active ability for the item `worldsmith:predel` in Minecraft Java 1.20.1 Forge.

## Required result

- The sword is named **Предел**.
- Its active ability is named **Разлом**.
- Default key: `R`, remappable in Controls.
- The client sends only an activation request.
- The server performs raycast, validates the target, creates a temporary 3×3×1 rift, owns cooldown and restoration, and prevents dupes.
- Valid blocks become a temporary non-colliding service block and restore after the configured duration.
- Existing approved particles are used for idle, swing, opening and closing.
- Do not add music or sound effects.

## Implementation workflow

1. Inspect the repository before changing code.
2. Identify the actual mod ID, Java package root, registries, networking, configs and resource locations.
3. Reuse existing infrastructure where possible.
4. Follow `PARAMETERS.json` exactly for IDs and prototype values.
5. Follow `TECH_SPEC.md` for behavior and edge cases.
6. Satisfy every check in `ACCEPTANCE.md`.
7. Run the project Gradle build.
8. Summarize changed files and any remaining limitation.

Do not silently simplify persistence, cooldown validation, collision safety, permissions, or server authority.
