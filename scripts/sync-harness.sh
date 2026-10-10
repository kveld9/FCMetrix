#!/bin/sh
set -e

# Resolve project root as the parent dir of the script location
SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)
PROJECT_ROOT=$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd -P)

AGENTS_MD="$PROJECT_ROOT/AGENTS.md"
TOML_FILE="$PROJECT_ROOT/gradle/libs.versions.toml"
BUILD_GRADLE="$PROJECT_ROOT/app/build.gradle.kts"

# Required inputs: AGENTS.md and gradle/libs.versions.toml relative to root
if [ ! -f "$AGENTS_MD" ]; then
    printf "Error: AGENTS.md not found at %s\n" "$AGENTS_MD" >&2
    exit 1
fi

if [ ! -f "$TOML_FILE" ]; then
    printf "Error: libs.versions.toml not found at %s\n" "$TOML_FILE" >&2
    exit 1
fi

printf "Scanning project dependencies and configuration...\n"

get_toml_version() {
    _key="$1"
    _file="$2"
    _val=$(sed -n -E "s/^[[:space:]]*${_key}[[:space:]]*=[[:space:]]*[\"\']([^\"\']+)[\"\'].*/\\1/p" "$_file" | head -n 1)
    if [ -n "$_val" ]; then
        printf "%s\n" "$_val"
    else
        printf "UNKNOWN\n"
    fi
}

get_gradle_property() {
    _pattern="$1"
    _file="$2"
    if [ ! -f "$_file" ]; then
        printf "UNKNOWN\n"
        return
    fi
    _val=$(sed -n -E "s/.*${_pattern}.*/\\1/p" "$_file" | head -n 1)
    if [ -n "$_val" ]; then
        printf "%s\n" "$_val"
    else
        printf "UNKNOWN\n"
    fi
}

# 1. Parse libs.versions.toml
KOTLIN_VER=$(get_toml_version "kotlin" "$TOML_FILE")
COMPOSE_BOM_VER=$(get_toml_version "composeBom" "$TOML_FILE")
ROOM_VER=$(get_toml_version "room" "$TOML_FILE")
DATASTORE_VER=$(get_toml_version "datastore" "$TOML_FILE")
SERIALIZATION_VER=$(get_toml_version "serialization" "$TOML_FILE")
AGP_VER=$(get_toml_version "agp" "$TOML_FILE")

# 2. Parse app/build.gradle.kts
MIN_SDK=$(get_gradle_property "minSdk[[:space:]]*=[[:space:]]*([0-9]+)" "$BUILD_GRADLE")
TARGET_SDK=$(get_gradle_property "targetSdk[[:space:]]*=[[:space:]]*([0-9]+)" "$BUILD_GRADLE")
COMPILE_SDK=$(get_gradle_property "compileSdk[[:space:]]*=[[:space:]]*([0-9]+)" "$BUILD_GRADLE")
JVM_TARGET=$(get_gradle_property "jvmTarget\\.set\\(JvmTarget\\.JVM_([0-9]+)\\)" "$BUILD_GRADLE")
if [ "$JVM_TARGET" = "UNKNOWN" ]; then
    JVM_TARGET=$(get_gradle_property "sourceCompatibility[[:space:]]*=[[:space:]]*JavaVersion\\.VERSION_([0-9]+)" "$BUILD_GRADLE")
fi

# 3. Verify Section 1 anchor in AGENTS.md
if ! grep -q "^## 1\. IDENTITY AND OBSERVED STACK" "$AGENTS_MD"; then
    printf "Error: Could not locate Section 1 in AGENTS.md to perform replacement.\n" >&2
    exit 1
fi

TMP_DIR=$(mktemp -d)
trap 'rm -rf "$TMP_DIR"' EXIT INT TERM

NEW_SEC_FILE="$TMP_DIR/new_section1.txt"
UPDATED_FILE="$TMP_DIR/agents_updated.md"

cat << EOF > "$NEW_SEC_FILE"
## 1. IDENTITY AND OBSERVED STACK

- **Product**: FCMetrix — OVR / GRL (Global Rating Level) calculator and optimizer for FC Mobile.
- **Language**: Kotlin ${KOTLIN_VER} (JVM Target ${JVM_TARGET} / JVM 24-25 compatible).
- **Platform / Runtime**: Android SDK (\`minSdk ${MIN_SDK}\`, \`targetSdk ${TARGET_SDK}\`, \`compileSdk ${COMPILE_SDK}\`).
- **UI Framework**: Jetpack Compose (Material 3), Compose BOM \`${COMPOSE_BOM_VER}\`.
- **Persistence**: Local SQLite via Room Database \`${ROOM_VER}\` with KSP (\`LineupDatabase\`, \`LineupDao\`, \`TeamEntity\`).
- **Preferences**: DataStore Preferences \`${DATASTORE_VER}\` (\`ThemePreferences\`).
- **Serialization**: Kotlinx Serialization JSON \`${SERIALIZATION_VER}\` (\`LineupConverters\`, \`JsonBackupManager\`).
- **Concurrency**: Kotlin Coroutines & Flow (\`StateFlow\`, \`Dispatchers.IO\`).
- **Architecture**: Unidirectional Reactive MVVM (UDF) structured into clean layers:
  - \`domain\`: Pure calculation logic (\`GrlCalculator.kt\`) without Android framework dependencies.
  - \`data\`: Repositories, backups, and local persistence (\`LineupRepository.kt\`, \`data/backup/\`, \`local/\`, \`ThemePreferences.kt\`).
  - \`ui\`: Jetpack Compose components, screens, theme, and \`GrlViewModel.kt\`.
- **Build System**: Gradle (AGP \`${AGP_VER}\`, Kotlin DSL: \`build.gradle.kts\`, \`app/build.gradle.kts\`, \`gradle/libs.versions.toml\`).
- **Performance**: AndroidX Baseline Profiles (\`:baselineprofile\`).
EOF

# Replace in AGENTS.md from Section 1 up to (not including) Section 2
awk -v sec_file="$NEW_SEC_FILE" '
BEGIN {
    state = 0
}
/^## 1\. IDENTITY AND OBSERVED STACK/ {
    if (state == 0) {
        while ((getline line < sec_file) > 0) {
            print line
        }
        close(sec_file)
        printf "\n"
        state = 1
        next
    }
}
/^## 2\. MODES OF OPERATION/ {
    if (state == 1) {
        state = 2
    }
}
{
    if (state != 1) {
        print $0
    }
}
' "$AGENTS_MD" > "$UPDATED_FILE"

cat "$UPDATED_FILE" > "$AGENTS_MD"

printf "SUCCESS: AGENTS.md Section 1 successfully synchronized with codebase!\n"
