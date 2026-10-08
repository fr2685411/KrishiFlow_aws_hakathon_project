package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AppThemeOption
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary

@Composable
fun ThemeSelectorDialog(
    currentTheme: AppThemeOption,
    onSelectTheme: (AppThemeOption) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("theme_selector_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = GreenLight,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "App Theme Options",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Customize visual appearance & contrast",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Theme List
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppThemeOption.entries.forEach { option ->
                        val isSelected = currentTheme == option
                        val previewColors = when (option) {
                            AppThemeOption.LIGHT -> listOf(Color(0xFF166534), Color(0xFFDCFCE7), Color(0xFFFFFFFF))
                            AppThemeOption.DARK -> listOf(Color(0xFF4ADE80), Color(0xFF162319), Color(0xFF0B140E))
                            AppThemeOption.HARVEST_GOLD -> listOf(Color(0xFFB45309), Color(0xFFFEF3C7), Color(0xFFFFFBEB))
                            AppThemeOption.EMERALD_NATURE -> listOf(Color(0xFF047857), Color(0xFFD1FAE5), Color(0xFFF0FDF4))
                            AppThemeOption.EARTH_CLAY -> listOf(Color(0xFFC2410C), Color(0xFFFFEDD5), Color(0xFFFFF7ED))
                            AppThemeOption.SYSTEM -> listOf(Color(0xFF166534), Color(0xFF0B140E), Color(0xFF94A3B8))
                        }

                        val themeIcon = when (option) {
                            AppThemeOption.LIGHT -> Icons.Default.LightMode
                            AppThemeOption.DARK -> Icons.Default.DarkMode
                            AppThemeOption.HARVEST_GOLD -> Icons.Default.Palette
                            AppThemeOption.EMERALD_NATURE -> Icons.Default.Eco
                            AppThemeOption.EARTH_CLAY -> Icons.Default.Agriculture
                            AppThemeOption.SYSTEM -> Icons.Default.SettingsBrightness
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) GreenPrimary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, GreenPrimary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectTheme(option) }
                                .testTag("theme_option_${option.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = themeIcon,
                                        contentDescription = null,
                                        tint = if (isSelected) GreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )

                                    Column {
                                        Text(
                                            text = option.label,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) GreenPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                        Text(
                                            text = option.subtitle,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Mini color swatch circles
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        previewColors.forEach { color ->
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clip(CircleShape)
                                                    .background(color)
                                                    .border(0.5.dp, Color.Gray.copy(alpha = 0.4f), CircleShape)
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = GreenPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Close Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("theme_dialog_close_btn")
                ) {
                    Text("Apply Theme", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
