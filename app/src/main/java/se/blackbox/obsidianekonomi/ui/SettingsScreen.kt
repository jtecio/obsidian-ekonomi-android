package se.blackbox.obsidianekonomi.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import se.blackbox.obsidianekonomi.data.AppSettings
import se.blackbox.obsidianekonomi.data.MarkdownFormat
import se.blackbox.obsidianekonomi.data.StorageMethod
import se.blackbox.obsidianekonomi.data.TagFormat
import java.io.File

/**
 * Inställningsskärm för att konfigurera vault-sökväg och format
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onSettingsChanged: (AppSettings) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showStorageMethodDialog by remember { mutableStateOf(false) }
    var showMarkdownFormatDialog by remember { mutableStateOf(false) }
    var showTagFormatDialog by remember { mutableStateOf(false) }

    // Folder picker launcher
    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            // Bevara persistenta permissions
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(it, flags)

            // Konvertera URI till faktisk sökväg (förenklad)
            val path = when {
                it.path?.contains("primary:") == true -> {
                    val afterPrimary = it.path?.substringAfter("primary:") ?: ""
                    "/storage/emulated/0/$afterPrimary"
                }
                else -> it.path ?: settings.vaultPath
            }

            onSettingsChanged(settings.copy(vaultPath = path))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inställningar") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Tillbaka")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Vault-inställningar sektion
            item {
                SectionHeader("Vault")
            }

            item {
                SettingItem(
                    title = "Vault-sökväg",
                    description = settings.vaultPath.ifBlank { "Inte konfigurerad" },
                    icon = {
                        Icon(Icons.Default.Folder, contentDescription = null)
                    },
                    onClick = { folderPicker.launch(null) }
                )
            }

            item {
                if (settings.vaultPath.isNotBlank()) {
                    VaultPathInfo(settings.vaultPath)
                }
            }

            // Sparningsmetod sektion
            item {
                SectionHeader("Sparningsmetod")
            }

            item {
                SettingItem(
                    title = "Lagringsmetod",
                    description = when (settings.storageMethod) {
                        StorageMethod.DAILY_NOTES -> "Daily Notes (Rekommenderat)"
                        StorageMethod.DEDICATED_ECON_NOTE -> "Dedikerad ekonomi-note"
                        StorageMethod.SEPARATE_TRANSACTIONS -> "Separat note per transaktion"
                    },
                    onClick = { showStorageMethodDialog = true }
                )
            }

            if (settings.storageMethod == StorageMethod.DAILY_NOTES) {
                item {
                    InfoCard(
                        text = "Transaktioner läggs i dagens daily note under rubriken '${settings.dailyNotesHeading}'"
                    )
                }
            }

            // Format sektion
            item {
                SectionHeader("Format")
            }

            item {
                SettingItem(
                    title = "Markdown-format",
                    description = when (settings.markdownFormat) {
                        MarkdownFormat.TABLE -> "Tabell (Rekommenderat)"
                        MarkdownFormat.BULLET_LIST -> "Punktlista"
                        MarkdownFormat.DATAVIEW_INLINE -> "Dataview inline"
                    },
                    onClick = { showMarkdownFormatDialog = true }
                )
            }

            item {
                SettingItem(
                    title = "Tag-format",
                    description = when (settings.tagFormat) {
                        TagFormat.EMOJI -> "Emoji-tags (#🍔)"
                        TagFormat.TEXT -> "Text-tags (#mat)"
                        TagFormat.NESTED -> "Nested tags (#utgift/mat)"
                    },
                    onClick = { showTagFormatDialog = true }
                )
            }

            // Formatexempel
            item {
                MarkdownFormatExample(settings)
            }

            // Om-sektion
            item {
                SectionHeader("Om")
            }

            item {
                InfoCard(
                    text = """
                        Obsidian Ekonomi v0.1 (MVP)

                        En snabb app för att logga ekonomisk data till ditt Obsidian-vault.

                        Utvecklad med Jetpack Compose och Material 3.

                        GitHub: github.com/jtecio/obsidian-ekonomi-android
                    """.trimIndent()
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    // Dialogs
    if (showStorageMethodDialog) {
        StorageMethodDialog(
            currentMethod = settings.storageMethod,
            onDismiss = { showStorageMethodDialog = false },
            onSelect = { method ->
                onSettingsChanged(settings.copy(storageMethod = method))
                showStorageMethodDialog = false
            }
        )
    }

    if (showMarkdownFormatDialog) {
        MarkdownFormatDialog(
            currentFormat = settings.markdownFormat,
            onDismiss = { showMarkdownFormatDialog = false },
            onSelect = { format ->
                onSettingsChanged(settings.copy(markdownFormat = format))
                showMarkdownFormatDialog = false
            }
        )
    }

    if (showTagFormatDialog) {
        TagFormatDialog(
            currentFormat = settings.tagFormat,
            onDismiss = { showTagFormatDialog = false },
            onSelect = { format ->
                onSettingsChanged(settings.copy(tagFormat = format))
                showTagFormatDialog = false
            }
        )
    }
}

@Composable
private fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text.uppercase(),
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun SettingItem(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.invoke()

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun VaultPathInfo(
    path: String,
    modifier: Modifier = Modifier
) {
    val exists = remember(path) {
        try {
            File(path).exists()
        } catch (e: Exception) {
            false
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (exists) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (exists) "✓" else "⚠",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = if (exists) {
                    "Vault hittades"
                } else {
                    "Varning: Sökvägen finns inte eller är inte åtkomlig"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (exists) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onErrorContainer
                }
            )
        }
    }
}

@Composable
private fun InfoCard(
    text: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MarkdownFormatExample(
    settings: AppSettings,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "📄 Exempel",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            val example = when (settings.markdownFormat) {
                MarkdownFormat.TABLE -> """
                    | Tid | Belopp | Kategori | Beskrivning |
                    |-----|--------|----------|-------------|
                    | 14:23 | 150 kr | #🍔 | Lunch |
                """.trimIndent()

                MarkdownFormat.BULLET_LIST -> """
                    - **150 kr** #🍔 - Lunch (14:23)
                """.trimIndent()

                MarkdownFormat.DATAVIEW_INLINE -> """
                    - [belopp:: 150] [kategori:: #🍔] [beskrivning:: Lunch] [tid:: 14:23]
                """.trimIndent()
            }

            Text(
                text = example,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

// Dialogs
@Composable
private fun StorageMethodDialog(
    currentMethod: StorageMethod,
    onDismiss: () -> Unit,
    onSelect: (StorageMethod) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Välj lagringsmetod") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StorageMethod.values().forEach { method ->
                    SelectableItem(
                        title = when (method) {
                            StorageMethod.DAILY_NOTES -> "Daily Notes"
                            StorageMethod.DEDICATED_ECON_NOTE -> "Dedikerad ekonomi-note"
                            StorageMethod.SEPARATE_TRANSACTIONS -> "Separat note per transaktion"
                        },
                        description = when (method) {
                            StorageMethod.DAILY_NOTES -> "Lägg till i dagens daily note (rekommenderat)"
                            StorageMethod.DEDICATED_ECON_NOTE -> "En note per månad för ekonomi"
                            StorageMethod.SEPARATE_TRANSACTIONS -> "Varje transaktion blir en egen note"
                        },
                        selected = method == currentMethod,
                        onClick = { onSelect(method) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Stäng")
            }
        }
    )
}

@Composable
private fun MarkdownFormatDialog(
    currentFormat: MarkdownFormat,
    onDismiss: () -> Unit,
    onSelect: (MarkdownFormat) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Välj markdown-format") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MarkdownFormat.values().forEach { format ->
                    SelectableItem(
                        title = when (format) {
                            MarkdownFormat.TABLE -> "Tabell"
                            MarkdownFormat.BULLET_LIST -> "Punktlista"
                            MarkdownFormat.DATAVIEW_INLINE -> "Dataview inline"
                        },
                        description = when (format) {
                            MarkdownFormat.TABLE -> "Strukturerad tabell (bäst för läsbarhet)"
                            MarkdownFormat.BULLET_LIST -> "Enkel punktlista"
                            MarkdownFormat.DATAVIEW_INLINE -> "För Dataview-queries"
                        },
                        selected = format == currentFormat,
                        onClick = { onSelect(format) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Stäng")
            }
        }
    )
}

@Composable
private fun TagFormatDialog(
    currentFormat: TagFormat,
    onDismiss: () -> Unit,
    onSelect: (TagFormat) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Välj tag-format") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TagFormat.values().forEach { format ->
                    SelectableItem(
                        title = when (format) {
                            TagFormat.EMOJI -> "Emoji-tags"
                            TagFormat.TEXT -> "Text-tags"
                            TagFormat.NESTED -> "Nested tags"
                        },
                        description = when (format) {
                            TagFormat.EMOJI -> "#🍔 #⛽ (visuellt, snabbt att känna igen)"
                            TagFormat.TEXT -> "#mat #bensin (enklare text)"
                            TagFormat.NESTED -> "#utgift/mat (hierarkiska)"
                        },
                        selected = format == currentFormat,
                        onClick = { onSelect(format) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Stäng")
            }
        }
    )
}

@Composable
private fun SelectableItem(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Spacer(modifier = Modifier.size(24.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}
