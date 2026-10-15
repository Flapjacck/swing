# Swing

Client-only Fabric mod for Minecraft 26.2. Scales the item in your first-person hand and lets you speed up or slow down the swing animation. Nothing on the server changes, so this is visual only.

I made this because the usual "viewmodel" mods come packed with a pile of extra features I already get from other mods. I play Hypixel Skyblock. I wanted a small jar I could edit by changing a few constants and rebuilding.

## Settings

All knobs are in `src/main/java/com/example/itemtweaks/ItemTweaksConfig.java`. Change a number, rebuild, drop the new jar in `mods`.

- `HELD_ITEM_SCALE` (default 0.75): size of the first-person held item. 1.0 is vanilla.
- `SWING_SPEED_MULTIPLIER` (default 1.0): arm swing animation. Above 1 is faster, below 1 is slower.

Scaling does not apply in third person / F5.

## Build

Needs JDK 25.

```bash
./gradlew build
```

Use `build/libs/fabric-swing-1.0.0.jar`. Skip the `-sources` jar.

Dev launch:

```bash
./gradlew runClient
```

For Prism: Minecraft 26.2, Fabric Loader 0.19.5, Fabric API, then put the jar in that instance's `mods` folder.

Pinned versions: Fabric API 0.161.0+26.2, Loom 1.18.4, Gradle 9.7.1. Minecraft 26.x uses Mojang names, so there is no Yarn mappings line.

## License

CC0 1.0. Public domain. Copy it, fork it, ship it, sell it, whatever.
