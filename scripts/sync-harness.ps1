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
    $regex = '(?m)^\s*' + [regex]::Escape($Key) + '\s*=\s*["'']([^"'']+)["'']'
    if ($TomlContent -match $regex) {
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
$Template = @'
## 1. IDENTITY AND OBSERVED STACK

- **Product**: FCMetrix — OVR / GRL (Global Rating Level) calculator and optimizer for FC Mobile.
- **Language**: Kotlin __KOTLIN__ (JVM Target __JVM__ / JVM 24-25 compatible).
- **Platform / Runtime**: Android SDK (`minSdk __MINSDK__`, `targetSdk __TARGETSDK__`, `compileSdk __COMPILESDK__`).
- **UI Framework**: Jetpack Compose (Material 3), Compose BOM `__COMPOSEBOM__`.
- **Persistence**: Local SQLite via Room Database `__ROOM__` with KSP (`LineupDatabase`, `LineupDao`, `TeamEntity`).
- **Preferences**: DataStore Preferences `__DATASTORE__` (`ThemePreferences`).
- **Serialization**: Kotlinx Serialization JSON `__SERIALIZATION__` (`LineupConverters`, `JsonBackupManager`).
- **Concurrency**: Kotlin Coroutines & Flow (`StateFlow`, `Dispatchers.IO`).
- **Architecture**: Unidirectional Reactive MVVM (UDF) structured into clean layers:
  - `domain`: Pure calculation logic (`GrlCalculator.kt`) without Android framework dependencies.
  - `data`: Repositories, backups, and local persistence (`LineupRepository.kt`, `data/backup/`, `local/`, `ThemePreferences.kt`).
  - `ui`: Jetpack Compose components, screens, theme, and `GrlViewModel.kt`.
- **Build System**: Gradle (AGP `__AGP__`, Kotlin DSL: `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`).
- **Performance**: AndroidX Baseline Profiles (`:baselineprofile`).
'@

$NewSection1 = $Template
$NewSection1 = $NewSection1.Replace("__KOTLIN__", $KotlinVer)
$NewSection1 = $NewSection1.Replace("__JVM__", $JvmTarget)
$NewSection1 = $NewSection1.Replace("__MINSDK__", $MinSdk)
$NewSection1 = $NewSection1.Replace("__TARGETSDK__", $TargetSdk)
$NewSection1 = $NewSection1.Replace("__COMPILESDK__", $CompileSdk)
$NewSection1 = $NewSection1.Replace("__COMPOSEBOM__", $ComposeBomVer)
$NewSection1 = $NewSection1.Replace("__ROOM__", $RoomVer)
$NewSection1 = $NewSection1.Replace("__DATASTORE__", $DataStoreVer)
$NewSection1 = $NewSection1.Replace("__SERIALIZATION__", $SerializationVer)
$NewSection1 = $NewSection1.Replace("__AGP__", $AgpVer)

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
