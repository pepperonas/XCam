package io.celox.xcam.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import io.celox.xcam.data.model.ThemeMode

/**
 * Material 3 **Expressive** theme: the spring-based [MotionScheme.expressive] physics (every
 * component and every custom animation in the app reads `MaterialTheme.motionScheme`), the XCam
 * brand colours, and — opt-in — the wallpaper colours (Material You). minSdk 33, so dynamic colour
 * is always available.
 */
@Composable
fun XCamTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val dark = isDarkTheme(themeMode)
    val context = LocalContext.current
    val colorScheme: ColorScheme =
        when {
            dynamicColor -> if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            dark -> XCamDarkColors
            else -> XCamLightColors
        }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        shapes = XCamShapes,
        content = content,
    )
}

/**
 * Whether the app renders dark for [themeMode]. Also drives the system-bar icon colour in
 * `MainActivity`: the bar icons must follow the *app's* choice, not the OS.
 */
@Composable
fun isDarkTheme(themeMode: ThemeMode): Boolean =
    when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
