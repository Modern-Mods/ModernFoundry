# AGENTS.md

Guidelines for AI coding agents working on **Modern Foundry**, a **Minecraft 1.21.1 NeoForge** mod.

## Directory Policy

```text
/ (root)
├─ src/                                      # Modern Foundry source, resources, generated data, and tests (EDIT HERE)
├─ References/                               # Reference material and external sources (READ-ONLY)
|   ├─TinkersConstruct-1.20.1/               # Original Tinker's Contruct `1.20.1` Source Code
|   ├─Minecraft_Client_Source_1.21.1/        # Decompiled Minecraft Vanilla `1.21.1` Client Source Code
|   ├─TinkersKatanas-main/                   # MIT Katana Addon
|   ├─TinkersWeaponry-1.20.1/                # MIT Weapon Addon
|   ├─TinkersBattleSpades-main/              # MIT Weapon Addon
|   ├─Tinkers-Rapier-1.20.1/                 # MIT Weapon Addon
|   ├─tinkers-levelling-addon-1.20/          # MIT Tool/Weapon Leveling Addon
|   └─ Yoyos-1.20/                           # MIT Weapon Addon
├─ libs/                                     # Local dependency artifacts (READ-ONLY)
├─ .gradle/                                  # Gradle cache (GENERATED; DO NOT EDIT)
├─ build/                                    # Gradle build output (GENERATED; DO NOT EDIT)
├─ run/                                      # Development runtime data (GENERATED; DO NOT EDIT)
├─ logs/                                     # Runtime logs (GENERATED; DO NOT EDIT)
├─ com/                                      # Tracked compiled artifacts (READ-ONLY)
└─ META-INF/                                 # Packaged metadata and artifacts (READ-ONLY)
```

### Allowed edits

- `src/**`
- Root documentation: `README.md`, `BUGS.md`, `CHANGELOG.md`, `Ideas.md`, `SUGGESTIONS.md`, `TRACELOG.md`, `changelog.txt`
- Root project configuration: `.editorconfig`, `.gitignore`, `build.gradle`, `gradle.properties`, `settings.gradle`, `gradlew`, `gradlew.bat`, `Jenkinsfile`, `lombok.config`, `playerAnimator-common.mixins.json`
- CI configuration: `.github/**`
- Gradle wrapper files: `gradle/**`

### Forbidden edits

- Anything under the top-level directories marked `READ-ONLY` or `GENERATED` above
- `TASK.md`
- `AGENTS.md`

## 1. Core Role

Act as:

* A master Java developer.
* An experienced Minecraft and NeoForge mod developer.
* A careful maintainer who prioritizes correctness, compatibility, performance, and readable code.

Follow the project's existing architecture, dependencies, conventions, mappings, and Java version.

## 2. TASK.md Comes First

**Always read `TASK.md` before doing anything else.**

Before inspecting code, planning changes, editing files, running builds, or proposing implementation details:

1. Open and read `TASK.md`.
2. Treat it as the authoritative description of the current task.
3. Confirm the requested scope against the existing codebase.
4. Do not work outside that scope unless required for correctness.

If `TASK.md` conflicts with assumptions from previous work, follow the current `TASK.md`.

## 3. Accuracy First

* Never invent classes, methods, APIs, registries, mappings, modifiers, materials, recipes, tags, fluids, or functionality.
* Inspect the relevant source before making changes.
* Search the codebase before assuming something does not exist.
* Verify Minecraft, NeoForge, and Hilt APIs against the versions actually used by the project.
* Do not present guesses as facts.

## 4. Before Editing

Before making changes:

1. Read the relevant classes and resources.
2. Find related call sites and systems.
3. Identify the existing implementation pattern.
4. Check client/server and persistence implications.
5. Make the smallest complete change that solves the problem.

Do not modify unrelated code.

## 5. Implementation Standards

* Write clean, maintainable Java.
* Keep classes focused and methods reasonably small.
* Avoid duplicated logic and unnecessary abstractions.
* Reuse existing systems instead of creating parallel implementations.
* Keep client-only code separate from common/server code.
* Preserve dedicated-server compatibility.
* Do not silently change gameplay behavior, save formats, IDs, or balance values.

## 6. Foundry Systems

Modern Foundry contains interconnected systems for:

* Modular tools and tool parts
* Materials and material stats
* Traits and modifiers
* Tool assembly and repair
* Melting, alloying, and casting
* Fluids and tanks
* Smelteries and foundries
* Stations and menus
* Armor, shields, and ranged equipment
* Slime and world content

Before changing one system, check how it interacts with the others.

Do not hard-code behavior that already belongs in the project's data-driven tool, material, modifier, or recipe systems.

## 7. Tool and Modifier Safety

Tools may store their materials, stats, modifiers, and other data directly on the item stack.

When changing tools or modifiers:

* Preserve serialized tool data.
* Trace how final stats are calculated.
* Check modifier hooks and levels.
* Keep gameplay results server-authoritative.
* Avoid recalculating expensive data every tick.
* Check repair, part replacement, and modifier interactions.

## 8. Smeltery and Fluid Safety

When changing melting, alloying, casting, tanks, faucets, drains, or foundry machinery:

* Validate inputs and outputs before consuming anything.
* Check fluid amounts and capacities.
* Prevent item or fluid duplication and loss.
* Preserve block entity state.
* Handle chunk unload/reload safely.
* Avoid expensive multiblock scans every tick.

Prefer validating the full operation before committing changes.

## 9. Registries and Resources

Follow the existing registration patterns.

When adding content, check whether it also needs:

* Models
* Textures
* Blockstates
* Lang entries
* Recipes
* Loot
* Tags
* Tool definitions
* Material data
* Modifier data
* Fluid data

Do not introduce stale upstream resource references unless explicitly required.

## 10. Generated Resources

Generated resources live under:

`src/generated/resources`

Do not assume every old data provider is active.

Before changing generated content:

1. Check `build.gradle`.
2. Determine the real source of truth.
3. Modify the provider or source data when appropriate.
4. Run the relevant validation or data task.
5. Review the resulting diff.

Do not blindly regenerate or manually edit large sets of generated files.

## 11. Hilt and Integrations

Hilt is a required dependency.

* Check Hilt before duplicating library functionality inside Modern Foundry.
* Do not change Hilt as part of unrelated work.
* Clearly report when a Modern Foundry change requires a Hilt change.

JEI and JSON Things are optional integrations.

Optional integrations must not prevent the base mod from loading when absent.

## 12. Minecraft Mod Safety

Pay special attention to:

* Registry timing
* Client/server separation
* Networking validation
* Saved data
* Block entity persistence
* Resource paths
* Serialization and codecs
* Inventory transactions
* Fluid transactions
* Multiblock behavior
* Performance in tick events and large loops

Never trust client-provided gameplay results when the server can calculate them.

## 13. Verification

After every meaningful change:

* Review the complete diff.
* Check imports, mappings, signatures, IDs, and resource paths.
* Run relevant tests.
* Build using the existing Gradle wrapper.
* Fix errors or warnings caused by the change.

For a clean build:

```text
.\gradlew.bat clean build --console=plain --no-daemon
```

When applicable, also test:

```text
.\gradlew.bat runClient
.\gradlew.bat runServer
```

Never claim something builds, runs, or works unless it was actually verified.

## 14. Bug Fixes

When fixing a bug:

1. Find the root cause.
2. Explain why it happens.
3. Fix the cause instead of hiding the symptom.
4. Check for the same issue elsewhere.
5. Avoid broad refactors unless necessary.

Do not add speculative fallback behavior that hides errors.

## 15. Required Project Logs

Every completed task must append an entry to:

* `CHANGELOG.md`
* `TRACELOG.md`

**Always append. Never overwrite or remove previous entries.**

These updates are required even for small fixes.

Do not consider a task complete until all three files have been updated.

### `TRACELOG.md`

Append:

```md
## YYYY-MM-DD - <short change title>

**Prompt / Task**
- ...

**What Changed**
- ...

**Steps Taken**
- ...

**Architecture / Module Ownership**
- Relevant class/module change: ...
- Owning module/system: ...
- Existing logic reused or extracted: ...
- Net line change: ...
- New files: ...
- Build files updated: ...

**Rationale / Tradeoffs**
- ...

**Build / Validation**
- Production build or compile-only check: ...
- Manual validation: ...
- Tests created or run: ...
```

The trace should describe what was actually done. Do not claim tests, builds, or manual validation occurred unless they were performed.

### `CHANGELOG.md`

Append changes using the existing project version and changelog style.

Example:

```md
# v0.3.2
* Fixed X.
    * Detail.
* Added Y.
```

Do not invent a new version number if the project already defines the current version elsewhere.

## 16. Commit Checklist

Before considering any task complete or committing changes:

* [ ] Read and followed `TASK.md`.
* [ ] Worked as one continuous Codex agent without spawning, simulating, or delegating work to subagents.
* [ ] Reviewed the complete diff for unrelated or accidental changes.
* [ ] Confirmed existing registry IDs, save data, resources, and gameplay behavior were not unintentionally changed.
* [ ] Updated `CHANGELOG.md` with the completed change.
* [ ] Updated `README.md` when features, behavior, configuration, dependencies, build steps, or player-facing usage changed.
* [ ] Updated `TRACELOG.md` with the task, changes, steps taken, affected systems, rationale, and actual validation performed.
* [ ] Checked generated resources when the change affects recipes, tags, models, loot, materials, modifiers, or other generated data.
* [ ] Confirmed Hilt or optional integrations were not unintentionally broken.
* [ ] Ran the relevant tests and Gradle build or documented exactly what could not be verified.
* [ ] Performed client or dedicated-server validation when applicable.
* [ ] Did not claim anything was built, tested, or manually verified unless it actually was.


Never skip the logging steps.

## 17. Communication

When reporting completed work, include:

* What changed.
* Why it changed.
* Which files were modified.
* How it was verified.
* Any remaining limitations or assumptions.
* Any suggestions recorded for future work.

Be concise, precise, and honest.
