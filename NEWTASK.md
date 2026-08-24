# Modern Foundry Native Integration Port Plan

## Status and current boundary

This is a planning task only. The current request authorizes creating this
file; it does not authorize porting code, editing source or resources,
changing Gradle files, regenerating data, or updating project logs.

Implementation requires a separate explicit authorization. Until then, the
seven reference projects remain read-only research input and every
implementation checkbox below remains unchecked.

## Objective

Port and directly integrate the following reference content into Modern
Foundry for Minecraft 1.21.1 NeoForge:

- `References/Armory-Expansion-master`
- `References/constructsarmory-master`
- `References/ConstructsArsenal-main`
- `References/Oreberries-1.12.1`
- `References/TinkersBags-main`
- `References/TinkersWands-main`
- `References/TinkersConstruct-1.12`

The result is one native Modern Foundry feature set, not seven bundled addons.
All Java belongs below `modernmods.modernfoundry`; all runtime IDs, assets,
data, translations, configs, packets, and books use the `modernfoundry`
namespace. Players must not need the original mods, the Tinkers Construct
1.12 reference, Json Things, or a new runtime dependency to use the
integrated features.

## Target baseline

Reconfirm these values from the live checkout immediately before coding:

- Minecraft `1.21.1`
- NeoForge `21.1.240`
- Java `21`
- Mod ID and resource namespace `modernfoundry`
- Java package root `modernmods.modernfoundry`
- Current Modern Foundry version `4.1.6`
- Required Hilt version `1.13`
- JEI and JSON Things remain optional integrations

The current build excludes many legacy data-provider classes from the active
Java source set. Determine the real source of truth for each addition before
editing. Static resources under `src/main/resources` may be authoritative in
this checkout even when older provider classes still exist for reference.

## Existing Modern Foundry overlap to reconcile first

The live checkout already contains related content and systems from earlier
work. Do not duplicate or overwrite it:

- `TinkerTools` already contains `katana`, `fuma_shuriken`, `greatsword`,
  `rapier`, `estoc`, `battle_spade`, `pike`, `lance`, and yoyo items.
- `TinkerToolParts` already contains several weapon and armor parts, including
  great-blade, spear-head, slender-blade, plating, and shield-related parts.
- Modern Foundry already owns `ModifiableArmorItem`, `MultilayerArmorItem`,
  `ArmorDefinitions`, armor model reload/render systems, tool definitions,
  station layouts, modifier modules, inventory capabilities, `TinkerNetwork`,
  and the seared fluid cannon used by Tinkers Wands.
- Modern Foundry already contains slime-related content and movement/render
  infrastructure. Audit those owners before adding the Slime Boots or Slime
  Sling so the port does not duplicate slime colors, armor, fall, sound, or
  networking behavior.
- `improvable` already exists as a native modifier with armor leveling
  configuration. Compare it with Construct's Armory's optional Tinkers Tool
  Leveling integration before adding anything.
- A source search found no current native owner named `armor_station` or
  `armor_forge`; verify whether the existing Tinker station infrastructure can
  express the reference UI before creating a dedicated block/menu.
- The worktree contains unrelated and user-owned modified and untracked files.
  Preserve them and inspect overlap before any future implementation edit.

## Non-negotiable boundaries

- Never edit anything under `References/`; copy facts and behavior, not trees.
- Do not copy external package names, registry owners, mod IDs, network
  channels, config namespaces, or Json Things loaders into Modern Foundry.
- Do not add a generic addon framework, parallel tool serialization format,
  parallel inventory system, parallel network channel, or parallel data
  generator when an existing Modern Foundry seam can own the behavior.
- Preserve registry IDs when they do not collide. Resolve every collision in a
  written mapping before implementation; never silently replace existing
  content.
- Preserve tool and armor stack serialization, modifier state, repair,
  material replacement, inventory contents, and save/reload behavior.
- Keep common/server code free of client-only imports. Dedicated-server
  loading is a release requirement.
- Treat `src/generated/resources` as output. Change the active provider or
  source data, regenerate only when appropriate, and review the result.
- Preserve player-visible stats, recipes, layouts, controls, traits,
  modifiers, effects, sounds, particles, visuals, defaults, and restrictions.
  Every unavoidable 1.21.1/NeoForge adaptation must record its reason and
  manual test.
- Do not claim parity from compilation, data loading, or a built JAR alone.
  Separate automated, client, dedicated-server, fresh-world, persistence,
  and multiplayer evidence.
- Do not update `README.md`, `CHANGELOG.md`, or `TRACELOG.md` during this
  planning-only task. Update them only after implementation is authorized and
  actually completed.

## Reference inventory and integration strategy

| Reference | Observed content | Native integration strategy |
| --- | --- | --- |
| Armory Expansion | Forge 1.12.2 Java material/integration library. It loads material, armor-stat, trait, ore-dictionary, and alloy JSON; converts existing Tinkers materials into Construct's Armory core/plates/trim stats; adds client material rendering; and has optional Ice and Fire, Matter Overdrive, Construct's Armory, and custom-material integrations. | Do not port its `aelib` framework or external dependency graph. Translate the actual material-stat and armor-trait behavior into Modern Foundry's material, armor-stat, trait, tag, render, and optional-plugin systems. Preserve the source conversion formula only where it maps cleanly to current stats. External-mod content remains optional and must not be required by the base mod. |
| Construct's Armory | Forge 1.12.2 Java armor system. It adds four customizable armor pieces, six armor parts plus polishing kit, Armor Station, Armor Forge, soft obsidian, an armory book, armor material stats/traits, armor modifiers, accessory inventories, keybinds, armor events, client models, GUI previews, packets, and optional Tinkers Tool Leveling support. | Reuse Modern Foundry's modifiable armor, material stats, tool-stack state, station, modifier-hook, inventory, config, client model, and network systems. Add only the missing native armor block/menu/runtime seams needed for exact behavior. Reconcile with existing armor before registering anything. |
| Constructs Arsenal | MIT Json Things thingpack. Adds `throwing_card`, `buckler`, `quarterstaff`, `helix_blade`, and `scissors`; `card_top`, `card_bottom`, and `cardstock_cast`; `guillotine` and `range` modifiers; tool definitions, station layouts, recipes, tags, weapon attributes, models, textures, colors, and book content. | Convert all `construct_arsenal` and `tconstruct` references into Modern Foundry's native item, part, modifier, definition, station, recipe, tag, model, and book systems. Use existing tool actions/modules before adding runtime code. |
| Oreberries | Forge 1.12.1 Java mod. Defaults define Iron, Gold, Copper, Tin, Aluminum, and Essence bushes; configurable names, colors, drops, nugget/smelting results, growth, light, dimensions, biomes, heights, density, rarity, replacement blocks, and tradeability; age-based bush growth/harvesting, cactus damage, silk-touch rules, world generation, recipes, villager trades, item/block tinting, and an XP Essence Berry. | Port as native blocks/items, NeoForge config or data, tags/ingredients, recipes, worldgen, villager integration, client tint/model, and server-authoritative interaction logic. Replace Ore Dictionary and legacy registry hooks with current tags/registries. Prove a safe registry/config lifecycle before supporting arbitrary user-defined oreberry types. |
| Tinkers Bags | MIT Json Things thingpack. Adds `backpack`, `travelers_bindle`, and `canvas`; `small_stash`, `hoarding`, and `extra_pocket`; inventory modules, armor/tool definitions, station layouts, recipes, tags, armor/modifier models, colors, textures, and book pages. | Reuse Modern Foundry's inventory capability, inventory menu module, armor item/model system, tool-stack serialization, and existing armor/staff definitions. Preserve slot counts, key behavior, modifier restrictions, and inventory persistence. Resolve the `travel_sack` semantic collision with Construct's Armory explicitly. |
| Tinkers Wands | MIT Json Things thingpack. Adds `fluid_wand` built from two bow limbs and a seared fluid cannon, with 15 block amount, 0.4 use speed, tank/spitting traits, 2D circle AOE, dual-option interaction, tool definition, station layout, recipe, tags, models, textures, and book pages. | Port as native data plus the smallest required item registration. Reuse Modern Foundry's existing seared fluid cannon, tank, spitting, AOE, dual-interaction, tool, station, recipe, and client systems. No custom runtime class is justified unless a parity check proves the existing modules insufficient. |
| Tinkers Construct 1.12 gadgets | MIT-licensed Tinkers Construct 1.12 gadget content containing `slimesling` and `slime_boots`. Both have six slime-type metadata variants, with five visible creative variants (`green`, `blue`, `purple`, `blood`, and `magma`); the boots are feet-only, unstackable, tinted armor with fall bouncing and reduced sneaking fall damage, while the sling charges on the ground, requires a block ray trace, launches opposite the look vector, plays its sound, and preserves bounce motion. | Port the two gadgets as native Modern Foundry items using the existing armor, slime-color, fall, movement, use, sound, network, recipe, creative-tab, and client-render systems. Preserve the six recipe variants and legacy bounce/launch behavior while adapting motion synchronization to current server-authoritative NeoForge hooks. Do not copy the legacy `slimeknights.tconstruct` package or require Tinkers Construct at runtime. |

## Licensing and attribution gate

Resolve this before copying source-derived code or assets:

- Construct's Armory is LGPL v3, copyright C4, and includes modified or
  derived Tinkers' Construct material/assets that require their attribution.
- Constructs Arsenal is MIT, copyright Xenon372, 2026.
- Tinkers Bags is MIT, copyright Xenon372, 2026.
- Tinkers Wands is MIT, copyright Xenon372, 2025.
- Tinkers Construct 1.12's README states that its code, textures, and
  binaries are MIT licensed; inspect its alternate-license notices and the
  specific slime gadget assets/sounds before redistribution.
- Oreberries is GPL v2 or later, copyright Joseph C. Sible. Its README also
  identifies reused 1.7.10 Tinkers' Construct resources under CC0 and CC-BY
  3.0. Keep those notices separate from Oreberries' GPL notice and verify each
  texture's redistribution terms.
- Armory Expansion identifies YaibaToKen and credits the MMDLib/MMD
  community in metadata, but no repository license file was found during
  reconnaissance. Treat that as an unresolved legal blocker: do not copy its
  source or assets until permission or a usable license is established.
- Do not copy Forge MDK boilerplate, dependency license text, or stale
  external-mod assets as if they were feature attribution.

The final JAR must retain applicable license/notice/author/asset credits under
the project's existing attribution convention. Record any source or asset
that cannot legally be redistributed and its clean replacement or omission.

## Namespace and identity mapping

| Original concern | Required result |
| --- | --- |
| Java | Adapt all new Java below `modernmods.modernfoundry...`; never retain `c4.conarm`, `josephcsible.oreberries`, `org.softc.armoryexpansion`, `slimeknights.tconstruct`, or another external package tree. |
| Runtime IDs | Use `modernfoundry:<original_content_name>` when free. Use a documented canonical Modern Foundry ID for collisions such as armor, storage, reach, range, or existing weapon content. |
| Assets/data | Convert `assets/conarm`, `assets/oreberries`, `assets/construct_arsenal`, `assets/tinkers_bags`, `assets/tinkers_wands`, and the Tinkers Construct 1.12 `assets/tconstruct` gadget/model/recipe/sound paths to the active `assets/modernfoundry` and `data/modernfoundry` conventions. Keep legitimate `minecraft` and supported common-tag references only. |
| Parts/stats | Extend `TinkerToolParts`, existing material/stat types, armor definitions, and casts. Add a new part only when an existing part cannot represent the reference stat type or visual role. |
| Tools/armor | Extend `TinkerTools`, `ToolDefinitions`, `ArmorDefinitions`, and existing modifiable item classes. Do not make data-only tools depend on Json Things at runtime. |
| Modifiers/traits | Reuse an existing semantic match. Add a native modifier or hook only for behavior that is genuinely absent; compare names, recipes, slot rules, levels, and event behavior first. |
| Config | Use Modern Foundry's config owner and current validation/range conventions. Preserve reference defaults and document any renamed or removed setting. |
| Networking | Extend `TinkerNetwork` and existing packet patterns. Validate player, level, stack, slot, menu, target, and permissions on the server. |
| Client | Register colors, property overrides, armor/accessory models, item models, particles, keybind responses, screens, and render layers through current client-only entry points. |
| Optional integrations | JEI, JSON Things, Ice and Fire, Matter Overdrive, and other external mods may recognize or extend native content, but absence of any optional integration must not prevent base loading. |

## Behavior parity contract

Before implementing each feature group, create a source-to-target parity table
covering:

- Registry IDs, display names, creative-tab placement, book placement, and
  translation keys.
- Parts, casts, materials, slot layouts, stat types, recipes, tags, and
  crafting/station restrictions.
- Base stats, attributes, traits, modifier levels, costs, incompatibilities,
  armor slots, tool actions, and configurable defaults.
- Use, attack, block, projectile, AOE, movement, interaction, growth,
  collision, damage, cooldown, sound, particle, animation, and retraction
  behavior.
- Fall cancellation/bounce thresholds, sneaking fall damage, sling charge
  timing, ground and block-target preconditions, launch vector, motion
  synchronization, and bounce-state expiration for the slime gadgets.
- Stack components/NBT, modifier data, inventories, config reload behavior,
  death/drop handling, save/reload, chunk unload/reload, and repair/part
  replacement behavior.
- Client colors, models, armor/accessory layers, GUI state, tooltips, and
  packet synchronization.
- License, copyright, author, sound, texture, and book attribution.

Every missing or adapted entry must state the reason, the exact target
behavior, and the manual test that covers it. Do not silently rebalance or add
fallback behavior that the references do not provide.

## Phased implementation plan

### Phase 0 - Freeze the contract, inventory conflicts, and clear licenses

- Re-read `AGENTS.md`, `TASK.md`, this file, `gradle.properties`,
  `build.gradle`, and the active registration/data-generation patterns.
- Confirm Minecraft, NeoForge, Java, Hilt, mod version, namespace, source-set
  exclusions, optional dependencies, and current output artifact.
- Record the current dirty worktree and identify any overlap before editing
  `TinkerTools`, `TinkerToolParts`, `TinkerModifiers`, armor, data, config,
  client, or network surfaces.
- Inventory every reference registry, source class, event hook, mixin or
  client hook, config field, packet, screen/menu, item/block/entity, recipe,
  tag, model, texture, sound, book page, and license notice.
- Build a collision matrix for existing armor, storage modifiers, reach/range,
  staff tools, shield tools, fluid tools, material IDs, and oreberry IDs.
- Build a module-coverage matrix for `inventory`, `inventory_menu`, `tank`,
  `spitting`, circle AOE, dual interaction, blocking, throwing, returning,
  shears, sweeping, sling knockback, armor hooks, and worldgen.
- Resolve the Armory Expansion license blocker and every third-party asset
  attribution decision before copying any source-derived implementation or
  texture.

**Exit gate:** a complete inventory, collision map, module map, attribution
record, and implementation owner exists for every runtime path. No unresolved
license blocker remains.

### Phase 1 - Native Construct's Armory foundation

- Compare the reference four-piece `helmet`, `chestplate`, `leggings`, and
  `boots` with current `travelers` and `plate` armor. Decide whether an
  existing item is an exact semantic match or whether a distinct canonical ID
  is required; record the mapping before registration.
- Port the armor core model: `helmet_core`, `chest_core`, `leggings_core`,
  `boots_core`, `armor_plate`, `armor_trim`, and `polishing_kit`, including
  part-builder patterns, casts, material stat types, repair behavior, and
  station restrictions.
- Reproduce the Armor Station and Armor Forge behavior, preview/compact GUI
  option, slot validation, selection/text packets, output validation, and
  server-side recipe transaction. Reuse Tinker station/menu infrastructure
  wherever it can express the same behavior.
- Port soft obsidian, the armory book, recipes, models, blockstates, textures,
  translations, creative placement, and any book unlock/spawn configuration.
- Map `ArmorCore` state, armor defense/toughness calculations, attributes,
  broken behavior, durability, material display, tooltips, and repair hooks to
  current `ModifiableArmorItem` and armor stat APIs. Preserve serialized data.
- Inventory every armor modifier and accessory from `ArmorModifiers`:
  Speedy, Parasitic, Powerful, Telekinetic, Dexterous, Emerald, Diamond,
  Amphibious, Waterwalk/Frost Walker, Sticky, Shulkerweight, High Stride,
  Glowing, Concealed, Mending, Fire/Projectile/Blast/All Resistant,
  Reinforced, Soulbound, Polished, Extra Trait, Travel Belt, Potion Belt,
  Travel Sack, Travel Goggles, Travel Night, Travel Soul, Travel Sneak, and
  Travel Slow Fall.
- Inventory every armor material trait from `ArmorTraits`, including
  Aquaspeed, Lightweight, Enderport, Combustible, Absorbent, Magnetic levels,
  Tasty, Baconlicious, Cheapskate/Cheap, Vengeful, Invigorating, Slimey
  variants, Heavy, Ambitious, Blessed, Spiny, Mundane levels, Aridiculous,
  Petravidity, Superhot, Shielding, Steady, Skeletal, Calcic, Autoforge,
  Alien, Infernal, Subterranean, Dramatic, Indomitable, Prideful, Rough,
  Ecological, Duritae, Dense, Voltaic, Bouncy, and Featherweight.
- For every listed modifier/trait, reuse current module/hook behavior when
  equivalent; otherwise port the smallest native hook. Cover equipment change,
  tick, jump, pickup, heal, hurt, damage, knockback, fall, repair, FOV,
  visibility, keybind, potion cleanup, rendering, and incompatibility paths.
- Port accessory inventories and controls without loss or duplication:
  nine-slot hotbar swap, seven-slot potion belt, 27-slot travel sack, toggle
  state, armor damage rules, goggles vision/zoom/soul particle behavior,
  night vision cleanup, invisibility/visibility, slow fall, and accessory
  models.
- Compare Construct's Armory leveling settings and optional integration with
  native `improvable`; reuse native persistence and network logic where the
  semantics match instead of creating a second leveling format.
- Move `spawnWithBook`, compact GUI, bouncy durability, and leveling defaults
  into the existing config system with current range validation.

**Exit gate:** all four armor pieces, parts, construction paths, modifiers,
traits, accessory inventories, client layers, and config behavior work without
Construct's Armory or Tinkers Tool Leveling installed. Stack copy, repair,
part replacement, save/reload, death/drop, and multiplayer inventory tests
preserve all state.

### Phase 2 - Armory Expansion material and trait parity

- Inventory the `conarm`, `iceandfire`, `matteroverdrive`, and custom-material
  JSON sets separately. Record every material ID, core/plates/trim stat,
  render type, color, trait, ore-dictionary entry, alloy, and external owner.
- Map material stats into Modern Foundry's active material data and armor stat
  providers. Do not copy the old JSON integration framework or retain
  `armoryexpansion`, `conarm`, `iceandfire`, or `matteroverdrive` as a required
  namespace/dependency.
- Preserve the observed Construct's Armory conversion behavior where it is
  still intended: source head/handle/extra stats are converted using the
  reference `1.25` multiplier, with durability clamped to `1..120`, defense
  to `0..50`, and toughness to `0..5`. Recalculate only when the current stat
  model requires an explicit, documented adaptation.
- Translate compatible armor traits into native material traits and record
  unsupported external trait identifiers such as traits owned by absent
  addon mods. Do not invent replacements that change gameplay.
- Port material rendering/color fallback behavior through current material
  render and armor texture systems. Verify tinting, animated textures, broken
  armor, and multiplayer client projection.
- If optional Ice and Fire or Matter Overdrive compatibility is retained,
  isolate it behind existing optional-plugin boundaries and test the base mod
  with each integration absent. Do not add those mods to the base dependency
  graph.

**Exit gate:** every legal and supported reference material has a documented
  native ID/stat/trait/render mapping; external content is optional; no stale
  external namespace remains in the shipped native data.

### Phase 3 - Constructs Arsenal native tools

- Register and integrate `throwing_card`, `buckler`, `quarterstaff`,
  `helix_blade`, and `scissors` through the existing item/tool registries and
  creative tab.
- Port `card_top` as the arrow-head stat part, `card_bottom` as the fletching
  stat part, and `cardstock_cast`; preserve throwing-card stack size,
  material-name behavior, projectile stats, velocity, accuracy, and water
  inertia.
- Port buckler from shield core, bow limb, and bow grip with its attack/block
  stats, durability multiplier, blocking, throwing, returning, block angle,
  and block amount.
- Port quarterstaff from two tool handles, adze head, and shield core with
  range, blocking, circle AOE, attack/durability multipliers, and slot layout.
- Port helix blade from four scaled small blades and a tough handle with silky
  AOE shears, sword digging, cobweb behavior, sweep range, mining overrides,
  attack/durability/mining multipliers, and its limited upgrades.
- Port scissors from scaled head/handle stats with shears, guillotine,
  severing level 3, hoe digging, scythe effectiveness, wool/vine/glow-lichen
  mining overrides, AOE, and sweep behavior.
- Port `guillotine` using native sling-knockback behavior only if its damage
  multiplier `2.25`, draw-time multiplier `1.5`, and zero force behavior match;
  otherwise add the smallest missing module or hook.
- Compare `range` against current reach/attribute modifiers before adding it.
  Preserve the reference +2 attack/reach distance per level and offhand/mainhand
  slot rules without creating duplicate modifier semantics.
- Convert all tool definitions, station layouts, recipes, tags, weapon
  attributes, colors, models, textures, translations, and book pages to
  `modernfoundry` data. Replace all `tconstruct` tag ownership with the
  corresponding native Modern Foundry tags.

**Exit gate:** all five tools construct in a fresh world, have the reference
stats/actions/recipes, render in hand and in inventory, and pass combat,
blocking, projectile, AOE, mining, and modifier checks without Json Things.

### Phase 4 - Tinkers Bags native storage tools and modifiers

- Register `backpack`, `travelers_bindle`, and `canvas` through native tool,
  armor, and part ownership. Preserve backpack chestplate behavior, leather
  default material, bindle fixed material-name behavior, and all stack limits.
- Port backpack composition: chestplate plating scaled `0.65`, canvas,
  two shield cores, default material rules, attack/durability multipliers,
  hoarding/heavy traits, and one ability/two upgrade slots.
- Port travelers bindle composition: two bow limbs, bowstring, five upgrades,
  leather build requirement, and the level-one small stash trait.
- Port `small_stash` as three inventory slots per level opened on any key,
  `hoarding` as nine slots per level opened with shift, and `extra_pocket` as
  three slots per level opened with shift. Preserve tooltip and level-display
  behavior, recipe/salvage rules, and modifier tags.
- Reuse `ToolInventoryCapability`, `InventoryMenuModule`, existing tool menus,
  synchronization, and armor interaction hooks. Validate menu ownership,
  stack identity, slot bounds, insertion/extraction, and server authority.
- Resolve the overlap between Tinkers Bags' storage behavior and Construct's
  Armory's `travel_sack`, `travel_belt`, and `potion_belt` before registration.
  Keep distinct player-visible behavior unless an exact semantic match is
  proven, and document any canonical ID mapping.
- Port backpack/bindle armor and item models, modifier models/icons, material
  color handlers, textures, recipes, tags, translations, and book content.

**Exit gate:** storage contents survive inventory movement, modifier changes,
repair, part replacement, death/drop, save/reload, chunk unload/reload, and
multiplayer menu access. Invalid packets cannot open or mutate another
player's tool inventory.

### Phase 5 - Tinkers Wands native fluid wand

- Register the native `fluid_wand` item and translate its creative placement,
  tool definition, station layout, recipe, tags, models, textures,
  translations, and book pages.
- Reuse the existing seared fluid cannon part and verify the current registry
  ID before writing data. Preserve the two bow-limb parts, 15 block amount,
  0.4 use speed, three upgrades, one ability slot, spitting and tank traits,
  non-3D diameter-one circle AOE, and dual-option interaction.
- Verify fluid fill/drain, tank capacity, consumption, block/entity targeting,
  AOE boundaries, hand selection, cooldown/use timing, client animation,
  and dedicated-server behavior. Add runtime code only if an existing native
  module cannot reproduce the reference behavior.
- Replace all `tconstruct` and `tinkers_wands` ownership in tags, icons, book
  content, and models with native Modern Foundry ownership while preserving
  legitimate vanilla/common tag references.

**Exit gate:** the wand is constructible and usable without the reference
thingpack or Json Things, preserves fluid transactions without loss or
duplication, and remains synchronized for two players.

### Phase 6 - Oreberries native blocks, items, config, and worldgen

- Freeze the canonical default content from the reference JSON: Iron, Gold,
  Copper, Tin, Aluminum, and Essence. Record the exact names, colors,
  tooltips, nugget/smelting outcomes, rarity, density, heights, size chance,
  light rules, tradeability, and drop counts.
- Decide the supported configuration contract before registering blocks. The
  reference creates registry objects from `oreberries.json`; prove that a
  NeoForge-safe pre-registration configuration path exists. If arbitrary
  user-defined registry IDs cannot be made deterministic and reload-safe,
  ship the canonical IDs and expose only safe data/config tuning rather than
  mutating registries at runtime. Record that ceiling instead of hiding it.
- Port age `0..3` bushes, random growth, optional bonemeal growth, placement
  soil/light checks, left/right harvest, berry counts, cactus damage,
  pathfinding damage type, collision/render bounds, non-suffocation, and
  silk-touch harvesting/drop behavior.
- Port oreberry and nugget items, including configurable display names,
  tooltips, colors, optional Essence Berry XP behavior, ingot/nugget recipes,
  berry smelting, and missing-result handling.
- Replace Ore Dictionary lookups with current item tags and validated
  ingredients. Integrate native material/common-metal tags only where the
  reference actually supports the material; do not create duplicate metal
  registries or silently change outputs.
- Port worldgen clusters, rarity/density, preferred/min/max heights, size
  distribution, replace-block rules, dimension and biome whitelist/blacklist
  behavior, and the flat-world exclusion through the current worldgen
  configuration/feature ownership.
- Port the optional Tinker villager profession/trades through current
  villager registration. Preserve the configurable trade toggle and prevent
  invalid items or empty ingredient lists from producing broken trades.
- Port client tint handlers, four growth stages, bush/item blockstates,
  inventory models, animated texture metadata, creative-tab placement, and
  translations through current client registration.
- Preserve GPL, Tinkers' Construct resource attribution, and any asset
  replacement decision in the final notice files.

**Exit gate:** each canonical bush grows, harvests, drops, smelts, generates,
renders, and trades correctly in a fresh world. Configured dimensions/biomes
and silk-touch/bonemeal boundaries are tested, and no duplicate drops,
registry mutation, or worldgen performance issue remains.

### Phase 7 - Tinkers Construct 1.12 Slime Boots and Slime Sling

- Inventory the reference `slime_boots` and `slimesling` registry IDs, six
  slime-type metadata values, five visible creative variants, six recipes per
  gadget, textures, models, tint handlers, translations, book content,
  sounds, and license/asset notices.
- Register native `slime_boots` and `slimesling` IDs only after checking for
  existing Modern Foundry collisions. Preserve feet-only equipping,
  unstackability, zero armor attributes, color/overlay rendering, and the
  intended creative-tab placement.
- Reproduce the boots' fall behavior: bounce for falls over the reference
  threshold when not sneaking, cancel normal fall damage, preserve the
  bounce motion, and apply the reference reduced fall damage while sneaking.
  Use current fall and movement hooks and keep damage/motion decisions
  server-authoritative.
- Reproduce the sling's hold/use presentation, ground-only activation, charge
  curve and cap, block-ray-trace precondition, inverse-look launch vector,
  vertical launch scaling, sound, and bounce-state handling. Use the existing
  network pattern for client motion synchronization only where current
  NeoForge movement replication requires it; validate the sender and player
  state on the server.
- Reuse existing slime colors, armor models, item tinting, recipes, creative
  tabs, sounds, movement, and networking. Add a new runtime hook only when a
  parity check proves the existing owner cannot express the behavior.

**Exit gate:** all five visible variants and the fallback metadata behavior
load and craft correctly, boots bounce and sneak-dampen falls as specified,
the sling launches only under the correct preconditions, and both gadgets
remain synchronized in single-player, dedicated-server, and two-player
tests without Tinkers Construct installed.

### Phase 8 - Cross-feature hardening

- Audit all Java imports, registry owners, resource paths, translation keys,
  tags, configs, packets, books, models, textures, and generated output for
  stale external namespaces.
- Check every new tool and armor piece through existing part replacement,
  material stats, modifier slots, repair, durability, anvil/station, crafting,
  inventory, creative-tab, book, and recipe flows.
- Check the interaction matrix for storage modifiers, armor accessories,
  `improvable`, fluid tanks, shields, projectile tools, reach modifiers,
  existing armor, and current dirty-worktree additions.
- Test with neither JEI nor JSON Things, JEI only, JSON Things only, and both
  present. Test optional external compatibility mods absent and present where
  an optional plugin is intentionally retained.
- Review server authority and packet validation for armor menus, accessory
  toggles, inventory menus, fluid wand use, villager trades, and any client
  visual state.
- Check that slime boots and sling motion do not bypass fall-damage,
  movement, cooldown, sound, permission, or server-authority rules.
- Review performance-sensitive paths: armor tick hooks, accessory entity
  scans, inventory synchronization, oreberry random ticks/worldgen, tint/model
  reloads, and modifier event fan-out.
- Review the complete diff for unrelated changes and preserve all existing
  user-owned worktree edits.

**Exit gate:** no unresolved namespace, dependency, persistence, transaction,
security, performance, compatibility, or attribution issue remains.

### Phase 9 - Verification, attribution, and release readiness

- Run relevant compile, test, data, resource, and JSON validation tasks using
  the checkout's actual Gradle configuration.
- Run the clean production build:

  ```text
  .\gradlew.bat clean build --console=plain --no-daemon
  ```

- Run configured data generation only after verifying which providers are
  active; inspect generated-resource diffs instead of accepting bulk output.
- Inspect the built JAR for all seven feature groups, expected
  `modernmods.modernfoundry` classes, `modernfoundry` assets/data, retained
  licenses/credits, and absence of stale `conarm`, `oreberries`,
  `armoryexpansion`, `construct_arsenal`, `tinkers_bags`, `tinkers_wands`, or
  Json Things runtime requirements.
- Run `git diff --check` and review the complete diff.
- Run `runClient` and `runServer` where applicable. Perform fresh-world
  checks for armor construction/GUI, every Arsenal tool, both Bags tools,
  fluid wand transactions, all canonical oreberries, and both slime gadgets.
- Perform dedicated-server/two-player checks for armor packets and menus,
  accessory toggles, storage synchronization, fluid wand use, slime boot fall
  behavior, sling motion synchronization, entity damage, save/reload, chunk
  unload/reload, invalid input, and optional integrations.
- Update player documentation, `CHANGELOG.md`, and `TRACELOG.md` only after
  implementation is complete. Report build evidence separately from manual
  coverage and do not claim an unperformed smoke test.

## Ownership and file map for implementation

| Responsibility | Existing owner/pattern | Boundary |
| --- | --- | --- |
| Tools and parts | `TinkerTools`, `TinkerToolParts`, `ToolDefinitions`, `TinkerSmeltery` casts | Extend native registries; do not create an addon registry. |
| Armor | `ModifiableArmorItem`, `MultilayerArmorItem`, `ArmorDefinitions`, armor stat types, armor model manager | Reuse current armor stack/state/render systems; add dedicated station code only if parity requires it. |
| Modifiers and traits | `TinkerModifiers`, existing modifier modules/hooks, native modifier data | Compare semantics before adding IDs; no duplicate range, storage, leveling, or armor behavior. |
| Tool data | Active native `data/modernfoundry` resources and providers confirmed during Phase 0 | Do not assume excluded legacy providers are active; generated output is not source. |
| Storage | `ToolInventoryCapability`, `InventoryMenuModule`, existing menus/network | Server owns inventory mutation; preserve stack-local state and prevent dupes/loss. |
| Fluid wand | Existing seared fluid cannon, tank/spitting/AOE/dual-interaction modules | Prefer data-only integration; add runtime behavior only for a demonstrated gap. |
| Oreberries | Current block/item/worldgen/config/tag/recipe registration patterns | Use deterministic NeoForge registries and current tags; no legacy Ore Dictionary or unsafe runtime registration. |
| Slime gadgets | Current armor/item/fall/movement/sound/network/client and recipe patterns | Reuse native slime and movement ownership; preserve server-authoritative bounce/launch behavior without a legacy Tinkers Construct runtime dependency. |
| Client | Current client bootstrap, `TinkerClient`, `ToolClientEvents`, armor model/color systems | Keep client-only imports and render state client-side. |
| Network | `TinkerNetwork` and current packet/menu patterns | Validate sender, level, stack, menu, slot, target, and permissions server-side. |
| Optional integrations | Existing plugin boundaries for JEI/JSON Things and other mods | Optional code must not break the base mod when absent. |
| Attribution | Existing `META-INF/licenses`/root documentation convention | Preserve every applicable license, author, credit, and asset notice. |

## Main risks and controls

| Risk | Control |
| --- | --- |
| Current content collides with the port | Freeze a registry/canonical-ID map in Phase 0 and compare behavior before merging. |
| Old 1.12 Java APIs are copied into 1.21.1 | Port call sites against current NeoForge/Hilt APIs; do not create a compatibility shim for one legacy class. |
| Data-only JSON is mistaken for runtime behavior | Trace every module, hook, item type, and event; add runtime code only for a proven gap. |
| Construct's Armory armor state is lost | Test copy, repair, replacement, modifier edits, death/drop, inventory movement, save/reload, and chunk unload/reload. |
| Accessory or bag inventories duplicate or lose items | Use one native inventory capability, validate menus on the server, and test every transfer path. |
| Oreberries config mutates registries unsafely | Prove deterministic pre-registration or limit the supported config surface to safe data/config changes. |
| Oreberries worldgen is too expensive | Use bounded feature placement and validate chunk-generation cost in representative worlds. |
| Legacy slime gadget motion diverges on 1.21.1 | Compare fall thresholds, charge timing, launch vector, bounce preservation, and client/server synchronization against the reference; test single-player, dedicated-server, and two-player cases. |
| External material/trait IDs are unavailable | Keep external integrations optional and record unsupported mappings instead of inventing behavior. |
| Unlicensed Armory Expansion source/assets are copied | Stop at the licensing gate until permission or a usable license exists. |
| Tinkers Construct 1.12 alternate asset notices are missed | Inspect the reference notices and each slime gadget texture/sound before redistribution; replace or omit any asset that is not cleared. |
| Generated resources retain stale namespaces | Generate from the active source, run namespace audits, and inspect the final JAR. |
| Build success is mistaken for parity | Require client, dedicated-server, fresh-world, persistence, and multiplayer evidence for applicable phases. |

## Completion criteria

The integration is complete only when all of the following are true:

- All seven reference feature groups are available from one Modern Foundry JAR,
  or every documented legal/technical exclusion has an explicit decision.
- No original mod, Json Things pack, external runtime dependency, or external
  namespace is required for the base features.
- Native armor, tools, storage, fluid, worldgen, config, client, network,
  recipe, tag, book, and material systems own the integrated content.
- Existing Modern Foundry IDs and behavior are not silently replaced.
- Player-visible behavior, stats, recipes, controls, persistence, and defaults
  are preserved, or every adaptation has a reason and test.
- Dedicated-server loading, optional-integration absence, fresh-world use,
  multiplayer synchronization, save/reload, chunk unload/reload, and invalid
  input are verified where applicable.
- License, author, sound, texture, book, and third-party attribution is
  retained and distributable.
- Build, data/resource validation, artifact inspection, diff review, and the
  required project documentation updates are complete.

## Verification snapshot - 2026-08-23

- [x] Native implementation and resource integration for all seven reference groups is present in the current checkout.
- [x] Fresh `runServer --rerun-tasks` reached `Done (4.948s)` on Java 21 / NeoForge 21.1.240.
- [x] `clean build` passed, including Gradle `test` and `check`.
- [x] The final JAR contains the native resource families, generated data, runtime classes, and all nine bundled license notices.
- [x] The targeted external namespace audit and `git diff --check` passed.
- [ ] Client/fresh-world gameplay and visual smoke, persistence/chunk-reload, two-player synchronization, and optional-integration parity remain open; they were not performed in this release-gate execution.

The phase boxes below remain unchecked where their exit gates require manual
coverage that was not performed. The checked snapshot above records only
implementation, server-bootstrap, build, archive, attribution, and static
audit evidence.

## Implementation checklist

- [ ] Phase 0: contract, overlap inventory, collision map, module map, and licensing.
- [ ] Phase 1: Construct's Armory armor, stations, modifiers, traits, accessories, and config.
- [ ] Phase 2: Armory Expansion material stats, traits, rendering, and optional compatibility.
- [ ] Phase 3: Constructs Arsenal native tools, parts, modifiers, data, and visuals.
- [ ] Phase 4: Tinkers Bags native tools, inventories, modifiers, menus, and visuals.
- [ ] Phase 5: Tinkers Wands native fluid wand and fluid interaction parity.
- [ ] Phase 6: Oreberries blocks, items, config, worldgen, recipes, trades, and visuals.
- [ ] Phase 7: Tinkers Construct 1.12 Slime Boots, Slime Sling, motion parity, and client/server synchronization.
- [ ] Phase 8: Cross-feature namespace, persistence, performance, security, and optional-integration audit.
- [ ] Phase 9: Build, resource/data checks, artifact inspection, manual smoke, attribution, and documentation.
