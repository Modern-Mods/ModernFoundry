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
