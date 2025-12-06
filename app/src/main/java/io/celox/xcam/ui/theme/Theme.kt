package io.celox.xcam.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// =============================================================================
// XCam Professional Dark & Red Theme
// =============================================================================

/**
 * Custom Dark Color Scheme - Primary theme for XCam
 * Features: Red primary color on dark surfaces
 */
private val XCamDarkColorScheme = darkColorScheme(
    // Primary colors
    primary = Red40,
    onPrimary = OnRed,
    primaryContainer = Red30,
    onPrimaryContainer = Red80,

    // Secondary colors
    secondary = Gray60,
    onSecondary = Gray90,
    secondaryContainer = Gray40,
    onSecondaryContainer = Gray80,

    // Tertiary colors
    tertiary = RecordingRed,
    onTertiary = OnRed,
    tertiaryContainer = Red20,
    onTertiaryContainer = Red80,

    // Background & Surface
    background = DarkBackground,
    onBackground = OnDarkSurface,
    surface = DarkSurface,
    onSurface = OnDarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = OnDarkSurfaceVariant,

    // Other semantic colors
    error = ErrorRed,
    onError = Color.Black,
    errorContainer = Red30,
    onErrorContainer = Red80,

    outline = Gray40,
    outlineVariant = Gray20,
    inverseSurface = Gray90,
    inverseOnSurface = DarkSurface,
    inversePrimary = Red30,

    surfaceTint = Red40
)

/**
 * Light Color Scheme - For users who prefer light mode
 */
private val XCamLightColorScheme = lightColorScheme(
    primary = Red40,
    onPrimary = OnRed,
    primaryContainer = Red80,
    onPrimaryContainer = Red20,

    secondary = Gray60,
    onSecondary = Color.White,
    secondaryContainer = Gray80,
    onSecondaryContainer = Gray20,

    tertiary = RecordingRed,
    onTertiary = OnRed,

    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),

    error = Color(0xFFB3261E),
    onError = Color.White,

    outline = Gray40,
    outlineVariant = Gray80
)

/**
 * Custom Shape System for XCam
 */
val XCamShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun XCamTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    // Set to false to always use our custom red theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> XCamDarkColorScheme
        else -> XCamLightColorScheme
    }

    // Update system bars to match theme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? android.app.Activity)?.window
            window?.let {
                WindowCompat.getInsetsController(it, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = XCamShapes,
        content = content
    )
}