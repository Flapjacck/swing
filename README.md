# Swing

Client-only Fabric mod for Minecraft 26.2. Scales down the item in your first-person hand and lets you slow the swing animation. Nothing on the server changes, so this is visual only.

I made this because the usual "viewmodel" skyblock mods come packed with a pile of extra features I already get from other mods.

## Settings

In game, run `/swing`. That opens a dark settings panel with sliders and a number box for each value (0.10–1.00, step 0.01). Defaults are vanilla `1.00` / `1.00`. Values apply live, save to `config/swing.json` on Apply or Close, and reload next launch.

- Held item scale: size of the first-person held item. `1.00` is vanilla.
- Swing speed: arm swing animation. `1.00` is vanilla; lower is slower.

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
