package com.example.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.CropPriceThreshold
import com.example.data.model.MandiRateEntity
import com.example.data.model.ThresholdTriggerCondition
import kotlin.math.roundToInt

/**
 * Local Notification Alert Dispatcher for Farmer Crop Market Price Thresholds.
 * Creates dedicated Notification Channels and delivers real-time local heads-up
 * alerts when mandi prices breach user-defined targets.
 */
object CropPriceAlertNotificationHelper {

    private const val TAG = "PriceAlertHelper"
    const val CHANNEL_ID_PRICE_ALERTS = "krishiflow_price_alerts_channel"
    const val CHANNEL_NAME_PRICE_ALERTS = "KrishiFlow Mandi Price Alerts (मंडी भाव सूचनाएं)"

    /**
     * Initializes the notification channel on Android 8.0+ (API 26+)
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_PRICE_ALERTS,
                CHANNEL_NAME_PRICE_ALERTS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent notifications when mandi crop prices reach your configured selling target"
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
            Log.d(TAG, "Initialized notification channel: $CHANNEL_ID_PRICE_ALERTS")
        }
    }

    /**
     * Checks if POST_NOTIFICATIONS permission is granted (required on Android 13+).
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    /**
     * Dispatches a local system notification when a crop hits the specified market threshold.
     */
    fun sendPriceThresholdAlert(
        context: Context,
        threshold: CropPriceThreshold,
        currentMandiRate: MandiRateEntity
    ) {
        if (!hasNotificationPermission(context)) {
            Log.w(TAG, "Notification permission not granted. Skipping notification.")
            return
        }

        // Tap action: opens KrishiFlow MainActivity
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_SCREEN", "KISAN_DASHBOARD")
            putExtra("TARGET_CROP", threshold.cropName)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            threshold.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val priceDifference = currentMandiRate.maxPricePerKg - threshold.targetPricePerKg
        val diffSymbol = if (priceDifference >= 0) "+₹${priceDifference.roundToInt()}" else "-₹${(-priceDifference).roundToInt()}"

        val title = "🎯 Price Alert: ${threshold.cropName} hit ₹${currentMandiRate.maxPricePerKg.toInt()}/kg!"
        val shortContent = "Target ₹${threshold.targetPricePerKg.toInt()} reached at ${currentMandiRate.mandiName} ($diffSymbol/kg). Tap to sell or pledge harvest."

        val bigText = StringBuilder().apply {
            append("📈 Mandi Price Target Reached!\n")
            append("• Crop: ${threshold.cropName} (${threshold.variety.ifBlank { "Standard Variety" }})\n")
            append("• Current APMC Rate: ₹${currentMandiRate.maxPricePerKg.toInt()}/kg (Modal: ₹${currentMandiRate.modalPricePerKg.toInt()}/kg)\n")
            append("• Your Target: ₹${threshold.targetPricePerKg.toInt()}/kg\n")
            append("• Mandi: ${currentMandiRate.mandiName} (${currentMandiRate.state})\n")
            append("• Trend: ${currentMandiRate.priceTrend} (Arrivals: ${currentMandiRate.arrivalTonnes} T)\n")
            append("• Net Payout with 0% Brokerage: ₹${currentMandiRate.krishiFlowNetFarmerRate.toInt()}/kg\n\n")
            append("Tap to lock in orders with verified buyers and eliminate mandi cut!")
        }.toString()

        val notificationId = (threshold.cropName.hashCode() * 31) + currentMandiRate.mandiName.hashCode()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_PRICE_ALERTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(shortContent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setColor(0xFF166534.toInt()) // KrishiFlow Primary Green
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, notification)
            Log.d(TAG, "Sent price alert notification for ${threshold.cropName} @ ₹${currentMandiRate.maxPricePerKg}")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException sending notification", e)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send notification", e)
        }
    }

    /**
     * Dispatches a test/simulation alert immediately so the farmer can verify notification settings.
     */
    fun sendTestPriceAlert(
        context: Context,
        cropName: String = "Hybrid Tomato",
        targetPrice: Double = 32.0,
        currentPrice: Double = 38.0,
        mandiName: String = "Pimpalgaon APMC"
    ) {
        if (!hasNotificationPermission(context)) {
            Log.w(TAG, "Notification permission not granted for test alert.")
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            9999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "🔔 Live Test Alert: $cropName hit ₹${currentPrice.toInt()}/kg!"
        val bigText = "Mandi price for $cropName at $mandiName is currently ₹${currentPrice.toInt()}/kg, exceeding your target of ₹${targetPrice.toInt()}/kg (+₹${(currentPrice - targetPrice).toInt()}/kg premium).\n\nLocal notifications are active and ready to alert you during live APMC spikes!"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_PRICE_ALERTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText("Target ₹${targetPrice.toInt()} breached at $mandiName! Tap to view.")
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setColor(0xFF166534.toInt())
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(8888, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Error posting test alert", e)
        }
    }
}
