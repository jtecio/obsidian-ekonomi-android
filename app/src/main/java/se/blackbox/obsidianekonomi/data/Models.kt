package se.blackbox.obsidianekonomi.data

import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.LocalTime

/**
 * Kategori för utgifter/inkomster
 */
data class Category(
    val name: String,
    val emoji: String,
    val color: Color = Color.Blue,
    val isDefault: Boolean = false
) {
    fun toTag(format: TagFormat): String {
        return when (format) {
            TagFormat.EMOJI -> "#$emoji"
            TagFormat.TEXT -> "#${name.lowercase()}"
            TagFormat.NESTED -> "#utgift/${name.lowercase()}"
            TagFormat.COMBINED -> "#$emoji #${name.lowercase()}"
        }
    }
}

/**
 * En ekonomisk transaktion
 */
data class Transaction(
    val id: String = java.util.UUID.randomUUID().toString(),
    val amount: Double,
    val category: Category,
    val description: String,
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime = LocalTime.now(),
    val receiptPath: String? = null,
    val isIncome: Boolean = false
) {
    val formattedTime: String
        get() = String.format("%02d:%02d", time.hour, time.minute)
}

/**
 * Summering av transaktioner
 */
data class TransactionSummary(
    val transactions: List<Transaction>,
    val totalExpenses: Double,
    val totalIncome: Double,
    val netAmount: Double,
    val expensesByCategory: Map<Category, Double>
) {
    companion object {
        fun from(transactions: List<Transaction>): TransactionSummary {
            val expenses = transactions.filter { !it.isIncome }
            val income = transactions.filter { it.isIncome }

            val totalExpenses = expenses.sumOf { it.amount }
            val totalIncome = income.sumOf { it.amount }

            val expensesByCategory = expenses
                .groupBy { it.category }
                .mapValues { (_, trans) -> trans.sumOf { it.amount } }

            return TransactionSummary(
                transactions = transactions,
                totalExpenses = totalExpenses,
                totalIncome = totalIncome,
                netAmount = totalIncome - totalExpenses,
                expensesByCategory = expensesByCategory
            )
        }
    }
}

/**
 * App-inställningar
 */
data class AppSettings(
    val vaultPath: String = "",
    val storageMethod: StorageMethod = StorageMethod.DAILY_NOTES,
    val dailyNotesFolder: String = "Journal/Daily/{YYYY}",
    val dailyNotesFilename: String = "{YYYY-MM-DD}.md",
    val dailyNotesHeading: String = "## 💰 Ekonomi",
    val econNoteFolder: String = "Privat/Ekonomi",
    val econNoteFrequency: EconNoteFrequency = EconNoteFrequency.MONTHLY,
    val receiptFolder: String = "Media/Kvitton",
    val receiptFilenameFormat: String = "{YYYY-MM-DD-HHmm}.jpg",
    val receiptCompress: Boolean = true,
    val receiptQuality: Int = 85,
    val tagFormat: TagFormat = TagFormat.EMOJI,
    val markdownFormat: MarkdownFormat = MarkdownFormat.TABLE,
    val ocrEnabled: Boolean = true,
    val categories: List<Category> = defaultCategories
) {
    companion object {
        val defaultCategories = listOf(
            Category("Mat", "🍔", Color(0xFF4CAF50)),
            Category("Bensin", "⛽", Color(0xFFF44336)),
            Category("Hem", "🏠", Color(0xFF2196F3)),
            Category("Arbete", "💼", Color(0xFF9C27B0)),
            Category("Hälsa", "💊", Color(0xFFE91E63)),
            Category("Shopping", "🛒", Color(0xFFFF9800)),
            Category("Nöje", "🎬", Color(0xFF00BCD4)),
            Category("Övrigt", "📱", Color(0xFF607D8B))
        )
    }
}

enum class StorageMethod {
    DAILY_NOTES,
    DEDICATED_ECON_NOTE,
    SEPARATE_TRANSACTIONS
}

enum class EconNoteFrequency {
    MONTHLY,
    YEARLY,
    SINGLE
}

enum class TagFormat {
    EMOJI,
    TEXT,
    NESTED,
    COMBINED
}

enum class MarkdownFormat {
    TABLE,
    BULLET_LIST,
    DATAVIEW_INLINE;

    val displayName: String
        get() = when (this) {
            TABLE -> "Tabell"
            BULLET_LIST -> "Punktlista"
            DATAVIEW_INLINE -> "Dataview inline"
        }
}

/**
 * OCR-resultat från kvitto
 */
data class ReceiptOcrResult(
    val amount: Double? = null,
    val date: LocalDate? = null,
    val merchant: String? = null,
    val suggestedCategory: Category? = null,
    val confidence: Float = 0f,
    val rawText: String = ""
)
