![Header](https://i.imgur.com/AqONaeU.png)

## Forge tools. Refine materials. Make the result yours.

Modern Foundry is an **unofficial** and **independent** NeoForge port of [Tinker's Construct](https://www.curseforge.com/minecraft/mc-mods/tinkers-construct) for 1.21.1. It brings modular tools, material parts, molten fluids, casting, modifiers, armor, ranged equipment, slime content, and foundry machinery into one coherent Minecraft experience.

The central idea is simple: the tool is not finished when it is crafted. Gather the right materials, shape the parts, melt and alloy resources, cast components, assemble the tool, and then tune it with upgrades and traits for the work ahead.

**Requires:** [Hilt](https://www.curseforge.com/minecraft/mc-mods/hilt)

## What is in the foundry

### Modular toolcrafting

Modern Foundry supports the full workshop loop through:

*   Part Builder patterns and material-aware part recipes
*   Tinker Station tool assembly, repair, part swapping, and renaming
*   Modifier Worktable upgrades and modifier management
*   Tinkers' and Scorched Anvils for advanced station layouts
*   Data-driven tool definitions, station slot layouts, material stats, and material traits
*   Repair kits, casts, tool recycling, and material-based repair paths

Tool parts include pick heads, hammer heads, axe heads, blades, adze heads, plates, bindings, handles, bow limbs, bow grips, arrow components, shield cores, armor plating, maille, and other specialized components.

### A working smeltery and foundry

The processing line is built around both seared and scorched structures. Depending on the job, players can use:

*   Melters, heaters, alloyers, Smeltery Controllers, and Foundry Controllers
*   Seared and scorched tanks, casting tanks, gauges, drains, ducts, chutes, channels, and faucets
*   Seared melters and fuel tanks use the common glass tag and are harvestable with a stone pickaxe
*   Casting Tables and Casting Basins for parts, ingots, nuggets, gems, plates, gears, wires, and special components
*   Fluid cannons, portable tanks, copper cans, and other fluid-handling tools
*   Data-driven melting, alloying, casting, container-filling, and cast-duplication recipes

This makes the foundry more than a single machine: it is a connected production system for turning raw resources and byproducts into useful materials and equipment.

### Tools for every job

The current tool library covers mining, excavation, farming, combat, ranged combat, exploration, and utility. Examples include pickaxes, sledge hammers, vein hammers, mattocks, pickadzes, excavators, hand axes, broad axes, kamas, scythes, daggers, swords, katanas, Fuma Shurikens, battle spades, rapiers, estocs, greatswords, pikes, lances, cleavers, longbows, crossbows, fishing rods, javelins, arrows, shurikens, throwing axes, staffs, melting pans, war picks, battlesigns, and swashers.

Integrated reference content also adds Rapier and Estoc leap behavior, Rapier sting attacks, and eight tiered Yoyos: wooden, stone, copper, iron, golden, diamond, netherite, and creative. The 1.21.1 Yoyos reference's optional compatibility tiers are also registered when their source mod is loaded, while Modern Foundry's own TConstruct materials are available natively. Yoyos use the reference entity interactions for server-side damage and collection. The Improvable modifier tracks tool experience and progression; apply it at the Foundry Anvil with one Nether Star and four Experience Bottles. Rapier sting damage and Improvable progression and XP sources remain configurable in `modernfoundry-common.toml`.

The native integrations suite adds cross-mod material fluids, storage, recipes, and equipment modifiers behind loaded-mod checks, so the base Modern Foundry feature set does not require those source mods. The additional native equipment suite adds the repeating crossbow, atlatl, seeking arrows, drying rack, soul shards, bronze materials, effects, potions, and its expanded modifier set.

The bundle also includes modifiable shields, slime-themed armor, slime wings, crystalshot ammunition, and other equipment built on the same material-and-modifier systems.

Ordinary Modern Foundry tools and weapons use vanilla-style first-person equip, attack-speed cooldown, and swing animations.

Server-side modifiable-tool block breaking uses the shared harvest path, preserving tool-specific area-of-effect mining and other block-break behavior.

### Native reference integrations

The native integration wave also includes Construct's Armory armor parts and accessories, Constructs Arsenal tools, Tinkers Bags, Tinkers Wands, six canonical Oreberry bushes, and the Slime Boots and Slime Sling gadgets. They use Modern Foundry registries, data, recipes, tags, models, translations, and optional-integration boundaries; the reference mods and Json Things are not runtime requirements.

Construct's Armory visual resources are namespaced under `assets/modernfoundry/textures/armory` and are covered by the bundled LGPL v3 notice and attribution records. Armory Expansion itself supplied material/trait JSON rather than bitmap assets. Its snapshot contains 21 Ice and Fire material records, 2 Matter Overdrive material records, and 45 Construct's Armory trait records. Those external-owned IDs remain an optional compatibility inventory: native equivalents are reused only where Modern Foundry already owns the behavior, and unsupported external traits are documented rather than replaced with invented gameplay.

Oreberries intentionally ships the six canonical IDs from the reference (`iron`, `gold`, `copper`, `tin`, `aluminum`, and `essence`). Registry mutation from arbitrary user JSON was not carried forward; safe growth, bonemeal, silk-touch, villager-trade, and world-generation settings remain in the native configuration/data path.

### Materials, fluids, and traits

Materials affect the statistics and behavior of the parts they make up. The repository includes materials such as cobalt, steel, slimesteel, amethyst bronze, rose gold, pig iron, cinderslime, Queen's Slime, manyullyn, hepatizon, knightmetal, Knightslime, soulsteel, and additional material families loaded from data.

The fluid system includes molten metals and alloys alongside material-specific fluids such as slime, blood, venom, magma, soul, and other processing or food-related fluids. Modern Foundry's fluid block-entity and projectile renderers use Minecraft's shader-compatible translucent path so tank contents and fluid effects remain visible with shader packs. Material stats, repair values, traits, tool slots, and modifier effects are kept separate so the combinations can be expanded without hard-coding every tool recipe.

### Modifiers and equipment progression

Modifiers let a tool specialize after assembly. Upgrade, ability, and trait families cover effects such as autosmelting, silk harvesting, magnetic pickup, fiery or freezing attacks, ranged projectile behavior, tank and energy storage, durability changes, reach extension, mobility, armor defenses, and tool interactions.

Tools store their materials, statistics, modifiers, and additional data as part of the tool stack. This allows the same construction system to support ordinary mining tools, combat weapons, bows, shields, armor, and unusual utility equipment.

### Slime and world content

Modern Foundry extends the foundry theme into the world with slime materials, crystals, foliage, trees, grass, vines, blocks, heads, particles, structures, and custom entities. Earth, sky, ender, magma, and other slime-related content connect exploration and world generation back to the materials used in tool construction.

Nether cobalt ore is a Diamond+ harvest block: diamond and netherite pickaxes can mine it for drops.

## The core progression

1.  Gather ores, wood, patterns, and crafting components.
2.  Build a Crafting Station, Part Builder, and Tinker Station.
3.  Use patterns and the Part Builder to create parts from the materials you have found.
4.  Build a Melter or a larger Smeltery/Foundry to process molten materials and alloys.
5.  Cast tool parts, ingots, upgrades, and special components.
6.  Assemble a tool from its parts and choose the combination that fits the job.
7.  Apply modifiers, repair the tool, and continue refining it as the world becomes more dangerous.

The goal is for material choice, part choice, and modifier choice to create meaningful differences in durability, mining speed, attack behavior, reach, utility, and survivability.

## Development status

Modern Foundry is under active development. The current source is forked from the original MIT Source Code, and AI was utilzied to assist in the porting process, as well as create promotional material such as the thumbnail and banner art.

## Automated builds and releases

Pushes to `Neo/1.21.1` run the GitHub Actions workflow in `.github/workflows/build.yml`. It fetches the pinned [Hilt](https://github.com/Modern-Mods/Hilt) release JAR required by the source, then builds only Modern Foundry. Hilt is not checked out or built by this workflow.

Each new `mod_version` publishes a GitHub release marked **Latest** with the current `ModernFoundry-1.21.1-<version>-NeoForge.jar`. Builds still run on every push, but release creation is skipped when that version already has a release. Increment `mod_version` when a new Modern Foundry release/build number is ready; Hilt is released independently.

## Credits and attribution

[Tinker's Construct - Original Mod](https://www.curseforge.com/minecraft/mc-mods/tinkers-construct)

Copyright (c) 2025-2026 Mrthomas20121
Source license: References/TinkersWeaponry-1.20.1/LICENSE

Yoyo runtime, recipes, enchantments, models, and tier textures adapted from the MIT Yoyos Relaunched 1.21.1 reference by iglee42

Copyright (c) 2018-2023 Jozsef Augusztiny
Source license: MIT, as declared by `References/Yoyos-1.21-Decompiled/META-INF/neoforge.mods.toml`

Native integrations and equipment content adapted from the MIT references `References/TCIntegrations-1.20.1` by Wendall Cada (wendall911) and `References/Tinkers-Thinking-1.20.1` by CreepingCreeper.

Source licenses: `References/TCIntegrations-1.20.1/LICENSE` and `References/Tinkers-Thinking-1.20.1/LICENSE`. The damage-blocking sound remains credited in `src/main/resources/assets/modernfoundry/sounds/_credits.txt` under its Creative Commons Attribution license.

Construct's Armory armor resources are adapted from C4's LGPL v3 project and retain the Tinkers' Construct-derived attribution required by that project. Armory Expansion material-data provenance and the optional external-material boundary are recorded in `src/main/resources/META-INF/licenses/armory-expansion.txt`.

Thanks to the SlimeKnights projects and contributors, NeoForge, Just Enough Items, and the other libraries and integrations that make this work possible.

Project source: [Modern-Mods/ModernFoundry](https://github.com/Modern-Mods/ModernFoundry)
