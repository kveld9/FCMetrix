# HARNESS ENGINEERING GUIDE — FCMETRIX (Android / Kotlin)

> Operational guide and governance system for AI agents operating on the **FCMetrix** repository.

---

## 1. IDENTITY AND OBSERVED STACK

- **Product**: FCMetrix — OVR / GRL (Global Rating Level) calculator and optimizer for FC Mobile.
- **Language**: Kotlin UNKNOWN (JVM Target 11 / JVM 24-25 compatible).
- **Platform / Runtime**: Android SDK (`minSdk 24`, `targetSdk 35`, `compileSdk 37`).
- **UI Framework**: Jetpack Compose (Material 3), Compose BOM `UNKNOWN`.
- **Persistence**: Local SQLite via Room Database `UNKNOWN` with KSP (`LineupDatabase`, `LineupDao`, `TeamEntity`).
- **Preferences**: DataStore Preferences `UNKNOWN` (`ThemePreferences`).
- **Serialization**: Kotlinx Serialization JSON `UNKNOWN` (`LineupConverters`, `JsonBackupManager`).
- **Concurrency**: Kotlin Coroutines & Flow (`StateFlow`, `Dispatchers.IO`).
- **Architecture**: Unidirectional Reactive MVVM (UDF) structured into clean layers:
  - `domain`: Pure calculation logic (`GrlCalculator.kt`) without Android framework dependencies.
  - `data`: Repositories, backups, and local persistence (`LineupRepository.kt`, `data/backup/`, `local/`, `ThemePreferences.kt`).
  - `ui`: Jetpack Compose components, screens, theme, and `GrlViewModel.kt`.
- **Build System**: Gradle (AGP `UNKNOWN`, Kotlin DSL: `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`).
- **Performance**: AndroidX Baseline Profiles (`:baselineprofile`).

## 2. MODES OF OPERATION (MODE)

At the beginning of **each session**, the ROOT agent determines and sets the operation mode. The mode cannot be switched within the same session.

```text
MODE = INSTALL/VALIDATE | OPERATE
```

### `MODE = INSTALL/VALIDATE`
- Activated if any harness component is missing (`AGENTS.md`, `.agents/agents/engineer.md`, `.agents/agents/auditor.md`, `.agents/skills/`) or if an inconsistency in the rules is detected.
- **Actions**: Install, repair, or audit the harness.
- **Prohibition**: DO NOT execute functional product requirements during this session, nor modify business logic or database schema.

### `MODE = OPERATE`
- Activated if the harness is complete, integral, and verified.
- **Actions**: Validate baseline, establish Scope Lock, execute minimal changes, and verify deterministically and inferentially.
- **Prohibition**: DO NOT reinstall the harness or alter workflow rules.

---

## 3. HARNESS AUTHORITY CONTROL AND PROTECTION

The following files are protected against automatic modification during functional tasks:
- `AGENTS.md`
- `.agents/agents/engineer.md`
- `.agents/agents/auditor.md`
- `.agents/skills/ui-design-engineering.md`

If a functional task requires a change in harness rules:
```text
STOP → REPORT HARNESS CHANGE REQUIRED → HUMAN / ROOT DECISION
```

---

## 4. SENSORS AND VERIFICATION (QUALITY-LEFT)

### Available Deterministic Sensors

| Sensor | Windows / PowerShell Command | Scope / Purpose |
| :--- | :--- | :--- |
| **Unit Tests** | `.\gradlew.bat testDebugUnitTest` | Verifies domain logic (`GrlCalculator`), ViewModel (`GrlViewModel`), converters, data persistence, and backups. Fast and deterministic. |
| **Assemble Debug** | `.\gradlew.bat assembleDebug` | Compiles debug APK, validates syntax, KSP, Compose compiler, and resource packaging. |
| **Android Lint** | `.\gradlew.bat lintDebug` | Static analysis for Android code and resources. |
| **Git Status** | `git status` | Read-only check of working tree status and delta isolation. |

### Preferred Verification Order (Fast Feedback Loop)
```text
LOCAL / FAST (Domain / Data specific unit tests)
  ↓
MODULE (.\gradlew.bat testDebugUnitTest)
  ↓
BUILD INTEGRITY (.\gradlew.bat assembleDebug)
  ↓
INFERENTIAL AUDIT (if Risk Gate requires it)
```

---

## 5. DEVELOPMENT WORKFLOW (OPERATE MODE)

```text
1. RECONNAISSANCE & BASELINE (git status + testDebugUnitTest)
2. SCOPE LOCK (Define affected files, symbols, and invariants)
3. IMPLEMENTATION (Engineer: minimal changes, surgical patching discipline)
4. LOCAL DETERMINISTIC VERIFICATION (testDebugUnitTest / assembleDebug)
5. BLAST RADIUS EVALUATION & RISK GATE
6. INFERENTIAL AUDIT (Auditor: required if Risk Gate demands it)
7. FINAL SCOPE & .gitignore AUDIT (Diff ⊆ Initial Scope + Verify clean git status and .gitignore optimization)
```

### Failure Classification
When a sensor fails, classify the cause with evidence:
- `PREEXISTING`: Defect already present before the task.
- `ENVIRONMENT`: JDK, memory, or system tooling issue.
- `HARNESS`: Error in harness configuration or script.
- `INTRODUCED_BY_CHANGE`: Defect introduced by the current modification (requires immediate fix).
- `VERIFICATION_GAP`: Lack of test coverage for the evaluated scenario.

---

## 6. SCOPE LOCK AND EXPANSION CONTROL

Before modifying code, ROOT logs:
```text
INITIAL SCOPE:
- Affected files / modules
- Affected symbols / contracts
- Behavior that must change
- Behavior that must NOT change
- Compatibility constraints (e.g. minSdk 24, targetSdk 35)
```

### Expansion Rule
The Engineer **cannot unilaterally expand the scope**. If external dependencies are discovered:
```text
STOP → REPORT DEPENDENCY AND ROOT CAUSE → AWAIT ROOT DECISION (APPROVE / REJECT)
```

Strict rule:
$$\text{FINAL DIFF} \subseteq \text{INITIAL SCOPE} + \text{APPROVED EXPANSIONS}$$

---

## 7. RISK GATE & INFERENTIAL AUDITING

The adversarial Auditor (`.agents/agents/auditor.md`) is invoked unconditionally if changes touch:
- OVR calculation algorithms or rounding rules (`GrlCalculator.kt`).
- Room Database schema, migrations, DAOs, or converters (`LineupDatabase.kt`, `LineupDao.kt`, `TeamEntity.kt`, `LineupConverters.kt`).
- Asynchronous persistence, Coroutines, or `Flow` / `StateFlow` handling.
- User preferences in DataStore (`ThemePreferences.kt`).
- Backup and JSON portability (`JsonBackupManager.kt`).
- File sharing or export with `FileProvider` (`ShareProvider.kt`).
- Android API compatibility (`minSdk 24` to `targetSdk 35`).
- Cross-cutting blast radius across `domain`, `data`, and `ui`.

For cosmetic changes, documentation, copy, or isolated unit tests: light checklist without formal auditor.

---

## 8. PATCH DISCIPLINE AND SAFE ROLLBACK

- **Minimal Patches**: Use targeted replacements of contiguous blocks with context. Do not regenerate entire files unnecessarily.
- **Preservation**: Strictly forbidden: `git reset --hard`, `git clean -fd`, or `git checkout -- <file>`.
- **Diff Classification**:
  - `SEMANTIC CHANGE`: Functional logic or UI change.
  - `FORMATTER CHANGE`: Re-indentation or spacing.
  - `TOOLING-INDUCED CHANGE`: Generated by KSP, Gradle, or Compose compiler.
- **Anti-Loop**: If a fix fails twice consecutively, stop, re-evaluate root cause, and change strategy.

---

## 9. DRIFT DETECTION

If the rules described here contradict the real repository state (e.g. versions in `libs.versions.toml`, domain signatures, Room contracts):
```text
STOP → REPORT DRIFT → RESOLVE DISCREPANCY WITH REAL EVIDENCE
```

---

## 10. ZERO DEAD CODE & HYGIENE POLICY

Any agent making changes or refactoring must ensure complete absence of orphaned code and resources:
- **Kotlin / Compose**: Forbidden to leave unused imports, obsolete constants, unreferenced functions, or dead parameters in public/private signatures.
- **XML Resources (`res/`)**: Forbidden to leave unused strings (`strings.xml`), colors (`colors.xml`), drawables, or layouts.
- **Hygiene Sensor**: Periodically verify resources and warnings via compiler and Android Lint (`.\gradlew.bat lintDebug` / `assembleDebug`).

---

## 11. LANGUAGE AND DOCUMENTATION STANDARD

All harness governance specifications, agent definition documents, skill guides, architecture files, code docstrings, and all future additions must strictly be authored and maintained in **English**:
- **Harness Scope**: `AGENTS.md`, `.agents/agents/*.md`, `.agents/skills/*.md`, and any newly created agentic workflows or subagent configurations.
- **Code & Comments**: All Kotlin classes, interfaces, comments, and commit messages.
- **Requirement for Future Agents**: Any AI agent operating on this codebase is prohibited from adding harness documentation, rules, or agent files in any language other than English.

---

## 12. LOW-END DEVICE OPTIMIZATION & ANTI-OBSOLESCENCE POLICY

To actively combat planned obsolescence and ensure universal accessibility for users on budget or older hardware (e.g., 1 GB RAM, low-end multi-core CPUs), the app must remain lean, responsive, and lightweight:

- **Strict Compatibility Baseline**: Preserve `minSdk 24` (Android 7.0 Nougat) backward compatibility. Never raise `minSdk` without explicit human approval.
- **Memory Ceiling & Footprint**:
  - Runtime heap memory must remain strictly contained ($\le 50\,\text{MB}$).
  - Do not allocate short-lived objects, heavy lists, or lambdas inside hot recomposition loops.
  - High-resolution bitmap captures must be generated strictly on-demand and promptly dereferenced.
- **CPU & Battery Preservation**:
  - 0 background daemons, 0 WakeLocks, and 0 unnecessary alarm timers.
  - UI state collection must use `collectAsStateWithLifecycle()` to pause rendering when the app is paused or in background.
  - List items must use stable keys (`key(player.id)`) to minimize recomposition overhead on slow CPUs.
- **Disk & Storage Discipline**:
  - Keep release APK size minimal ($\le 5\,\text{MB}$) via R8 minification, ProGuard shrinking, and Baseline Profiles.
  - All database queries and JSON stream parsing must run off the UI thread via `Dispatchers.IO`.
  - Zero telemetry, analytics SDKs, or background tracking services.

---

## 13. REPOSITORY HYGIENE & `.gitignore` INTEGRITY POLICY

To prevent repository bloat, accidental secret leaks, and unwanted build or environment pollution, `.gitignore` must be maintained strictly optimized:

- **Build Artifacts & Generated Files**:
  - Exclude all Gradle, KSP, and compiler caches (`.gradle/`, `build/`, `*/build/`, `.kotlin/`, `ksp/`, `.cxx/`, `.externalNativeBuild/`).
  - Exclude compilation binaries, packages, and outputs (`*.apk`, `*.aab`, `app/release/`).
- **IDE, System & Tooling Metadata**:
  - Exclude IDE project files and workspace caches (`.idea/`, `*.iml`, `.vscode/`).
  - Exclude local developer environment files (`local.properties`).
  - Exclude operating system metadata (`.DS_Store`, `Thumbs.db`, `desktop.ini`).
- **Security & Signing Materials**:
  - Strictly prohibit committing keystores and private keys (`*.jks`, `*.keystore`, `*.key`, `*.pem`).
- **Diagnostics, Memory Dumps & Temporary Logs**:
  - Exclude profiling artifacts, heap dumps, and execution logs (`*.hprof`, `*.log`, `captures/`).
- **Post-Change Mandatory Verification**:
  - After completing every feature implementation, refactoring, or file deletion, the agent must run `git status` to inspect the working tree.
  - If any untracked build outputs, temporary caches, or tooling artifacts are detected, the agent must immediately update and optimize `.gitignore`.
- **Zero Drift**: The `.gitignore` file must remain cleanly categorized, sorted, and free of redundant or obsolete entries.

---

## 14. MANDATORY UNIVERSAL UNIT TESTING POLICY (QUALITY-LEFT)

To ensure long-term stability, deterministic quality, and zero regression risk across all modules, unit test coverage is strictly mandatory for the entire codebase:

- **Universal Test Obligation**:
  - Every new feature, calculation rule, repository method, ViewModel interaction, serialization converter, DataStore preference, or utility MUST have dedicated unit tests in `app/src/test/`.
  - When refactoring existing code, agents must inspect coverage and proactively fill any `VERIFICATION_GAP` by authoring comprehensive unit tests.
- **Mandatory Execution Post-Implementation**:
  - Immediately following any code modification, the agent MUST run `.\gradlew.bat testDebugUnitTest`.
  - No task or implementation is considered complete unless 100% of unit tests pass with zero failures.
- **Coverage Standards & Edge Cases**:
  - Tests must cover the standard happy path, boundary values (min/max limits), negative/invalid inputs, empty states, and error handling branches (`Result.failure`, exceptions, duplicate policies).
  - Domain invariants (`GrlCalculator`), persistence strategies (`LineupRepository`), and state reducers (`GrlViewModel`) must be tested deterministically without reliance on Android emulator/device dependencies.
