package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.notification.CropPriceAlertNotificationHelper
import com.example.ui.KrishiApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.KrishiViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()
        } catch (_: Throwable) {
            // Safe fallback if window controller edge-to-edge is not supported by device skin
        }

        // Initialize local notification channels for crop price thresholds
        CropPriceAlertNotificationHelper.createNotificationChannel(applicationContext)

        setContent {
            val viewModel: KrishiViewModel = viewModel()
            val themeMode by viewModel.appThemeMode.collectAsStateWithLifecycle()
            MyApplicationTheme(themeOption = themeMode) {
                KrishiApp(viewModel = viewModel)
            }
        }
    }
}
