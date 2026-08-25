# Unreleased
* Fixed smeltery fluid synchronization racing with external pipe transfers.
    * Network packets now snapshot fluid stacks before asynchronous encoding, preventing Mekanism and other compatible fluid pipes from disconnecting clients during imports or exports.
* Fixed Slime armor rendering on Minecraft 1.21.1.
    * Renders every armor material layer through NeoForge's texture hook instead of indexing only the first layer.
    * Applies dye tint only to dyeable layers and ignores armor items assigned to the wrong equipment slot.
* Fixed Nether cobalt ore drops when mined with diamond-tier tools.
    * Added cobalt ore to the vanilla pickaxe and diamond-tool tags; netherite tools qualify through the vanilla diamond-tier tag.
* Built and verified the latest Modern Foundry 1.21.1-4.1.4 NeoForge JAR.
    * SHA-256: `238b86893f814f95341f1fff10bc3bab8c704633f1f17dddb387688f35768ada`.
* Fixed Crafting Station recipes failing to consume compacted-grid inputs.
    * Centered or otherwise shifted 1x1 ore recipes now consume the actual backing slot, preventing infinite crafting and Shift + Right Click inventory filling.
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
* Restored complete block harvest-tag coverage from the Tinkers Construct 1.20.1 reference.
    * Restored the axe, hoe, pickaxe, and shovel mineable tags plus the wood, stone, iron, gold, diamond, and netherite tier tags.
    * Kept cobalt ore Diamond+ and seared fuel tanks/melters Stone-tier; all 93 material-stat files match the reference after namespace translation.
* Built and verified the updated Modern Foundry 1.21.1-4.1.4 NeoForge JAR.
    * SHA-256: `145dfb244cdade4dc47faadbef504974e1bae2b509f5fea2f117e787ff0e8fb8`.

* Fixed inventory blocks voiding their contents when opened, filled, or broken.
    * Registered NeoForge item-handler capabilities for stations, anvils, and all Tinkers' chests, and included patterns in the part-chest whitelist.
* Fixed sky slime leaves dropping as blocks instead of saplings.
    * Switched leaf loot generation to NeoForge's native shear-ability condition.
* Fixed black/purple missing textures in JEI modifier icons.
    * Added the existing `modernfoundry:gui/modifiers` directory to Minecraft's block atlas.
* Fixed Part, Tinkers', and Cast Chests deleting their contents when broken.
    * Each chest now drops and clears its live inventory before block-entity removal, and Cast Chests use normal drop handling.
* Increased Part, Tinkers', and Cast Chest slot limits to 64 items.
* Fixed Blood and Ichor slime leaves dropping themselves instead of their saplings.
    * Normal harvesting now follows the regular slime-leaf behavior: matching saplings can drop, while the leaf block is reserved for Silk Touch or shears.
* Fixed slime leaves treating Tinkers shear-capable tools as vanilla shears.
    * Leaf blocks now require the actual shears item or Silk Touch; Kamas, Scythes, and similar tools receive sapling drops instead.

* Fixed slime leaf items rendering invisible in hand and as dropped items.
    * Block-derived item color aliases now preserve an opaque alpha channel for tinted foliage models.
* Fixed Blood slime leaf loot failing to load.
    * Removed the stale reference to the nonexistent `modernfoundry:blood_slime_ball` item so normal harvesting can return Blood slime saplings.
* Fixed slime leaves returning leaf blocks instead of saplings.
    * Earth, Sky, Blood, Ichor, and Ender leaves now use sapling-only harvest outputs; existing slimeball and Fortune behavior is retained.
* Restored regular slime-leaf harvesting behavior.
    * Shears or Silk Touch now return the leaf block, while ordinary breaking rolls the matching sapling, including Greenheart saplings from Earth slime leaves.
    * Existing slimeball and Fortune behavior remains gated to ordinary harvesting.
* Built and verified the updated Modern Foundry 1.21.1-4.1.5 NeoForge JAR.
    * SHA-256: `86f1797b007f1fee023249d4b326e7e2f3a89b8584eb32b913d3ff54f4431a85`.

* Fixed smeltery item serialization with Minecraft 1.21.1 dynamic registries.
    * Melting inventories now use the active registry provider when saving and loading item stacks, preventing enchanted stacks from crashing world ticks.
    * Preserved the existing NBT schema and built-in-lookup compatibility overloads.
* Built and verified the latest Modern Foundry 1.21.1-4.1.6 NeoForge JAR.
    * SHA-256: `fffa8566c1cdc326eb8b93f9e3f7f71f8862615aa38c8c04f1495c458f855eb7`.
* Added native NeoForge fluid capability exposure to smeltery ducts.
    * Compatible fluid transport mods can now import from and export to ducts; the existing drain capability remains available.
* Added FTB Ultimine compatibility for Modern Foundry mining tools.
    * Primary and stone harvest tools are included without changing ordinary tool or weapon behavior.
