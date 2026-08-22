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

## 2026-08-17 - Restore complete block harvest-tag coverage

**Prompt / Task**
- Audit tool materials and block harvest levels against `References/TinkersConstruct-1.20.1/` and fix every incorrect or missing mining-level/tool-type assignment.

**What Changed**
- Restored the shipped `minecraft` mineable tags for axe, hoe, pickaxe, and shovel.
- Restored the shipped Minecraft and NeoForge wood, stone, iron, gold, diamond, and netherite harvest-tier tags.
- Kept `modernfoundry:cobalt_ore` in the Diamond tier only, so diamond and netherite tools qualify; removed it from the lower Iron/Gold assignments.
- Kept `modernfoundry:seared_fuel_tank` and `modernfoundry:seared_melter` in Stone and out of Gold.
- Confirmed the 93 material-stat files, including mining tier and speed, match the reference after `tconstruct` to `modernfoundry` namespace normalization.

**Steps Taken**
- Read the empty root `TASK.md` and confirmed the active `Neo/1.21.1` branch.
- Compared all 10 shipped harvest/mineable tag files with the corresponding reference files, accounting for the intentional cobalt and seared-fuel-tier corrections.
- Parsed every changed/new tag as JSON and checked the focused tag assignments directly.
- Ran the clean production build and inspected the resulting archive for all harvest tags and the existing cobalt ore loot table.

**Architecture / Module Ownership**
- Relevant resource changes: `src/generated/resources/data/{minecraft,neoforge}/tags/block/` harvest and mineable JSON files.
- Owning module/system: vanilla/NeoForge block harvest tags consumed by Minecraft's correct-tool-for-drops logic.
- Existing logic reused or extracted: existing `BlockTagProvider` assignments, vanilla Diamond-tier inheritance for netherite, and the existing cobalt ore loot table; no Java code or loot-table rewrite was needed.
- Net line change: 514 added lines and 1 removed line across 10 generated tag files; 7 new files and 3 updated files.
- New files: axe, hoe, shovel, iron, gold, netherite, and wood harvest-tag JSON files.
- Build files updated: none.

**Rationale / Tradeoffs**
- Fixed the shipped data at the tag layer where Minecraft decides whether a block breaks with correct drops, instead of adding custom block-break logic or duplicating loot behavior.
- Preserved the requested Diamond+ boundary and existing material-stat values; no unrelated balance changes were introduced.

**Build / Validation**
- Production build: `rtk .\\gradlew.bat clean build --console=plain --no-daemon` passed in 7m37s; existing deprecation warnings remain.
- Tests/checks: Gradle `:test` and `:check` passed; `:testJunit` reported `NO-SOURCE`.
- Static validation: `git diff --check` passed; all 10 tag comparisons passed; all 93 material-stat comparisons passed.
- Archive validation: the exact JAR contains all 10 harvest/tag entries and `data/modernfoundry/loot_table/blocks/cobalt_ore.json`; cobalt is Diamond-only and the cobalt loot table has pools/output.
- Exact artifact: `build/libs/ModernFoundry-1.21.1-4.1.4-NeoForge.jar`; SHA-256 `145dfb244cdade4dc47faadbef504974e1bae2b509f5fea2f117e787ff0e8fb8`.
- Manual validation: not performed. Fresh-world Nether mining with diamond and netherite pickaxes, plus client/dedicated-server smoke tests, remain outstanding.

## 2026-08-17 - Fix Crafting Station ore duplication

**Prompt / Task**
- Fix Crafting Station ore duplication: ingredients were not consumed, and Shift + Right Click could fill the inventory with ore blocks or ingots.

**What Changed**
- Mapped compact `CraftingInput` remainder coordinates back to the raw 3x3 Crafting Station slots in `CraftingStationBlockEntity.takeResult`.
- Treated missing remainder entries as empty so the shared consumption path cannot skip an input.

**Steps Taken**
- Read the empty root `TASK.md` and traced the Crafting Station menu, lazy result slot, block entity, and vanilla `CraftingInput.ofPositioned` behavior.
- Confirmed Minecraft compacts non-empty crafting bounds, while the old consumption loop indexed the untrimmed 3x3 inventory directly.
- Updated the single shared result-consumption path without changing recipes, recipe IDs, or output balance.

**Architecture / Module Ownership**
- Relevant class: `tables/block/entity/table/CraftingStationBlockEntity.java`.
- Owning module/system: Crafting Station result consumption and vanilla crafting-grid coordinate mapping.
- Existing logic reused or extracted: `CraftingInput.Positioned` and the existing recipe remainder contract.
- Net line change: 8 additions and 4 removals in the production source; no new files.
- Build files updated: none.

**Rationale / Tradeoffs**
- The fix preserves vanilla remainder-item behavior while translating compact recipe coordinates to the real table slots; no recipe-specific ore allowlist or special case was added.

**Build / Validation**
- Production build: `rtk .\\gradlew.bat build --console=plain --no-daemon` passed; existing deprecation warnings remain.
- Tests/checks: `rtk .\\gradlew.bat test check --console=plain --no-daemon` passed; `testJunit` reported `NO-SOURCE`.
- Static validation: `rtk git diff --check` passed.
- Archive validation: `build/libs/ModernFoundry-1.21.1-4.1.5-NeoForge.jar` contains `CraftingStationBlockEntity.class`; SHA-256 `06a09cb958eca27753952d45eebe6735a1a931c6ea5f795c3f5c4476ee1ffee2`.
- Manual validation: not performed; live Crafting Station tests with centered ore recipes and Shift + Right Click remain outstanding.

## 2026-08-17 - Restore inventory drops, leaf saplings, and JEI modifier textures

**Prompt / Task**
- Fix reported inventory loss in stations, anvils, and chests; restore pattern-chest slots and pattern insertion/drops; make sky leaves drop saplings; and remove black/purple missing textures from JEI.

**What Changed**
- Registered NeoForge `Capabilities.ItemHandler.BLOCK` providers for the Hilt inventory tables/anvils and the three Tinkers' chest block-entity types.
- Added `TinkerTags.Items.PATTERNS` to `TinkerTags.Items.CHEST_PARTS` in both the provider and shipped generated tag.
- Replaced the always-true loot shim with NeoForge's native `CanItemPerformAbility.canItemPerformAbility(ItemAbilities.SHEARS_DIG)` and removed the unused shim.
- Added the existing `gui/modifiers` directory to the shipped Minecraft block atlas.

**Steps Taken**
- Read the empty root `TASK.md`, traced Hilt's `InventoryBlock.onRemove` and `InventoryBlockEntity.registerItemHandler` paths, and confirmed missing capabilities caused empty chest screens and voided drops.
- Traced the part chest validator, leaf loot provider, generated sky-leaf loot table, and JEI `ModifierIconManager` atlas lookup.
- Confirmed the processed resources and JAR contain the updated atlas, chest tag, and native leaf ability condition.

**Architecture / Module Ownership**
- Relevant class/resource changes: `tables/TinkerTables.java`, `common/data/tags/ItemTagProvider.java`, `common/data/loot/BlockLootTableProvider.java`, `data/modernfoundry/tags/item/chest_parts.json`, and `assets/minecraft/atlases/blocks.json`.
- Owning module/system: NeoForge block capabilities, Tinker table/chest inventories, generated loot/tags, and the JEI modifier icon atlas.
- Existing logic reused or extracted: Hilt's shared inventory capability registration/drop path, existing pattern/chest tags, NeoForge's native loot condition, and existing modifier textures.
- Net line change: 25 additions, 4 removals, and 1 unused compatibility-shim deletion across the production/resource files; required changelog and trace entries appended.
- New files: none.
- Build files updated: none.

**Rationale / Tradeoffs**
- Fixed the shared capability registration boundary so every affected table/chest receives the same slot, automation, and break-drop behavior without block-specific drop code.
- Kept the existing loot and texture assets; only corrected their native registration/atlas exposure.

**Build / Validation**
- Production build: `rtk cmd.exe /d /c ".\\gradlew.bat check build --console=plain --no-daemon"` passed after final dead-shim cleanup in 52 seconds; existing deprecation warnings remain.
- Tests/checks: Gradle `test` and `check` passed; `testJunit` reported `NO-SOURCE`; `git diff --check` passed.
- Archive validation: `build/libs/ModernFoundry-1.21.1-4.1.5-NeoForge.jar` contains the updated atlas, chest tag, and sky-leaf loot table; SHA-256 `295a71340ceb605ebdf9aa22745552001fe7827570506d94cb0b8414bcf618fe`.
- Manual validation: not performed; fresh-world station/anvil/chest break-and-replace tests, pattern-chest UI/insertion, sky-leaf harvesting, and JEI visual smoke remain outstanding.
- Tests created or run: no dedicated tests added.

## 2026-08-17 - Fix chest inventory drops and stack limits

**Prompt / Task**
- Make Part, Tinkers', and Cast Chests drop their contents when broken and allow item stacks up to 64.

**What Changed**
- Added shared chest break handling that drops and clears the live chest item handler before Hilt's block-entity removal path runs.
- Enabled normal drops for Cast Chests, which were registered with `dropsItems = false`.
- Raised the Part, Cast, and Tinkers' Chest handler slot limits from 8/4/16 to 64.

**Steps Taken**
- Read the empty root `TASK.md` and traced `ChestBlock`, Hilt's `InventoryBlock.onRemove`, the NeoForge item-handler providers, and all three custom chest handlers.
- Confirmed the Cast Chest registration explicitly disabled drops and that the custom slot limits were 8, 4, and 16.
- Kept the existing capability registrations for menu/automation access while making the shared chest break path independent of capability lookup success.

**Architecture / Module Ownership**
- Relevant classes: `tables/block/ChestBlock.java`, `tables/TinkerTables.java`, and the three `tables/block/entity/chest/*BlockEntity.java` handlers.
- Owning module/system: custom chest block removal and NeoForge `IItemHandler` storage limits.
- Existing logic reused or extracted: Hilt's `InventoryBlock` removal contract, vanilla `Containers.dropItemStack`, the existing chest handlers, and existing capability providers.
- Net line change: 18 additions and 1 removal in `ChestBlock`, 1 registration correction, and 6 handler/comment updates; no new files.
- Build files updated: none.

**Rationale / Tradeoffs**
- Dropping through the actual chest handler before calling Hilt's cleanup prevents item loss even if the block capability lookup is unavailable; extracting each stack before spawning it prevents handler mutation from clearing the dropped entity's stack, and leaves the normal Hilt path empty.
- The 64 limit removes the custom artificial cap while preserving each item's own maximum stack size and the existing per-slot item validation.

**Build / Validation**
- Production build: `rtk cmd.exe /d /c ".\\gradlew.bat check build --console=plain --no-daemon"` passed in 2m32s; existing deprecation warnings remain.
- Tests/checks: Gradle `test` and `check` passed; `testJunit` reported `NO-SOURCE`; `git diff --check` passed.
- Archive validation: `build/libs/ModernFoundry-1.21.1-4.1.5-NeoForge.jar` contains the changed chest classes and does not contain the removed `CanToolPerformAction` shim.
- Exact artifact SHA-256: `1e2badc702a73f54879a0539535caae4e00fe038251185b9ac50d4b6011355da`.
- Manual validation: not performed; live break-and-pickup tests for all three chest types and 64-item insertion remain outstanding.
- Tests created or run: no dedicated tests added.

## 2026-08-17 - Fix Nether slime leaf drops

**Prompt / Task**
- Make slime leaves drop their matching saplings sometimes instead of always dropping leaf blocks, like regular leaves.

**What Changed**
- Reused the existing regular slime-leaf loot helper for Nether Blood and Ichor leaves in `BlockLootTableProvider`.
- Updated the canonical Blood and Ichor generated loot tables so normal harvesting can drop their matching saplings and slimeballs, while Silk Touch or shears still return the leaf block.

**Steps Taken**
- Read the empty root `TASK.md` and traced both foliage-type branches through the loot-table provider.
- Compared the stale Blood and Ichor tables with the already-correct Sky and Earth leaf tables.
- Updated the provider and the two shipped canonical JSON resources, then parsed both JSON files and checked their generated archive entries.

**Architecture / Module Ownership**
- Relevant source/resources: `common/data/loot/BlockLootTableProvider.java` and `data/modernfoundry/loot_table/blocks/{blood,ichor}_slime_leaves.json`.
- Owning module/system: Modern Foundry generated block loot tables for slime foliage.
- Existing logic reused or extracted: `randomDropSlimeBallOrSapling`, `dropSapling`, and the existing native shear-ability condition; no new helper or abstraction.
- Net line change: one existing Nether branch correction plus two generated loot-table updates; no new files.
- Build files updated: none.

**Rationale / Tradeoffs**
- Fixed the stale shipped data at the runtime resource boundary and kept the existing sapling chances, Fortune behavior, slimeball behavior, Silk Touch behavior, and shears behavior unchanged.

**Build / Validation**
- Production build: `rtk cmd.exe /d /c ".\\gradlew.bat check build --console=plain --no-daemon"` passed in 3m11s; existing Gradle deprecation warnings remain.
- Tests/checks: Gradle `test`, `check`, and `testJunit` (`NO-SOURCE`) completed successfully; `git diff --check` passed.
- Archive validation: `build/libs/ModernFoundry-1.21.1-4.1.5-NeoForge.jar` contains both updated leaf loot tables, the chest tag, and the modifier atlas; SHA-256 `b7c39d3e32a68382bf1e85240b014a16f59ee5ad545e76377392bb9069905fae`.
- Manual validation: not performed; in-game harvesting with bare hands/tools, Silk Touch, shears, Fortune, and explosion drops remains outstanding.
- Tests created or run: no dedicated tests added.

## 2026-08-17 - Keep shear-capable tools on sapling drops

**Prompt / Task**
- Stop slime leaves from returning the leaf block instead of the matching sapling when broken.

**What Changed**
- Changed the leaf self-drop dispatch from NeoForge's generic `SHEARS_DIG` ability to a literal `minecraft:shears` item check plus Silk Touch.
- Updated all five shipped slime-leaf loot tables so Kamas, Scythes, and other Tinkers tools with `SHEARS_DIG` receive the normal sapling/propagule-or-slimeball path.
- Kept the generic shears ability for ferns, tall grass, and vines.

**Steps Taken**
- Read the empty root `TASK.md` and verified the Earth table already had a sapling chance for ordinary tools.
- Traced `CanItemPerformAbility`, `ShearsModule`, and the KAMA/SCYTHE tool definitions; those tools expose `SHEARS_DIG`, which selected the leaf-block branch.
- Updated the shared provider condition and canonical Earth, Sky, Blood, Ichor, and Ender leaf tables, then parsed every table and inspected the packaged JSON.

**Architecture / Module Ownership**
- Relevant source/resources: `common/data/loot/BlockLootTableProvider.java` and `data/modernfoundry/loot_table/blocks/*_slime_leaves.json`.
- Owning module/system: Modern Foundry slime-foliage loot generation and shipped block loot resources.
- Existing logic reused or extracted: vanilla `minecraft:match_tool`, existing Silk Touch/sapling/slimeball logic, and the existing generic shear-ability path for non-leaf foliage.
- Net line change: one shared leaf dispatch condition plus five generated leaf-table condition updates; no new files.
- Build files updated: none.

**Rationale / Tradeoffs**
- This preserves regular vanilla behavior for actual shears and Silk Touch while preventing Tinkers' shear-like harvesting tools from forcing leaf-block drops.

**Build / Validation**
- Production build: `rtk cmd.exe /d /c ".\\gradlew.bat check build --console=plain --no-daemon"` passed in 3m26s; existing Gradle deprecation warnings remain.
- Tests/checks: Gradle `test`, `check`, and `testJunit` (`NO-SOURCE`) completed successfully; all five JSON tables parsed; `git diff --check` passed.
- Archive validation: the exact JAR contains all five updated leaf tables, each with `minecraft:match_tool`/`minecraft:shears` for the leaf-block branch; SHA-256 `0a8e1477ecfb20c8db853fc0e988c2b0b9fcee0fa06f2823b28cbecf169c4d2d`.
- Manual validation: not performed; live tests with a KAMA/Scythe, ordinary tool, Silk Touch, vanilla shears, Fortune, and explosions remain outstanding.
- Tests created or run: no dedicated tests added.

## 2026-08-17 - Fix slime leaf item rendering and Blood loot loading

**Prompt / Task**
- Fix Slime Leaves continuing to drop leaf blocks and appearing invisible in the player's hand and as dropped item entities.

**What Changed**
- Removed the invalid `modernfoundry:blood_slime_ball` entry from the shipped Blood slime-leaf loot table; Blood has no registered slime-ball item.
- Added an opaque alpha channel when block colors are reused for block-item colors, making tinted slime-leaf item quads render instead of becoming fully transparent.

**Steps Taken**
- Read the empty root `TASK.md` and traced the shipped loot tables, block/item models, client color aliases, and the Testing instance log.
- Confirmed the log's Blood loot-table parse error and verified that the leaf item models use tinted `block/leaves` quads.
- Parsed all five slime-leaf loot JSON files, checked the Blood table no longer references the unregistered item, and built the project.

**Architecture / Module Ownership**
- Relevant source/resource: `common/ClientEventBase.java` and `data/modernfoundry/loot_table/blocks/blood_slime_leaves.json`.
- Owning module/system: client block-item color registration and generated slime-foliage loot resources.
- Existing logic reused or extracted: the existing block color handlers, loot-table sapling dispatch, and existing model resources; no new helper or abstraction.
- Net line change: one shared color-handler expression and removal of the stale Blood loot pool; no new files.
- Build files updated: none.

**Rationale / Tradeoffs**
- Minecraft item rendering consumes ARGB colors while block color handlers provide RGB values; adding only the missing opaque alpha fixes the shared alias path without changing foliage colors.
- Removing the invalid Blood item reference lets its already-shipped sapling dispatch load normally; Silk Touch/actual shears behavior remains unchanged.

**Build / Validation**
- Production build: `rtk cmd.exe /d /c ".\\gradlew.bat check build --console=plain --no-daemon"` passed in 3m15s; existing Gradle deprecation warnings remain.
- Tests/checks: Gradle `test`, `check`, and `testJunit` (`NO-SOURCE`) completed successfully; all five leaf JSON files parsed; `git diff --check` passed.
- Archive validation: `build/libs/ModernFoundry-1.21.1-4.1.5-NeoForge.jar` contains the corrected Blood loot table, all five leaf item models, the atlas, and `ClientEventBase.class`.
- Exact artifact SHA-256: `69c91a4a8fa2658f9df9bae15c9204b2636f432748a0d41a9e7b5dbf3c78f3a0`.
- Manual validation: not performed; live harvesting and client visual smoke with bare hands/tools, Silk Touch, vanilla shears, and dropped items remain outstanding.
- Tests created or run: no dedicated tests added.

## 2026-08-17 - Make slime leaf drops sapling-only

**Prompt / Task**
- Stop Slime Leaves from dropping their leaf blocks and return the matching saplings instead.

**What Changed**
- Removed the leaf-block self-drop branch from all five slime-leaf loot tables.
- Earth, Sky, Blood, Ichor, and Ender leaves now use their matching sapling or propagule as the harvest output, while existing slimeball and Fortune behavior remains.

**Steps Taken**
- Verified the Testing instance was using the prior build, then traced the current loot tables and `BlockLootTableProvider`.
- Confirmed the player test world contained a vanilla shears stack, which the prior tables intentionally treated as a leaf-block drop.
- Changed the shared sapling helper and Ender-leaf registration, updated all five canonical generated tables, parsed the resources, and checked the packaged archive for leaf-block outputs.
- Rebuilt and synchronized the exact artifact to the Testing Prism instance.

**Architecture / Module Ownership**
- Relevant source/resources: `common/data/loot/BlockLootTableProvider.java` and `data/modernfoundry/loot_table/blocks/*_slime_leaves.json`.
- Owning module/system: Modern Foundry generated block loot tables for slime foliage.
- Existing logic reused or extracted: the existing sapling/Fortune/slimeball loot helpers and explosion handling; no new helper or abstraction.
- Net line change: one shared loot helper, the Ender registration, and five canonical loot tables; no new files.
- Build files updated: none.

**Rationale / Tradeoffs**
- The leaf item is no longer a possible output for any tool, including vanilla shears or Silk Touch, matching the requested saplings-only behavior.
- Slimeball secondary drops remain gated by the existing shear/Silk Touch exclusion and Fortune chances.

**Build / Validation**
- Production build: `rtk cmd.exe /d /c ".\\gradlew.bat check build --console=plain --no-daemon"` passed in 3m11s; existing Gradle deprecation warnings remain.
- Tests/checks: Gradle `test` and `check` passed; `testJunit` reported `NO-SOURCE`; JSON parsing and `git diff --check` passed.
- Archive validation: all five packaged leaf tables contain matching sapling outputs and no leaf-block output names.
- Exact artifact: `build/libs/ModernFoundry-1.21.1-4.1.5-NeoForge.jar`; SHA-256 `9f49be19f2dcac331053be4298a8991c8955df63e6f5f8d30fe793781fc41daf`.
- Testing instance artifact: synchronized to the same SHA-256.
- Manual validation: not performed; live harvesting in Minecraft remains the final smoke-test gap.
- Tests created or run: no dedicated tests added.

## 2026-08-18 - Restore slime-leaf harvesting dispatch

**Prompt / Task**
- Restore normal slime-leaf behavior: shears must drop the leaf block, while ordinary breaking must sometimes drop the matching sapling, such as a Greenheart sapling from Earth slime leaves.

**What Changed**
- Changed the shared slime-leaf sapling helper to reuse vanilla `createSilkTouchOrShearsDispatchTable`.
- Restored the same shears/Silk Touch dispatch and sapling chance in all five shipped slime-leaf loot tables.
- Kept slimeball and Fortune drops restricted to ordinary harvesting.

**Steps Taken**
- Read the empty root `TASK.md` and traced the current provider, vanilla `BlockLootSubProvider` leaves helper, and all five canonical generated loot tables.
- Confirmed the previous saplings-only helper had removed the self-drop dispatch, which explained both missing shears drops and the broken normal-harvest path.
- Updated the shared helper and canonical generated resources, then rebuilt and synchronized the exact JAR to the Testing Prism instance.

**Architecture / Module Ownership**
- Relevant source/resources: `common/data/loot/BlockLootTableProvider.java` and `data/modernfoundry/loot_table/blocks/*_slime_leaves.json`.
- Owning module/system: Modern Foundry generated slime-foliage block loot.
- Existing logic reused or extracted: vanilla `createSilkTouchOrShearsDispatchTable`, the existing sapling/Fortune helper, and existing slimeball pools.
- Net line change: one shared helper correction plus five generated loot-table dispatch restorations; no new files.
- Build files updated: none.

**Rationale / Tradeoffs**
- The native dispatch is the smallest root-cause fix and keeps the mod aligned with Minecraft/NeoForge leaf behavior instead of duplicating shears and Silk Touch predicates.

**Build / Validation**
- Production build: `rtk cmd.exe /d /c ".\\gradlew.bat check build --console=plain --no-daemon"` passed in 3m02s; existing Gradle deprecation warnings remain.
- Data task: `rtk .\\gradlew.bat runData --console=plain --no-daemon` passed; this checkout has no active data providers, so tracked generated resources were updated directly from the provider contract.
- Tests/checks: Gradle `check` passed; `testJunit` reported `NO-SOURCE`; all five loot JSON files parsed; `git diff --check` passed.
- Archive validation: the exact JAR contains all five updated slime-leaf loot tables.
- Exact artifact and Testing instance SHA-256: `86f1797b007f1fee023249d4b326e7e2f3a89b8584eb32b913d3ff54f4431a85`.
- Manual validation: not performed; live tests with ordinary tools, Fortune, Silk Touch, and shears remain outstanding.
- Tests created or run: no dedicated tests added.

## 2026-08-19 - Integrate native weapon content phases 1 and 2

**Prompt / Task**
- Proceed with implementation of the Modern Foundry native integration plan.
- Complete the Katanas, Battle Spades, and Tinkers' Weaponry content wave in the Modern Foundry namespace.

**What Changed**
- Added native `katana`, `fuma_shuriken`, and `battle_spade` registrations and data.
- Added `great_blade` and `spear_head`, their gold/sand casts, and `greatsword`, `pike`, and `lance` tool definitions.
- Added the `lengthy` modifier with the reference entity-range and durability effects.
- Added station layouts, tool-building and part-casting recipes, item tags, modifier tags, client item properties/colors, models, material texture generation metadata, translations, and player-facing README attribution.
- Removed duplicate generic fallback copies from generated tool texture folders; canonical static textures remain in main resources and generated material variants remain shipped.

**Steps Taken**
- Read `TASK.md`, project instructions, current registrations/providers, and the reference Tinkers' Weaponry generated data and license.
- Extended existing Modern Foundry registries, providers, tags, client hooks, and resource conventions without adding an addon framework or dependency.
- Added the 20 Phase 2 generator texture entries and synchronized the tracked namespace/data outputs because this checkout excludes providers from compilation and has no active GatherData providers.
- Audited JSON syntax, generated/main resource duplicates, external addon namespaces, model texture references, and the built archive.

**Architecture / Module Ownership**
- Relevant class/module change: `TinkerToolParts`, `TinkerSmeltery`, `TinkerTools`, `ToolDefinitions`, `ToolDefinitionDataProvider`, `StationSlotLayoutProvider`, `ToolsRecipeProvider`, `ModifierProvider`, `ToolClientEvents`, and existing item-model/tag providers.
- Owning module/system: Modern Foundry native Tinker tool, part, modifier, recipe, client, and resource systems.
- Existing logic reused or extracted: existing `TinkerModule` registries, tool-definition modules, station layout builders, casting/part recipe helpers, item tag helpers, client property/color registration, and model providers; no new Java classes or parallel framework.
- Net line change: existing Java providers and registries extended; new generated/native data and reference-derived assets added; no build files or optional dependencies changed.
- New files: native Phase 1/2 models, textures, recipes, layouts, tags, tool definitions, and modifier data.
- Build files updated: none.

**Rationale / Tradeoffs**
- Used Modern Foundry's `modernfoundry` namespace and existing serialization/data systems so the content does not require the original mods or Json Things.
- Preserved reference defaults and player-facing names; 1.20.1 Forge assets/data were adapted to the active 1.21.1 NeoForge model and registry conventions.
- Kept optional integrations optional and retained attribution in `README.md` for `References/TinkersWeaponry-1.20.1/LICENSE`.

**Build / Validation**
- Production build: `rtk cmd.exe /d /c ".\\gradlew.bat check build --console=plain --no-daemon"` passed; existing Gradle/NeoForge deprecation warnings remain.
- Tests/checks: Gradle `check`, `test`, and `testJunit` (`NO-SOURCE`) passed; `verifyGeneratedTextures` passed; JSON parsing and `git diff --check` passed; main/generated duplicate audit returned zero.
- Archive validation: `build/libs/ModernFoundry-1.21.1-4.1.6-NeoForge.jar` contains the new tools, parts, casts, modifier, data, client resources, and classes; targeted external addon namespace audit returned zero.
- Exact artifact SHA-256: `43ECAF22D14DA9CE4073CF06BBA6CE974A7205C5C49A23503D9EA8ED21E31E64`.
- Manual validation: not performed; fresh-world construction, live combat/harvest behavior, client visuals, dedicated-server loading, multiplayer sync, and optional-integration smoke remain outstanding.
- Tests created or run: no dedicated tests added.

## 2026-08-20 - Complete native rapier, leveling, and yoyo integration

**Prompt / Task**
- Continue implementation of `TASK.md` through the remaining reference-content phases.
- Complete the Rapier/Estoc behavior, Improvable leveling, yoyo runtime, cross-feature audit, and release-readiness checks.

**What Changed**
- Phase 3: added native `slender_blade`, its casts, `rapier` and `estoc` definitions, station layouts, recipes, tags, models, textures, translations, and `RapierItem` leap/sting behavior.
- Phase 3: routed Rapier sting damage through the server-side tool path and exposed the reference multiplier through `rapierAttackBonus` with bounded config validation.
- Phase 4: added the `improvable` modifier, persistent tool progression through the existing tool-stack data model, configurable level/slot/stat rules, XP hooks for mining, harvesting, shearing, combat, projectiles, armor damage, interactions, movement, and modifier actions, plus level-up packets, tooltip data, existing command-path integration, and sounds.
- Phase 5: added six native yoyo items, recipes, tags, translations, models, textures, the registered yoyo entity, controller behaviors, targeting, movement, collision, attacks, retraction, particles, tracker/retract packets, renderer, and narrow first-/third-person hand rendering hooks.
- Phase 6: audited Modern Foundry namespace ownership, external addon identifiers, generated/main resource duplicates, optional integration boundaries, registry/data paths, and shared network registration.
- Phase 7: inspected the built archive for expected yoyo, rapier, estoc, leveling, and native namespace content; updated player documentation and attribution.

**Steps Taken**
- Read the active `TASK.md`, current Modern Foundry registration/config/network patterns, and the remaining reference behavior before editing.
- Reused existing tool definitions, station layouts, recipe/tag providers, modifier registration, tool-stack serialization, `TinkerNetwork`, entity registration, client bootstrap, and renderer conventions.
- Kept yoyo simulation and damage server-authoritative; retained only the narrow client hand-layer mixin needed to hide a yoyo item while its entity is deployed.
- Fixed yoyo clip destination writes before the final build so entity targeting receives the computed segment intersection.
- Audited the final JAR and confirmed the new rapier, estoc, leveling, and yoyo paths use only the `modernfoundry` namespace; pre-existing legacy book/compatibility paths were kept outside this integration wave.

**Architecture / Module Ownership**
- Relevant class/module change: `RapierItem`, `ImprovableModifier`, `ToolLevellingUtil`, `LevelUpPacket`, yoyo item/entity/controller/behavior/control/packet classes, `YoyoRenderer`, `TinkerTools`, `TinkerNetwork`, `Config`, tool data providers, client hooks, and native resources.
- Owning module/system: Modern Foundry tool actions, modifier progression, native entity runtime, client rendering, networking, configuration, and data/resource providers.
- Existing logic reused or extracted: native modular-tool definitions and stack data, existing modifier hooks, server event paths, existing network channel, entity tracking, item model generation, and client render registration; no addon framework or new dependency.
- Net line change: existing registries/providers/config/network/client paths extended with new runtime classes and native data/assets; no build files or optional integrations changed.
- New files: Rapier/Estoc runtime and generated data/assets, leveling runtime/packet, yoyo runtime/renderer/mixin and six item/resource sets.
- Build files updated: none.

**Rationale / Tradeoffs**
- Kept all IDs, packages, assets, data, packets, and config keys in the `modernfoundry`/`modernmods.modernfoundry` namespace.
- Preserved reference defaults and behavior where the 1.21.1 NeoForge APIs required adaptation; Netherite Yoyo reuses the Diamond texture because the reference has no Netherite texture, as documented in `README.md` and `Credits.txt`.
- Used a narrow client mixin only for deployed-yoyo hand visibility because no existing item-layer event provides equivalent cancellation; common entity logic remains dedicated-server safe.
- No new external dependency was added; JEI and JSON Things remain optional.

**Build / Validation**
- Production build: `rtk cmd.exe /d /c ".\\gradlew.bat clean build --console=plain --no-daemon"` passed in 5m17s; existing Gradle/NeoForge deprecation warnings remain.
- Tests/checks: `verifyGeneratedTextures`, Gradle `check`, and project tests passed; targeted diff inspection for `README.md`, `CHANGELOG.md`, and `TRACELOG.md` is clean; repository-wide `git diff --check` still reports pre-existing trailing whitespace in dirty `Ideas.md:14` and `Ideas.md:23`; no dedicated test sources were added.
- Archive validation: `build/libs/ModernFoundry-1.21.1-4.1.6-NeoForge.jar` contains the expected rapier, estoc, leveling, and yoyo classes/assets/data; targeted new-content audit found no `yoyos`, `jozufozu`, or `jsonthings` paths.
- Exact artifact SHA-256: `c61c2955bfd5f77f637e8294e1e695b72ec322565ae09a6fdadb4ce12445901d`.
- Client smoke: client bootstrap/resource reload completed and `ItemInHandLayerMixin` applied; run was stopped before fresh-world gameplay.
- Dedicated-server smoke: Modern Foundry/Hilt loading reached the server bootstrap, then Hilt 1.13 failed before world load because `DumpLootModifiers` rejected `neoforge:loot_modifiers/global_loot_modifiers.json` as a `Component.translatable` argument.
- Manual validation: fresh-world construction, live combat/harvest, yoyo physics/render interaction, two-player tracking, save/reload, chunk unload/reload, and optional-integration smoke remain outstanding.
- Tests created or run: no dedicated tests added.

## 2026-08-20 - Fix Rapier, Yoyo hand state, and Improvable recipe

**Prompt / Task**
- Fix Rapier backwards leap, Yoyo hand rendering after use/item switching, and Foundry Anvil application of Improvable with Nether Star and Experience Bottles.

**What Changed**
- Kept Rapier leap execution independent of the base modifiable-item `PASS` result, preserving the server-side backward movement and cooldown path.
- Made first- and third-person Yoyo hiding compare the deployed entity stack with the currently held stack; switched weapons now render instead of being hidden by stale tracker state.
- Added native `improvable` modifier and salvage recipe resources under `src/main/resources/data/modernfoundry/recipe/tools/modifiers/`, using one Nether Star, four Experience Bottles, one ability slot, and the `modernfoundry:modifiable` tag.
- Documented the Improvable recipe in `README.md`.

**Steps Taken**
- Read `TASK.md`, traced the existing `ModifiableItem` use path, Yoyo reference mixins, tracker state, active resource roots, and modifier recipe schema.
- Confirmed this checkout excludes data-provider classes from the main source set and has no active GatherData providers, so the runtime recipe is shipped as a canonical main resource rather than left only in an unused provider.
- Parsed both recipe JSON files, compiled the changed Java, ran the full clean build, inspected the built JAR, and ran `git diff --check`.

**Architecture / Module Ownership**
- Relevant class/module change: `RapierItem`, `ItemInHandRendererMixin`, `ItemInHandLayerMixin`, modifier recipe resources, and `README.md`.
- Owning module/system: native Modern Foundry tool actions, Yoyo client hand rendering, and Foundry Anvil modifier recipes.
- Existing logic reused or extracted: the reference leap formula, existing Yoyo tracker/entity stack, existing modifier recipe schema, and the existing `modernfoundry:modifiable` item tag; no new framework or dependency.
- Net line change: targeted hand-state guards and two recipe resources; no build files changed.
- New files: `src/main/resources/data/modernfoundry/recipe/tools/modifiers/ability/improvable.json` and `src/main/resources/data/modernfoundry/recipe/tools/modifiers/salvage/ability/improvable.json`.
- Build files updated: none.

**Rationale / Tradeoffs**
- Stack matching removes stale tracker hiding without changing Yoyo entity ownership or server-side discard behavior.
- Main-resource recipe files match this checkout's shipped-resource ownership and ensure the recipe exists in the runtime JAR; the unused provider addition remains aligned with the same recipe contract.
- The Yoyo entity renderer retains the reference's diamond-block 3D placeholder because no dedicated redistributable custom Yoyo mesh exists; item hand models remain the reference handheld item models.

**Build / Validation**
- Production build: `rtk cmd.exe /d /c ".\\gradlew.bat clean build --console=plain --no-daemon"` passed in 5m40s; existing Gradle/NeoForge deprecation warnings remain.
- Tests/checks: `compileJava`, Gradle `test`, `check`, and `testJunit` (`NO-SOURCE`) passed; both recipe files parsed; `git diff --check` passed with only existing Git line-ending warnings.
- Data task: `rtk cmd.exe /d /c ".\\gradlew.bat runData --console=plain --no-daemon"` passed; configured GatherData reported zero active providers and wrote no tracked generated files.
- Archive validation: `build/libs/ModernFoundry-1.21.1-4.1.6-NeoForge.jar` contains both Improvable recipes, `modernfoundry.mixins.json`, Rapier/Yoyo classes, and Yoyo mixins.
- Exact artifact SHA-256: `d29f7984978edd8f70f73bde947d6d2e8d0ea2dbadd44f6999ef9ad470261071`.
- Manual validation: not performed; fresh-world Anvil application, Rapier movement, Yoyo rendering, weapon switching, dedicated-server, multiplayer, and optional-integration smoke remain outstanding.
- Tests created or run: no dedicated tests added.

## 2026-08-20 - Fix Rapier impulse, Yoyo model/state, and Improvable tooltip

**Prompt / Task**
- Continue `TASK.md` implementation for the reported Rapier leap, Yoyo rendering/hand-state, and Improvable tooltip bugs.

**What Changed**
- Marked Rapier's reference backward velocity as an impulse so server motion synchronization sends it to clients.
- Changed `yoyo_3d.json` from the flat generated-item parent to Minecraft's block model parent so its element geometry renders.
- Changed `YoyoTracker.apply` so zero IDs clear a hand while unresolved nonzero entity IDs preserve the current state.
- Hid the internal `tank_handler` modifier and removed the provider-owned dynamic Improvable JSON override; the native `ImprovableModifier` and translated level/XP tooltip now remain authoritative.
- Removed stale generated `src/generated/resources/data/modernfoundry/tinkering/modifiers/improvable.json` output.

**Steps Taken**
- Read current task/instructions and traced Rapier use, vanilla motion synchronization, Yoyo tracker packets/entity lifecycle, model inheritance, modifier display defaults, and data-provider output.
- Ran data generation/validation, inspected the generated-resource result, built from a clean state, parsed targeted JSON, checked the exact archive contents, and reviewed whitespace diagnostics.

**Architecture / Module Ownership**
- Relevant class/module change: `RapierItem`, `YoyoTracker`, `TinkerModifiers`, `ModifierProvider`, and the Yoyo item model.
- Owning module/system: native tool actions, Yoyo client/entity tracking, modifier registration/data, and tooltip display.
- Existing logic reused or extracted: vanilla `hasImpulse` motion path, existing Yoyo tracker packet/entity lookup, `BasicModifier.TooltipDisplay`, native `NoLevelsModifier`, and existing tooltip translations.
- Net line change: five targeted source/resource fixes plus removal of one stale generated modifier file; no new files.
- Build files updated: none.

**Rationale / Tradeoffs**
- Kept reference leap direction and strength unchanged; only restored vanilla motion propagation.
- Kept Yoyo packet semantics authoritative: zero means clear, unresolved entity means wait rather than erase state.
- Hid only the technical tank handler; real tank modifiers remain visible.

**Build / Validation**
- Data task: `rtk .\\gradlew.bat runData --console=plain --no-daemon` passed; configured GatherData reported no active providers and the stale generated override remained absent.
- Production build: standalone `clean` passed, followed by `rtk .\\gradlew.bat build --console=plain --no-daemon` from that clean state; build passed with existing deprecation warnings.
- Tests/checks: Gradle `test`, `check`, and `testJunit` (`NO-SOURCE`) passed; four targeted JSON files parsed; `git diff --check` reported no whitespace errors, only Git line-ending warnings.
- Archive validation: `ModernFoundry-1.21.1-4.1.6-NeoForge.jar` contains both Improvable recipes, all six Yoyo models, Rapier/Yoyo classes, and translations; no `tinkering/modifiers/improvable.json` override is present.
- Exact artifact SHA-256: `01b72ca72271849e513003ffd2ba469166ddc9dafe1f2aa4919324c950a9e232`.
- Manual validation: not performed; fresh-world Rapier movement, first-/third-person Yoyo use/switching, Anvil application, dedicated-server, multiplayer, and optional-integration smoke remain outstanding.
- Tests created or run: no dedicated tests added.
## 2026-08-21 - Replace Yoyos with the official 1.21.1 reference

**Prompt / Task**
- Replace Modern Foundry's prior Yoyo implementation based on `References/Yoyos-1.20` with the decompiled MIT 1.21.1 reference under `References/Yoyos-1.21-Decompiled`.

**What Changed**
- Replaced the legacy controller/tracker runtime with the official-style Yoyo item, entity, interaction, tier, enchantment, data-component, packet, renderer, config, and hand-rendering paths under `modernmods.modernfoundry`.
- Added the eight core Yoyos, cord, official recipes, enchantments, advancements, enchantable tags, Yoyo tags, sounds, translations, models, and textures using `modernfoundry` IDs.
- Added the reference's optional compatibility tiers and cord items with loaded-mod registration gates, reflection-safe mana and pneumatic behavior, pig-iron behavior, effect interactions, recipes, models, and copied compatibility textures. Modern Foundry's own TConstruct-material tiers register natively.
- Removed the old Yoyo controller, tracker, behavior, target, motion, packet, and obsolete item/resource files.

**Steps Taken**
- Read and inventoried the decompiled 1.21.1 items, runtime classes, compatibility plugins, recipes, models, textures, mixins, and attribution metadata.
- Adapted package names, registry IDs, resource namespaces, network ownership, entity registration, creative-tab registration, and client hooks to Modern Foundry's existing systems.
- Copied the reference textures into `assets/modernfoundry`, parsed the targeted JSON resources, audited the source and archive for stale `yoyos`/external Java namespaces, and reviewed the final diff.

**Architecture / Module Ownership**
- Relevant class/module change: `TinkerTools`, `TinkerModule`, `TinkerNetwork`, `YoyoItem`, `YoyoEntity`, `Interaction`, `YoyoCompat`, Yoyo client/network classes, mixins, and Yoyo resources.
- Owning module/system: Modern Foundry's native item/entity/deferred-register, network, client-render, sound, and resource systems.
- Existing logic reused or extracted: native Modern Foundry registers, `TinkerNetwork`, `ENTITIES`, creative-tab callback, and client renderer/event patterns; no new runtime dependency was added.
- Net line change: legacy Yoyo runtime/resources were removed and replaced by the reference-derived runtime plus core and compatibility resources; an exact aggregate line count was not separately calculated because the work includes binary assets and untracked additions.
- New files: official-style Yoyo API/runtime/client/network classes, compatibility item classes, compatibility recipes/models/textures, enchantments, advancements, and tags.
- Build files updated: none.

**Rationale / Tradeoffs**
- The official 1.21.1 reference is now the Yoyo source of truth; all integrated Java and runtime IDs use the Modern Foundry namespace.
- Optional mods remain optional: compatibility classes use loaded-mod gates and reflection instead of compile-time dependencies. Same-name compatibility tiers follow the first registered source when multiple reference plugins provide the same name.
- The reference's Creative Yoyo advancement still points at its intentionally absent normal recipe, adapted to `modernfoundry:creative`.

**Build / Validation**
- Production build: `./gradlew.bat build --console=plain --no-daemon` passed.
- Tests/checks: `compileJava`, Gradle `test`, `check`, `testJunit` (`NO-SOURCE`), targeted JSON parsing, `verifyGeneratedTextures`, and `git diff --check` passed; existing Gradle/NeoForge deprecation warnings remain.
- Archive validation: `build/libs/ModernFoundry-1.21.1-4.1.6-NeoForge.jar` contains the required Yoyo classes, core and compatibility resource paths, and no stale external Yoyo namespace paths.
- Exact artifact SHA-256: `2cefefa197b575065e3f9fc33df90f67a3a2e0bec88f17713a7143be3b8b7e3a`.
- Manual validation: Prism/fresh-world JEI search, creative-tab display, crafting, Anvil enchantment application, throwing/retraction, attack/collecting/breaking, first-/third-person rendering, save/reload, dedicated-server tracking, multiplayer sync, and optional-mod smoke remain outstanding.
- Tests created or run: no dedicated Yoyo tests added.

## 2026-08-21 - Finish native integration registration and release verification

**Prompt / Task**
- Complete the native 1.21.1 fold-in of `References/TCIntegrations-1.20.1` and `References/Tinkers-Thinking-1.20.1`, translating their Tinkers/Mantle-facing implementation to Modern Foundry/Hilt systems.

**What Changed**
- Removed Thinking's duplicate `sinistral` static modifier registration; the imported crossbow path already uses Modern Foundry's existing `TinkerModifiers.sinistral` implementation, which is semantically identical.
- Renamed the Thinking creative-tab display strings to Modern Foundry names and documented the native integration content and MIT/sound attributions in `README.md`.

**Steps Taken**
- Compared both `SinistralModifier` implementations and traced every runtime/resource reference before editing.
- Compiled the port, launched the dedicated server, audited the exact JAR for native integration/tool/resource paths, checked the new integration source/resources for stale external namespace identifiers, and reviewed whitespace diagnostics.

**Architecture / Module Ownership**
- Relevant class/resource changes: `thinking/common/register/ModModifiers.java`, `assets/modernfoundry/lang/en_us.json`, `README.md`, and the required project logs.
- Owning module/system: Modern Foundry's native modifier registration, translated Thinking registrations, existing Hilt-backed tool/data systems, and optional integration boundaries.
- Existing logic reused or extracted: `TinkerModifiers.sinistral`; no new registry, compatibility layer, dependency, or parallel modifier implementation was added.
- Net line change: one duplicate registration removed, two player-facing labels translated, and documentation/log entries appended.
- New files: none in this final fix.
- Build files updated: none in this final fix.

**Rationale / Tradeoffs**
- Keeping one canonical `modernfoundry:sinistral` ID preserves existing recipes, tags, tool serialization, and crossbow behavior without an ID rename or duplicate static state.
- Reference attribution remains in the player-facing README and copied sound credit file; original source trees remain read-only.

**Build / Validation**
- Production build: `rtk .\\gradlew.bat clean build --console=plain --no-daemon` passed; existing Java/NeoForge deprecation warnings remain.
- Tests/checks: Gradle `test`, `check`, and `testJunit` (`NO-SOURCE`) passed; `git diff --check` passed.
- Dedicated-server smoke: `runServer` reached `Done (5.008s)` after the duplicate registration fix. The dev server still logs unrelated existing data warnings/errors for optional-material yoyo tag entries, the Copshowium recipe, the Quartz Staff layout, and legacy tag conventions; it did not block readiness.
- Archive validation: `ModernFoundry-1.21.1-4.1.6-NeoForge.jar` contains native `integrations` and `thinking` classes plus the translated tool definitions, layouts, recipes, and models. Final SHA-256: `842ee265194b202585bcc1cd61f5d1967b5973e9681dc1fc7d9305c01a35cd48`.
- Manual validation: fresh-world feature parity, client visuals, optional-mod combinations, multiplayer synchronization, save/reload, and gameplay smoke remain outstanding.
- Tests created or run: no dedicated integration tests added.

## 2026-08-21 - Fix Yoyo tooltips and entity damage

**Prompt / Task**
- Fix raw Yoyo tooltip translations and restore Yoyo entity damage reported during Prism testing.

**What Changed**
- Removed a stray leading `+` from `assets/modernfoundry/lang/en_us.json`; the invalid JSON prevented all Yoyo names and tooltip translations from loading.
- Added compatibility aliases for the earlier `enchantment.modernfoundry.yoyo.*` translation keys shown in the affected tooltip.
- Copied each `YoyoTier` entity/block interaction list into `YoyoItem` during construction and removed the compatibility path's duplicate copy, restoring core Yoyo attacks without double-applying compatibility interactions.

**Steps Taken**
- Read the task contract and inspected the supplied screenshot as evidence.
- Traced tooltip keys through the language resource and Yoyo enchantment data.
- Traced the server collision path through `YoyoEntity`, `YoyoItem`, `YoyoCompat`, and `Interaction.attackEntity`.
- Compared the interaction registration path with the decompiled 1.21.1 reference.

**Architecture / Module Ownership**
- Relevant class/module change: `YoyoItem`, `YoyoCompat`, and the Modern Foundry English language resource.
- Owning module/system: native Yoyo item/tier registration and client resource localization.
- Existing logic reused or extracted: the existing `YoyoTier` interaction lists and reference `Interaction` handlers.
- Net line change: small targeted source/resource fix, plus documentation.
- New files: none.
- Build files updated: none.

**Rationale / Tradeoffs**
- The constructor is the single ownership point for tier interactions, so core and optional tiers share one path and compatibility interactions are not duplicated.
- The current enchantment data keys remain unchanged; aliases preserve compatibility with the key form visible in the user's installed resource state.

**Build / Validation**
- Production build: `./gradlew.bat build --console=plain --no-daemon` passed.
- Compile/checks: `compileJava`, `test`, `check`, `testJunit` (`NO-SOURCE`), `verifyGeneratedTextures`, and JSON parsing passed; existing Gradle/NeoForge deprecation warnings remain.
- Manual validation: Prism gameplay retest remains required after installing the rebuilt JAR; no manual in-game test was performed here.
- Tests created or run: no dedicated Yoyo tests added.
