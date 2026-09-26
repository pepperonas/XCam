package io.celox.xcam.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/*
 * XCam colour scheme, generated once from the brand seed with material-color-utilities
 * via tools/color-scheme.mjs (not a runtime dependency).
 *
 *  - Accent roles (primary/secondary/tertiary/error + containers) come from SchemeVibrant — a
 *    saturated "record red" the app is recognisable by.
 *  - Neutral roles (background, surface*, outline, inverse) come from SchemeTonalSpot of the same
 *    seed: calmer surfaces, so a large dark screen does not glow red at night.
 *
 * Regenerate instead of hand-editing single values — the roles are tuned against each other for
 * contrast (on* roles meet WCAG AA on their container).
 */

/** The brand seed every role derives from. */
val XCamSeed = Color(0xFFE5484D)

val XCamLightColors = lightColorScheme(
    primary = Color(0xFFBF0025),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDAD8),
    onPrimaryContainer = Color(0xFF93001A),
    inversePrimary = Color(0xFFFFB3B0),
    secondary = Color(0xFF815342),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDBCF),
    onSecondaryContainer = Color(0xFF663C2D),
    tertiary = Color(0xFF865229),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDCC5),
    onTertiaryContainer = Color(0xFF6A3B13),
    surfaceTint = Color(0xFFBF0025),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    background = Color(0xFFFFF8F7),
    onBackground = Color(0xFF231919),
    surface = Color(0xFFFFF8F7),
    onSurface = Color(0xFF231919),
    surfaceVariant = Color(0xFFF4DDDC),
    onSurfaceVariant = Color(0xFF534342),
    inverseSurface = Color(0xFF382E2D),
    inverseOnSurface = Color(0xFFFFEDEB),
    outline = Color(0xFF857372),
    outlineVariant = Color(0xFFD7C1C0),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFFFFF8F7),
    surfaceContainer = Color(0xFFFCEAE8),
    surfaceContainerHigh = Color(0xFFF6E4E3),
    surfaceContainerHighest = Color(0xFFF0DEDD),
    surfaceContainerLow = Color(0xFFFFF0EF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFE8D6D5),
)

val XCamDarkColors = darkColorScheme(
    primary = Color(0xFFFFB3B0),
    onPrimary = Color(0xFF68000F),
    primaryContainer = Color(0xFF93001A),
    onPrimaryContainer = Color(0xFFFFDAD8),
    inversePrimary = Color(0xFFBF0025),
    secondary = Color(0xFFF5B9A4),
    onSecondary = Color(0xFF4C2618),
    secondaryContainer = Color(0xFF663C2D),
    onSecondaryContainer = Color(0xFFFFDBCF),
    tertiary = Color(0xFFFDB886),
    onTertiary = Color(0xFF4E2500),
    tertiaryContainer = Color(0xFF6A3B13),
    onTertiaryContainer = Color(0xFFFFDCC5),
    surfaceTint = Color(0xFFFFB3B0),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1A1111),
    onBackground = Color(0xFFF0DEDD),
    surface = Color(0xFF1A1111),
    onSurface = Color(0xFFF0DEDD),
    surfaceVariant = Color(0xFF534342),
    onSurfaceVariant = Color(0xFFD7C1C0),
    inverseSurface = Color(0xFFF0DEDD),
    inverseOnSurface = Color(0xFF382E2D),
    outline = Color(0xFFA08C8B),
    outlineVariant = Color(0xFF534342),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF423736),
    surfaceContainer = Color(0xFF271D1D),
    surfaceContainerHigh = Color(0xFF322827),
    surfaceContainerHighest = Color(0xFF3D3232),
    surfaceContainerLow = Color(0xFF231919),
    surfaceContainerLowest = Color(0xFF140C0C),
    surfaceDim = Color(0xFF1A1111),
)
