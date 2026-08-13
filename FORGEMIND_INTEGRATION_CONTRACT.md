# Worldsmith ForgeMind Integration Contract

This document defines the only shared Java surfaces that ForgeMind may extend automatically.

## Safety model

- Existing Worldsmith Java is read-only by default.
- ForgeMind may modify an existing Java file only inside one explicit `FORGEMIND-EXTENSION` block.
- The API mapper records the SHA-256 of the complete containing file before an extension is planned.
- If the file changes after planning, the extension must be rejected and re-planned.
- ForgeMind must never create a parallel shared registry or shared network root when a contracted root exists.
- Feature-local systems such as a new `SavedData`, screen, menu, event handler, or ability manager should remain isolated unless architecture explicitly selects a contracted shared surface.

## Contracted shared surfaces

| Requirement | Canonical file | Marker |
| --- | --- | --- |
| Item registry | `src/main/java/ru/theframetrip/worldsmith/registry/ModItems.java` | `worldsmith.items` |
| Block registry | `src/main/java/ru/theframetrip/worldsmith/registry/ModBlocks.java` | `worldsmith.blocks` |
| Particle registry | `src/main/java/ru/theframetrip/worldsmith/registry/ModParticleTypes.java` | `worldsmith.particles` |
| Shared networking | `src/main/java/ru/theframetrip/worldsmith/network/ModNetwork.java` | `worldsmith.network.messages` |

## Networking rule

`ModNetwork` is the canonical shared channel for new Worldsmith features.

`PrahNetwork` is an existing feature-specific isolated channel. It remains valid for Prah, but it is not an automatic extension surface for unrelated features. ForgeMind must not choose `PrahNetwork` merely because it is another `SimpleChannel`.

New feature networking must bind to the shared channel instead of creating another `SimpleChannel`. The contracted API is:

- `ModNetwork.CHANNEL` — the canonical channel used for packet registration;
- `ModNetwork.nextMessageId()` — synchronized allocation of message IDs after existing hand-owned IDs;
- `ModNetwork.sendTo(ServerPlayer, Object)` — canonical S2C sending helper;
- `ModNetwork.sendToServer(Object)` — canonical C2S sending helper.

The existing Predel packet keeps message ID `0`. Contract-bound feature packets allocate IDs starting at `1` through `nextMessageId()` and must never hard-code their own shared-channel IDs.

The `worldsmith.network.messages` marker is executed from `ModNetwork.register()`, which is already called by the Worldsmith mod entry point. Therefore a contract-bound feature must not add a second common-setup bootstrap solely to register its packets.

## Marker format

```java
// FORGEMIND-EXTENSION:worldsmith.example:BEGIN
// generated extension content may be inserted here
// FORGEMIND-EXTENSION:worldsmith.example:END
```

Marker IDs are unique across the project. A malformed, duplicated, reversed, or missing marker must block automatic extension.

## Current scope

This contract intentionally starts small. New shared roots such as entities, menus, sounds, commands, or additional registries should receive a dedicated marker only when Worldsmith has a stable canonical owner for that subsystem.
