package se.blackbox.obsidianekonomi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import se.blackbox.obsidianekonomi.data.Category
import se.blackbox.obsidianekonomi.data.Transaction
import se.blackbox.obsidianekonomi.data.TransactionSummary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*

/**
 * Summerings-skärm som visar ekonomisk statistik
 * Läser data från Obsidian vault och visar:
 * - Dagens utgifter
 * - Veckans utgifter
 * - Månadens utgifter
 * - Utgifter per kategori
 * - Lista över alla transaktioner
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    transactions: List<Transaction>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPeriod by remember { mutableStateOf(Period.TODAY) }

    // Filtrera transaktioner baserat på vald period
    val filteredTransactions = remember(transactions, selectedPeriod) {
        filterTransactionsByPeriod(transactions, selectedPeriod)
    }

    // Beräkna summering
    val summary = remember(filteredTransactions) {
        TransactionSummary.from(filteredTransactions)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📊 Summering") },
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
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Period-väljare
            item {
                PeriodSelector(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { selectedPeriod = it }
                )
            }

            // Huvudsummering
            item {
                MainSummaryCard(
                    period = selectedPeriod,
                    summary = summary
                )
            }

            // Summering per kategori
            if (summary.expensesByCategory.isNotEmpty()) {
                item {
                    Text(
                        text = "Per Kategori",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(summary.expensesByCategory.entries.toList()) { (category, amount) ->
                    CategorySummaryCard(
                        category = category,
                        amount = amount,
                        totalExpenses = summary.totalExpenses
                    )
                }
            }

            // Transaktionslista
            if (filteredTransactions.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Transaktioner (${filteredTransactions.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(filteredTransactions) { transaction ->
                    TransactionListItem(transaction = transaction)
                }
            } else {
                item {
                    EmptyStateMessage(period = selectedPeriod)
                }
            }
        }
    }
}

/**
 * Period-väljare (Idag, Vecka, Månad)
 */
@Composable
private fun PeriodSelector(
    selectedPeriod: Period,
    onPeriodSelected: (Period) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Period.values().forEach { period ->
            FilterChip(
                selected = selectedPeriod == period,
                onClick = { onPeriodSelected(period) },
                label = { Text(period.displayName) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Huvudsummerings-kort
 */
@Composable
private fun MainSummaryCard(
    period: Period,
    summary: TransactionSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Period-text
            Text(
                text = getPeriodText(period),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            // Totala utgifter
            Text(
                text = "%.0f kr".format(summary.totalExpenses),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            // Antal transaktioner
            Text(
                text = "${summary.transactions.size} transaktioner",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )

            // Om det finns inkomster, visa netto
            if (summary.totalIncome > 0) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Inkomster",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "+%.0f kr".format(summary.totalIncome),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4CAF50) // Grön
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Netto",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "%.0f kr".format(summary.netAmount),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (summary.netAmount >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Kategori-summerings-kort
 */
@Composable
private fun CategorySummaryCard(
    category: Category,
    amount: Double,
    totalExpenses: Double,
    modifier: Modifier = Modifier
) {
    val percentage = if (totalExpenses > 0) (amount / totalExpenses * 100) else 0.0

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Kategori emoji + namn
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Emoji-cirkel
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

                Column {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "%.0f%% av utgifter".format(percentage),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Belopp
            Text(
                text = "%.0f kr".format(amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = category.color
            )
        }

        // Progress bar
        LinearProgressIndicator(
            progress = { (percentage / 100).toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = category.color,
            trackColor = category.color.copy(alpha = 0.2f)
        )
    }
}

/**
 * Transaktions-lista-item
 */
@Composable
private fun TransactionListItem(
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
            // Emoji + info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Emoji-cirkel
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(transaction.category.color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = transaction.category.emoji,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Column {
                    Text(
                        text = transaction.description.ifBlank { transaction.category.name },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${transaction.formattedTime} • ${transaction.date.format(DateTimeFormatter.ofPattern("d MMM", Locale("sv")))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Belopp
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
 * Tom-state meddelande
 */
@Composable
private fun EmptyStateMessage(
    period: Period,
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
                text = "📭",
                style = MaterialTheme.typography.displayMedium
            )
            Text(
                text = "Inga transaktioner",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Inga utgifter registrerade ${period.displayName.lowercase()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Perioder att filtrera på
 */
enum class Period(val displayName: String) {
    TODAY("Idag"),
    WEEK("Denna vecka"),
    MONTH("Denna månad")
}

/**
 * Filtrera transaktioner baserat på period
 */
private fun filterTransactionsByPeriod(
    transactions: List<Transaction>,
    period: Period
): List<Transaction> {
    val now = LocalDate.now()

    return when (period) {
        Period.TODAY -> {
            transactions.filter { it.date == now }
        }
        Period.WEEK -> {
            val weekFields = WeekFields.of(Locale("sv", "SE"))
            val currentWeek = now.get(weekFields.weekOfWeekBasedYear())
            val currentYear = now.get(weekFields.weekBasedYear())

            transactions.filter {
                val transactionWeek = it.date.get(weekFields.weekOfWeekBasedYear())
                val transactionYear = it.date.get(weekFields.weekBasedYear())
                transactionWeek == currentWeek && transactionYear == currentYear
            }
        }
        Period.MONTH -> {
            transactions.filter {
                it.date.month == now.month && it.date.year == now.year
            }
        }
    }
}

/**
 * Generera period-text för huvudkortet
 */
private fun getPeriodText(period: Period): String {
    val now = LocalDate.now()

    return when (period) {
        Period.TODAY -> "Idag ${now.format(DateTimeFormatter.ofPattern("d MMMM", Locale("sv")))}"
        Period.WEEK -> {
            val weekFields = WeekFields.of(Locale("sv", "SE"))
            val weekNumber = now.get(weekFields.weekOfWeekBasedYear())
            "Vecka $weekNumber"
        }
        Period.MONTH -> now.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("sv")))
    }
}
