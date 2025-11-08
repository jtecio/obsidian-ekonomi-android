#!/bin/bash

# Validation script for Obsidian Ekonomi Android app
# Checks project structure and configuration without building

echo "🔍 Validating Obsidian Ekonomi project..."
echo ""

PROJECT_ROOT="/home/johan/Documents/Blackbox/Arbete/Android Apps/ObsidianEkonomi"
cd "$PROJECT_ROOT" || exit 1

ERRORS=0
WARNINGS=0

# Color codes
RED='\033[0;31m'
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
NC='\033[0m' # No Color

check_error() {
    if [ $? -ne 0 ]; then
        echo -e "${RED}✗ $1${NC}"
        ERRORS=$((ERRORS + 1))
        return 1
    else
        echo -e "${GREEN}✓ $1${NC}"
        return 0
    fi
}

check_file() {
    if [ -f "$1" ]; then
        echo -e "${GREEN}✓ Found: $1${NC}"
        return 0
    else
        echo -e "${RED}✗ Missing: $1${NC}"
        ERRORS=$((ERRORS + 1))
        return 1
    fi
}

check_optional() {
    if [ -f "$1" ]; then
        echo -e "${GREEN}✓ Found: $1${NC}"
    else
        echo -e "${YELLOW}⚠ Optional missing: $1${NC}"
        WARNINGS=$((WARNINGS + 1))
    fi
}

echo "=== Checking Project Structure ==="
echo ""

# Essential build files
check_file "build.gradle.kts"
check_file "settings.gradle.kts"
check_file "app/build.gradle.kts"

echo ""
echo "=== Checking Source Files ==="
echo ""

# Main source files
check_file "app/src/main/AndroidManifest.xml"
check_file "app/src/main/java/se/blackbox/obsidianekonomi/MainActivity.kt"
check_file "app/src/main/java/se/blackbox/obsidianekonomi/MainViewModel.kt"

# Data layer
check_file "app/src/main/java/se/blackbox/obsidianekonomi/data/Models.kt"
check_file "app/src/main/java/se/blackbox/obsidianekonomi/data/ObsidianVault.kt"
check_file "app/src/main/java/se/blackbox/obsidianekonomi/data/SettingsRepository.kt"

# UI layer
check_file "app/src/main/java/se/blackbox/obsidianekonomi/ui/HomeScreen.kt"
check_file "app/src/main/java/se/blackbox/obsidianekonomi/ui/SummaryScreen.kt"
check_file "app/src/main/java/se/blackbox/obsidianekonomi/ui/SettingsScreen.kt"

# Theme
check_file "app/src/main/java/se/blackbox/obsidianekonomi/ui/theme/Theme.kt"
check_file "app/src/main/java/se/blackbox/obsidianekonomi/ui/theme/Type.kt"

echo ""
echo "=== Checking Resources ==="
echo ""

check_file "app/src/main/res/values/strings.xml"
check_file "app/src/main/res/values/themes.xml"
check_file "app/src/main/res/xml/backup_rules.xml"
check_file "app/src/main/res/xml/data_extraction_rules.xml"

echo ""
echo "=== Checking Documentation ==="
echo ""

check_file "README.md"
check_file "NÄSTA_STEG.md"
check_file "BUILD_AND_TEST.md"
check_file "APP_ICON_GUIDE.md"
check_file "IMPLEMENTATION_SUMMARY.md"

echo ""
echo "=== Checking Optional Files ==="
echo ""

check_optional ".gitignore"
check_optional "gradle.properties"
check_optional "keystore.properties"

echo ""
echo "=== Checking Kotlin Syntax (basic) ==="
echo ""

# Count Kotlin files
KOTLIN_FILES=$(find app/src/main/java -name "*.kt" | wc -l)
echo "Found $KOTLIN_FILES Kotlin files"

# Check for common syntax errors (basic grep patterns)
echo ""
echo "Checking for potential issues..."

# Check for unbalanced braces in Kotlin files
for file in app/src/main/java/se/blackbox/obsidianekonomi/**/*.kt; do
    if [ -f "$file" ]; then
        OPEN_BRACES=$(grep -o '{' "$file" | wc -l)
        CLOSE_BRACES=$(grep -o '}' "$file" | wc -l)

        if [ "$OPEN_BRACES" -ne "$CLOSE_BRACES" ]; then
            echo -e "${YELLOW}⚠ Potential brace mismatch in $(basename "$file"): {=$OPEN_BRACES, }=$CLOSE_BRACES${NC}"
            WARNINGS=$((WARNINGS + 1))
        fi
    fi
done

# Check AndroidManifest for required elements
echo ""
echo "Checking AndroidManifest.xml..."

if grep -q "android.permission.CAMERA" app/src/main/AndroidManifest.xml; then
    echo -e "${GREEN}✓ CAMERA permission declared${NC}"
else
    echo -e "${RED}✗ CAMERA permission missing${NC}"
    ERRORS=$((ERRORS + 1))
fi

if grep -q "android.permission.READ_EXTERNAL_STORAGE" app/src/main/AndroidManifest.xml; then
    echo -e "${GREEN}✓ Storage permissions declared${NC}"
else
    echo -e "${YELLOW}⚠ Storage permissions may be missing${NC}"
    WARNINGS=$((WARNINGS + 1))
fi

if grep -q "MainActivity" app/src/main/AndroidManifest.xml; then
    echo -e "${GREEN}✓ MainActivity declared${NC}"
else
    echo -e "${RED}✗ MainActivity not declared in manifest${NC}"
    ERRORS=$((ERRORS + 1))
fi

# Check build.gradle for dependencies
echo ""
echo "Checking dependencies in app/build.gradle.kts..."

REQUIRED_DEPS=(
    "androidx.compose.material3:material3"
    "androidx.navigation:navigation-compose"
    "androidx.datastore:datastore-preferences"
    "androidx.lifecycle:lifecycle-viewmodel-compose"
)

for dep in "${REQUIRED_DEPS[@]}"; do
    if grep -q "$dep" app/build.gradle.kts; then
        echo -e "${GREEN}✓ Dependency: $dep${NC}"
    else
        echo -e "${RED}✗ Missing dependency: $dep${NC}"
        ERRORS=$((ERRORS + 1))
    fi
done

# Check for package name consistency
echo ""
echo "Checking package name consistency..."

PACKAGE_NAME="se.blackbox.obsidianekonomi"

if grep -q "namespace = \"$PACKAGE_NAME\"" app/build.gradle.kts; then
    echo -e "${GREEN}✓ Package name in build.gradle.kts: $PACKAGE_NAME${NC}"
else
    echo -e "${YELLOW}⚠ Package name mismatch in build.gradle.kts${NC}"
    WARNINGS=$((WARNINGS + 1))
fi

if grep -q "package $PACKAGE_NAME" app/src/main/java/se/blackbox/obsidianekonomi/MainActivity.kt; then
    echo -e "${GREEN}✓ Package name in MainActivity.kt: $PACKAGE_NAME${NC}"
else
    echo -e "${RED}✗ Package name mismatch in MainActivity.kt${NC}"
    ERRORS=$((ERRORS + 1))
fi

# Summary
echo ""
echo "========================================="
echo "VALIDATION SUMMARY"
echo "========================================="
echo ""

if [ $ERRORS -eq 0 ] && [ $WARNINGS -eq 0 ]; then
    echo -e "${GREEN}✓ All checks passed! Project looks good.${NC}"
    echo ""
    echo "Next step: Open in Android Studio and sync Gradle"
    exit 0
elif [ $ERRORS -eq 0 ]; then
    echo -e "${YELLOW}⚠ $WARNINGS warnings found (non-critical)${NC}"
    echo -e "${GREEN}✓ No errors found. Project should build.${NC}"
    echo ""
    echo "Next step: Open in Android Studio and sync Gradle"
    exit 0
else
    echo -e "${RED}✗ $ERRORS errors found${NC}"
    if [ $WARNINGS -gt 0 ]; then
        echo -e "${YELLOW}⚠ $WARNINGS warnings found${NC}"
    fi
    echo ""
    echo "Please fix errors before building."
    exit 1
fi
