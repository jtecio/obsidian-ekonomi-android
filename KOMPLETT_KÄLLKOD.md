# Obsidian Ekonomi - Komplett Källkod

Detta dokument innehåller all källkod för Android-appen. Kopiera filerna till rätt platser i Android Studio-projektet.

## Projektstruktur

```
ObsidianEkonomi/
├── app/
│   ├── build.gradle.kts ✅ Skapad
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/se/blackbox/obsidianekonomi/
│       │   ├── MainActivity.kt
│       │   ├── MainViewModel.kt
│       │   ├── data/
│       │   │   ├── Models.kt ✅ Skapad
│       │   │   ├── ObsidianVault.kt ✅ Skapad
│       │   │   ├── SettingsRepository.kt
│       │   │   └── ReceiptScanner.kt
│       │   ├── ui/
│       │   │   ├── HomeScreen.kt
│       │   │   ├── SettingsScreen.kt
│       │   │   ├── SummaryScreen.kt
│       │   │   └── components/
│       │   │       └── CategoryPicker.kt
│       │   └── util/
│       │       └── Permissions.kt
│       └── res/
│           ├── values/
│           │   ├── strings.xml
│           │   └── colors.xml
│           └── xml/
│               └── file_paths.xml
├── build.gradle.kts ✅ Skapad
└── settings.gradle.kts ✅ Skapad
```

---

## Fil: app/src/main/AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-feature android:name="android.hardware.camera" android:required="false" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
        android:maxSdkVersion="32" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
        android:maxSdkVersion="28"
        tools:ignore="ScopedStorage" />
    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.ObsidianEkonomi"
        tools:targetApi="31">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.ObsidianEkonomi">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>
    </application>
</manifest>
```

---

## Fil: app/src/main/res/xml/file_paths.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-path name="external_files" path="." />
    <cache-path name="cache" path="." />
</paths>
```

---

## Fil: app/src/main/res/values/strings.xml

```xml
<resources>
    <string name="app_name">Obsidian Ekonomi</string>
    <string name="quick_expense">Snabb Utgift</string>
    <string name="amount">Belopp (kr)</string>
    <string name="category">Kategori</string>
    <string name="description">Beskrivning</string>
    <string name="save_expense">Spara Utgift</string>
    <string name="receipt_photo">Kvittofoto</string>
    <string name="take_photo">Ta Foto</string>
    <string name="today">Idag</string>
    <string name="this_week">Denna vecka</string>
    <string name="this_month">Denna månad</string>
    <string name="total_expenses">Totala utgifter</string>
    <string name="settings">Inställningar</string>
    <string name="vault_path">Vault-sökväg</string>
    <string name="select_folder">Välj mapp</string>
</resources>
```

---

## Fil: app/src/main/res/values/colors.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="purple_200">#FFBB86FC</color>
    <color name="purple_500">#FF6200EE</color>
    <color name="purple_700">#FF3700B3</color>
    <color name="teal_200">#FF03DAC5</color>
    <color name="teal_700">#FF018786</color>
    <color name="black">#FF000000</color>
    <color name="white">#FFFFFFFF</color>
</resources>
```

---

## Fil: app/src/main/java/se/blackbox/obsidianekonomi/data/SettingsRepository.kt

```kotlin
package se.blackbox.obsidianekonomi.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val VAULT_PATH = stringPreferencesKey("vault_path")
        val STORAGE_METHOD = stringPreferencesKey("storage_method")
        val DAILY_NOTES_FOLDER = stringPreferencesKey("daily_notes_folder")
        val DAILY_NOTES_FILENAME = stringPreferencesKey("daily_notes_filename")
        val DAILY_NOTES_HEADING = stringPreferencesKey("daily_notes_heading")
        val MARKDOWN_FORMAT = stringPreferencesKey("markdown_format")
        val TAG_FORMAT = stringPreferencesKey("tag_format")
        val OCR_ENABLED = booleanPreferencesKey("ocr_enabled")
    }

    val settingsFlow: Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            vaultPath = prefs[VAULT_PATH] ?: "",
            storageMethod = StorageMethod.valueOf(
                prefs[STORAGE_METHOD] ?: StorageMethod.DAILY_NOTES.name
            ),
            dailyNotesFolder = prefs[DAILY_NOTES_FOLDER] ?: "Journal/Daily/{YYYY}",
            dailyNotesFilename = prefs[DAILY_NOTES_FILENAME] ?: "{YYYY-MM-DD}.md",
            dailyNotesHeading = prefs[DAILY_NOTES_HEADING] ?: "## 💰 Ekonomi",
            markdownFormat = MarkdownFormat.valueOf(
                prefs[MARKDOWN_FORMAT] ?: MarkdownFormat.TABLE.name
            ),
            tagFormat = TagFormat.valueOf(prefs[TAG_FORMAT] ?: TagFormat.EMOJI.name),
            ocrEnabled = prefs[OCR_ENABLED] ?: true
        )
    }

    suspend fun updateVaultPath(path: String) {
        dataStore.edit { prefs ->
            prefs[VAULT_PATH] = path
        }
    }

    suspend fun updateStorageMethod(method: StorageMethod) {
        dataStore.edit { prefs ->
            prefs[STORAGE_METHOD] = method.name
        }
    }

    suspend fun updateMarkdownFormat(format: MarkdownFormat) {
        dataStore.edit { prefs ->
            prefs[MARKDOWN_FORMAT] = format.name
        }
    }

    suspend fun updateTagFormat(format: TagFormat) {
        dataStore.edit { prefs ->
            prefs[TAG_FORMAT] = format.name
        }
    }
}
```

---

## FORTSÄTTER I NÄSTA MEDDELANDE (för långt för ett meddelande)...

Vill du att jag:
1. Fortsätter med resten av koden (MainActivity, UI-skärmar, osv)?
2. Eller skapar separata filer istället för ett stort dokument?
