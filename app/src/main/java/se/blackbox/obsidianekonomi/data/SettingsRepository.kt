package se.blackbox.obsidianekonomi.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository för att hantera app-inställningar med DataStore
 */
class SettingsRepository(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

        // Preference keys
        private val VAULT_PATH = stringPreferencesKey("vault_path")
        private val STORAGE_METHOD = stringPreferencesKey("storage_method")
        private val DAILY_NOTES_FOLDER = stringPreferencesKey("daily_notes_folder")
        private val DAILY_NOTES_FILENAME = stringPreferencesKey("daily_notes_filename")
        private val DAILY_NOTES_HEADING = stringPreferencesKey("daily_notes_heading")
        private val MARKDOWN_FORMAT = stringPreferencesKey("markdown_format")
        private val TAG_FORMAT = stringPreferencesKey("tag_format")
        private val ECON_NOTE_FOLDER = stringPreferencesKey("econ_note_folder")
        private val ECON_NOTE_FREQUENCY = stringPreferencesKey("econ_note_frequency")
        private val TRANSACTIONS_FOLDER = stringPreferencesKey("transactions_folder")
        private val RECEIPT_FOLDER = stringPreferencesKey("receipt_folder")
        private val RECEIPT_FILENAME_FORMAT = stringPreferencesKey("receipt_filename_format")
    }

    /**
     * Flow med app-inställningar
     */
    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        AppSettings(
            vaultPath = preferences[VAULT_PATH] ?: "",
            storageMethod = preferences[STORAGE_METHOD]?.let {
                StorageMethod.valueOf(it)
            } ?: StorageMethod.DAILY_NOTES,
            dailyNotesFolder = preferences[DAILY_NOTES_FOLDER] ?: "Journal/Daily/{YYYY}",
            dailyNotesFilename = preferences[DAILY_NOTES_FILENAME] ?: "{YYYY-MM-DD}.md",
            dailyNotesHeading = preferences[DAILY_NOTES_HEADING] ?: "## 💰 Ekonomi",
            markdownFormat = preferences[MARKDOWN_FORMAT]?.let {
                MarkdownFormat.valueOf(it)
            } ?: MarkdownFormat.TABLE,
            tagFormat = preferences[TAG_FORMAT]?.let {
                TagFormat.valueOf(it)
            } ?: TagFormat.EMOJI,
            econNoteFolder = preferences[ECON_NOTE_FOLDER] ?: "Privat/Ekonomi",
            econNoteFrequency = preferences[ECON_NOTE_FREQUENCY]?.let {
                EconNoteFrequency.valueOf(it)
            } ?: EconNoteFrequency.MONTHLY,
            transactionsFolder = preferences[TRANSACTIONS_FOLDER] ?: "Privat/Ekonomi/Transaktioner/{YYYY}",
            receiptFolder = preferences[RECEIPT_FOLDER] ?: "Media/Kvitton/{YYYY}/{MM}",
            receiptFilenameFormat = preferences[RECEIPT_FILENAME_FORMAT] ?: "{YYYY-MM-DD}-{HHmm}"
        )
    }

    /**
     * Uppdatera app-inställningar
     */
    suspend fun updateSettings(settings: AppSettings) {
        context.dataStore.edit { preferences ->
            preferences[VAULT_PATH] = settings.vaultPath
            preferences[STORAGE_METHOD] = settings.storageMethod.name
            preferences[DAILY_NOTES_FOLDER] = settings.dailyNotesFolder
            preferences[DAILY_NOTES_FILENAME] = settings.dailyNotesFilename
            preferences[DAILY_NOTES_HEADING] = settings.dailyNotesHeading
            preferences[MARKDOWN_FORMAT] = settings.markdownFormat.name
            preferences[TAG_FORMAT] = settings.tagFormat.name
            preferences[ECON_NOTE_FOLDER] = settings.econNoteFolder
            preferences[ECON_NOTE_FREQUENCY] = settings.econNoteFrequency.name
            preferences[TRANSACTIONS_FOLDER] = settings.transactionsFolder
            preferences[RECEIPT_FOLDER] = settings.receiptFolder
            preferences[RECEIPT_FILENAME_FORMAT] = settings.receiptFilenameFormat
        }
    }

    /**
     * Uppdatera vault-sökväg
     */
    suspend fun updateVaultPath(path: String) {
        context.dataStore.edit { preferences ->
            preferences[VAULT_PATH] = path
        }
    }

    /**
     * Uppdatera storage method
     */
    suspend fun updateStorageMethod(method: StorageMethod) {
        context.dataStore.edit { preferences ->
            preferences[STORAGE_METHOD] = method.name
        }
    }

    /**
     * Uppdatera markdown format
     */
    suspend fun updateMarkdownFormat(format: MarkdownFormat) {
        context.dataStore.edit { preferences ->
            preferences[MARKDOWN_FORMAT] = format.name
        }
    }

    /**
     * Uppdatera tag format
     */
    suspend fun updateTagFormat(format: TagFormat) {
        context.dataStore.edit { preferences ->
            preferences[TAG_FORMAT] = format.name
        }
    }

    /**
     * Rensa alla inställningar (reset till default)
     */
    suspend fun clearSettings() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
