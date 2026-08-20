# Modern Foundry Native Integration Port Plan

## Status and current boundary

This is a planning task only. The current request authorizes updating this
file; it does not authorize porting code, editing source or resources,
changing Gradle files, regenerating data, or updating project logs.

Implementation must wait for a separate explicit authorization. Until then,
the six reference projects remain read-only research input and every
implementation checkbox below remains unchecked.

## Objective

Port and directly integrate the following reference content into Modern
Foundry for Minecraft 1.21.1 NeoForge:

- `References/TinkersKatanas-main`
- `References/TinkersWeaponry-1.20.1`
- `References/TinkersBattleSpades-main`
- `References/Tinkers-Rapier-1.20.1`
- `References/tinkers-levelling-addon-1.20`
- `References/Yoyos-1.20`

The result is one Modern Foundry feature set, not six bundled addons. It must
use Modern Foundry's own namespace, registration, data, networking, client,
configuration, and documentation systems. Players must not need the original
mods or Json Things to use the integrated content.

## Target baseline

Reconfirm these values from the live checkout immediately before coding, but
the reconnaissance for this plan found:

- Minecraft `1.21.1`
- NeoForge `21.1.240`
- Java `21`
- Mod/resource namespace: `modernfoundry`
- Java package root: `modernmods.modernfoundry`
- Required integration: Hilt `1.13`
- Optional integrations: JEI and JSON Things

The current Modern Foundry seams to reuse are:

- `TinkerTools`, `TinkerToolParts`, and `TinkerModifiers` for registration.
- `ToolDefinitions` and the existing tool data model for modular tools.
- Existing station layouts, recipes, tags, models, and data providers.
- `ToolClientEvents` and the established client registration pattern.
- `TinkerNetwork` and existing projectile/entity/rendering infrastructure.
- Generated tinkering data under `src/generated/resources/data/modernfoundry/tinkering/`.

Do not create a parallel addon framework, registrar, tool serialization
format, networking channel, or data-generation system when one of these seams
already covers the requirement.

## Non-negotiable boundaries

- Never edit anything under `References/`; copy facts and behavior, not files.
- All Java added for this work belongs below `modernmods.modernfoundry`.
- All runtime IDs, assets, recipes, tags, models, translations, and data use
  `modernfoundry`, for example `modernfoundry:<id>`,
  `assets/modernfoundry/**`, and `data/modernfoundry/**`.
- Do not retain an external mod ID, package, registry owner, network channel,
  config namespace, or runtime dependency.
- Do not add a Json Things dependency merely to reproduce the data-only
  reference packs. Convert their content into Modern Foundry's native tool,
  registry, recipe, station, tag, and data-provider systems.
- Preserve the reference player-facing behavior, stats, traits, recipes,
  station layouts, visuals, controls, and defaults unless a 1.21.1/NeoForge
  incompatibility makes an adaptation necessary. Record every adaptation.
- Do not overwrite an existing Modern Foundry registry ID. If a name collides,
  resolve the canonical Modern Foundry ID before implementation and record the
  mapping; do not silently replace existing content.
- Preserve tool stack serialization and existing modifier/repair/part
  replacement behavior.
- Keep common/server code free of client-only imports. Dedicated-server
  loading is a release requirement.
- Treat `src/generated/resources` as output. Change providers or their source
  data, then regenerate and review the result; do not hand-edit generated
  output as the source of truth.
- Preserve each reference project's license, copyright, author, and asset
  attribution. The levelling addon also requires retaining third-party sound
  credits and licenses. Resolve redistribution questions before copying any
  code or asset.
- Do not claim parity from compilation alone. Separate automated evidence from
  client, dedicated-server, fresh-world, and multiplayer smoke evidence.

## Reference inventory and migration strategy

| Reference | Confirmed content | Integration strategy |
| --- | --- | --- |
| TinkersKatanas | Json Things data pack containing `katana` and `fuma_shuriken`, tool definitions, station layouts, recipes, tags, models, textures, and weapon attributes. | Translate the data into the existing Modern Foundry tool-definition, station, recipe, tag, model, texture, and creative-tab systems. Confirm whether any item action needs an existing runtime hook. |
| TinkersBattleSpades | Json Things data pack containing `battle_spade`, using `adze_head`, `tool_handle`, and `small_blade`. | Reuse existing parts and native tool-definition/recipe systems. Add only the missing registration/data/assets required for the reference behavior. |
| TinkersWeaponry | Forge 1.20.1 Java addon containing `greatsword`, `pike`, `lance`, `great_blade`, `spear_head`, casts, the `lengthy` modifier, data providers, models, textures, tags, and client color/property registration. | Port behavior to NeoForge 1.21.1, extend native part/tool/modifier registration, and use existing client/data-provider hooks. Compare `lengthy` with existing reach-related modifiers before creating anything new. |
| Tinkers-Rapier | Forge 1.20.1 Java addon containing `rapier_tic`, `estoc_tic`, `slender_blade`, its cast, custom leap/use behavior, custom sting attacks, and a configurable attack multiplier. | Keep the data in native tool systems and implement only the behavior that cannot be represented by the current tool hooks. Calculate outcomes server-side and route the multiplier through the existing configuration pattern. |
| Tinkers Levelling Addon | `improvable` modifier; XP from mining, harvesting, combat, projectiles, armor damage, interactions, and movement; persistent `experience`, `level`, `slot_history`, and `stat_history`; progression, slot, stat, tooltip, command, network, and sound features. | Integrate with Modern Foundry's modifier and tool-stack state model. Use existing event and network infrastructure, make XP/state changes server-authoritative, and preserve all persistence/configuration behavior. |
| Yoyos | Six tiered yoyo items: wooden, stone, iron, diamond, golden, and netherite; a custom `Yoyo` entity with controller, targeting, movement, collision, attack, retraction, particles, renderer, packets, and tracking; LivingEntity and hand-rendering mixins. | Port as native Modern Foundry item/entity/runtime content. Do not force yoyos into Tinker tool-part definitions. Audit existing hooks first and retain a mixin only when no stable event or renderer seam can provide equivalent behavior. |

## Namespace and identity mapping

| Concern | Required result |
| --- | --- |
| Java | Rename/adapt all integrated classes into `modernmods.modernfoundry...`; no external package imports or copied package tree. |
| Registry IDs | Use `modernfoundry:<original_content_name>` when it does not collide. Resolve and document any collision before registering. |
| Assets and data | Convert every reference path to `assets/modernfoundry/**` or `data/modernfoundry/**`; remove stale external namespace references. |
| Tool definitions | Use the existing `ToolDefinitions` and generated/native tinkering data conventions. |
| Parts, modifiers, items, entities | Extend the existing `TinkerToolParts`, `TinkerModifiers`, item/entity registries, and creative-tab registration rather than adding new registries. |
| Networking | Extend `TinkerNetwork` or its existing message pattern; validate sender, level, target, stack, and permissions on the server. |
| Configuration | Use Modern Foundry's existing config owner and naming pattern. Preserve reference defaults and expose values that were configurable; do not hard-code tunables. |
| Client hooks | Register colors, item properties, renderers, particles, and hand behavior through the existing client-only entry points. |
| Compatibility | JSON Things, JEI, and other optional integrations may recognize the content, but the base feature must load and function without them. |

## Behavior parity contract

Before implementing each feature, make a small source-to-target parity table
covering:

- Registry IDs and display names.
- Parts, casts, slot layouts, material requirements, recipes, tags, and
  creative-tab placement.
- Base stats, attributes, traits, modifier levels, and configurable defaults.
- Use, attack, projectile, movement, collision, cooldown, sound, particle,
  animation, and retraction behavior.
- Stack state, serialization keys/components, level progression, and reload
  behavior.
- Client rendering/property behavior and network messages.
- License/notice/asset obligations.

Any entry that cannot be preserved must state the reason, the exact adaptation,
and the manual test that covers the changed behavior. Do not silently rebalance
or add anticipation, fallback, or convenience behavior that the references do
not provide.

## Phased implementation plan

### Phase 0 - Freeze the contract and inventory conflicts

- Re-read `AGENTS.md`, this file, `gradle.properties`, `build.gradle`, and the
  current registration/data-generation patterns.
- Confirm the actual Minecraft, NeoForge, Java, Hilt, mod-version, and output
  namespace values at implementation time.
- Inventory every reference registry, source class, mixin, event hook, config,
  packet, data file, asset, sound, and license notice.
- Compare reference IDs and modifier behavior with existing Modern Foundry
  content. Existing candidates requiring semantic comparison include `pierce`,
  `silky_shears`, `pathing`, `bane_of_sssss`, and `reach`.
- Record all ID collisions, API replacements, license restrictions, and
  behavior that needs a manual test.
- Decide which reference assets can legally be redistributed before copying
  any asset or source-derived implementation.

**Exit gate:** a complete parity/attribution inventory exists, no unresolved
license blocker remains, and the implementation owner for every runtime path is
known.

### Phase 1 - Native registration and data-only weapon content

- Extend the existing part, tool, item, creative-tab, and data-provider
  registrations only where required.
- Integrate the TinkersKatanas `katana` and `fuma_shuriken` content.
- Integrate the TinkersBattleSpades `battle_spade` content using the existing
  `adze_head`, `tool_handle`, and `small_blade` parts.
- Port tool definitions, slot layouts, recipes, casts if present, tags,
  attributes, models, textures, and translations into the native namespace.
- Confirm any special action instead of assuming a Json Things definition
  provides runtime behavior by itself.
- Generate data through the project's active providers and inspect the output.

**Exit gate:** the content registers without the original data pack, appears in
the intended creative/recipe paths, constructs in a fresh world, and has no
external namespace or missing-resource references.

### Phase 2 - TinkersWeaponry

- Port and register `great_blade` and `spear_head`, their casts, and the
  `greatsword`, `pike`, and `lance` tools.
- Port the `lengthy` modifier only if the semantic comparison proves that no
  existing Modern Foundry modifier already supplies the behavior.
- Preserve the reference stats, attributes, traits, recipes, tags, tool
  layouts, client colors, item properties, models, textures, and translations.
- Replace Forge 1.20.1 APIs with the actual NeoForge 1.21.1 APIs used by this
  checkout; do not add a compatibility shim for a single ported class.

**Exit gate:** all parts, tools, modifier behavior, data, and client visuals
pass isolated construction/combat checks and the optional integrations remain
optional.

### Phase 3 - Tinkers-Rapier behavior

- Port `slender_blade` and its cast, then the `rapier_tic` and `estoc_tic`
  definitions.
- Trace current tool use, attack, movement, and modifier hooks before adding
  runtime classes.
- Reproduce leap/use behavior and sting attack behavior with server-authoritative
  calculations, correct cooldown/consumption rules, collision handling, and
  the reference sounds/particles.
- Put the attack multiplier in the existing configuration system with the
  reference default. Validate and clamp client-provided or packet-provided
  values on the server.
- Use a mixin only if the existing event/tool hooks cannot express the exact
  behavior; isolate any unavoidable mixin and document its target and reason.

**Exit gate:** rapier behavior is reproducible in a fresh single-player world
and a two-player server, with no client-authoritative damage or movement.

### Phase 4 - Tinkers Levelling Addon

- Port the `improvable` modifier through the existing modifier registration and
  modifier-data systems.
- Preserve per-tool `experience`, `level`, `slot_history`, and `stat_history`
  using the current Modern Foundry tool-stack serialization/data model.
- Port XP hooks for mining, harvesting, combat, projectiles, armor damage,
  interactions, and movement. Ensure one gameplay action cannot award XP twice
  through overlapping event paths.
- Port configurable level progression, available slots, stat changes, tooltip
  display, commands, networking, and sounds without changing reference
  defaults.
- Keep all state transitions server-side; make tooltips and render state
  clients read-only projections of synchronized data.
- Test stack copy, repair, part replacement, modifier changes, death/drop,
  container movement, chunk unload/reload, save/reload, and multiplayer sync.

**Exit gate:** every configured XP source has an evidence-backed test, level
and history data survive all supported stack transitions, and invalid packets
or commands cannot mutate another player's tool.

### Phase 5 - Yoyos

- Register the six tiered yoyo items and their recipes, tags, models, textures,
  translations, sounds, and creative-tab entries.
- Port the `Yoyo` entity and its controller, targeting, movement, collision,
  attack, retraction, particle, renderer, packet, and tracking behavior.
- Reuse Modern Foundry's entity registration, tracking, network, particle, and
  renderer patterns. Keep entity simulation and attack outcomes server-side.
- Audit the LivingEntity and hand-rendering mixins against current Modern
  Foundry hooks. Avoid a mixin when an existing event or renderer seam gives
  equivalent behavior; if unavoidable, keep it client/common-safe and narrow.
- Verify entity spawn/despawn, owner loss, chunk unload/reload, item loss or
  return, collision edge cases, attack cooldowns, and multiplayer interpolation.

**Exit gate:** each tier works in a fresh world, renders in first/third person,
tracks correctly on a dedicated server, and remains synchronized for two
  players without the reference mod installed.

### Phase 6 - Cross-feature hardening

- Audit all Java imports, registry owners, resource paths, translation keys,
  packets, config keys, and generated output for external namespaces.
- Check that new tools participate in existing repair, modifier, material,
  part-replacement, station, inventory, creative-tab, recipe, and book/help
  flows.
- Check that leveling works on the newly integrated tools and does not change
  unrelated existing tools.
- Check that rapier and yoyo actions do not bypass existing durability,
  cooldown, damage, permission, or server-authority rules.
- Test with neither optional integration, with JEI only, with JSON Things only,
  and with both present.
- Review performance-sensitive paths: movement XP, entity ticking, targeting,
  collision scans, particles, and network frequency.
- Review the complete diff for unrelated changes and preserve all user-owned
  worktree edits.

**Exit gate:** no unresolved namespace, dependency, persistence, security,
performance, or optional-integration issue remains.

### Phase 7 - Verification, attribution, and release readiness

- Run the project's relevant unit/data/resource checks and the full build:

  ```text
  .\gradlew.bat clean build --console=plain --no-daemon
  ```

- Run configured data generation/validation tasks after checking
  `build.gradle`; review generated-resource diffs rather than accepting bulk
  output blindly.
- Inspect the built JAR for the expected Modern Foundry namespace, all six
  content groups, licenses/credits, generated data, and absence of stale
  external namespace files.
- Run `git diff --check` and review the complete diff.
- Run `runClient` and `runServer` where applicable.
- Perform fresh-world manual smoke tests for construction, recipes, tool
  stats, modifier application, rapier actions, yoyo physics/rendering, and
  leveling.
- Perform dedicated-server and two-player tests for damage, movement, packets,
  entity tracking, save/reload, chunk unload/reload, and invalid-input paths.
- Update `README.md` for player-facing content or configuration, and append the
  required `CHANGELOG.md` and `TRACELOG.md` entries only when implementation is
  actually completed. Do not report manual coverage that was not performed.

## Ownership and file map for implementation

| Responsibility | Owner/pattern | Boundary |
| --- | --- | --- |
| Existing modular tools and parts | `TinkerTools`, `TinkerToolParts`, `ToolDefinitions`, existing tool data providers | Extend current ownership; do not create an addon registry. |
| Modifiers | `TinkerModifiers` and existing modifier classes/data | Reuse an existing semantic match; add a new modifier only for genuinely missing behavior. |
| Tool recipes/layouts/tags | Existing providers and native generated tinkering data | Source changes belong in providers/source data; generated files are output. |
| Weapon client behavior | `ToolClientEvents` and current client bootstrap | Keep colors, properties, models, and render-only code client-side. |
| Rapier runtime behavior | Existing tool/action/attack hooks, with a small dedicated class only if required | Server owns damage, movement, cooldown, and consumption. |
| Leveling state and XP | Existing tool stack/modifier serialization plus existing event/config/network patterns | No parallel persistence format or client-authoritative progression. |
| Yoyo runtime | Dedicated `modernmods.modernfoundry` yoyo item/entity/controller/render/network classes | Native entity content; not Tinker tool-part data. |
| Optional integrations | Existing JEI/JSON Things plugin boundaries | Optional code must not be loaded as a base-mod requirement. |
| Attribution and player documentation | Existing root documentation/license convention | Preserve notices and state any adaptation or third-party asset restriction. |

Exact new class names and paths are implementation details. Create them only
after tracing the existing owner and confirming that an existing class cannot
hold the behavior cleanly.

## Main risks and controls

| Risk | Control |
| --- | --- |
| Json Things content is mistaken for runtime behavior | Verify the reference's actual event/entity hooks and map each behavior to a native owner. |
| Duplicate modifier semantics | Compare existing modifier implementation, stats, hooks, and data before registering a new ID. |
| 1.20.1 Forge APIs are copied into 1.21.1 NeoForge | Port call sites against the dependencies in this checkout and compile each wave before continuing. |
| Client/server desynchronization | Validate every packet and calculate combat, XP, movement, and entity state on the server. |
| Leveling data is lost during stack transitions | Test serialization through repair, copying, part replacement, inventory movement, save/reload, and death/drop. |
| Yoyo mixins break unrelated rendering or entities | Prefer existing hooks; keep unavoidable mixins narrow and test first/third-person and dedicated-server loading. |
| Generated resources retain stale namespaces | Regenerate from providers, run namespace/resource audits, and inspect the final JAR. |
| Third-party assets or sounds cannot be redistributed | Preserve notices and stop for a license decision before adding the asset. |
| A successful build is mistaken for gameplay parity | Keep automated checks and manual smoke results separate in the final trace. |

## Completion criteria

The integration is complete only when all of the following are true:

- All six reference feature groups are available from one Modern Foundry JAR.
- No original mod, external namespace, Json Things pack, or new runtime
  dependency is required.
- All registry IDs, Java packages, assets, data, recipes, tags, configs, and
  packets follow the Modern Foundry namespace policy.
- Original player-visible content and behavior are preserved, or every
  incompatibility is documented with a reason and test.
- Existing Modern Foundry tools and systems still build, load, serialize,
  repair, modify, and render correctly.
- Dedicated-server loading, optional-integration absence, fresh-world
  construction, multiplayer synchronization, and save/reload behavior are
  verified.
- License, author, sound, and asset attribution is retained and distributable.
- The full build, resource/data checks, artifact inspection, diff review, and
  required project documentation updates are complete.

## Implementation checklist

- [ ] Phase 0: contract, reference inventory, collision audit, and licensing.
- [ ] Phase 1: Katanas and Battle Spades native data/content integration.
- [ ] Phase 2: TinkersWeaponry parts, tools, modifier, and client data.
- [ ] Phase 3: Rapier parts, tools, leap/use, sting, and configuration.
- [ ] Phase 4: Levelling modifier, XP, state, configuration, commands, and sync.
- [ ] Phase 5: Yoyo items, entity runtime, rendering, physics, and networking.
- [ ] Phase 6: Cross-feature namespace, persistence, performance, and optional-integration audit.
- [ ] Phase 7: Build, data/resource checks, manual smoke, attribution, artifact, and documentation review.
