# Technical specification — «Предел» / «Разлом»

## 1. Item card

- Name: **Предел**
- Item ID: `worldsmith:predel`
- Type: fantasy sword
- Force: Void
- Ability: **Разлом**
- Ability type: active
- Blockbench source: `worldsmith_sword7_void.bbmodel`
- Item model: `predel_void.json`
- Sword texture: `tex_predel.png`

Tooltip intent:

```text
Предел
Активная способность: Разлом
Временно выводит выбранные блоки из материального мира.
[R] Использовать способность
Перезарядка: 12 сек.
```

The key must be remappable. Do not add music or sound effects.

## 2. Approved mechanic

The player aims at a wall, floor or ceiling and presses the ability key. The server creates a planar rift up to 3×3×1 blocks based on the hit face.

Allowed blocks temporarily become `worldsmith:void_phase_block`. The temporary block has no collision and no raycast shape, so entities, projectiles and line traces can pass through it. After 80 ticks, the original `BlockState` is restored.

The ability must never:

- break blocks;
- create drops or experience;
- move phased blocks into inventories;
- destroy block data;
- duplicate blocks;
- force-load chunks.

## 3. Activation

Client behavior:

1. Detect the remappable ability key.
2. Send a C2S request with no target coordinates.
3. Display client-only visuals and cooldown feedback based on server-approved state.

Server behavior:

1. Verify the player is alive, not a spectator and can interact.
2. Verify `worldsmith:predel` is in the main hand.
3. Verify the player-wide cooldown for this item type is inactive.
4. Perform a server-side block raycast with range 8 blocks.
5. Require the target chunk to already be loaded.
6. Build the 3×3 plane from the hit block and face.
7. Filter every candidate position.
8. Abort without cooldown if no candidate remains.
9. Save original states, register ownership, place temporary blocks, apply cooldown and notify clients for particles.

Suggested packet name: `UsePredelAbilityPacket`. It must contain only an activation request, never trusted coordinates.

## 4. Plane orientation

- Hit north/south face: plane spans X and Y.
- Hit east/west face: plane spans Z and Y.
- Hit top/bottom face: plane spans X and Z.

The hit block is the center. Invalid positions are skipped. The ability may open fewer than 9 blocks, but it must never expand beyond the original 3×3 area.

## 5. Valid and invalid blocks

A block may be phased only when all conditions are true:

- chunk is loaded;
- block has no `BlockEntity`;
- block is not in `worldsmith:rift_immune`;
- block is not occupied by another active rift;
- permission check allows the player to affect the position;
- block is not a liquid;
- block is not part of a prohibited multi-block structure.

Reject by default:

- bedrock and barriers;
- portal blocks and portal frames;
- command, structure and jigsaw blocks;
- moving piston parts;
- every block with `BlockEntity`;
- containers and machines;
- fluids;
- doors, beds, tall plants and other multi-block structures;
- unloaded positions;
- positions owned by another rift.

Provide the data tag:

`data/worldsmith/tags/blocks/rift_immune.json`

## 6. Temporary block

Register `worldsmith:void_phase_block` with these properties:

- no collision;
- no occlusion/raycast shape for interaction and projectile passage;
- transparent rendering;
- no drops and no experience;
- unobtainable and absent from creative tabs;
- cannot be normally replaced or placed into by players;
- does not contain or accept fluid;
- must not trigger destructive neighbor behavior;
- no required `BlockEntity`;
- managed only by the rift manager.

Ownership is stored outside the block:

```text
dimension + BlockPos -> rift UUID
```

## 7. Runtime data

Recommended records/classes:

```text
RiftInstance
- UUID riftId
- UUID ownerPlayerId
- ResourceKey<Level> dimension
- List<RiftBlockSnapshot> blocks
- long createdGameTime
- long restoreGameTime

RiftBlockSnapshot
- BlockPos position
- BlockState originalState
```

Recommended manager: `PredelRiftManager`.

Responsibilities:

- create rifts;
- reject overlaps;
- snapshot original states;
- own position-to-rift mapping;
- tick expiration;
- restore states;
- persist through `SavedData`;
- queue restoration for unloaded chunks;
- clean inconsistent records.

## 8. Opening algorithm

```java
usePredelAbility(ServerPlayer player):
    if (!isPredelInMainHand(player)) return FAIL
    if (isPredelOnCooldown(player)) return FAIL

    hit = serverRaycast(player, 8.0)
    if (!hit.isBlock()) return FAIL

    candidates = buildPlane(hit.blockPos, hit.face, 3, 3)
    valid = []

    for pos in candidates:
        if (!level.hasChunkAt(pos)) continue
        if (!canPhaseBlock(level, pos)) continue
        if (!permissionHookAllows(player, level, pos)) continue
        if (riftManager.isPositionOccupied(level.dimension, pos)) continue
        valid.add(pos)

    if (valid.isEmpty()) return FAIL

    rift = riftManager.create(player, level, valid, gameTime + 80)

    for snapshot in rift.blocks:
        setVoidPhaseBlockWithoutDrops(level, snapshot.position)

    applySharedPredelCooldown(player, 240)
    sendOpenParticles(rift)
    return SUCCESS
```

Cooldown belongs to player + item type, not to one stack. Swapping swords must not bypass it. Cooldown starts only after at least one block is successfully opened.

## 9. Restoration

When `restoreGameTime` is reached:

1. For each snapshot, check whether its chunk is loaded.
2. If unloaded, queue restoration for chunk load without force-loading.
3. Verify the position is still owned by the same rift UUID.
4. Verify the position still contains `void_phase_block`.
5. Evacuate intersecting entities.
6. Restore the exact original `BlockState` without drops.
7. Release ownership.
8. Send close-particle data.
9. Remove the rift only when all positions are resolved.

If an administrator or another mod replaced the service block, do not overwrite the new block. Release ownership, log a warning and continue.

## 10. Entity safety

Before restoring each block, find intersecting entities.

Search for a safe destination using BFS or another deterministic search within radius 3. A valid destination must:

- fit the full entity hitbox;
- be outside the restoring block volume;
- have no solid collision;
- provide safe support for grounded entities;
- avoid lava, fire and other immediate hazards.

When found, move the entity without damage.

Fallback when no safe position exists:

1. Find nearest free space above the rift.
2. Move the entity there.
3. Deal 2.0 magic damage.
4. Apply a small impulse away from the rift center.

Normal closure must never leave an entity inside a solid block or suffocating.

## 11. Persistence and chunk lifecycle

Store active rifts and snapshots in world `SavedData`.

After restart, all stored rifts are treated as expired:

- restore immediately in loaded chunks;
- restore when an unloaded chunk is next loaded;
- never force-load a chunk for restoration;
- keep unresolved records in `SavedData` until restored.

Owner logout, death, dimension change or dropping the sword does not cancel restoration.

## 12. Multiple rifts

- Every rift has a unique UUID.
- One world position can belong to only one active rift.
- Occupied positions are skipped when creating another rift.
- A partially valid new rift is allowed when at least one position remains.
- Restoration must always verify the owner UUID.

## 13. Permissions

Do not hard-depend on one claim/protection mod.

Provide a cancellable server event or hook, suggested name:

`PredelRiftPermissionEvent`

Inputs:

- player;
- dimension/level;
- position;
- original `BlockState`.

Default behavior without integrations: allow when vanilla interaction rules permit it. A cancelled position is excluded from the rift.

## 14. Particles

Approved particle IDs:

- `worldsmith:void_mote` — sparse idle particles near the sword channel, around once every 8–14 ticks;
- `worldsmith:void_shard` — 4–7 short-lived particles during a swing;
- `worldsmith:void_rift` — opening and closing effect.

Opening sequence:

1. Square particles appear along selected block edges.
2. They contract toward the center.
3. Server replaces the block.
4. A subtle dark-purple haze remains.

Closing sequence:

1. Particles appear in the center.
2. They expand toward the edges.
3. Original block state restores.
4. A brief dark impulse appears.

Server decides when and where opening/closing happens; clients render the effect. Do not add audio.

## 15. Suggested code organization

Adapt to the existing project instead of forcing this exact tree:

```text
registry/ModItems.java
registry/ModBlocks.java
registry/ModParticles.java
item/PredelItem.java
ability/predel/PredelAbility.java
ability/predel/PredelRiftManager.java
ability/predel/RiftInstance.java
ability/predel/RiftBlockSnapshot.java
ability/predel/PredelRiftSavedData.java
block/VoidPhaseBlock.java
network/ModNetwork.java
network/UsePredelAbilityPacket.java
client/ModKeyMappings.java
client/ClientAbilityHandler.java
client/particle/VoidMoteParticle.java
client/particle/VoidShardParticle.java
client/particle/VoidRiftParticle.java
event/PredelRiftPermissionEvent.java
```

## 16. Resource layout

```text
assets/worldsmith/lang/ru_ru.json
assets/worldsmith/lang/en_us.json
assets/worldsmith/models/item/predel_void.json
assets/worldsmith/textures/item/tex_predel.png
assets/worldsmith/particles/void_mote.json
assets/worldsmith/particles/void_shard.json
assets/worldsmith/particles/void_rift.json
assets/worldsmith/textures/particle/void_mote.png
assets/worldsmith/textures/particle/void_shard.png
assets/worldsmith/textures/particle/void_rift.png
data/worldsmith/tags/blocks/rift_immune.json
```

Internal paths and IDs use lowercase `snake_case`.

Localization keys:

```json
{
  "item.worldsmith.predel": "Предел",
  "tooltip.worldsmith.predel.ability": "Активная способность: Разлом",
  "tooltip.worldsmith.predel.description": "Временно выводит выбранные блоки из материального мира.",
  "key.worldsmith.predel_ability": "Использовать способность Предела",
  "key.categories.worldsmith": "Worldsmith"
}
```
