# ModernFoundry

![Header](https://i.imgur.com/AqONaeU.png)

## Forge tools. Refine materials. Make the result yours.

ModernFoundry is a NeoForge toolcrafting and material-processing project for Minecraft 1.21.1. It brings modular tools, material parts, molten fluids, casting, modifiers, armor, ranged equipment, slime content, and foundry machinery into one coherent Minecraft experience.

The central idea is simple: the tool is not finished when it is crafted. Gather the right materials, shape the parts, melt and alloy resources, cast components, assemble the tool, and then tune it with upgrades and traits for the work ahead.

## What is in the foundry

### Modular toolcrafting

ModernFoundry supports the full workshop loop through:

- Part Builder patterns and material-aware part recipes
- Foundry Station tool assembly, repair, part swapping, and renaming
- Modifier Worktable upgrades and modifier management
- Foundry and Scorched Anvils for advanced station layouts
- Data-driven tool definitions, station slot layouts, material stats, and material traits
- Repair kits, casts, tool recycling, and material-based repair paths

Tool parts include pick heads, hammer heads, axe heads, blades, adze heads, plates, bindings, handles, bow limbs, bow grips, arrow components, shield cores, armor plating, maille, and other specialized components.

### A working smeltery and foundry

The processing line is built around both seared and scorched structures. Depending on the job, players can use:

- Melters, heaters, alloyers, Smeltery Controllers, and Foundry Controllers
- Seared and scorched tanks, casting tanks, gauges, drains, ducts, chutes, channels, and faucets
- Casting Tables and Casting Basins for parts, ingots, nuggets, gems, plates, gears, wires, and special components
- Fluid cannons, portable tanks, copper cans, and other fluid-handling tools
- Data-driven melting, alloying, casting, container-filling, and cast-duplication recipes

This makes the foundry more than a single machine: it is a connected production system for turning raw resources and byproducts into useful materials and equipment.

### Tools for every job

The current tool library covers mining, excavation, farming, combat, ranged combat, exploration, and utility. Examples include pickaxes, sledge hammers, vein hammers, mattocks, pickadzes, excavators, hand axes, broad axes, kamas, scythes, daggers, swords, cleavers, longbows, crossbows, fishing rods, javelins, arrows, shurikens, throwing axes, staffs, melting pans, war picks, battlesigns, and swashers.

The bundle also includes modifiable shields, slime-themed armor, slime wings, crystalshot ammunition, and other equipment built on the same material-and-modifier systems.

### Materials, fluids, and traits

Materials affect the statistics and behavior of the parts they make up. The repository includes materials such as cobalt, steel, slimesteel, amethyst bronze, rose gold, pig iron, cinderslime, Queen's Slime, manyullyn, hepatizon, knightmetal, Knightslime, soulsteel, and additional material families loaded from data.

The fluid system includes molten metals and alloys alongside material-specific fluids such as slime, blood, venom, magma, soul, and other processing or food-related fluids. Material stats, repair values, traits, tool slots, and modifier effects are kept separate so the combinations can be expanded without hard-coding every tool recipe.

### Modifiers and equipment progression

Modifiers let a tool specialize after assembly. Upgrade, ability, and trait families cover effects such as autosmelting, silk harvesting, magnetic pickup, fiery or freezing attacks, ranged projectile behavior, tank and energy storage, durability changes, mobility, armor defenses, and tool interactions.

Tools store their materials, statistics, modifiers, and additional data as part of the tool stack. This allows the same construction system to support ordinary mining tools, combat weapons, bows, shields, armor, and unusual utility equipment.

### Slime and world content

ModernFoundry extends the foundry theme into the world with slime materials, crystals, foliage, trees, grass, vines, blocks, heads, particles, structures, and custom entities. Earth, sky, ender, magma, and other slime-related content connect exploration and world generation back to the materials used in tool construction.

## The core progression

1. Gather ores, wood, patterns, and crafting components.
2. Build a Crafting Station, Part Builder, and Foundry Station.
3. Use patterns and the Part Builder to create parts from the materials you have found.
4. Build a Melter or a larger Smeltery/Foundry to process molten materials and alloys.
5. Cast tool parts, ingots, upgrades, and special components.
6. Assemble a tool from its parts and choose the combination that fits the job.
7. Apply modifiers, repair the tool, and continue refining it as the world becomes more dangerous.

The goal is for material choice, part choice, and modifier choice to create meaningful differences in durability, mining speed, attack behavior, reach, utility, and survivability.

## Built for Minecraft 1.21.1

ModernFoundry targets NeoForge directly rather than treating legacy Forge code as a drop-in runtime. Registries, menus, recipes, materials, fluids, tool definitions, world content, networking, and client screens are organized around the Minecraft 1.21.1 and NeoForge APIs used by this branch.

The mod uses `modernfoundry` as its runtime namespace and resource identifier. The player-facing name is Modern Foundry.

## Development status

ModernFoundry is under active development. The current source contains a broad implementation of the workshop, tool, modifier, smeltery, foundry, fluid, world, and integration systems, but registry coverage does not by itself mean that every path is release-ready.

Expect balancing changes, API changes, content adjustments, and breaking changes while the project is completed. Client rendering, dedicated-server startup, fresh-world progression, world generation, multiplayer synchronization, smeltery transactions, and optional integrations should be tested in-game in addition to running automated checks.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.x; the current Gradle configuration is pinned to NeoForge 21.1.240
- Java 21 for development and Gradle builds
- Hilt, required by the current build and mod metadata; the local checkout is `../Hilt`
- JEI 19.x, optional but supported for recipe lookup and transfer integration
- JSON Things 0.9.9, optional when its compatibility integration is installed

ModernFoundry is designed as one consolidated mod bundle. Optional integrations are not required for the base tool, material, or foundry systems.

## Building and running

The repository includes the Gradle wrapper. From the repository root on Windows:

```text
.\gradlew.bat build
.\gradlew.bat test
.\gradlew.bat runClient
.\gradlew.bat runServer
.\gradlew.bat runData
```

The current build includes the sibling Hilt checkout from `../Hilt` and resolves `modernmods.hilt:FoundryLibrary` from that included build. Build Hilt first when refreshing the dependency or changing Hilt APIs.

Generated recipes, tags, loot, advancements, material data, tool definitions, station layouts, and other generated resources belong under `src/generated/resources`. Do not edit generated files directly; change the corresponding data provider under `src/main/java` and run the data task again.

For a clean local verification, use:

```text
.\gradlew.bat clean build --console=plain --no-daemon
```

Automated tests cover core material managers, tool definitions, tool statistics, tool-stack data, modifier data, recipes, and related serialization paths. They should be paired with client, dedicated-server, fresh-world, and multiplayer smoke tests before calling a feature complete.

## Repository layout

```text
src/main/java/       Runtime code, registries, menus, networking, and data providers
src/main/resources/  Hand-authored assets, translations, metadata, and base data
src/generated/       Datagen output; regenerated rather than edited by hand
src/test/             Automated tests and Minecraft test fixtures
gradle.properties     Minecraft, NeoForge, dependency, and project version settings
build.gradle          NeoForge build, run configurations, dependencies, and packaging
```

## Credits and attribution

ModernFoundry carries forward upstream toolcrafting and material-processing work alongside the Hilt library. The current source retains required license notices and attribution.

Thanks to the SlimeKnights projects and contributors, NeoForge, Just Enough Items, and the other libraries and integrations that make this work possible. Review [`LICENSE`](LICENSE) and retained source headers before redistributing code, assets, or built artifacts.

Project source: [Modern-Mods/ModernFoundry](https://github.com/Modern-Mods/ModernFoundry)
