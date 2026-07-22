# Acceptance checklist — «Предел» / «Разлом»

The task is complete only when all applicable checks pass.

## Build

- [ ] Project compiles for Minecraft 1.20.1 Forge.
- [ ] Existing tests pass.
- [ ] Client-only classes are not loaded on a dedicated server.
- [ ] No new warning or error is introduced during normal startup.

## Item and controls

- [ ] Item is available as `worldsmith:predel` or is correctly adapted to the repository's actual namespace with documented migration.
- [ ] Display name is «Предел».
- [ ] Tooltip identifies active ability «Разлом».
- [ ] Default ability key is `R` and is remappable.
- [ ] Ability works only while the sword is in the main hand.
- [ ] No music or sound effects were added.

## Server authority and networking

- [ ] C2S packet contains no trusted target coordinates.
- [ ] Server performs the raycast and all target validation.
- [ ] Packet spam cannot bypass cooldown.
- [ ] Ability cannot reach farther than 8 blocks.
- [ ] World changes occur only on the logical server.

## Rift creation

- [ ] Wall hit creates an X/Y or Z/Y plane according to the hit face.
- [ ] Floor and ceiling hit create an X/Z plane.
- [ ] Area never exceeds 3×3×1 or 9 blocks.
- [ ] Invalid positions are skipped without expanding the plane.
- [ ] Failed activation with zero valid blocks causes no cooldown and no world change.
- [ ] Successful activation starts a 240-tick player-wide cooldown.
- [ ] Swapping to another copy of the sword does not bypass cooldown.
- [ ] Ability consumes no durability, XP or other resource.

## Block safety

- [ ] Blocks with `BlockEntity` are never phased.
- [ ] Fluids and prohibited multi-block structures are never phased.
- [ ] `worldsmith:rift_immune` excludes tagged blocks.
- [ ] Permission hook/event can reject individual positions.
- [ ] No block drops or XP are created.
- [ ] Neighbor updates do not destroy surrounding structures.
- [ ] Fluids do not occupy the temporary rift area.
- [ ] Another rift cannot claim an already occupied position.

## Temporary block

- [ ] `worldsmith:void_phase_block` has no collision.
- [ ] Entities, projectiles and traces can pass through it as intended.
- [ ] It is unobtainable, unbreakable through normal play and absent from creative tabs.
- [ ] It has no drops, XP or fluid storage.
- [ ] Position ownership is tracked by rift UUID outside the block.

## Restoration

- [ ] Original `BlockState` restores after 80 ticks.
- [ ] Restoration creates no drops or XP.
- [ ] Restoration verifies the rift UUID for each position.
- [ ] If the service block was replaced externally, the newer block is not overwritten.
- [ ] Unloaded chunks are not force-loaded.
- [ ] Pending positions restore when their chunks load.
- [ ] Active rifts persist through `SavedData`.
- [ ] After restart, all saved rifts are treated as expired and are restored as soon as possible.
- [ ] Logout, death, dimension change or dropping the sword does not cancel restoration.

## Entity safety

- [ ] Entities intersecting a restoring block are moved to a safe position.
- [ ] Search radius is 3 blocks.
- [ ] Valid destinations fit the full hitbox and avoid immediate hazards.
- [ ] Fallback moves the entity above the rift, applies 2.0 damage and a small outward impulse.
- [ ] Normal restoration never leaves an entity suffocating inside a solid block.

## Particles

- [ ] `worldsmith:void_mote` is used sparsely for idle sword visuals.
- [ ] `worldsmith:void_shard` is used for swing visuals.
- [ ] `worldsmith:void_rift` is used for opening and closing.
- [ ] Server controls opening/closing positions and timing.
- [ ] Effects do not obscure first-person view or create unreasonable particle load.

## Required final report from Codex

Codex must report:

1. Changed files.
2. Architecture decisions and any deviation from `TECH_SPEC.md`.
3. Build/test commands executed.
4. Build/test results.
5. Manual tests still required inside Minecraft.
6. Any namespace mismatch between `worldsmith` and the existing repository, without silently renaming approved public IDs.
