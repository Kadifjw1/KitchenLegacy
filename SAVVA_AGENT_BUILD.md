# Савва Урожайник — ForgeMind agent build

Generated feature: `savva_produce_vendor_v1`

## Build

```bash
./gradlew clean build --no-daemon
```

## Spawn

```mcfunction
/worldsmith savva spawn
```

The command requires permission level 2. Savva is a persistent farmer villager with
role `savva_produce_vendor`. Interact with the main hand between ticks
`1000` and `12000` to open the server-authoritative
vanilla merchant screen.

Currency lookup order:

1. `dragonlegacy:legacy_coin`
2. `minecraft:emerald`
3. `minecraft:emerald`

Offers are stored on the villager and reset once per Minecraft day. Vanilla merchant
transactions provide server-side inventory checks, stock limits and atomic item exchange.

## Honest boundary

This vertical slice proves autonomous feature generation and a buildable gameplay loop.
It intentionally uses the vanilla villager renderer. The approved custom Savva appearance
requires a later custom entity/renderer task after a valid 64×64 skin or GeckoLib model exists.
