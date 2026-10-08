package com.example.data.export

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CropCategory
import com.example.data.model.WasteDestination
import com.example.data.model.WasteLog
import com.example.data.model.WasteReason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.nio.charset.StandardCharsets

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WasteLogCsvExporterTest {

    @Test
    fun testExportToCsv_createsValidCsvWithCorrectTotalsAndEscaping() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val logs = listOf(
            WasteLog(
                id = 1,
                cropName = "Tomato, Hybrid",
                category = CropCategory.VEGETABLES,
                quantityKg = 100.0,
                destination = WasteDestination.DONATION,
                reason = WasteReason.MARKET_SURPLUS,
                destinationPartner = "Roti Bank \"Akshaya\"",
                estimatedValueRupees = 2000.0,
                notes = "Exported for food bank distribution, free meals",
                logDateTimestamp = 1760000000000L
            ),
            WasteLog(
                id = 2,
                cropName = "Cabbage",
                category = CropCategory.VEGETABLES,
                quantityKg = 50.0,
                destination = WasteDestination.COMPOSTING,
                reason = WasteReason.HARVEST_DAMAGE,
                destinationPartner = "Farm Vermicompost Pit",
                estimatedValueRupees = 250.0,
                notes = "Aerobic composting for bio-fertilizer",
                logDateTimestamp = 1760000000000L
            )
        )

        val result = WasteLogCsvExporter.exportToCsv(context, logs, prefix = "test_waste_log")

        assertTrue(result.file.exists())
        assertEquals(2, result.recordCount)
        assertEquals(150.0, result.totalQuantityKg, 0.01)
        assertEquals(150.0 * 1.9, result.totalCo2SavedKg, 0.01)
        assertEquals(2250.0, result.totalValueRupees, 0.01)

        val fileContent = result.file.readText(StandardCharsets.UTF_8)

        // Verify UTF-8 BOM
        assertTrue(fileContent.startsWith("\uFEFF"))

        // Verify escaping for commas and quotes
        assertTrue(fileContent.contains("\"Tomato, Hybrid\""))
        assertTrue(fileContent.contains("\"Roti Bank \"\"Akshaya\"\"\""))

        // Verify columns and summary row
        assertTrue(fileContent.contains("Log_ID,Log_Date,Crop_Name"))
        assertTrue(fileContent.contains("TOTAL_SUMMARY"))
        assertTrue(fileContent.contains("150.00"))
    }
}
