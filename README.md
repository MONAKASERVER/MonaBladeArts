# MonaBladeArts

Paper-native blade combat for MONAKA SERVER. It provides a server-authoritative four-step ground combo, aerial chain, dash cut, draw cut, guard/just guard, projectile reflection, summoned sword, multi-hit finisher, combat rank, kill/refinement/soul state, CraftEngine models, and Slimefun guide categories.

## Controls

- Attack: four-step combo
- Sprint + attack: dash cut
- Airborne attack: aerial chain and plunge
- Right click: summoned sword (uses soul gauge)
- Sneak + right click: finisher
- Sneak: guard; starting guard immediately before damage performs a just guard

All damage and cooldown checks are performed by Paper. Optional clients may only submit input intents over `monabladearts:input`.

## Build

Java 25 and Paper 26.1.2 are required.

```text
gradle clean test build
```

## License and upstream study

Plugin code is MIT. Combat vocabulary and state-machine behavior were studied from the MIT-licensed source of the referenced modern blade-combat project at commit `6e2a0a092fb794d7ea56fd83452869674f3ab1c7`. Its art resources are explicitly excluded because they are All Rights Reserved. This repository contains no upstream models, textures, VMD files, sound files, namespaced assets, or compiled code.
