package com.example.data.export

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.WasteLog
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Result data class returned when WasteLog records are exported to a local CSV file.
 */
data class WasteLogCsvExportResult(
    val file: File,
    val recordCount: Int,
    val totalQuantityKg: Double,
    val totalCo2SavedKg: Double,
    val totalValueRupees: Double,
    val fileSizeBytes: Long,
    val previewCsvString: String
)

/**
 * High-performance, offline CSV utility for exporting daily agricultural surplus
 * and waste logs stored in the local Room Database.
 *
 * Farmers can generate and store offline records on device, open them in spreadsheet
 * apps (Google Sheets, Microsoft Excel), or share them with local Panchayats, NGOs,
 * and agronomy auditors.
 */
object WasteLogCsvExporter {

    private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val FILE_NAME_DATE_FORMAT = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    /**
     * Exports a list of [WasteLog] records from the Room Database to a structured local CSV file.
     * Uses UTF-8 with BOM (\uFEFF) so standard spreadsheet applications properly decode text.
     */
    fun exportToCsv(
        context: Context,
        logs: List<WasteLog>,
        prefix: String = "krishiflow_wastelog"
    ): WasteLogCsvExportResult {
        // Ensure exports directory exists in app-specific external storage or cache
        val exportDir = File(context.getExternalFilesDir("exports") ?: context.cacheDir, "exports").apply {
            if (!exists()) mkdirs()
        }

        val timestampStr = FILE_NAME_DATE_FORMAT.format(Date())
        val csvFile = File(exportDir, "${prefix}_$timestampStr.csv")

        val totalQuantityKg = logs.sumOf { it.quantityKg }
        val totalCo2SavedKg = logs.sumOf { it.co2DivertedKg }
        val totalValueRupees = logs.sumOf { it.estimatedValueRupees }

        val sb = StringBuilder()

        // 1. Write UTF-8 Byte Order Mark (BOM) for Excel compatibility
        sb.append('\uFEFF')

        // 2. CSV Metadata Header
        sb.append("# KRISHIFLOW AGRICULTURAL SURPLUS & WASTE REDUCTION AUDIT LOG\n")
        sb.append("# Export Generated: ${DATE_FORMAT.format(Date())}\n")
        sb.append("# Total Records: ${logs.size}\n")
        sb.append(String.format(Locale.US, "# Total Diverted Biomass: %.2f kg\n", totalQuantityKg))
        sb.append(String.format(Locale.US, "# Total Avoided CO2e Emissions: %.2f kg CO2e\n", totalCo2SavedKg))
        sb.append(String.format(Locale.US, "# Estimated Recovered Value: ₹%.2f\n", totalValueRupees))
        sb.append("# Storage Engine: Android Room Database (Offline SQLite)\n")
        sb.append("\n")

        // 3. Column Headers
        val headers = listOf(
            "Log_ID",
            "Log_Date",
            "Crop_Name",
            "Category",
            "Quantity_Kg",
            "Destination_Code",
            "Destination_Label",
            "Surplus_Reason_Code",
            "Surplus_Reason_Label",
            "Destination_Partner",
            "Estimated_Value_INR",
            "Avoided_CO2e_Kg",
            "Estimated_Meals_Provided",
            "Notes"
        )
        sb.append(headers.joinToString(","))
        sb.append("\n")

        // 4. Data Rows
        logs.forEach { log ->
            val dateStr = DATE_FORMAT.format(Date(log.logDateTimestamp))
            val mealsProvided = (log.quantityKg * 2.5).toInt()

            val rowValues = listOf(
                log.id.toString(),
                escapeCsv(dateStr),
                escapeCsv(log.cropName),
                escapeCsv(log.category.displayName),
                String.format(Locale.US, "%.2f", log.quantityKg),
                escapeCsv(log.destination.name),
                escapeCsv(log.destination.displayName),
                escapeCsv(log.reason.name),
                escapeCsv(log.reason.displayName),
                escapeCsv(log.destinationPartner),
                String.format(Locale.US, "%.2f", log.estimatedValueRupees),
                String.format(Locale.US, "%.2f", log.co2DivertedKg),
                mealsProvided.toString(),
                escapeCsv(log.notes)
            )
            sb.append(rowValues.joinToString(","))
            sb.append("\n")
        }

        // 5. Summary Footer Row
        sb.append("\n")
        val summaryRow = listOf(
            "TOTAL_SUMMARY",
            "",
            "ALL_CROPS",
            "",
            String.format(Locale.US, "%.2f", totalQuantityKg),
            "",
            "DIVERTIED_FROM_LANDFILL",
            "",
            "",
            "",
            String.format(Locale.US, "%.2f", totalValueRupees),
            String.format(Locale.US, "%.2f", totalCo2SavedKg),
            (totalQuantityKg * 2.5).toInt().toString(),
            "100% Landfill Diversion"
        )
        sb.append(summaryRow.joinToString(","))
        sb.append("\n")

        val csvContent = sb.toString()

        // Write directly using OutputStreamWriter with UTF-8
        FileOutputStream(csvFile).use { fos ->
            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                writer.write(csvContent)
                writer.flush()
            }
        }

        return WasteLogCsvExportResult(
            file = csvFile,
            recordCount = logs.size,
            totalQuantityKg = totalQuantityKg,
            totalCo2SavedKg = totalCo2SavedKg,
            totalValueRupees = totalValueRupees,
            fileSizeBytes = csvFile.length(),
            previewCsvString = csvContent.lines().take(15).joinToString("\n")
        )
    }

    /**
     * Shares the CSV file via Android's native share intent sheet.
     */
    fun shareCsvFile(context: Context, csvFile: File) {
        if (!csvFile.exists()) {
            Toast.makeText(context, "CSV file not found", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            csvFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "KrishiFlow Agricultural Surplus & Waste Reduction Log (CSV)")
            putExtra(
                Intent.EXTRA_TEXT,
                "Attached is the daily agricultural surplus and food waste diversion record exported from KrishiFlow."
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Share WasteLog CSV")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    /**
     * Opens the CSV file in an installed spreadsheet viewer or text editor.
     */
    fun openCsvFile(context: Context, csvFile: File) {
        if (!csvFile.exists()) {
            Toast.makeText(context, "CSV file not found", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            csvFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "text/csv")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            val chooser = Intent.createChooser(intent, "Open WasteLog CSV")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "No app available to open CSV", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Copies the full CSV content to the device clipboard.
     */
    fun copyCsvToClipboard(context: Context, csvFile: File) {
        if (!csvFile.exists()) return
        val content = csvFile.readText(StandardCharsets.UTF_8)
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("KrishiFlow WasteLog CSV", content)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, "CSV copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    /**
     * Helper to escape CSV cell contents containing quotes, commas, or line breaks.
     */
    private fun escapeCsv(value: String?): String {
        if (value == null) return ""
        val needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")
        return if (needsQuotes) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}
