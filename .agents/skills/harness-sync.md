# HARNESS AUTO-SYNC SKILL — FCMETRIX

> Operational protocol and tool for synchronizing harness documentation (`AGENTS.md`) with the codebase state to prevent documentation and governance drift.

---

## 1. PURPOSE

Ensures that the repository harness specification (`AGENTS.md`) remains an exact reflection of the real build configuration, dependency catalogs, SDK boundaries, and architecture contracts without requiring manual updates.

---

## 2. TRIGGER CONDITIONS

Execute harness synchronization when:
1. Upgrading or changing dependencies in `gradle/libs.versions.toml`.
2. Changing SDK targets, JVM versions, or build configs in `app/build.gradle.kts`.
3. Adding or refactoring architectural layers in `domain/`, `data/`, or `ui/`.
4. Finalizing any task before submitting changes.

---

## 3. EXECUTION SCRIPT

Run the deterministic synchronization script:
```powershell
powershell -ExecutionPolicy Bypass -File scripts\sync-harness.ps1
```

---

## 4. ARCHITECTURAL DRIFT CHECKLIST

When an architectural pattern changes (e.g. new Room entities, DataStore preferences, or domain contracts):
1. **Verify Invariants**: Ensure clean layer boundaries are maintained (`domain` has zero Android dependencies).
2. **Execute Sync**: Run `scripts\sync-harness.ps1`.
3. **Inspect Diff**: Review `git diff AGENTS.md` to ensure only legitimate stack and contract updates are staged.
