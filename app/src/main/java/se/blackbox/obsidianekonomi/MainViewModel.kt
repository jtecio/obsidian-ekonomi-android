package se.blackbox.obsidianekonomi

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.blackbox.obsidianekonomi.data.AppSettings
import se.blackbox.obsidianekonomi.data.ObsidianVault
import se.blackbox.obsidianekonomi.data.SettingsRepository
import se.blackbox.obsidianekonomi.data.Transaction
import java.time.LocalDate

/**
 * Huvudsaklig ViewModel för appen
 * Hanterar:
 * - Laddning och sparande av transaktioner
 * - App-inställningar
 * - UI-tillstånd
 * - Felhantering
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)
    private lateinit var vault: ObsidianVault

    // UI State
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    // Settings State
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    init {
        loadSettings()
    }

    /**
     * Ladda inställningar från DataStore
     */
    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { loadedSettings ->
                _settings.value = loadedSettings

                // Initiera vault med nya inställningar
                if (loadedSettings.vaultPath.isNotBlank()) {
                    vault = ObsidianVault(loadedSettings)
                    loadTransactions()
                } else {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Vault-sökväg inte konfigurerad. Gå till inställningar."
                    )}
                }
            }
        }
    }

    /**
     * Ladda transaktioner från vault
     */
    fun loadTransactions(fromDate: LocalDate = LocalDate.now().minusDays(30)) {
        if (!::vault.isInitialized) {
            _uiState.update { it.copy(error = "Vault inte initialiserat") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val transactions = vault.readTransactions(fromDate = fromDate)
                _uiState.update { currentState ->
                    currentState.copy(
                        transactions = transactions,
                        todaysTransactions = transactions.filter { it.date == LocalDate.now() },
                        isLoading = false
                    )
                }
                Log.d(TAG, "Laddade ${transactions.size} transaktioner")
            } catch (e: Exception) {
                Log.e(TAG, "Fel vid laddning av transaktioner", e)
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Kunde inte läsa transaktioner: ${e.message}"
                )}
            }
        }
    }

    /**
     * Lägg till ny transaktion
     */
    fun addTransaction(transaction: Transaction) {
        if (!::vault.isInitialized) {
            _uiState.update { it.copy(error = "Vault inte initialiserat") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            vault.writeTransaction(transaction)
                .onSuccess {
                    Log.d(TAG, "Transaktion sparad: ${transaction.amount} kr - ${transaction.category.name}")

                    // Uppdatera lista
                    _uiState.update { currentState ->
                        val updatedTransactions = (currentState.transactions + transaction)
                            .sortedByDescending { it.date.atTime(it.time) }

                        currentState.copy(
                            transactions = updatedTransactions,
                            todaysTransactions = updatedTransactions.filter { it.date == LocalDate.now() },
                            isSaving = false,
                            showSuccessMessage = "Transaktion sparad!"
                        )
                    }

                    // Töm success-meddelande efter 3 sekunder
                    kotlinx.coroutines.delay(3000)
                    _uiState.update { it.copy(showSuccessMessage = null) }
                }
                .onFailure { exception ->
                    Log.e(TAG, "Fel vid sparning av transaktion", exception)
                    _uiState.update { it.copy(
                        isSaving = false,
                        error = "Kunde inte spara transaktion: ${exception.message}"
                    )}
                }
        }
    }

    /**
     * Uppdatera inställningar
     */
    fun updateSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            settingsRepository.updateSettings(newSettings)
            _settings.value = newSettings

            // Återinitiera vault med nya inställningar
            vault = ObsidianVault(newSettings)
            loadTransactions()
        }
    }

    /**
     * Uppdatera vault-sökväg
     */
    fun updateVaultPath(path: String) {
        viewModelScope.launch {
            val updatedSettings = _settings.value.copy(vaultPath = path)
            updateSettings(updatedSettings)
        }
    }

    /**
     * Rensa felmeddelande
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Rensa success-meddelande
     */
    fun clearSuccessMessage() {
        _uiState.update { it.copy(showSuccessMessage = null) }
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}

/**
 * UI State för hela appen
 */
data class AppUiState(
    val transactions: List<Transaction> = emptyList(),
    val todaysTransactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val showSuccessMessage: String? = null
)
