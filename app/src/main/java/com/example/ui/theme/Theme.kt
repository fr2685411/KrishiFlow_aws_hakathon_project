package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class AppThemeOption(
    val label: String,
    val subtitle: String,
    val primaryPreview: Color,
    val secondaryPreview: Color
) {
    SYSTEM(
        label = "System Default",
        subtitle = "Follows device dark/light setting",
        primaryPreview = Color(0xFF166534),
        secondaryPreview = Color(0xFFD97706)
    ),
    LIGHT(
        label = "Kisan Daylight",
        subtitle = "Fresh clean daylight agro theme",
        primaryPreview = Color(0xFF166534),
        secondaryPreview = Color(0xFF22C55E)
    ),
    DARK(
        label = "Mandi Dark",
        subtitle = "High-contrast dark mode for night work",
        primaryPreview = Color(0xFF4ADE80),
        secondaryPreview = Color(0xFFF59E0B)
    ),
    HARVEST_GOLD(
        label = "Golden Harvest",
        subtitle = "Rich amber harvest wheat & sunlight",
        primaryPreview = Color(0xFFB45309),
        secondaryPreview = Color(0xFFD97706)
    ),
    EMERALD_NATURE(
        label = "Emerald Crop",
        subtitle = "Deep lush evergreen organic vitality",
        primaryPreview = Color(0xFF047857),
        secondaryPreview = Color(0xFF0D9488)
    ),
    EARTH_CLAY(
        label = "Earth Terracotta",
        subtitle = "Warm agro-soil clay & terracotta contrast",
        primaryPreview = Color(0xFFC2410C),
        secondaryPreview = Color(0xFF854D0E)
    )
}

private val EmeraldNatureColorScheme = lightColorScheme(
    primary = Color(0xFF047857), // Emerald 700
    onPrimary = SurfaceWhite,
    primaryContainer = Color(0xFFD1FAE5), // Emerald 100
    onPrimaryContainer = Color(0xFF064E3B), // Emerald 900
    secondary = Color(0xFF0D9488), // Teal 600
    onSecondary = SurfaceWhite,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF115E59),
    tertiary = Color(0xFF65A30D), // Lime 600
    background = Color(0xFFF0FDF4), // Emerald 50
    surface = Color(0xFFF7FEE7),
    surfaceVariant = Color(0xFFDCFCE7),
    onBackground = Color(0xFF064E3B),
    onSurface = Color(0xFF064E3B),
    onSurfaceVariant = Color(0xFF047857),
    outline = Color(0xFFA7F3D0),
    error = SpoilageRed,
    errorContainer = SpoilageLight
)

private val EarthClayColorScheme = lightColorScheme(
    primary = Color(0xFFC2410C), // Orange 700 / Terracotta
    onPrimary = SurfaceWhite,
    primaryContainer = Color(0xFFFFEDD5), // Orange 100
    onPrimaryContainer = Color(0xFF7C2D12), // Orange 900
    secondary = Color(0xFF854D0E), // Yellow 800
    onSecondary = SurfaceWhite,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = GreenPrimary,
    background = Color(0xFFFFF7ED), // Orange 50
    surface = Color(0xFFFFFDF8),
    surfaceVariant = Color(0xFFFFE8D6),
    onBackground = Color(0xFF431407),
    onSurface = Color(0xFF431407),
    onSurfaceVariant = Color(0xFF7C2D12),
    outline = Color(0xFFFED7AA),
    error = SpoilageRed,
    errorContainer = SpoilageLight
)

private val HarvestGoldColorScheme = lightColorScheme(
    primary = Color(0xFFB45309), // Amber 700
    onPrimary = SurfaceWhite,
    primaryContainer = Color(0xFFFEF3C7), // Amber 100
    onPrimaryContainer = Color(0xFF78350F), // Amber 900
    secondary = GreenPrimary,
    onSecondary = SurfaceWhite,
    secondaryContainer = GreenLight,
    onSecondaryContainer = GreenPrimaryVariant,
    tertiary = EcoTeal,
    background = Color(0xFFFFFBEB), // Amber 50
    surface = Color(0xFFFFFDF5),
    surfaceVariant = Color(0xFFFEF9C3),
    onBackground = Color(0xFF451A03),
    onSurface = Color(0xFF451A03),
    onSurfaceVariant = Color(0xFF78350F),
    outline = Color(0xFFFDE68A),
    error = SpoilageRed,
    errorContainer = SpoilageLight
)

private val DarkColorScheme = darkColorScheme(
    primary = GreenPrimaryDark,
    onPrimary = SurfaceDark,
    primaryContainer = GreenPrimaryVariant,
    onPrimaryContainer = GreenLight,
    secondary = GoldSecondary,
    onSecondary = SurfaceDark,
    secondaryContainer = GoldPrimary,
    onSecondaryContainer = GoldLight,
    tertiary = EcoTeal,
    background = SurfaceDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceCardDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    error = SpoilageRed,
    errorContainer = SpoilageLight
)

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = SurfaceWhite,
    primaryContainer = GreenLight,
    onPrimaryContainer = GreenPrimaryVariant,
    secondary = GoldPrimary,
    onSecondary = SurfaceWhite,
    secondaryContainer = GoldLight,
    onSecondaryContainer = GoldPrimary,
    tertiary = EcoTeal,
    onTertiary = SurfaceWhite,
    tertiaryContainer = EcoTealLight,
    background = SurfaceLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceWhite,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = DividerColor,
    error = SpoilageRed,
    errorContainer = SpoilageLight
)

@Composable
fun MyApplicationTheme(
    themeOption: AppThemeOption = AppThemeOption.LIGHT,
    dynamicColor: Boolean = false, // Preserve brand agricultural identity
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val colorScheme = when (themeOption) {
        AppThemeOption.SYSTEM -> {
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (isSystemDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else if (isSystemDark) {
                DarkColorScheme
            } else {
                LightColorScheme
            }
        }
        AppThemeOption.LIGHT -> LightColorScheme
        AppThemeOption.DARK -> DarkColorScheme
        AppThemeOption.HARVEST_GOLD -> HarvestGoldColorScheme
        AppThemeOption.EMERALD_NATURE -> EmeraldNatureColorScheme
        AppThemeOption.EARTH_CLAY -> EarthClayColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
