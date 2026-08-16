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
