# Unreleased
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
