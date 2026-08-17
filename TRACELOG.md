## 2026-08-16 - Build and release ModernFoundry with Hilt

**Prompt / Task**
- Build the latest ModernFoundry JAR on push, build Hilt as the source dependency, and publish both projects as latest GitHub releases.

**What Changed**
- Added the ModernFoundry GitHub Actions workflow for the `Neo/1.21.1` branch.
- Corrected Hilt's workflow to run from `main`, use the Hilt release name, exclude source JARs, and mark each release as latest.
- The ModernFoundry workflow checks out Hilt beside the project, builds Hilt first, then runs the ModernFoundry build against the same included-build checkout.

**Steps Taken**
- Inspected `settings.gradle`, `build.gradle`, `gradle.properties`, the existing Hilt workflow, and both repositories' branch/remotes.
- Added version-derived release tags and exact main-JAR verification before publishing.

**Architecture / Module Ownership**
- Relevant class/module change: `.github/workflows/build.yml` in ModernFoundry and Hilt.
- Owning module/system: GitHub Actions build and release automation.
- Existing logic reused or extracted: Gradle wrappers, the existing Hilt included-build substitution, and Hilt's existing release workflow.
- Net line change: workflow and documentation additions; no Java source changes.
- New files: ModernFoundry `.github/workflows/build.yml`.
- Build files updated: none.

**Rationale / Tradeoffs**
- Hilt remains the source dependency and is checked out from `main` so ModernFoundry compiles against the exact Hilt source built in that run.
- Releases use unique build tags and GitHub's `--latest` flag; the workflow does not maintain a mutable `latest` tag.

**Build / Validation**
- Production build or compile-only check: `Hilt\gradlew.bat clean build --console=plain --no-daemon` and `ModernFoundry\gradlew.bat clean build --console=plain --no-daemon` both passed under Java 21.
- Exact artifacts verified: `Hilt/build/libs/Hilt-1.21.1-1.12.jar` and `ModernFoundry/build/libs/ModernFoundry-1.21.1-4.0.1-NeoForge.jar`.
- Workflow YAML was parsed successfully; `git diff --check` passed in both repositories.
- Manual validation: GitHub Actions release execution was not run locally; it requires a push or manual dispatch on GitHub.
- Tests created or run: Gradle `check` ran as part of both builds; Hilt emitted existing Java deprecation warnings and ModernFoundry emitted existing Java/NeoForge deprecation warnings.

## 2026-08-16 - Avoid duplicate same-version releases

**Prompt / Task**
- Keep building on every push, but create a release only when a new Hilt or ModernFoundry build number/version is introduced.

**What Changed**
- Changed release tags from GitHub run-number tags to `v<mod_version>` tags.
- Added a GitHub release existence check so repeated pushes with the same version build normally but skip release creation.
- Updated release titles and README guidance to make `mod_version` the release/build-number control.

**Steps Taken**
- Reused the existing version extraction steps and `gh release` publishing path.
- Applied the same duplicate-release guard to Hilt and ModernFoundry workflows.

**Architecture / Module Ownership**
- Relevant class/module change: `.github/workflows/build.yml` in ModernFoundry and Hilt.
- Owning module/system: GitHub Actions release gating.
- Existing logic reused or extracted: `gradle.properties` `mod_version` and GitHub CLI release lookup.
- Net line change: release gating and documentation updates; no Java source changes.
- New files: none.
- Build files updated: none.

**Rationale / Tradeoffs**
- `mod_version` is already part of each shipped JAR filename, so it is the existing build-number source of truth.
- A version gets one immutable release tag; pushing source changes without incrementing the version cannot create another release.

**Build / Validation**
- Production build or compile-only check: not rerun; this change only gates the release step after the already-passing Gradle builds.
- Manual validation: GitHub release lookup/publish was not run locally against the repositories; it requires GitHub Actions credentials.
- Tests created or run: workflow YAML was parsed successfully and `git diff --check` was run.

## 2026-08-16 - Fix Gradle wrapper permissions in GitHub Actions

**Prompt / Task**
- Fix the ModernFoundry GitHub workflow failure reporting `./gradlew: Permission denied` while building Hilt.

**What Changed**
- Added a wrapper-permission step that runs `chmod +x` for both `Hilt/gradlew` and `ModernFoundry/gradlew` before either build.

**Steps Taken**
- Used the workflow log to identify the failing command and inspected both repositories' tracked wrapper modes.
- Confirmed both wrappers are tracked as mode `100644`, so executable permission cannot be assumed on the Ubuntu runner.

**Architecture / Module Ownership**
- Relevant class/module change: ModernFoundry `.github/workflows/build.yml`.
- Owning module/system: GitHub Actions Linux build setup.
- Existing logic reused or extracted: the existing Hilt and ModernFoundry Gradle wrappers.
- Net line change: one workflow step; no Java source changes.
- New files: none.
- Build files updated: none.

**Rationale / Tradeoffs**
- The permission fix is applied once at the workspace root and covers both checked-out repositories before the included-build chain starts.

**Build / Validation**
- Production build or compile-only check: not rerun; the failure occurred before Gradle started and the fix only changes runner permissions.
- Manual validation: GitHub Actions rerun is still required to confirm the remote runner proceeds past Hilt.
- Tests created or run: tracked mode inspection, workflow YAML parse, and `git diff --check`.

## 2026-08-16 - Build ModernFoundry against released Hilt

**Prompt / Task**
- Build only ModernFoundry in its workflow, use the latest Hilt release, and publish only new ModernFoundry versions.

**What Changed**
- Removed the Hilt checkout and Hilt Gradle build from the ModernFoundry workflow.
- Added latest Hilt release-JAR download through the GitHub API.
- Added the `use_hilt_release` Gradle path for CI while preserving the existing local `../Hilt` included-build path.

**Steps Taken**
- Traced `settings.gradle` and the Hilt dependency declaration before removing the sibling checkout.
- Added a CI-only `libs/Hilt-latest.jar` dependency and updated the README workflow description.

**Architecture / Module Ownership**
- Relevant class/module change: `settings.gradle`, `build.gradle`, and `.github/workflows/build.yml`.
- Owning module/system: ModernFoundry dependency resolution and GitHub Actions build workflow.
- Existing logic reused or extracted: local Hilt included-build substitution remains unchanged unless `-Puse_hilt_release=true` is supplied.
- Net line change: CI dependency path and workflow simplification; no Java source changes.
- New files: none.
- Build files updated: `settings.gradle` and `build.gradle`.

**Rationale / Tradeoffs**
- CI now validates ModernFoundry against the Hilt artifact users would consume, avoiding repeated Hilt compilation during Foundry-only work.
- A Hilt release must exist; the workflow fails clearly if GitHub has no latest Hilt JAR.

**Build / Validation**
- Production build or compile-only check: pending after the CI release-JAR path is exercised locally.
- Manual validation: GitHub Actions execution pending.
- Tests created or run: workflow YAML parse and `git diff --check` pending after the edit.

## 2026-08-16 - Build ModernFoundry without building Hilt

**Prompt / Task**
- Make pushes build a fresh ModernFoundry and stop building Hilt as part of the ModernFoundry workflow.

**What Changed**
- Pinned `hilt_version` to the published Hilt `1.12` release and resolved it from a downloaded JAR.
- Removed the sibling Hilt included build and the Hilt checkout/build workflow steps.
- Kept the existing version-based release guard so only a new ModernFoundry `mod_version` creates a release.

**Steps Taken**
- Audited ModernFoundry's Hilt imports and required `hilt` metadata before changing dependency resolution.
- Updated the workflow to fetch the exact Hilt asset, run `clean build` for ModernFoundry, and verify only the ModernFoundry JAR.

**Architecture / Module Ownership**
- Relevant class/module change: `gradle.properties`, `build.gradle`, `settings.gradle`, and `.github/workflows/build.yml`.
- Owning module/system: ModernFoundry dependency resolution and GitHub Actions release automation.
- Existing logic reused or extracted: the version-derived JAR path and existing `mod_version` release guard.
- Net line change: removed Hilt source-build orchestration; no Java source changes.
- New files: none.
- Build files updated: `gradle.properties`, `build.gradle`, and `settings.gradle`.

**Rationale / Tradeoffs**
- ModernFoundry still requires Hilt APIs, so the workflow fetches the published binary without compiling Hilt. Removing Hilt entirely would require migrating the existing `modernmods.hilt` source usage.

**Build / Validation**
- Production build or compile-only check: `gradlew.bat clean build --console=plain --no-daemon` passed with the released `Hilt-1.21.1-1.12.jar` input.
- Exact artifact verified: `build/libs/ModernFoundry-1.21.1-4.0.1-NeoForge.jar` (SHA-256 `9334ddd35801b3cb2bed51f572b95eecc2d6c3df4182ef4e1f946b8898c4701c`) and `META-INF/neoforge.mods.toml` are present.
- Manual validation: GitHub Actions execution pending.
- Tests created or run: Gradle `check`, Ruby YAML parse, and `git diff --check` passed.

## 2026-08-16 - Restore vanilla first-person tool animations

**Prompt / Task**
- Make Modern Foundry tools and weapons use vanilla-style equip/draw and first-person swing animations.

**What Changed**
- Updated `ModifiableItemClientExtension` so ordinary idle/equip/swing rendering falls through to NeoForge and vanilla `ItemInHandRenderer`.
- Kept the extension-owned bow, crossbow, spear, brush, and spin-attack transforms, while delegating `NONE`, `BLOCK`, and unsupported use animations to vanilla as well.

**Steps Taken**
- Read the repository instructions and confirmed no `TASK.md` exists in the checkout.
- Traced `IClientItemExtensions.applyForgeHandTransform` through the 1.21.1 NeoForge renderer and compared the tool model transforms with `neoforge:item/default-tool`.
- Removed the custom generic swing/equip transform path and reviewed the resulting diff.

**Architecture / Module Ownership**
- Relevant class/module change: `src/main/java/modernmods/modernfoundry/library/client/item/ModifiableItemClientExtension.java`; player-facing behavior documented in `README.md`.
- Owning module/system: client first-person item rendering.
- Existing logic reused or extracted: NeoForge's normal fall-through to vanilla `ItemInHandRenderer`; existing custom active-use transforms were retained.
- Net line change: 6 fewer lines in the client extension (8 added, 14 removed).
- New files: none.
- Build files updated: none.

**Rationale / Tradeoffs**
- Returning `true` for the ordinary branch prevented vanilla from applying its equip and swing transforms, so the mod's copied path could drift from vanilla behavior. Returning `false` restores the native renderer for the requested cases without removing custom draw-time behavior.

**Build / Validation**
- Production build or compile-only check: `gradlew.bat clean build --console=plain --no-daemon` passed after restoring the ignored local `libs/Hilt-1.21.1-1.12.jar` build input.
- Exact artifact verified: `build/libs/ModernFoundry-1.21.1-4.0.1-NeoForge.jar`; archive contains `ModifiableItemClientExtension.class` and `META-INF/neoforge.mods.toml`; SHA-256 `bec4b0d9fa7994f60963a8dddb29ff99c72f35fcf08080c1ae94b2c717173a6`.
- Manual validation: first-person client smoke testing was not performed; compare a Modern Foundry sword/pickaxe with vanilla items in-game for equip interpolation, swing, offhand, and active-use cases.
- Tests created or run: Gradle `check`/tests and `git diff --check` passed; no dedicated renderer test was added.

## 2026-08-16 - Fix Hilt item-layer UV sampling

**Prompt / Task**
- Patch Hilt's shared item-layer renderer and rebuild Hilt and ModernFoundry.

**What Changed**
- Updated `HiltItemLayerModel.buildSideQuad` to use normalized per-side UV coordinates with a 0.1-pixel inset.
- Corrected the `FaceData` index stride from `vMax` to `uMax` for non-square textures.
- Preserved the existing 7.5/16 to 8.5/16 extrusion depth and unculled tool quads.
- Rebuilt Hilt, synchronized `libs/Hilt-1.21.1-1.12.jar`, and rebuilt ModernFoundry.

**Steps Taken**
- Confirmed the 1.21.1 `TextureAtlasSprite#getU/getV` contract is normalized and traced all tool layers through Hilt's item-layer model.
- Applied the Hilt-adapted side-quad and `FaceData` fixes.
- Ran clean Hilt and ModernFoundry Gradle builds and verified the resulting artifacts and hashes.

**Architecture / Module Ownership**
- Relevant class/module change: `Hilt/src/main/java/modernmods/hilt/client/model/util/HiltItemLayerModel.java`; ModernFoundry consumes the resulting local Hilt JAR.
- Owning module/system: Hilt client item-layer model baking and ModernFoundry tool/weapon model rendering.
- Existing logic reused or extracted: existing Hilt quad construction, front/back winding, and tool model depth were retained.
- Net line change: Hilt renderer +51/-16; no ModernFoundry Java source changes for this fix.
- New files: none.
- Build files updated: none.

**Rationale / Tradeoffs**
- The old side-quad path converted normalized coordinates back to legacy 0..16 values, causing atlas bleeding and incorrect perimeter sampling. Explicit side cases keep the fix local to the shared renderer.

**Build / Validation**
- Production build or compile-only check: Hilt `clean build` passed with 47 existing deprecation warnings; ModernFoundry `clean build` passed with 100 existing deprecation warnings.
- Exact artifacts verified: `Hilt-1.21.1-1.12.jar` SHA-256 `fca30abe9e401c78e52b42ff89755049a041e19b33f7b18881240be3d83a9d0a`; `ModernFoundry-1.21.1-4.0.1-NeoForge.jar` SHA-256 `056b519d4cd8929c37fb7a7885d38d228fe8b0723f3e87e003197f99f3e3a6dd`.
- Manual validation: client visual smoke testing was not performed; verify tools and weapons in-game, including transparent and non-square textures.
- Tests created or run: Hilt test task was `NO-SOURCE`; ModernFoundry `test` and `check` passed; `git diff --check` passed.

## 2026-08-16 - Restore native tool and weapon re-equip timing

**Prompt / Task**
- Make Modern Foundry tools and weapons draw with the same delayed equip behavior as vanilla tools and weapons instead of appearing immediately.

**What Changed**
- Removed the `ModifiableItem` and `ModifiableLauncherItem` overrides that replaced NeoForge's native re-equip predicate.
- Modifiable tools and launchers now inherit NeoForge's `!oldStack.equals(newStack)` equip decision, allowing `ItemInHandRenderer` to animate stack changes.
- Kept the existing material/modifier/attribute comparison for block-break reset behavior by calling it directly from `shouldCauseBlockBreakReset`.

**Steps Taken**
- Read the repository instructions and confirmed `TASK.md` is absent.
- Traced `ItemInHandRenderer.tick` through `ClientHooks.shouldCauseReequipAnimation` and `IItemExtension`.
- Compared the vanilla default predicate with Modern Foundry's semantic comparison and removed only the equip override.
- Built the matching local Hilt 1.13 dependency required by the existing worktree version, then rebuilt Modern Foundry.

**Architecture / Module Ownership**
- Relevant class/module change: `src/main/java/modernmods/modernfoundry/library/tools/item/ModifiableItem.java` and `src/main/java/modernmods/modernfoundry/library/tools/item/ranged/ModifiableLauncherItem.java`.
- Owning module/system: NeoForge item re-equip state and vanilla first-person hand rendering.
- Existing logic reused or extracted: NeoForge `IItemExtension` default re-equip behavior; Modern Foundry's existing tool-data comparison remains for block breaking.
- Net line change: 10 fewer lines across the two item classes.
- New files: none.
- Build files updated: none by this fix; the existing worktree Hilt 1.13 setting was used as-is.

**Rationale / Tradeoffs**
- Vanilla's first-person equip ramp is controlled by `ItemInHandRenderer` and only runs when `ClientHooks.shouldCauseReequipAnimation` reports a change. Modern Foundry's override treated equivalent tool data as unchanged, suppressing that ramp even when the equipped stack changed. Inheriting NeoForge's predicate restores vanilla timing without duplicating renderer transforms or changing tool serialization.

**Build / Validation**
- Production build or compile-only check: Hilt `gradlew.bat clean build --console=plain --no-daemon` passed; ModernFoundry `gradlew.bat clean build --console=plain --no-daemon` passed with existing deprecation warnings.
- Exact artifact verified: `build/libs/ModernFoundry-1.21.1-4.0.1-NeoForge.jar`; archive contains `ModifiableItem.class`, `ModifiableLauncherItem.class`, and `META-INF/neoforge.mods.toml`; SHA-256 `c6318b7787434bc4d11a835fae2fb8700e2b9af8245509d6c6408266b83aad0d`.
- Manual validation: first-person client smoke testing was not performed; compare a Modern Foundry axe/sword with vanilla equivalents for draw interpolation and normal swing in-game.
- Tests created or run: ModernFoundry `test`/`check` ran as part of the build; `git diff --check` passed; no dedicated renderer test was added.

## 2026-08-16 - Synchronize current animation fix into Foundry instance

**Prompt / Task**
- Investigate why Modern Foundry tools still appeared to draw instantly after the first-person animation fix.

**What Changed**
- Replaced the stale `Foundry` Prism `ModernFoundry-1.21.1-4.0.1-NeoForge.jar` with the current build artifact.
- Confirmed the deployed artifact no longer contains `ModifiableItem.shouldCauseReequipAnimation` and therefore uses NeoForge's native re-equip predicate.

**Steps Taken**
- Compared the build, `Testing`, and `Foundry` JAR SHA-256 hashes.
- Inspected the deployed `ModifiableItem` class signatures before and after synchronization.
- Copied the exact current build to the explicit `Foundry` instance mod path and re-hashed it.

**Architecture / Module Ownership**
- Relevant class/module change: local Prism deployment artifact for the existing client animation fix.
- Owning module/system: NeoForge `ItemInHandRenderer` re-equip state and Modern Foundry client item behavior.
- Existing logic reused or extracted: existing source fix and built JAR; no new animation path.
- Net line change: no Java source change in this synchronization.
- New files: none.
- Build files updated: none.

**Rationale / Tradeoffs**
- The tested `Foundry` instance was loading an older JAR, so further source animation edits would not affect that runtime. The explicit instance now uses the current artifact.

**Build / Validation**
- Production build or compile-only check: current build artifact was already produced by the preceding clean Modern Foundry build; its SHA-256 was verified as `c6318b7787434bc4d11a835fae2fb8700e2b9af8245509d6c6408266b83aad0d`.
- Manual validation: not performed; relaunch the `Foundry` Prism instance and compare equip interpolation and swing behavior in-game.
- Tests created or run: deployed-class signature inspection and `git diff --check` passed.

## 2026-08-16 - Repair seared melter and fuel tank crafting and harvesting

**Prompt / Task**
- Fix the empty `c:glass` ingredient shown when crafting the seared melter and seared fuel tank, and make both blocks breakable with a stone pickaxe.

**What Changed**
- Added the shipped `c:glass` item tag from the existing common glass subtags.
- Removed the seared melter and seared fuel tank from `c:needs_gold_tool` and added them to `minecraft:needs_stone_tool`.
- Added both blocks to `minecraft:mineable/pickaxe`.
- Updated the dormant tag providers so future data generation preserves the same behavior.

**Steps Taken**
- Read the repository instructions and confirmed `TASK.md` is absent.
- Traced the fuel-tank recipe's generic glass ingredient to the missing generated `c:glass` tag, and traced both blocks' harvest behavior to the existing gold-tier tags.
- Preserved unrelated worktree changes and added only the affected generated resources, providers, documentation, and required logs.

**Architecture / Module Ownership**
- Relevant class/module change: common item/block tag providers and generated data-pack tags under `src/generated/resources`.
- Owning module/system: smeltery recipe ingredients and NeoForge block harvest tags.
- Existing logic reused or extracted: existing glass subtags, `BlockTags.MINEABLE_WITH_PICKAXE`, and `BlockTags.NEEDS_STONE_TOOL`.
- Net line change: 25 lines added and 2 removed across the tag providers and generated tag JSON, excluding newline normalization.
- New files: `data/c/tags/item/glass.json`, `data/minecraft/tags/block/mineable/pickaxe.json`, and `data/minecraft/tags/block/needs_stone_tool.json`.
- Build files updated: none.

**Rationale / Tradeoffs**
- The common tag is defined once so every existing `Tags.Items.GLASS` recipe works, while the harvest change is limited to the two reported blocks and does not rebalance the other seared tanks or controllers.

**Build / Validation**
- Production build or compile-only check: `gradlew.bat clean build --console=plain --no-daemon` passed in 5m 50s with existing deprecation warnings.
- Exact artifact verified: `build/libs/ModernFoundry-1.21.1-4.0.1-NeoForge.jar`; archive contains the `c:glass`, pickaxe, stone-tier, and gold-tier tag entries plus the generated seared melter and fuel tank recipes; SHA-256 `9dd93de8277ddb4ac75df0b064e143f2837318f542c1ccfc03c7278ae63a52db`.
- Manual validation: not performed; a fresh-world client smoke test remains useful for the four seared tank recipes and stone-pickaxe harvesting.
- Tests created or run: baseline `compileJava --rerun-tasks` passed before this change; final tag/resource checks, archive inspection, and `git diff --check` passed; no dedicated gameplay test was added.

## 2026-08-16 - Route ordinary tools through vanilla first-person rendering

**Prompt / Task**
- Make Modern Foundry axes, swords, and other ordinary tools draw and swing with the same vanilla first-person behavior as their vanilla counterparts.

**What Changed**
- Removed `ModifiableItem.initializeClient`, so ordinary Modern Foundry tools and weapons no longer register `ModifiableItemClientExtension`.
- Kept the launcher-specific client extension for custom bow/crossbow draw and charge behavior.
- Preserved the existing native NeoForge re-equip predicate and the separate Modern Foundry tool-data comparison used for block-break reset behavior.

**Steps Taken**
- Read the repository instructions and confirmed `TASK.md` is absent.
- Traced vanilla `ItemInHandRenderer`, `ClientHooks.shouldCauseReequipAnimation`, and NeoForge's `IClientItemExtensions` path.
- Confirmed vanilla axes and swords do not have a special first-person renderer branch; ordinary items use the shared vanilla equip/swing transforms.
- Removed the remaining base-item client extension registration that still left ordinary tools on a custom hand-rendering path.

**Architecture / Module Ownership**
- Relevant class/module change: `src/main/java/modernmods/modernfoundry/library/tools/item/ModifiableItem.java`.
- Owning module/system: ordinary tool/weapon client rendering and NeoForge item hand extensions.
- Existing logic reused or extracted: vanilla `ItemInHandRenderer` equip/swing transforms; launcher subclasses retain `ModifiableItemClientExtension` for ranged use.
- Net line change: 8 lines removed from `ModifiableItem.java`; no new files.
- Build files updated: none.

**Rationale / Tradeoffs**
- Returning `false` from the custom hand extension still leaves ordinary items registered as custom hand-rendered items. Removing that registration lets the vanilla renderer own the entire ordinary equip and swing path, while limiting custom rendering to ranged items that require it.

**Build / Validation**
- Production build or compile-only check: `gradlew.bat clean build --console=plain --no-daemon` passed with 100 existing deprecation warnings.
- Exact artifact verified: `build/libs/ModernFoundry-1.21.1-4.0.1-NeoForge.jar`; SHA-256 `c86e9076331ad16ea723bfbe8497115af3f7da57e8c31f21858ce3bbc3742fc8`.
- Manual validation: skipped at the user's request; compare a Modern Foundry hand axe/sword with vanilla items in the Testing Prism instance.
- Tests created or run: ModernFoundry `test`/`check` passed as part of the build; `git diff --check` passed; no dedicated renderer test was added.

## 2026-08-16 - Restore Tinker station pattern atlas sprites

**Prompt / Task**
- Restore the missing tool-layout and part-slot textures displayed in the Tinker Station and Tinkers Anvil GUIs using the bundled Tinkers Construct 1.20.1 source as reference.

**What Changed**
- Added the existing `gui/tinker_pattern` texture directory to `assets/minecraft/atlases/blocks.json`.
- Kept the existing GUI rendering and all 63 pattern PNG assets unchanged.

**Steps Taken**
- Read the repository instructions; no `TASK.md` exists in the checkout.
- Traced `Pattern.getTexture()`, `GuiUtil.renderPattern`, `TinkerStationScreen.renderIcon`, station layout data, and the reference atlas/provider.
- Confirmed the source PNGs were present while the shipped atlas only listed `fluid/`.
- Preserved unrelated worktree changes and updated the existing main-resource atlas rather than creating a duplicate generated resource.

**Architecture / Module Ownership**
- Relevant class/module change: `src/main/resources/assets/minecraft/atlases/blocks.json`.
- Owning module/system: Minecraft block texture atlas used by Tinker Station and Tinkers Anvil pattern rendering.
- Existing logic reused or extracted: current `Pattern`/`GuiUtil.renderPattern` flow and existing `gui/tinker_pattern` assets; no Java rendering changes.
- Net line change: 5 lines added.
- New files: none.
- Build files updated: none.

**Rationale / Tradeoffs**
- The missing art was an atlas-stitching omission, not missing PNG files. Adding the directory source fixes every existing pattern through the shared render path with the smallest scoped change.

**Build / Validation**
- Production build or compile-only check: `gradlew.bat check --console=plain --no-daemon` passed; `verifyGeneratedTextures` and the test task passed.
- Resource validation: the copied `build/resources/main/assets/minecraft/atlases/blocks.json` contains `gui/tinker_pattern/`, and 63 source pattern PNGs were found.
- Full JAR validation: `check build` reached resource processing but the JAR task was blocked by the active Gradle `runServer`/file lock; `jar --rerun-tasks` was likewise blocked by the locked NeoForge client-extra cache file.
- Manual validation: not performed; relaunch a current 1.21.1 client and open both station types to verify the visual result.
- Tests created or run: existing `check`/resource verification; no dedicated test was added.

## 2026-08-16 - Audit 1.20.1 tool parity in the 1.21.1 port

**Prompt / Task**
- Verify that the requested Modern Foundry tools retain the supplied Tinkers Construct 1.20.1 behavior in the NeoForge 1.21.1 port.

**What Changed**
- No Java, generated-resource, registry, or gameplay changes were required.
- Confirmed the 26 requested entries, including the four original staff variants represented by the single Slime Staff category, are registered, exposed in the tools tab, tagged, defined, and modeled.

**Steps Taken**
- Read the repository instructions and confirmed `TASK.md` is absent.
- Compared the 26 current tool-definition JSON files with the supplied 1.20.1 definitions after the expected `modernfoundry`/`tconstruct`, common-tag, and `hilt`/`mantle` namespace migrations; all matched semantically.
- Checked current registration, item tags, tool modules, generated definitions, item models, and the original 1.20.1 registration/provider patterns.
- Ran a dedicated-server bootstrap. Modern Foundry reached mod discovery, event subscription, registry freeze, and common setup, then Hilt 1.13 failed during command registration before world load because `DumpLootModifiers` passed a `ResourceLocation` to `Component.translatable`.
- Ran a client bootstrap through resource reload; no Modern Foundry errors or tool-model load failures were reported.

**Architecture / Module Ownership**
- Relevant class/module change: none; this was a parity audit of `TinkerTools`, `ToolDefinitionDataProvider`, `ItemTagProvider`, tool item classes, generated tool definitions, and models.
- Owning module/system: existing Modern Foundry tool-definition, item, modifier, mining, weapon, projectile, shield, and client-resource systems.
- Existing logic reused or extracted: existing 1.21.1 port and the supplied 1.20.1 definitions; no parallel implementation added.
- Net line change: documentation only; no source or generated gameplay lines changed.
- New files: none.
- Build files updated: none.

**Rationale / Tradeoffs**
- The requested behavior is already present in the port, so speculative rewrites would increase risk without changing the verified result.
- The dedicated-server limitation belongs to the external Hilt 1.13 dependency and prevents a fresh-world server smoke test; it is not a Modern Foundry tool-definition failure.

**Build / Validation**
- Production build or compile-only check: `gradlew.bat check --console=plain --no-daemon` passed; forced `gradlew.bat jar --rerun-tasks --console=plain --no-daemon` passed.
- Exact artifact verified: `build/libs/ModernFoundry-1.21.1-4.0.1-NeoForge.jar`; all 26 requested tool-definition entries and 26 corresponding item-model entries are present; SHA-256 `C97201A1C646EBFD4A1814190CF3A509DDF2D315C46CA30FDBE88ACF6F5BA526`.
- Manual validation: client startup/resource reload completed; in-world use of mining AOE, vein mining, tree felling, crop harvesting, combat, ranged, projectile, melting, and shield behavior remains unperformed. Dedicated-server world load is blocked by the Hilt exception above.
- Tests created or run: existing JUnit/check suite passed; generated-texture verification passed; `git diff --check` passed; no new test was added because no implementation changed.

## 2026-08-16 - Restore NeoForge area-of-effect block breaking

**Prompt / Task**
- Restore 1:1 block-breaking behavior from the supplied Tinkers Construct 1.20.1 source after a sledge hammer displayed a 3x3 preview but broke only the center block in Modern Foundry 1.21.1.

**What Changed**
- Replaced the missing NeoForge item-hook invocation with a `PlayerInteractEvent.LeftClickBlock` server handler at the end of the interaction event chain.
- Routed the event's actual hit face through `ToolHarvestLogic` into the existing AOE iterator and harvest logic.
- Preserved the original stacked `ModifiableItem` behavior and left existing Hilt, tool definitions, and generated AOE data unchanged.

**Steps Taken**
- Read the repository instructions; `TASK.md` is absent in this checkout.
- Compared `ModifiableItem`, `ModifiableLauncherItem`, `ToolHarvestLogic`, `BoxAOEIterator`, and `BlockSideHitListener` with `TinkersConstruct-1.20.1`.
- Verified NeoForge 21.1's `IItemExtension` does not expose the old `onBlockStartBreak` hook and provides `LeftClickBlock` as the replacement event.
- Preserved the existing dirty worktree changes and limited source edits to the block-break path.

**Architecture / Module Ownership**
- Relevant class/module change: `src/main/java/modernmods/modernfoundry/tools/logic/ToolEvents.java` and `src/main/java/modernmods/modernfoundry/library/tools/helper/ToolHarvestLogic.java`.
- Owning module/system: Modern Foundry's NeoForge tool interaction and shared server harvest/AOE logic.
- Existing logic reused or extracted: existing `handleBlockBreak`, `runBlockBreak`, `BoxAOEIterator`, `BlockSideHitListener`, and data-driven sledge definition; no Hilt code changed.
- Net line change: 42 source lines net (45 added, 3 removed); no source files added.
- Build files updated: none.

**Rationale / Tradeoffs**
- The port retained the 1.20.1 method but NeoForge 21.1 removed the Forge item-level hook, leaving the AOE implementation unreachable for normal block clicks. The event bridge is the smallest runtime translation required.
- The handler runs at `LOWEST` so existing left-click interactions can cancel first, and it uses the event face rather than relying on stale server-side face state.

**Build / Validation**
- Production build or compile-only check: `gradlew.bat build --console=plain --no-daemon` passed; clean-build attempts were separately blocked by Gradle cleanup/generated Lombok-config races.
- Exact artifact verified: `build/libs/ModernFoundry-1.21.1-4.0.1-NeoForge.jar`; archive contains `ToolEvents.class`, `ToolHarvestLogic.class`, and the sledge definition; SHA-256 `31F5179218261D294B67926F730877E5EBA9132CD0BF67E4105CA48C053C2F14`.
- Manual validation: `runClient` reached NeoForge/Minecraft initialization but crashed before a world loaded because `assets/modernfoundry/book/encyclopedia/fr_fr/armor/info.json` is missing from the existing runtime resource tree; no in-world AOE smoke test was possible.
- Tests created or run: targeted `ToolHarvestLogicTest` passed; full `test`/`check` passed through `build`; `verifyGeneratedTextures` and `git diff --check` passed; no new test was added because the server event requires a live game interaction.

## 2026-08-16 - Restore stack-sensitive tool attack attributes

**Prompt / Task**
- Investigate Modern Foundry axes, swords, and hammers being equipped and swung like ordinary items, determine whether the fault belongs to Modern Foundry or Hilt, and restore vanilla-style behavior.

**What Changed**
- Added NeoForge's `getDefaultAttributeModifiers(ItemStack)` implementation to `ModifiableItem` and `ModifiableLauncherItem`.
- Converted the existing `AttributesModifierHook` results into NeoForge `ItemAttributeModifiers` for main-hand and off-hand use, preserving the existing data-driven tool stats and modifier hooks.
- Kept ordinary `ModifiableItem` stacks on Minecraft's vanilla first-person equip/swing renderer; launcher subclasses retain their custom ranged-use transforms.
- Updated the README to document the resulting vanilla-style behavior.

**Steps Taken**
- Read the repository instructions and confirmed `TASK.md` is absent.
- Compared Minecraft 1.21.1 `ItemInHandRenderer`, player attack-strength timing, vanilla sword/digger attributes, and the supplied Tinkers Construct 1.20.1 item/client-extension code.
- Inspected NeoForge 21.1's `IItemExtension` and `IItemStackExtension` to confirm the stack-sensitive attribute hook is the runtime path used when a stack has no explicit attribute component.
- Preserved unrelated dirty worktree changes and did not launch Prism or a live client.

**Architecture / Module Ownership**
- Relevant class/module change: `src/main/java/modernmods/modernfoundry/library/tools/item/ModifiableItem.java`, `src/main/java/modernmods/modernfoundry/library/tools/item/ranged/ModifiableLauncherItem.java`, and `README.md`.
- Owning module/system: Modern Foundry's shared item attribute bridge and Minecraft's vanilla first-person renderer.
- Existing logic reused or extracted: `AttributesModifierHook.getHeldAttributeModifiers(...)`, `ToolStack`, existing tool stats/modifier data, and NeoForge's `ItemAttributeModifiers` component API; no Hilt code changed.
- Net line change: 27 additions and 20 deletions across the three task files.
- New files: none.
- Build files updated: none.

**Rationale / Tradeoffs**
- Minecraft's equip interpolation and attack cooldown are driven by the player's effective `ATTACK_SPEED`; vanilla does not select a separate axe or sword first-person branch for ordinary swings.
- The port retained the old Forge-era `getAttributeModifiers(EquipmentSlot, ItemStack)` calculation but did not expose it through NeoForge 1.21.1's stack-sensitive item hook, so Hilt never received the missing tool attributes. The fix bridges the existing calculation instead of duplicating it or changing Hilt.

**Build / Validation**
- Production build or compile-only check: `gradlew.bat clean build --console=plain --no-daemon` passed.
- Exact artifact verified: `build/libs/ModernFoundry-1.21.1-4.0.1-NeoForge.jar`; SHA-256 `31f5179218261d294b67926f730877e5eba9132cd0bf67e4105ca48c053c2f14`.
- Bytecode/archive checks: `javap` confirmed the stack-sensitive method in both item bases; the archive contains both compiled classes and `META-INF/neoforge.mods.toml`.
- Manual validation: not performed; the user will perform the Prism client smoke test comparing vanilla and Modern Foundry equip/swing behavior.
- Tests created or run: existing `test` task passed through the full build; no new test was added because the remaining confirmation is client-visible timing/animation behavior.

## 2026-08-16 - Build latest ModernFoundry 4.0.1 JAR

**Prompt / Task**
- Build the latest Modern Foundry JAR.

**What Changed**
- Rebuilt the current 1.21.1-4.0.1 artifact with the existing Gradle workflow.
- No source, Gradle, or player-facing resource files were changed by this build task.

**Steps Taken**
- Confirmed `TASK.md` is absent and inspected the current branch, version, dependency JAR, and existing artifact.
- Ran `rtk proxy cmd.exe /d /c ".\\gradlew.bat build --rerun-tasks --console=plain --no-daemon"`.
- Inspected the fresh artifact timestamp, checksum, archive entries, and embedded NeoForge descriptor.

**Architecture / Module Ownership**
- Relevant class/module change: Gradle `build` output under `build/libs`.
- Owning module/system: Modern Foundry's existing NeoForge Gradle build.
- Existing logic reused or extracted: existing `build`, `test`, `check`, `verifyGeneratedTextures`, and `jar` tasks.
- Net line change: none from the build; only this required log entry was appended.
- New files: no tracked files; build outputs remain ignored.
- Build files updated: none.

**Rationale / Tradeoffs**
- `--rerun-tasks` was used so the reported JAR is freshly produced rather than an older up-to-date artifact.

**Build / Validation**
- Production build or compile-only check: `build` passed in 4m 13s; `compileJava`, `test`, `check`, and `jar` completed. The compiler reported 100 existing deprecation/unchecked warnings.
- Exact artifact verified: `build/libs/ModernFoundry-1.21.1-4.0.1-NeoForge.jar`; SHA-256 `40ea7a4189f8b3d2624667820c6caef7748386f468808dbc2e49c76acb44e1be`.
- Archive checks: `META-INF/neoforge.mods.toml`, `pack.mcmeta`, and `modernmods/modernfoundry/TConstruct.class` are present; the descriptor reports `modernfoundry` 4.0.1 and required Hilt 1.13.
- Manual validation: not performed; client, dedicated-server, fresh-world, multiplayer, and optional-integration behavior remain outside this build check.
- Tests created or run: existing `test`, `check`, `verifyGeneratedTextures`, and `git diff --check` passed; no new test was added.

## 2026-08-16 - Document repository directory policy

**Prompt / Task**
- Add a Modern Foundry-specific directory policy to the top of `AGENTS.md`.

**What Changed**
- Added the repository tree, allowed edit locations, and forbidden read-only/generated locations to `AGENTS.md`.
- Recorded the documentation change in `CHANGELOG.md`.

**Steps Taken**
- Read the root `TASK.md`, which is currently empty.
- Reviewed the existing top-level directories, `.gitignore`, tracked paths, and current worktree changes before editing.

**Architecture / Module Ownership**
- Relevant class/module change: repository instruction documentation in `AGENTS.md`.
- Owning module/system: Modern Foundry repository workflow and source-tree boundaries.
- Existing logic reused or extracted: existing root layout and `.gitignore` directory classifications.
- Net line change: documentation only; no Java, resource, or build logic changed.
- New files: none.
- Build files updated: none.

**Rationale / Tradeoffs**
- The policy identifies `src/**` as the implementation tree while protecting reference, dependency, generated, runtime, and packaged-artifact directories from accidental edits.

**Build / Validation**
- Production build or compile-only check: not run; this was a documentation-only change.
- Manual validation: reviewed the complete diff and confirmed the existing unrelated worktree changes remained untouched.
- Tests created or run: `git diff --check`.

## 2026-08-16 - Restore shader-compatible fluid rendering

**Prompt / Task**
- Fix Modern Foundry liquids becoming invisible when shader packs are enabled while retaining the working fluid behavior from the Tinkers Construct reference.

**What Changed**
- Changed Modern Foundry's shared fluid render type from Hilt's custom fog-fix shader to Minecraft's `POSITION_COLOR_TEX_LIGHTMAP_SHADER`.
- Routed smeltery tanks, gauges, faucets, channels, casting/proxy tanks, and fluid projectiles through the shared type.
- Added a buffer adapter for Hilt's scaled-fluid helper so its internal hard-coded Hilt fluid type is remapped to the Modern Foundry type.
- Documented the shader-compatible fluid path in `README.md` and `CHANGELOG.md`.

**Steps Taken**
- Read the root `TASK.md`, which is currently empty.
- Compared Modern Foundry's fluid render calls and render types against `TinkersConstruct-1.20.1`.
- Traced Hilt's `HiltRenderTypes.FLUID` and `HiltShaders` implementation; Hilt documents that its custom fluid fog-fix shader can break shader compatibility and exposes the vanilla shader as its fallback.
- Preserved unrelated dirty worktree changes and did not change the Hilt dependency source or JAR.

**Architecture / Module Ownership**
- Relevant class/module change: `library/client/TinkerRenderTypes.java`, `library/client/RenderUtils.java`, smeltery client renderers, and `tools/client/FluidEffectProjectileRenderer.java`.
- Owning module/system: Modern Foundry client-side fluid and block-entity rendering.
- Existing logic reused or extracted: Hilt's `FluidRenderer` geometry and fluid texture/light calculations; only the render type and Hilt buffer selection were changed.
- Net line change: 21 additions and 15 deletions across the eight task source files, excluding documentation and pre-existing unrelated worktree edits.
- New files: none.
- Build files updated: none.

**Rationale / Tradeoffs**
- The custom Hilt fluid shader is the shader-specific compatibility boundary. Using Minecraft's existing position/color/texture/lightmap shader keeps the established translucent, no-cull fluid geometry while allowing shader packs to handle the draw through a vanilla shader path.
- The Hilt dependency remains unchanged; the adapter is limited to Hilt's `FLUID` render type and preserves all other buffer requests.

**Build / Validation**
- Production build or compile-only check: `gradlew.bat build --console=plain --no-daemon` passed in 5m 12s; the compiler reported 20 existing deprecation warnings.
- Exact artifact verified: `build/libs/ModernFoundry-1.21.1-4.1.0-NeoForge.jar`; SHA-256 `F81343EB09FBD887173A5B7519C0F94EC8B72290D45B620B8F1EDDE41E729AE5`.
- Manual validation: not performed; an in-world client smoke test with the affected shader pack is still required for tanks, casting fluids, world fluids, and projectile effects.
- Tests created or run: `compileJava`, `build`, and `git diff --check`; no new test was added because this is a client render-state integration change.

## 2026-08-16 - Complete 1.20.1 tool and tool-part parity data

**Prompt / Task**
- Make Modern Foundry's 1.21.1 weapons, tools, materials, and tool parts mirror the supplied Tinkers Construct 1.20.1 values, speeds, effects, and supporting data.

**What Changed**
- Added Jadeite to the material registry/data providers, composite recipe, Nether-gated material tag, render metadata, and generated material resources.
- Restored missing ribcage repair stats and material traits: Blazing Bone conductive, End Rod ammo hover, and End Rod ribcage float.
- Added the missing `float` modifier and `ModifierIds.floaty` provider entry.
- Changed armor luck and fortune targeting to the worn-armor tag so the modifier applies to equipped armor rather than every armor stack.
- Restored the reference shell support in the unknown material sprite provider and the reference fiery render fallbacks.
- Preserved the existing NeoForge AOE harvest bridge and stack-sensitive tool attribute path; no Hilt source change was required.

**Steps Taken**
- Read the root `TASK.md`, which is currently empty.
- Compared the Modern Foundry generated tool/material/modifier resources with `References/TinkersConstruct-1.20.1`, normalizing only the known Modern Foundry/Hilt namespace and 1.21.1 NeoForge/common-tag/attribute API translations.
- Verified parity counts and zero normalized mismatches for 45 tool definitions, 98 material definitions, 93 material-stat files, 93 trait files, 292 modifiers, and 94 material-render files.
- Ran `runData`; it completed successfully, but this checkout registered zero active data providers and wrote no generated provider output, so the committed generated resources were synchronized manually from the read-only reference and reviewed.
- Preserved unrelated dirty documentation, render, and configuration changes in the worktree.

**Architecture / Module Ownership**
- Relevant class/module change: material and modifier data providers under `tools/data/material`, `tools/data/sprite`, and `tools/data/ModifierProvider`, plus synchronized generated JSON and required ribcage/repair-kit textures.
- Owning module/system: Modern Foundry's data-driven material, modifier, tool-part, and tool-definition systems.
- Existing logic reused or extracted: existing material IDs, stat types, trait builders, modifier builders, generated-resource layout, and the existing AOE/harvest/attribute runtime paths.
- Net line change: parity data/provider corrections only; no new runtime abstraction or Hilt change.
- New files: Jadeite data/recipe/render resources, `float.json`, and the generated ribcage/repair-kit textures listed by the worktree diff.
- Build files updated: none.

**Rationale / Tradeoffs**
- Kept the port's current `modernfoundry`, Hilt, NeoForge, and common-tag identifiers while comparing semantics against the old Forge/Tinkers Construct identifiers; this preserves 1.21.1 compatibility without copying obsolete API names.
- Corrected the source providers as well as generated output. Because no active data providers are registered in this checkout, generated resources were updated from the read-only reference only after the provider/source diff was reviewed.
- Hilt was not modified: the parity gaps were in Modern Foundry's material/modifier data and NeoForge-facing runtime translation already owned by Modern Foundry.

**Build / Validation**
- Production build: `rtk cmd /d /c ".\\gradlew.bat clean build --console=plain --no-daemon"` passed in 4m 55s; the follow-up `build` after the provider corrections passed in 3m 06s. Existing compiler deprecation/unchecked warnings remained.
- Data task: `runData --console=plain --no-daemon` passed in 34s and reported `All providers took: 0 ms` with zero generated providers.
- Exact artifact verified: `build/libs/ModernFoundry-1.21.1-4.1.0-NeoForge.jar`; SHA-256 `f4112ca95ae790deed89b9d9be5829c413e8cc919f349ce6fd5591e43b7ad00a`.
- Tests/checks: existing `test`/`check`, `verifyGeneratedTextures`, parsing of all 9,894 generated JSON files, normalized reference parity audit, and `git diff --check` passed.
- Manual validation: not performed. A fresh-world client and dedicated-server smoke matrix is still required for mining speed, AOE/vein/tree/crop behavior, melee/severing, projectiles, melting, hybrid tools, and both shields.

## 2026-08-16 - Preserve anvil material textures across reloads

**Prompt / Task**
- Preserve a manyullyn/material texture on a Modern Foundry Anvil after a client restart.

**What Changed**
- Migrated `RetexturedTableBlockEntity` texture persistence to provider-aware `saveSynced` and `loadAdditional` hooks.
- Migrated `TinkerStationBlockEntity` material persistence to the same active hooks, covering Tinkers and Scorched Anvils.
- Kept the existing `texture` and `Material` NBT keys and dynamic model path unchanged.

**Steps Taken**
- Read the root `TASK.md`, which is currently empty.
- Traced anvil placement through `TinkerStationBlock` and the shared retextured table block entity.
- Verified the Hilt 1.13 save/load bridge and NeoForge 1.21.1 block-entity persistence signatures before changing the overrides.
- Preserved unrelated generated, documentation, and configuration state in the worktree.

**Architecture / Module Ownership**
- Relevant class/module change: `tables/block/entity/table/RetexturedTableBlockEntity.java` and `TinkerStationBlockEntity.java`.
- Owning module/system: Modern Foundry retextured table block entities and dynamic anvil model data.
- Existing logic reused or extracted: existing texture/material serialization, `RetexturedHelper`, and `ModelProperties` model-data flow.
- Net line change: 2 source lines net; no new files.
- Build files updated: none.

**Rationale / Tradeoffs**
- NeoForge 1.21.1 loads world block entities through `loadAdditional` and saves them through provider-aware `saveAdditional`; Hilt delegates its synchronized data through the matching provider-aware `saveSynced` hook. The old no-provider overrides were not part of the disk path.
- Fixed the shared persistence boundary so all retextured tables benefit, while leaving IDs, resource models, and gameplay behavior unchanged.

**Build / Validation**
- Production compile: `rtk proxy .\\gradlew.bat compileJava --console=plain --no-daemon` passed; existing deprecation warnings remained.
- Production build: `rtk proxy .\\gradlew.bat build --console=plain --no-daemon` passed in 3m 45s, including `test`, `check`, and `verifyGeneratedTextures`.
- Exact artifact verified: `build/libs/ModernFoundry-1.21.1-4.1.1-NeoForge.jar`; SHA-256 `002a0b5c486bb9505257827c9a1c0cdecfb8589d404d7ee3b067f28534aa6972`.
- Tests/checks: `git diff --check` passed; `testJunit` reported `NO-SOURCE`.
- Manual validation: not performed. A client smoke test placing a manyullyn anvil, restarting the client/world, and checking both anvil variants remains required.

## 2026-08-16 - Write anvil textures through the chunk-save path

**Prompt / Task**
- Fix anvils still reverting to the stock appearance after relogging; Jade also showed the original untextured anvil.

**What Changed**
- Added provider-aware `saveAdditional` overrides to `RetexturedTableBlockEntity` and `TinkerStationBlockEntity`.
- Persisted the existing `texture` and `Material` keys directly through the inventory block-entity disk-save path.
- Retained `saveSynced` for update tags and `loadAdditional` for world/client loading.

**Steps Taken**
- Used the supplied screenshot as runtime evidence: the Jade pick-stack icon confirmed the block entity had fallen back to the default texture state.
- Inspected Hilt 1.13 bytecode and found `InventoryBlockEntity.saveAdditional` writes inventory through `NameableBlockEntity` without delegating to Hilt's `saveSynced` bridge.
- Added the missing disk-save hooks, rebuilt the project, and reviewed the complete diff.

**Architecture / Module Ownership**
- Relevant class/module change: `tables/block/entity/table/RetexturedTableBlockEntity.java` and `TinkerStationBlockEntity.java`.
- Owning module/system: Modern Foundry retextured table block-entity persistence and dynamic anvil model data.
- Existing logic reused or extracted: existing NBT keys, `RetexturedHelper`, `ModelProperties`, and provider-aware load/update hooks.
- Net line change: 16 additional source lines; no new files.
- Build files updated: none.

**Rationale / Tradeoffs**
- The previous fix corrected the provider-aware sync/load signatures but did not cover the actual world-save override inherited from `InventoryBlockEntity`. Explicit `saveAdditional` serialization closes that gap without changing IDs, models, or gameplay behavior.

**Build / Validation**
- Production compile: `rtk proxy .\\gradlew.bat compileJava --console=plain --no-daemon` passed; existing deprecation warnings remained.
- Production build: `rtk proxy .\\gradlew.bat build --console=plain --no-daemon` passed in 51s, including `test`, `check`, and `verifyGeneratedTextures`.
- Exact artifact verified: `build/libs/ModernFoundry-1.21.1-4.1.1-NeoForge.jar`; SHA-256 `1e6ff9e9837e3058b51c71167cf1b88ed68aa58c311ac537e5911bafb64564c3`.
- Tests/checks: `git diff --check` passed; `testJunit` reported `NO-SOURCE`.
- Manual validation: not performed. The rebuilt JAR still requires the in-game manyullyn-anvil placement/relog/Jade smoke test.

## 2026-08-16 - Refresh anvil model data after reload

**Prompt / Task**
- Fix manyullyn/material anvils that render as the stock white anvil after relogging and only regain their color after a nearby block is broken.

**What Changed**
- Replaced the ineffective `getModelData()` comparison in `RetexturedTableBlockEntity.textureUpdated()` with Hilt's existing `RetexturedHelper.onTextureUpdated(this)` refresh path.
- Reloaded retextured tables now request fresh model data and send the normal block update, invalidating the client renderer cache immediately.
- Kept the existing NBT keys, material/texture selection, block IDs, and model resources unchanged.

**Steps Taken**
- Used the nearby-block-break behavior to identify stale client model data rather than missing material persistence.
- Traced the shared retextured-table update path and reused the existing Hilt helper instead of adding a second renderer refresh mechanism.
- Rebuilt the project and reviewed the complete worktree diff.

**Architecture / Module Ownership**
- Relevant class/module change: `tables/block/entity/table/RetexturedTableBlockEntity.java`.
- Owning module/system: Modern Foundry retextured table block entities and dynamic anvil model data.
- Existing logic reused or extracted: Hilt `RetexturedHelper`, `ModelProperties` model-data flow, and the provider-aware persistence hooks from the preceding fix.
- Net line change: replaced the dead comparison with one shared helper call; no new files.
- Build files updated: none.

**Rationale / Tradeoffs**
- The old comparison inspected model data after `texture` had already been changed, so it could compare equal and skip `requestModelDataUpdate()`. A neighboring block break then forced the rebuild, which exactly matched the reported behavior.
- The shared helper is the smallest fix that covers every retextured table using this path without changing gameplay or save data.

**Build / Validation**
- Production build: `rtk proxy cmd.exe /d /c ".\\gradlew.bat build --console=plain --no-daemon"` passed in 55s, including `test`, `check`, and `verifyGeneratedTextures`.
- Exact artifact verified: `build/libs/ModernFoundry-1.21.1-4.1.1-NeoForge.jar`; SHA-256 `35be57576b3741ac096f6eb2f952359145c81cce5ec5289d7a1dfe462d6224ae`.
- Tests/checks: `testJunit` reported `NO-SOURCE`; `git diff --check` passed.
- Manual validation: not performed. The new JAR still requires a fresh manyullyn-anvil placement, relog/client restart, Jade tooltip, and nearby-block-break smoke test.

## 2026-08-16 - Translate floating island worldgen templates

**Prompt / Task**
- Ensure the original Tinkers Construct 1.20.1 worldgen, including floating slime islands, is properly translated to Modern Foundry on Minecraft 1.21.1.

**What Changed**
- Rewrote legacy `tconstruct:` namespace IDs in all 25 shipped island structure NBT templates.
- Covered the dirt, sky, earth, blood, and End template families across all five island sizes.
- Left the read-only reference and unrelated book structure NBT untouched.

**Steps Taken**
- Compared the current worldgen Java and generated JSON inventory with `References/TinkersConstruct-1.20.1/`.
- Confirmed the six island structures, four structure sets, biome tags, placement logic, tree features, and spawn settings were already present.
- Found legacy namespace IDs in compressed NBT palettes/data markers, translated them to `modernfoundry`, and verified the built JAR contents.

**Architecture / Module Ownership**
- Relevant class/module change: `src/main/resources/data/modernfoundry/structures/islands/` and `src/generated/resources/data/modernfoundry/structures/islands/`.
- Owning module/system: Modern Foundry island structure templates consumed by `IslandPiece` and `IslandStructure`.
- Existing logic reused or extracted: existing 1.21.1 structure registration, placement, data-marker handling, and repalletter mappings.
- Net line change: 0 text lines; 25 compressed NBT resources updated.
- New files: none.
- Build files updated: none.

**Rationale / Tradeoffs**
- Registration and placement parity was already complete; the remaining defect was resource-level namespace translation. The stale IDs prevented current `modernfoundry` palettes and `IslandPiece` data markers from resolving.
- Kept the fix data-only and scoped to worldgen, avoiding unrelated legacy book structures.

**Build / Validation**
- Production build: `rtk proxy cmd.exe /d /c ".\\gradlew.bat clean build --console=plain --no-daemon"` passed in 5m 53s.
- Static validation: all 25 NBT files decoded with no `tconstruct:`/`slimeknights:` IDs; all 31 worldgen JSON files parsed; six structures and four structure sets were present; `git diff --check` passed.
- Exact artifact: `build/libs/ModernFoundry-1.21.1-4.1.2-NeoForge.jar`; SHA-256 `541bdc40e9fedfd6199e7f9a5780b8b43f2cbec9e3ef13a7aa6c4de373e796d6`.
- JAR validation: all 25 translated island templates were present and readable in the artifact.
- Manual validation: not performed. A fresh-world client and dedicated-server smoke test remains required to confirm visible island placement, collision, trees, foliage, fluids, and mob spawns.

## 2026-08-16 - Repair translated island NBT string lengths

**Prompt / Task**
- Investigate why `/place template` failed and `/place structure modernfoundry:sky_slime_island` reported success without showing a floating island.

**What Changed**
- Corrected the two-byte NBT string-length fields for every translated `modernfoundry:` namespace value across all 25 island templates.
- Repaired 2,500 namespace fields in the generated sky, earth, blood, End, and main-resource dirt templates.
- No Java, registry, structure JSON, or generation-placement logic changed.

**Steps Taken**
- Compared the launch log command results with the 1.21.1 `StructureTemplate.placeInWorld` return paths.
- Decompressed a failing template and found values such as `modernfoundry:earth_slime_dirt` still carrying the shorter legacy `tconstruct:` length.
- Repaired the length fields, decoded all 25 source NBT files, and verified their palettes, block lists, dimensions, and namespace contents.
- Rebuilt the production JAR and decoded all 25 templates from inside the artifact.

**Architecture / Module Ownership**
- Relevant class/module change: `src/main/resources/data/modernfoundry/structures/islands/` and `src/generated/resources/data/modernfoundry/structures/islands/`.
- Owning module/system: Modern Foundry island structure templates consumed by `IslandPiece` and `StructureTemplate`.
- Existing logic reused or extracted: existing island structure registration and vanilla compressed-NBT template loading.
- Net line change: 0 text lines; 25 compressed NBT resources repaired.
- New files: none.
- Build files updated: none.

**Rationale / Tradeoffs**
- The namespace byte replacement was incomplete at the NBT encoding layer: the three-byte namespace expansion shifted each value past its stored length, leaving Minecraft with invalid template data. Updating those fields fixes the load failure at the resource boundary without changing worldgen behavior.

**Build / Validation**
- Production build: `rtk proxy cmd.exe /d /c ".\\gradlew.bat clean build --console=plain --no-daemon"` passed in 5m 12s; existing deprecation warnings remain.
- Static validation: all 25 source NBT files and all 25 JAR NBT entries decoded fully; no `tconstruct:` bytes remained; `git diff --check` passed.
- Exact artifact: `build/libs/ModernFoundry-1.21.1-4.1.2-NeoForge.jar`; SHA-256 `22da5dc299b88c1dd4f4809c69136d6bb7c4d7cb1b8d4c9cf3a310fcbaa09331`.
- Manual validation: not performed. The existing Slime Test world must be restarted with this rebuilt JAR, and a fresh chunk or direct `/place template` test is still required; prior failed generation cannot retroactively create blocks.

## 2026-08-16 - Fix 1.21.1 island template resource paths

**Prompt / Task**
- Investigate the exact `/place template` failure after the island NBT namespace and string-length repairs.

**What Changed**
- Moved all 25 island NBT templates from `data/modernfoundry/structures/islands/` to `data/modernfoundry/structure/islands/`.
- Kept the repaired NBT contents unchanged; this corrects only the resource directory consumed by Minecraft 1.21.1.

**Steps Taken**
- Used the in-game result `There is no template with id "modernfoundry:islands/sky/11x1x11"` to trace the failure to resource discovery.
- Verified Minecraft 1.21.1 uses `FileToIdConverter("structure", ".nbt")`, while the JAR exposed the files under the plural `structures` directory.
- Renamed the generated sky, earth, blood, End, and main-resource dirt template paths and rebuilt the production artifact.
- Synced the rebuilt artifact to the local Prism Testing instance.

**Architecture / Module Ownership**
- Relevant class/module change: `src/generated/resources/data/modernfoundry/structure/islands/` and `src/main/resources/data/modernfoundry/structure/islands/`.
- Owning module/system: vanilla `StructureTemplateManager` resource loading used by `IslandPiece` and direct `/place template` commands.
- Existing logic reused or extracted: existing island registration and template IDs; no Java or worldgen JSON changes.
- Net line change: required-log documentation updates; 25 binary resource renames.
- New files: none.
- Build files updated: none.

**Rationale / Tradeoffs**
- Minecraft 1.21.1 resolves structure templates from the singular `structure` directory. The previous plural path let the resources appear in the JAR while remaining invisible to `StructureTemplateManager`.
- A resource-path correction is sufficient; adding a custom loader or command would duplicate vanilla behavior.

**Build / Validation**
- Production build: `rtk proxy cmd.exe /d /c ".\\gradlew.bat clean build --console=plain --no-daemon"` passed in 6m 19s.
- Static validation: the JAR contains all 25 templates under `data/modernfoundry/structure/islands/`, including `sky/11x1x11.nbt`; no old plural island path is present.
- Exact artifact: `build/libs/ModernFoundry-1.21.1-4.1.2-NeoForge.jar`; SHA-256 `f83ad1f29da6bdca868d8de8b435ffbb5a72939435d1e7e08fc053b204459346`.
- Tests/checks: `testJunit` reported `NO-SOURCE`; `check`, `verifyGeneratedTextures`, and `git diff --check` passed.
- Manual validation: the fixed JAR is synced, but the currently running client must be restarted before the direct `/place template` smoke test.

## 2026-08-16 - Correct custom slime hitboxes

**Prompt / Task**
- Fix Enderslime, Skyslime, and Terracube hitboxes that were substantially larger than their rendered entities.

**What Changed**
- Changed the registered base dimensions for all three custom slime entity types from `2.04F` to `0.52F`.
- Left the slime renderers, entity subclasses, IDs, and size/splitting behavior unchanged.

**Steps Taken**
- Read the empty root `TASK.md`.
- Traced the three entity registrations and the shared vanilla `Slime` dimension path.
- Confirmed Minecraft 1.21.1's vanilla slime registration uses `0.52F` base dimensions while the current port retained the pre-1.21 `2.04F` value.
- Reviewed the focused source diff and checked that all three registrations now use `0.52F`.

**Architecture / Module Ownership**
- Relevant class/module change: `world/TinkerWorld.java` entity registrations.
- Owning module/system: Modern Foundry custom slime entity types and vanilla slime dimensions.
- Existing logic reused or extracted: vanilla 1.21.1 `Slime` dimension scaling; no new helper or abstraction.
- Net line change: 3 source lines; no new files.
- Build files updated: none.

**Rationale / Tradeoffs**
- The old `2.04F` value is the legacy maximum slime base size. In 1.21.1, slime dimensions are scaled from a `0.52F` base, so retaining `2.04F` inflated size-1 collision boxes by roughly four times.
- Kept the fix at the registration boundary so combat, movement, spawning, rendering, and slime splitting retain their existing behavior.

**Build / Validation**
- Production build: `rtk proxy cmd.exe /d /c ".\\gradlew.bat build --console=plain --no-daemon"` passed after retrying a transient `:test` cleanup failure caused by a locked generated `build/test-results/test/binary/output.bin`; existing deprecation warnings remain.
- Tests/checks: `:test` completed as `UP-TO-DATE` after retry, `check` passed, and `testJunit` reported `NO-SOURCE`.
- Static validation: `git diff --check` passed; all three registrations were confirmed at `0.52F`.
- Exact artifact: `build/libs/ModernFoundry-1.21.1-4.1.2-NeoForge.jar`; SHA-256 `2cde569c2c8b81036491c71cc0eb4a552f5f2009a301c1a923906af1a59b2bdb`.
- Manual validation: not performed. An in-game F3+B smoke test with freshly spawned custom slimes remains required.

## 2026-08-17 - Allow vanilla glass in early smeltery recipes

**Prompt / Task**
- Fix recipes such as fuel gauges and tanks requiring Modern Foundry clear glass before a smeltery or melter can be made.

**What Changed**
- Added `minecraft:glass` to the shipped `c:glass/colorless` block and item tags.
- Added `minecraft:glass_pane` to the shipped `c:glass_panes/colorless` block and item tags.
- Kept the existing fuel gauge, fuel tank, and other recipe definitions unchanged.

**Steps Taken**
- Read the empty root `TASK.md` and confirmed the active `Neo/1.21.1` branch.
- Traced the generated fuel gauge and fuel tank recipes to `c:glass`, then traced that tag through its colorless subtag.
- Checked `build.gradle`; the canonical singular generated tag resources are shipped while the legacy data providers are excluded from the main source set.
- Reviewed the focused diff and verified the resulting archive contents.

**Architecture / Module Ownership**
- Relevant resource change: `src/generated/resources/data/c/tags/{block,item}/glass{,_panes}/colorless.json`.
- Owning module/system: shipped NeoForge common block and item tags used by crafting recipes.
- Existing logic reused or extracted: existing `c:glass` and `c:glass_panes/colorless` recipe tags; no new helper or abstraction.
- Net line change: four vanilla tag entries across four existing files; no new files.
- Build files updated: none.

**Rationale / Tradeoffs**
- Broadened the common colorless tags at their source so every existing recipe using the common tags accepts vanilla glass, while retaining Modern Foundry clear glass as a valid alternative.
- No recipe-specific exceptions or balance changes were added.

**Build / Validation**
- Production build: `rtk .\\gradlew.bat clean build --console=plain --no-daemon` passed; existing deprecation warnings remain.
- Tests/checks: Gradle `:test` and `:check` passed; `:testJunit` reported `NO-SOURCE`.
- Static validation: `git diff --check` passed; the exact JAR archive contains all four corrected tag files and all four seared/scorched fuel gauge/tank recipes.
- Exact artifact: `build/libs/ModernFoundry-1.21.1-4.1.3-NeoForge.jar`; SHA-256 `4ea4eef92250eeeaa356d9386ba877568a119a8da40309e13b9fb05adf02e459`.
- Manual validation: not performed. A fresh-world client smoke test remains useful for recipe UI and runtime tag resolution.

## 2026-08-17 - Build ModernFoundry 4.1.4 JAR

**Prompt / Task**
- Build the latest Modern Foundry JAR.

**What Changed**
- Produced the versioned 4.1.4 NeoForge JAR from the clean checkout.
- No source or shipped resource changes were made.

**Steps Taken**
- Read the empty root `TASK.md` and confirmed the active `Neo/1.21.1` branch.
- Checked `gradle.properties`, the local Hilt 1.13 dependency, and the clean worktree.
- Ran `rtk .\\gradlew.bat clean build --console=plain --no-daemon`.
- Inspected the exact primary JAR and confirmed its required descriptor, metadata, repaired glass tag, and fuel-tank recipe entries.

**Architecture / Module Ownership**
- Relevant artifact: `build/libs/ModernFoundry-1.21.1-4.1.4-NeoForge.jar`.
- Owning module/system: Gradle production build and NeoForge JAR packaging.
- Existing logic reused or extracted: existing Gradle wrapper, resource processing, tests, and packaging tasks.
- Net line change: zero source/resource lines; build output is generated and ignored.
- New files: none in the repository.
- Build files updated: none.

**Rationale / Tradeoffs**
- Used the clean build path to ensure the reported JAR was regenerated from the current version instead of relying on a stale artifact.

**Build / Validation**
- Production build: `rtk .\\gradlew.bat clean build --console=plain --no-daemon` passed in 7m49s; existing deprecation warnings remain.
- Tests/checks: Gradle `:test` and `:check` passed; `:testJunit` reported `NO-SOURCE`.
- Archive validation: `META-INF/neoforge.mods.toml`, `pack.mcmeta`, `data/c/tags/item/glass/colorless.json`, and the seared fuel-tank recipe are present.
- Exact artifact: `build/libs/ModernFoundry-1.21.1-4.1.4-NeoForge.jar`; SHA-256 `238b86893f814f95341f1fff10bc3bab8c704633f1f17dddb387688f35768ada`.
- Manual validation: not performed; client, dedicated-server, and fresh-world smoke tests remain outstanding.

## 2026-08-17 - Make cobalt ore harvestable with diamond-tier tools

**Prompt / Task**
- Make Nether cobalt ore drop its ore output when mined with a diamond pickaxe; diamond+ includes netherite.

**What Changed**
- Added `modernfoundry:cobalt_ore` to `minecraft:mineable/pickaxe`.
- Added `modernfoundry:cobalt_ore` to `minecraft:needs_diamond_tool`, so diamond and netherite tools qualify.
- Updated the dormant `BlockTagProvider` to preserve the diamond-tier harvest requirement during future data generation.

**Steps Taken**
- Read the empty root `TASK.md` and confirmed the active `Neo/1.21.1` branch.
- Traced `TinkerWorld.cobaltOre`, its `requiresCorrectToolForDrops()` property, the shipped vanilla harvest tags, and the existing cobalt ore loot table.
- Found that the shipped pickaxe tag contained only the seared melter and fuel tank, while cobalt ore was absent from a vanilla harvest-tier tag.
- Updated the canonical generated resources and the dormant provider without changing the existing loot table.

**Architecture / Module Ownership**
- Relevant source/resource changes: `common/data/tags/BlockTagProvider.java` and `data/minecraft/tags/block/{mineable/pickaxe,needs_diamond_tool}.json`.
- Owning module/system: vanilla Minecraft block harvest tags and Modern Foundry cobalt ore loot data.
- Existing logic reused or extracted: `requiresCorrectToolForDrops()`, vanilla diamond-tier tag semantics, and the existing `blocks/cobalt_ore` loot table.
- Net line change: 9 additions and 2 removals across the source and shipped tag resources.
- New files: `data/minecraft/tags/block/needs_diamond_tool.json`.
- Build files updated: none.

**Rationale / Tradeoffs**
- Kept the requested Diamond+ boundary at the vanilla tag layer; netherite inherits diamond-tier eligibility without a second custom rule or loot-table change.

**Build / Validation**
- Production build: `rtk .\\gradlew.bat clean build --console=plain --no-daemon` passed in 4m33s; existing deprecation warnings remain.
- Tests/checks: Gradle `:test` and `:check` passed; `:testJunit` reported `NO-SOURCE`.
- Archive validation: the exact JAR contains the pickaxe tag, diamond-tool tag, and `data/modernfoundry/loot_table/blocks/cobalt_ore.json`, each containing `modernfoundry:cobalt_ore` where expected; `git diff --check` passed.
- Exact artifact: `build/libs/ModernFoundry-1.21.1-4.1.4-NeoForge.jar`; SHA-256 `4a48afbf07d572ee50ef1c8ebaa94c93277575ce3ffc1765fd1a76d69b35418e`.
- Manual validation: not performed; fresh-world Nether mining with diamond and netherite pickaxes remains the gameplay smoke-test gap.
