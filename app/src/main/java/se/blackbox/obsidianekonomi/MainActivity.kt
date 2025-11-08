package se.blackbox.obsidianekonomi

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import se.blackbox.obsidianekonomi.ui.HomeScreen
import se.blackbox.obsidianekonomi.ui.SettingsScreen
import se.blackbox.obsidianekonomi.ui.SummaryScreen
import se.blackbox.obsidianekonomi.ui.theme.ObsidianEkonomiTheme

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel

    // Camera permission launcher
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // TODO: Öppna kamera för kvittofoto
            Toast.makeText(this, "Kamera-funktion kommer snart!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Kamera-behörighet krävs för kvittofoton", Toast.LENGTH_SHORT).show()
        }
    }

    // Storage permission launcher (Android 12 and below)
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.loadTransactions()
        } else {
            Toast.makeText(this, "Filåtkomst krävs för att läsa Obsidian vault", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initiera ViewModel
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[MainViewModel::class.java]

        // Begär storage permissions
        checkAndRequestStoragePermission()

        setContent {
            ObsidianEkonomiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent(viewModel = viewModel)
                }
            }
        }
    }

    @Composable
    private fun AppContent(viewModel: MainViewModel) {
        val navController = rememberNavController()
        val uiState by viewModel.uiState.collectAsState()
        val settings by viewModel.settings.collectAsState()

        // Visa error snackbar
        if (uiState.error != null) {
            LaunchedEffect(uiState.error) {
                Toast.makeText(this@MainActivity, uiState.error, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        // Visa success snackbar
        if (uiState.showSuccessMessage != null) {
            LaunchedEffect(uiState.showSuccessMessage) {
                Toast.makeText(this@MainActivity, uiState.showSuccessMessage, Toast.LENGTH_SHORT).show()
                viewModel.clearSuccessMessage()
            }
        }

        NavHost(navController = navController, startDestination = "home") {
            composable("home") {
                HomeScreen(
                    todaysTransactions = uiState.todaysTransactions,
                    onAddTransaction = { transaction ->
                        viewModel.addTransaction(transaction)
                    },
                    onTakeReceipt = { handleReceiptPhoto() },
                    onNavigateToSummary = { navController.navigate("summary") },
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }

            composable("summary") {
                SummaryScreen(
                    transactions = uiState.transactions,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("settings") {
                SettingsScreen(
                    settings = settings,
                    onSettingsChanged = { newSettings ->
                        viewModel.updateSettings(newSettings)
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }

    private fun handleReceiptPhoto() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // TODO: Öppna kamera
                Toast.makeText(this, "Kamera-funktion kommer snart!", Toast.LENGTH_SHORT).show()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun checkAndRequestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ använder scoped storage, ingen permission behövs för Documents folder
            viewModel.loadTransactions()
        } else {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    viewModel.loadTransactions()
                }
                else -> {
                    storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }
    }
}
