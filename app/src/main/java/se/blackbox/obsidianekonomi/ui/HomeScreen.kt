package se.blackbox.obsidianekonomi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import se.blackbox.obsidianekonomi.data.Category
import se.blackbox.obsidianekonomi.data.Transaction
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Huvudskärm för snabb inmatning av ekonomisk data
 * Funktioner:
 * - Snabbformulär för nya utgifter
 * - Lista över dagens transaktioner
 * - Knapp för kvittofoto
 * - Navigering till summering och inställningar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    todaysTransactions: List<Transaction>,
    onAddTransaction: (Transaction) -> Unit,
    onTakeReceipt: () -> Unit,
    onNavigateToSummary: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var isIncome by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("💰 Ekonomi")
                        Text(
                            text = LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("sv"))),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Inställningar")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Kamera-knapp för kvitto
                SmallFloatingActionButton(
                    onClick = onTakeReceipt,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Default.Camera, contentDescription = "Ta kvittofoto")
                }

                // Summerings-knapp
                ExtendedFloatingActionButton(
                    onClick = onNavigateToSummary,
                    text = { Text("📊 Summering") },
                    icon = { }
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Snabbinmatnings-kort
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Ny transaktion",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Inkomst/Utgift-växlare
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = !isIncome,
                                onClick = { isIncome = false },
                                label = { Text("Utgift") },
                                modifier = Modifier.weight(1f),
                                leadingIcon = { Text("➖") }
                            )
                            FilterChip(
                                selected = isIncome,
                                onClick = { isIncome = true },
                                label = { Text("Inkomst") },
                                modifier = Modifier.weight(1f),
                                leadingIcon = { Text("➕") }
                            )
                        }

                        // Kategori-väljare
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCategoryPicker = true }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (selectedCategory != null) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(selectedCategory!!.color.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = selectedCategory!!.emoji,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                        }
                                        Text(
                                            text = selectedCategory!!.name,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "Välj kategori...",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                                Text("▼", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        // Belopp-fält
                        OutlinedTextField(
                            value = amount,
                            onValueChange = {
                                // Tillåt endast siffror och punkt
                                if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                    amount = it
                                }
                            },
                            label = { Text("Belopp (kr)") },
                            placeholder = { Text("0") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            singleLine = true
                        )

                        // Beskrivning-fält
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Beskrivning (valfri)") },
                            placeholder = { Text("Vad köpte du?") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    if (selectedCategory != null && amount.isNotBlank()) {
                                        val amountValue = amount.toDoubleOrNull() ?: 0.0
                                        if (amountValue > 0) {
                                            onAddTransaction(
                                                Transaction(
                                                    amount = amountValue,
                                                    category = selectedCategory!!,
                                                    description = description,
                                                    isIncome = isIncome
                                                )
                                            )
                                            // Återställ formulär
                                            amount = ""
                                            description = ""
                                            selectedCategory = null
                                            isIncome = false
                                        }
                                    }
                                }
                            ),
                            singleLine = true
                        )

                        // Lägg till-knapp
                        Button(
                            onClick = {
                                if (selectedCategory != null && amount.isNotBlank()) {
                                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                                    if (amountValue > 0) {
                                        onAddTransaction(
                                            Transaction(
                                                amount = amountValue,
                                                category = selectedCategory!!,
                                                description = description,
                                                isIncome = isIncome
                                            )
                                        )
                                        // Återställ formulär
                                        amount = ""
                                        description = ""
                                        selectedCategory = null
                                        isIncome = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = selectedCategory != null && amount.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Lägg till")
                        }
                    }
                }
            }

            // Dagens transaktioner
            if (todaysTransactions.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Idag (${todaysTransactions.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        val todayTotal = todaysTransactions
                            .filter { !it.isIncome }
                            .sumOf { it.amount }

                        Text(
                            text = "-%.0f kr".format(todayTotal),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                items(todaysTransactions) { transaction ->
                    TransactionCard(transaction = transaction)
                }
            } else {
                item {
                    EmptyTodayCard()
                }
            }

            // Padding för FAB
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Kategori-väljare modal
    if (showCategoryPicker) {
        CategoryPickerDialog(
            onDismiss = { showCategoryPicker = false },
            onCategorySelected = { category ->
                selectedCategory = category
                showCategoryPicker = false
            }
        )
    }
}

/**
 * Transaktionskort för lista
 */
@Composable
private fun TransactionCard(
    transaction: Transaction,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(transaction.category.color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = transaction.category.emoji,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Column {
                    Text(
                        text = transaction.description.ifBlank { transaction.category.name },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = transaction.formattedTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Text(
                text = "${if (transaction.isIncome) "+" else "-"}%.0f kr".format(transaction.amount),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (transaction.isIncome) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Tom-state kort när inga transaktioner finns
 */
@Composable
private fun EmptyTodayCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "📝",
                style = MaterialTheme.typography.displayMedium
            )
            Text(
                text = "Inga transaktioner ännu",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Lägg till din första utgift ovan",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Kategori-väljare dialog
 */
@Composable
private fun CategoryPickerDialog(
    onDismiss: () -> Unit,
    onCategorySelected: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Välj kategori") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(Category.values()) { category ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCategorySelected(category) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(category.color.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = category.emoji,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Avbryt")
            }
        }
    )
}
