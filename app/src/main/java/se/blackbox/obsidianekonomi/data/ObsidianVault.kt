package se.blackbox.obsidianekonomi.data

import android.util.Log
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Hanterar läsning och skrivning till Obsidian vault
 */
class ObsidianVault(private val settings: AppSettings) {

    companion object {
        private const val TAG = "ObsidianVault"
    }

    /**
     * Skriv en transaktion till vault
     */
    fun writeTransaction(transaction: Transaction): Result<Unit> {
        return try {
            val filePath = getFilePath(transaction.date)
            val file = File(filePath)

            // Skapa mapp om den inte finns
            file.parentFile?.mkdirs()

            val entry = formatEntry(transaction)

            when (settings.storageMethod) {
                StorageMethod.DAILY_NOTES -> appendToDailyNote(file, entry)
                StorageMethod.DEDICATED_ECON_NOTE -> appendToEconNote(file, entry, transaction.date)
                StorageMethod.SEPARATE_TRANSACTIONS -> createSeparateNote(file, transaction)
            }

            Log.d(TAG, "Skrev transaktion till: $filePath")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Fel vid skrivning: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Läs transaktioner från vault för ett visst datumintervall
     */
    fun readTransactions(fromDate: LocalDate, toDate: LocalDate = LocalDate.now()): List<Transaction> {
        val transactions = mutableListOf<Transaction>()

        try {
            var currentDate = fromDate
            while (!currentDate.isAfter(toDate)) {
                val dailyTransactions = readTransactionsFromDate(currentDate)
                transactions.addAll(dailyTransactions)
                currentDate = currentDate.plusDays(1)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fel vid läsning: ${e.message}", e)
        }

        return transactions.sortedByDescending { it.date.atTime(it.time) }
    }

    /**
     * Läs transaktioner från en specifik dag
     */
    private fun readTransactionsFromDate(date: LocalDate): List<Transaction> {
        val filePath = getFilePath(date)
        val file = File(filePath)

        if (!file.exists()) {
            return emptyList()
        }

        val content = file.readText()
        return parseTransactionsFromMarkdown(content, date)
    }

    /**
     * Parsa transaktioner från markdown-innehåll
     */
    private fun parseTransactionsFromMarkdown(content: String, date: LocalDate): List<Transaction> {
        val transactions = mutableListOf<Transaction>()

        when (settings.markdownFormat) {
            MarkdownFormat.TABLE -> {
                // Parsa tabell-format: | 14:23 | 150 kr | #🍔 | Lunch | ![[...]] |
                val tableRowRegex = Regex("""^\|\s*(\d{2}:\d{2})\s*\|\s*(\d+(?:\.\d+)?)\s*kr\s*\|\s*#(\S+)\s*\|\s*([^|]+)\s*\|""")
                content.lines().forEach { line ->
                    tableRowRegex.find(line.trim())?.let { match ->
                        val (time, amount, category, description) = match.destructured
                        transactions.add(
                            Transaction(
                                amount = amount.toDoubleOrNull() ?: 0.0,
                                category = findCategory(category),
                                description = description.trim(),
                                date = date,
                                time = LocalTime.parse(time)
                            )
                        )
                    }
                }
            }
            MarkdownFormat.BULLET_LIST -> {
                // Parsa bullet-format: - **150 kr** #🍔 - Lunch (14:23)
                val bulletRegex = Regex("""^-\s*\*\*(\d+(?:\.\d+)?)\s*kr\*\*\s*#(\S+)\s*-\s*([^(]+)\((\d{2}:\d{2})\)""")
                content.lines().forEach { line ->
                    bulletRegex.find(line.trim())?.let { match ->
                        val (amount, category, description, time) = match.destructured
                        transactions.add(
                            Transaction(
                                amount = amount.toDoubleOrNull() ?: 0.0,
                                category = findCategory(category),
                                description = description.trim(),
                                date = date,
                                time = LocalTime.parse(time)
                            )
                        )
                    }
                }
            }
            MarkdownFormat.DATAVIEW_INLINE -> {
                // Parsa dataview-format: - [belopp:: 150] [kategori:: #🍔] [beskrivning:: Lunch] [tid:: 14:23]
                val dataviewRegex = Regex("""\[belopp::\s*(\d+(?:\.\d+)?)\].*?\[kategori::\s*#(\S+)\].*?\[beskrivning::\s*([^\]]+)\].*?\[tid::\s*(\d{2}:\d{2})\]""")
                content.lines().forEach { line ->
                    dataviewRegex.find(line.trim())?.let { match ->
                        val (amount, category, description, time) = match.destructured
                        transactions.add(
                            Transaction(
                                amount = amount.toDoubleOrNull() ?: 0.0,
                                category = findCategory(category),
                                description = description.trim(),
                                date = date,
                                time = LocalTime.parse(time)
                            )
                        )
                    }
                }
            }
        }

        return transactions
    }

    /**
     * Hitta kategori baserat på emoji/namn
     */
    private fun findCategory(identifier: String): Category {
        return settings.categories.find {
            it.emoji == identifier || it.name.lowercase() == identifier.lowercase()
        } ?: settings.categories.last() // Fallback till "Övrigt"
    }

    /**
     * Formatera en transaktion till markdown
     */
    private fun formatEntry(transaction: Transaction): String {
        val receiptLink = if (transaction.receiptPath != null) {
            "![[${transaction.receiptPath}]]"
        } else "-"

        val tag = transaction.category.toTag(settings.tagFormat)

        return when (settings.markdownFormat) {
            MarkdownFormat.TABLE -> {
                "| ${transaction.formattedTime} | ${transaction.amount} kr | $tag | ${transaction.description} | $receiptLink |"
            }
            MarkdownFormat.BULLET_LIST -> {
                val receipt = if (transaction.receiptPath != null) " ![[${transaction.receiptPath}]]" else ""
                "- **${transaction.amount} kr** $tag - ${transaction.description} (${transaction.formattedTime})$receipt"
            }
            MarkdownFormat.DATAVIEW_INLINE -> {
                val receipt = if (transaction.receiptPath != null) " ![[${transaction.receiptPath}]]" else ""
                "- [belopp:: ${transaction.amount}] [kategori:: $tag] [beskrivning:: ${transaction.description}] [tid:: ${transaction.formattedTime}]$receipt"
            }
        }
    }

    /**
     * Append till daily note
     */
    private fun appendToDailyNote(file: File, entry: String) {
        if (file.exists()) {
            val content = file.readText()

            if (content.contains(settings.dailyNotesHeading)) {
                // Hitta ekonomi-sektionen och lägg till
                val lines = content.lines().toMutableList()
                val headingIndex = lines.indexOfFirst { it == settings.dailyNotesHeading }

                // Hitta första raden efter heading som är tabell/lista
                var insertIndex = headingIndex + 1
                while (insertIndex < lines.size && lines[insertIndex].isBlank()) {
                    insertIndex++
                }

                if (settings.markdownFormat == MarkdownFormat.TABLE) {
                    // Hoppa över header-rader
                    if (insertIndex < lines.size && lines[insertIndex].startsWith("|")) {
                        insertIndex += 2 // Hoppa header + separator
                    }
                }

                lines.add(insertIndex, entry)
                file.writeText(lines.joinToString("\n"))
            } else {
                // Lägg till ny sektion
                val newSection = buildString {
                    appendLine()
                    appendLine(settings.dailyNotesHeading)
                    appendLine()
                    if (settings.markdownFormat == MarkdownFormat.TABLE) {
                        appendLine("| Tid | Belopp | Kategori | Beskrivning | Kvitto |")
                        appendLine("|-----|--------|----------|-------------|--------|")
                    }
                    appendLine(entry)
                }
                file.appendText(newSection)
            }
        } else {
            // Skapa ny daily note
            file.writeText(createDailyNoteTemplate(entry))
        }
    }

    /**
     * Append till dedikerad ekonomi-note
     */
    private fun appendToEconNote(file: File, entry: String, date: LocalDate) {
        val dayHeading = "## ${date}"

        if (file.exists()) {
            val content = file.readText()

            if (content.contains(dayHeading)) {
                // Lägg till i befintlig dag-sektion (liknande daily note-logik)
                val lines = content.lines().toMutableList()
                val headingIndex = lines.indexOfFirst { it == dayHeading }
                lines.add(headingIndex + 3, entry) // Efter heading + table header
                file.writeText(lines.joinToString("\n"))
            } else {
                // Skapa ny dag-sektion
                val newDaySection = buildString {
                    appendLine()
                    appendLine(dayHeading)
                    appendLine()
                    if (settings.markdownFormat == MarkdownFormat.TABLE) {
                        appendLine("| Tid | Belopp | Kategori | Beskrivning | Kvitto |")
                        appendLine("|-----|--------|----------|-------------|--------|")
                    }
                    appendLine(entry)
                    appendLine()
                    appendLine("**Dagsumma:** ${0.0} kr") // TODO: Beräkna
                }
                file.appendText(newDaySection)
            }
        } else {
            // Skapa ny månads/års-note
            file.writeText(createEconNoteTemplate(date, entry))
        }
    }

    /**
     * Skapa separat note per transaktion
     */
    private fun createSeparateNote(file: File, transaction: Transaction) {
        val tag = transaction.category.toTag(settings.tagFormat)
        val receiptSection = if (transaction.receiptPath != null) {
            "\n\n## Kvitto\n\n![[${transaction.receiptPath}]]"
        } else ""

        val template = """
            |---
            |type: transaktion
            |datum: ${transaction.date}
            |tid: ${transaction.formattedTime}
            |belopp: ${transaction.amount}
            |kategori: ${transaction.category.name.lowercase()}
            |tags:
            |  - ${transaction.category.emoji}
            |  - utgift
            |---
            |
            |# ${transaction.description}
            |
            |**Belopp:** ${transaction.amount} kr
            |**Kategori:** $tag
            |**Datum:** ${transaction.date} ${transaction.formattedTime}
            |$receiptSection
        """.trimMargin()

        file.writeText(template)
    }

    /**
     * Skapa template för ny daily note
     */
    private fun createDailyNoteTemplate(firstEntry: String): String {
        val date = LocalDate.now()

        return buildString {
            appendLine("---")
            appendLine("typ: Daily")
            appendLine("date: $date")
            appendLine("---")
            appendLine()
            appendLine("# $date")
            appendLine()
            appendLine(settings.dailyNotesHeading)
            appendLine()
            if (settings.markdownFormat == MarkdownFormat.TABLE) {
                appendLine("| Tid | Belopp | Kategori | Beskrivning | Kvitto |")
                appendLine("|-----|--------|----------|-------------|--------|")
            }
            appendLine(firstEntry)
        }
    }

    /**
     * Skapa template för ekonomi-note
     */
    private fun createEconNoteTemplate(date: LocalDate, firstEntry: String): String {
        val heading = when (settings.econNoteFrequency) {
            EconNoteFrequency.MONTHLY -> "Ekonomi ${date.month.name.lowercase().capitalize()} ${date.year}"
            EconNoteFrequency.YEARLY -> "Ekonomi ${date.year}"
            EconNoteFrequency.SINGLE -> "Ekonomi"
        }

        return buildString {
            appendLine("---")
            appendLine("type: ekonomi")
            appendLine("datum: $date")
            appendLine("---")
            appendLine()
            appendLine("# $heading")
            appendLine()
            appendLine("## $date")
            appendLine()
            if (settings.markdownFormat == MarkdownFormat.TABLE) {
                appendLine("| Tid | Belopp | Kategori | Beskrivning | Kvitto |")
                appendLine("|-----|--------|----------|-------------|--------|")
            }
            appendLine(firstEntry)
        }
    }

    /**
     * Generera filsökväg baserat på inställningar
     */
    private fun getFilePath(date: LocalDate): String {
        val folder = when (settings.storageMethod) {
            StorageMethod.DAILY_NOTES -> {
                settings.dailyNotesFolder
                    .replace("{YYYY}", date.year.toString())
                    .replace("{MM}", date.monthValue.toString().padStart(2, '0'))
            }
            StorageMethod.DEDICATED_ECON_NOTE -> settings.econNoteFolder
            StorageMethod.SEPARATE_TRANSACTIONS -> "${settings.econNoteFolder}/Transaktioner"
        }

        val filename = when (settings.storageMethod) {
            StorageMethod.DAILY_NOTES -> {
                settings.dailyNotesFilename
                    .replace("{YYYY}", date.year.toString())
                    .replace("{MM}", date.monthValue.toString().padStart(2, '0'))
                    .replace("{DD}", date.dayOfMonth.toString().padStart(2, '0'))
                    .replace("{YYYYMMDD}", date.format(DateTimeFormatter.BASIC_ISO_DATE))
            }
            StorageMethod.DEDICATED_ECON_NOTE -> {
                when (settings.econNoteFrequency) {
                    EconNoteFrequency.MONTHLY -> "${date.year}-${date.monthValue.toString().padStart(2, '0')}.md"
                    EconNoteFrequency.YEARLY -> "${date.year}.md"
                    EconNoteFrequency.SINGLE -> "Ekonomi.md"
                }
            }
            StorageMethod.SEPARATE_TRANSACTIONS -> {
                "${date}-${System.currentTimeMillis()}.md"
            }
        }

        return "${settings.vaultPath}/$folder/$filename"
    }

    /**
     * Kopiera kvittobild till vault
     */
    fun copyReceiptToVault(sourcePath: String, description: String): String? {
        return try {
            val now = LocalDate.now().atTime(LocalTime.now())
            val filename = settings.receiptFilenameFormat
                .replace("{YYYY}", now.year.toString())
                .replace("{MM}", now.monthValue.toString().padStart(2, '0'))
                .replace("{DD}", now.dayOfMonth.toString().padStart(2, '0'))
                .replace("{HH}", now.hour.toString().padStart(2, '0'))
                .replace("{mm}", now.minute.toString().padStart(2, '0'))
                .replace("{beskrivning}", description.take(20).filter { it.isLetterOrDigit() })

            val destPath = "${settings.vaultPath}/${settings.receiptFolder}/$filename"
            val destFile = File(destPath)
            destFile.parentFile?.mkdirs()

            File(sourcePath).copyTo(destFile, overwrite = true)

            "${settings.receiptFolder}/$filename" // Relativ sökväg för wikilink
        } catch (e: Exception) {
            Log.e(TAG, "Fel vid kopiering av kvitto: ${e.message}", e)
            null
        }
    }
}
