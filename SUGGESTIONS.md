## 2026-08-16 - Suggestions after build and release automation
- Pin the Hilt checkout to a reviewed commit if reproducible dependency provenance becomes more important than always using the current `main` source.

## 2026-08-16 - Suggestions after duplicate-release gating
- No additional suggestions at this time.

## 2026-08-16 - Suggestions after Gradle wrapper permission fix
- No additional suggestions at this time.

## 2026-08-16 - Suggestions after released Hilt dependency path
- No additional suggestions at this time.

## 2026-08-16 - Suggestions after ModernFoundry-only workflow
- No additional suggestions at this time.

## 2026-08-16 - Suggestions after vanilla first-person tool animation fix
- Perform a client smoke test comparing a Modern Foundry sword and pickaxe with vanilla equivalents for equip interpolation, normal swing, offhand rendering, and active-use animations.

## 2026-08-16 - Suggestions after Hilt item-layer UV fix
- Perform client smoke testing with a Modern Foundry weapon/tool using transparent perimeter pixels and a non-square texture to confirm atlas sampling and side-mask behavior visually.

## 2026-08-16 - Suggestions after native re-equip detection fix
- No additional suggestions at this time; the existing first-person client smoke-test item remains the only outstanding validation.

## 2026-08-16 - Suggestions after Foundry instance synchronization
- Relaunch the synchronized Foundry Prism instance and perform the outstanding first-person client smoke test for equip interpolation, ordinary swings, offhand rendering, and active-use animations.

## 2026-08-16 - Suggestions after seared melter and fuel tank repair
- Perform a fresh-world client smoke test for the four seared tank recipes and stone-pickaxe harvesting; no additional code changes are suggested.

## 2026-08-16 - Suggestions after ordinary vanilla first-person rendering
- No additional suggestions at this time.

## 2026-08-16 - Suggestions after Tinker station atlas fix
- Perform a client smoke test after relaunching to verify every tool-layout icon and part-slot pattern in both the Tinker Station and Tinkers Anvil.

## 2026-08-16 - Suggestions after 1.20.1 tool parity audit
- Resolve the external Hilt 1.13 `DumpLootModifiers` command-registration exception, then run a fresh-world dedicated-server smoke test covering every requested tool category.
- Perform the remaining in-world client smoke tests for mining/harvest AOE, vein mining, tree felling, crop and plant handling, melee/severing, bow/crossbow/projectile use, melting, and both shields.

## 2026-08-16 - Suggestions after NeoForge area-of-effect block breaking
- Repair the missing `assets/modernfoundry/book/encyclopedia/fr_fr/armor/info.json` resource or its staging path so `runClient` can reach an in-world smoke test.
- After startup is clean, verify sledge, excavator, vein hammer, broad axe, scythe, and the remaining requested tool behaviors in a fresh world.

## 2026-08-16 - Suggestions after stack-sensitive tool attack attributes
- No additional suggestions at this time; the remaining equip and swing confirmation is the user's planned Prism client smoke test.

## 2026-08-16 - Suggestions after latest JAR build
- Perform the existing client and dedicated-server smoke tests before treating the fresh artifact as runtime-validated; the build and archive checks do not prove in-game behavior.

## 2026-08-16 - Suggestions after complete 1.20.1 tool and tool-part parity
- Run the remaining fresh-world client and dedicated-server smoke matrix for all requested tools, especially ordinary sword/dagger block hits, AOE tools, projectiles, the melting pan, war pick, battlesign, and both shields.
