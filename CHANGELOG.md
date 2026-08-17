# Unreleased
* Fixed common colorless glass tags omitting vanilla glass.
    * Early fuel gauges, tanks, and pane-based smeltery recipes now accept vanilla glass before a smeltery or melter is available.
* Fixed Modern Foundry 1.21.1 island templates not being discoverable by Minecraft.
    * Moved all 25 dirt, sky, earth, blood, and End NBT templates to the singular `data/modernfoundry/structure/` resource path used by 1.21.1.
* Fixed Modern Foundry 1.21.1 floating slime island templates retaining legacy `tconstruct:` NBT IDs.
    * Translated all 25 dirt, sky, earth, blood, and End island templates to the active `modernfoundry` namespace so terrain and slime data markers load.
* Fixed the translated floating-island NBT templates retaining legacy string lengths.
    * Corrected all 2,500 namespace string-length fields so Minecraft can decode the palettes and place the island blocks.
* Fixed Modern Foundry Anvil colors staying stock until a nearby block update.
    * Reloaded anvils now invalidate their cached model data immediately, so the saved manyullyn/material texture is rendered without breaking another block.
* Fixed Anvil material textures not being written to chunk NBT.
    * Added explicit provider-aware `saveAdditional` hooks because the inventory block-entity save path bypasses Hilt's synchronized-data hook.
* Fixed Modern Foundry Anvils losing their material texture after a client/world restart.
    * Migrated retextured table and anvil material block-entity persistence to NeoForge 1.21.1's provider-aware save/load hooks.
* Documented Modern Foundry directory ownership and allowed edit boundaries in `AGENTS.md`.
* Added GitHub Actions release automation for Modern Foundry.
    * Builds Modern Foundry on every push and publishes only new `mod_version` releases.
* Limited release creation to new `mod_version` values.
    * Repeated pushes continue to build without creating duplicate releases for the same version.
* Fixed Linux GitHub Actions wrapper permissions for the ModernFoundry build.
* Updated the ModernFoundry workflow to fetch the required Hilt release JAR without rebuilding Hilt.
* Fixed Modern Foundry first-person tool and weapon animation handling.
    * Vanilla now owns ordinary equip and swing transforms while custom active-use transforms remain available.
* Fixed Hilt item-layer side UV sampling for Modern Foundry tools and weapons.
    * Added normalized per-side UVs with a 0.1-pixel inset and corrected non-square texture mask indexing while preserving the existing thin extrusion depth.
* Restored NeoForge's native re-equip detection for Modern Foundry tools and weapons.
    * Stack changes now trigger the vanilla equip interpolation while the existing tool-data comparison remains scoped to block-break reset behavior.
* Synchronized the current 4.0.1 build to the local Foundry Prism instance after identifying a stale animation-fix JAR.
* Fixed seared melter and fuel tank crafting and harvesting.
    * Restored the common `c:glass` tag and made both blocks harvestable with a stone pickaxe.
* Fixed ordinary Modern Foundry tools and weapons using the vanilla first-person renderer.
    * Ordinary `ModifiableItem` stacks now use vanilla equip and swing handling, while launcher subclasses retain their custom ranged-use transforms.
* Fixed missing Tinker Station and Tinkers Anvil tool-layout and part-slot icons.
    * Added the existing `modernfoundry:gui/tinker_pattern` directory to Minecraft's block atlas.
* Audited the 1.20.1 tool set against the Modern Foundry 1.21.1 port.
    * Confirmed all 26 requested tool-definition and item-model entries are shipped; no source rewrite was required.
* Restored NeoForge 1.21.1 block-break dispatch for modifiable tools.
    * Area-of-effect mining now reaches the existing server-side harvest logic instead of falling back to the center block only.
    * The event hit face is passed through so side-hit AOE expansion preserves the correct 3x3 plane.
* Restored stack-sensitive melee attributes for Modern Foundry tools and weapons.
    * Computed attack damage and attack speed now reach NeoForge's 1.21.1 item-stack attribute path, allowing vanilla equip interpolation, attack cooldowns, and swing handling.
* Built and verified the latest Modern Foundry 1.21.1-4.0.1 NeoForge JAR.
    * SHA-256: `40ea7a4189f8b3d2624667820c6caef7748386f468808dbc2e49c76acb44e1be`.
* Fixed Modern Foundry fluid contents becoming invisible with shader packs.
    * Fluid block-entity and projectile quads now use the vanilla shader-compatible translucent path, including Hilt's scaled-fluid helper calls.
* Completed the 1.20.1 tool, weapon, and tool-part parity pass for Modern Foundry 1.21.1.
    * Added the missing Jadeite material data, ribcage repair stats and traits, `float`, and worn-armor luck/fortune targeting.
    * Restored the shell and fiery material render fallbacks from the reference data.
    * Verified all 45 tool definitions, 98 material definitions, 93 material-stat files, 93 trait files, and 292 modifier files after the required 1.21.1 namespace/API translations.
* Fixed Enderslime, Skyslime, and Terracube hitboxes being larger than their rendered bodies.
    * Updated their Minecraft 1.21.1 slime base dimensions from the legacy `2.04F` value to `0.52F`.
