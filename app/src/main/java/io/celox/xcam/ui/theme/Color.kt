package io.celox.xcam.ui.theme

import androidx.compose.ui.graphics.Color

// =============================================================================
// XCam Professional Color Palette - Dark & Red Theme
// =============================================================================

// Primary Colors - Rot (Recording/Camera Theme)
val Red80 = Color(0xFFFFB3B3)           // Light theme primary
val Red60 = Color(0xFFEF5350)           // Medium red
val Red40 = Color(0xFFE53935)           // Dark theme primary (Kräftiges Rot)
val Red30 = Color(0xFFB71C1C)           // Darker red for containers
val Red20 = Color(0xFF7F0000)           // Deep red

// Recording Accent Colors
val RecordingRed = Color(0xFFFF1744)    // Bright recording indicator
val RecordingRedGlow = Color(0x40FF1744) // Glow effect (25% alpha)
val RecordingRedPulse = Color(0xFFFF5252) // Pulse animation color

// Surface Colors - Dark Theme
val DarkBackground = Color(0xFF0A0A0A)   // Almost black background
val DarkSurface = Color(0xFF121212)      // Main surface color
val DarkSurfaceVariant = Color(0xFF1E1E1E) // Elevated surfaces
val DarkSurfaceHigh = Color(0xFF2D2D2D)  // Highest elevation

// Neutral/Gray Colors
val Gray90 = Color(0xFFE8E8E8)           // Lightest gray (text on dark)
val Gray80 = Color(0xFFBDBDBD)           // Secondary text
val Gray60 = Color(0xFF757575)           // Disabled/hint text
val Gray40 = Color(0xFF424242)           // Borders
val Gray20 = Color(0xFF212121)           // Dark gray

// Semantic Colors
val SuccessGreen = Color(0xFF4CAF50)     // Success states
val WarningAmber = Color(0xFFFFC107)     // Warning states
val ErrorRed = Color(0xFFCF6679)         // Error states (softer for dark theme)
val InfoBlue = Color(0xFF03A9F4)         // Info states

// On-Colors (Text/Icons on colored backgrounds)
val OnDarkSurface = Color(0xFFE8E8E8)    // White text on dark surfaces
val OnDarkSurfaceVariant = Color(0xFFBDBDBD) // Secondary text
val OnRed = Color(0xFFFFFFFF)            // White text on red

// Legacy colors (kept for compatibility)
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)