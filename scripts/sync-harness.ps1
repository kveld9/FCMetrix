# SYNC-HARNESS.PS1
# Automatically synchronizes AGENTS.md stack specifications with real build files.
# Prevents documentation and governance drift.

$ErrorActionPreference = "Stop"

$ProjectRoot = Resolve-Path "$PSScriptRoot\.."
$AgentsMdPath = Join-Path $ProjectRoot "AGENTS.md"
$TomlPath = Join-Path $ProjectRoot "gradle\libs.versions.toml"
$AppBuildGradlePath = Join-Path $ProjectRoot "app\build.gradle.kts"

if (-not (Test-Path $AgentsMdPath)) {
    Write-Error "AGENTS.md not found at $AgentsMdPath"
}
if (-not (Test-Path $TomlPath)) {
    Write-Error "libs.versions.toml not found at $TomlPath"
}

Write-Host "Scanning project dependencies and configuration..." -ForegroundColor Cyan

# 1. Parse libs.versions.toml
$TomlContent = Get-Content $TomlPath -Raw
function Get-TomlVersion($Key) {
    if ($TomlContent -match "(?m)^\s*$Key\s*=\s*[`"']([^`"']+)`"'") {
        return $matches[1]
    }
    return "UNKNOWN"
}

$KotlinVer = Get-TomlVersion "kotlin"
$ComposeBomVer = Get-TomlVersion "composeBom"
$RoomVer = Get-TomlVersion "room"
$DataStoreVer = Get-TomlVersion "datastore"
$SerializationVer = Get-TomlVersion "serialization"
$AgpVer = Get-TomlVersion "agp"

# 2. Parse app/build.gradle.kts
$BuildGradleContent = Get-Content $AppBuildGradlePath -Raw
function Get-GradleProperty($Pattern) {
    if ($BuildGradleContent -match $Pattern) {
        return $matches[1]
    }
    return "UNKNOWN"
}

$MinSdk = Get-GradleProperty "minSdk\s*=\s*(\d+)"
$TargetSdk = Get-GradleProperty "targetSdk\s*=\s*(\d+)"
$CompileSdk = Get-GradleProperty "compileSdk\s*=\s*(\d+)"
$JvmTarget = Get-GradleProperty "jvmTarget\.set\(JvmTarget\.JVM_(\d+)\)"
if ($JvmTarget -eq "UNKNOWN") {
    $JvmTarget = Get-GradleProperty "sourceCompatibility\s*=\s*JavaVersion\.VERSION_(\d+)"
}

# 3. Construct updated Section 1
$NewSection1 = @"
## 1. IDENTITY AND OBSERVED STACK

- **Product**: FCMetrix — OVR / GRL (Global Rating Level) calculator and optimizer for FC Mobile.
- **Language**: Kotlin $KotlinVer (JVM Target $JvmTarget / JVM 24-25 compatible).
- **Platform / Runtime**: Android SDK (``minSdk $MinSdk``, ``targetSdk $TargetSdk``, ``compileSdk $CompileSdk``).
- **UI Framework**: Jetpack Compose (Material 3), Compose BOM ``$ComposeBomVer``.
- **Persistence**: Local SQLite via Room Database ``$RoomVer`` with KSP (``LineupDatabase``, ``LineupDao``, ``TeamEntity``).
- **Preferences**: DataStore Preferences ``$DataStoreVer`` (``ThemePreferences``).
- **Serialization**: Kotlinx Serialization JSON ``$SerializationVer`` (``LineupConverters``, ``JsonBackupManager``).
- **Concurrency**: Kotlin Coroutines & Flow (``StateFlow``, ``Dispatchers.IO``).
- **Architecture**: Unidirectional Reactive MVVM (UDF) structured into clean layers:
  - ``domain``: Pure calculation logic (``GrlCalculator.kt``) without Android framework dependencies.
  - ``data``: Repositories, backups, and local persistence (``LineupRepository.kt``, ``data/backup/``, ``local/``, ``ThemePreferences.kt``).
  - ``ui``: Jetpack Compose components, screens, theme, and ``GrlViewModel.kt``.
- **Build System**: Gradle (AGP ``$AgpVer``, Kotlin DSL: ``build.gradle.kts``, ``app/build.gradle.kts``, ``gradle/libs.versions.toml``).
- **Performance**: AndroidX Baseline Profiles (``:baselineprofile``).
"@

# 4. Patch AGENTS.md
$AgentsContent = Get-Content $AgentsMdPath -Raw
$PatternSection1 = "(?s)## 1\. IDENTITY AND OBSERVED STACK.*?(?=## 2\. MODES OF OPERATION)"

if ($AgentsContent -match $PatternSection1) {
    $UpdatedAgentsContent = $AgentsContent -replace $PatternSection1, ($NewSection1 + "`r`n`r`n")
    Set-Content -Path $AgentsMdPath -Value $UpdatedAgentsContent -NoNewline
    Write-Host "SUCCESS: AGENTS.md Section 1 successfully synchronized with codebase!" -ForegroundColor Green
} else {
    Write-Error "Could not locate Section 1 in AGENTS.md to perform replacement."
}
