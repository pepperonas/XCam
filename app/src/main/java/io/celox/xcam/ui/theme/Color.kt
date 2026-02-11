package io.celox.xcam.ui.theme

import androidx.compose.ui.graphics.Color

// =============================================================================
// XCam Professional Color Palette - Dark & Amber Theme
// =============================================================================

// Primary Colors - Amber/Orange (Recording/Camera Theme)
val Amber80 = Color(0xFFFFE0B2)            // Light theme primary
val Amber60 = Color(0xFFFFAB40)            // Medium amber
val Amber40 = Color(0xFFFF9100)            // Dark theme primary
val Amber30 = Color(0xFFE65100)            // Darker amber for containers
val Amber20 = Color(0xFFBF360C)            // Deep amber

// Recording Accent Colors
val RecordingAmber = Color(0xFFFF9100)     // Bright recording indicator (amber)
val RecordingAmberGlow = Color(0x40FF9100) // Glow effect (25% alpha)
val RecordingAmberPulse = Color(0xFFFFAB40) // Pulse animation color
val RecordingRed = Color(0xFFFF1744)       // Recording active dot (universal red)

// Glassmorphism Colors
val SurfaceGlass = Color(0x1AFFFFFF)       // 10% white for glass effect
val SurfaceGlassStroke = Color(0x33FFFFFF) // 20% white for glass border

// Surface Colors - Dark Theme
val DarkBackground = Color(0xFF0A0A0A)     // Almost black background
val DarkSurface = Color(0xFF121212)        // Main surface color
val DarkSurfaceVariant = Color(0xFF1E1E1E) // Elevated surfaces
val DarkSurfaceHigh = Color(0xFF2D2D2D)    // Highest elevation

// Neutral/Gray Colors
val Gray90 = Color(0xFFE8E8E8)             // Lightest gray (text on dark)
val Gray80 = Color(0xFFBDBDBD)             // Secondary text
val Gray60 = Color(0xFF757575)             // Disabled/hint text
val Gray40 = Color(0xFF424242)             // Borders
val Gray20 = Color(0xFF212121)             // Dark gray

// Semantic Colors
val SuccessGreen = Color(0xFF4CAF50)       // Success states
val WarningAmber = Color(0xFFFFC107)       // Warning states
val ErrorRed = Color(0xFFCF6679)           // Error states (softer for dark theme)
val InfoBlue = Color(0xFF03A9F4)           // Info states

// On-Colors (Text/Icons on colored backgrounds)
val OnDarkSurface = Color(0xFFE8E8E8)      // White text on dark surfaces
val OnDarkSurfaceVariant = Color(0xFFBDBDBD) // Secondary text
val OnAmber = Color(0xFF000000)            // Black text on amber (better contrast)
