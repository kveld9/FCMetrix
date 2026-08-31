// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.androidx.baselineprofile) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

abstract class SyncHarnessTask : DefaultTask() {
    @get:InputFile
    abstract val tomlFile: RegularFileProperty

    @get:InputFile
    abstract val buildGradleFile: RegularFileProperty

    @get:OutputFile
    abstract val agentsMdFile: RegularFileProperty

    @TaskAction
    fun sync() {
        val toml = tomlFile.get().asFile
        val buildGradle = buildGradleFile.get().asFile
        val agentsMd = agentsMdFile.get().asFile

        if (!toml.exists() || !agentsMd.exists() || !buildGradle.exists()) {
            println("Harness sync skipped: required files not found.")
            return
        }

        val tomlContent = toml.readText()
        fun getTomlVersion(key: String): String {
            val regex = Regex("""(?m)^\s*${Regex.escape(key)}\s*=\s*["']([^"']+)["']""")
            return regex.find(tomlContent)?.groupValues?.get(1) ?: "UNKNOWN"
        }

        val kotlinVer = getTomlVersion("kotlin")
        val composeBomVer = getTomlVersion("composeBom")
        val roomVer = getTomlVersion("room")
        val dataStoreVer = getTomlVersion("datastore")
        val serializationVer = getTomlVersion("serialization")
        val agpVer = getTomlVersion("agp")

        val buildGradleContent = buildGradle.readText()
        fun getGradleProp(pattern: String): String {
            val regex = Regex(pattern)
            return regex.find(buildGradleContent)?.groupValues?.get(1) ?: "UNKNOWN"
        }

        val minSdk = getGradleProp("""minSdk\s*=\s*(\d+)""")
        val targetSdk = getGradleProp("""targetSdk\s*=\s*(\d+)""")
        val compileSdk = getGradleProp("""compileSdk\s*=\s*(\d+)""")
        var jvmTarget = getGradleProp("""jvmTarget\.set\(JvmTarget\.JVM_(\d+)\)""")
        if (jvmTarget == "UNKNOWN") {
            jvmTarget = getGradleProp("""sourceCompatibility\s*=\s*JavaVersion\.VERSION_(\d+)""")
        }

        val newSection1 = """
## 1. IDENTITY AND OBSERVED STACK

- **Product**: FCMetrix — OVR / GRL (Global Rating Level) calculator and optimizer for FC Mobile.
- **Language**: Kotlin $kotlinVer (JVM Target $jvmTarget / JVM 24-25 compatible).
- **Platform / Runtime**: Android SDK (`minSdk $minSdk`, `targetSdk $targetSdk`, `compileSdk $compileSdk`).
- **UI Framework**: Jetpack Compose (Material 3), Compose BOM `$composeBomVer`.
- **Persistence**: Local SQLite via Room Database `$roomVer` with KSP (`LineupDatabase`, `LineupDao`, `TeamEntity`).
- **Preferences**: DataStore Preferences `$dataStoreVer` (`ThemePreferences`).
- **Serialization**: Kotlinx Serialization JSON `$serializationVer` (`LineupConverters`, `JsonBackupManager`).
- **Concurrency**: Kotlin Coroutines & Flow (`StateFlow`, `Dispatchers.IO`).
- **Architecture**: Unidirectional Reactive MVVM (UDF) structured into clean layers:
  - `domain`: Pure calculation logic (`GrlCalculator.kt`) without Android framework dependencies.
  - `data`: Repositories, backups, and local persistence (`LineupRepository.kt`, `data/backup/`, `local/`, `ThemePreferences.kt`).
  - `ui`: Jetpack Compose components, screens, theme, and `GrlViewModel.kt`.
- **Build System**: Gradle (AGP `$agpVer`, Kotlin DSL: `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`).
- **Performance**: AndroidX Baseline Profiles (`:baselineprofile`).
""".trimIndent()

        val agentsContent = agentsMd.readText()
        val pattern = Regex("""(?s)## 1\. IDENTITY AND OBSERVED STACK.*?(?=## 2\. MODES OF OPERATION)""")
        if (pattern.containsMatchIn(agentsContent)) {
            val updated = pattern.replace(agentsContent, newSection1 + "\n\n")
            agentsMd.writeText(updated)
            println("SUCCESS: AGENTS.md Section 1 successfully synchronized with codebase (Cross-Platform Gradle)!")
        } else {
            System.err.println("WARNING: Could not locate Section 1 in AGENTS.md to perform replacement.")
        }
    }
}

tasks.register<SyncHarnessTask>("syncHarness") {
    group = "verification"
    description = "Synchronizes AGENTS.md with current dependencies and configuration."
    tomlFile.set(layout.projectDirectory.file("gradle/libs.versions.toml"))
    buildGradleFile.set(layout.projectDirectory.file("app/build.gradle.kts"))
    agentsMdFile.set(layout.projectDirectory.file("AGENTS.md"))
}

