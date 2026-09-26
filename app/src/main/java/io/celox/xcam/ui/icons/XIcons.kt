package io.celox.xcam.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * The app's icon set, built from Material icon path data (Apache 2.0) instead of pulling in the
 * ~10 MB material-icons-extended artifact. Every icon is a 24 dp [ImageVector] tinted by the caller
 * (`Icon` applies `LocalContentColor`).
 *
 * Outlined variants for the navigation bar are drawn by *stroking* the filled glyph rather than
 * shipping a second path — the unselected tab reads as a line icon, the selected one as solid,
 * which is the Material navigation convention.
 */
object XIcons {
    val Videocam by lazy { filled("Videocam", VIDEOCAM) }
    val VideocamOutlined by lazy { outlined("VideocamOutlined", VIDEOCAM) }
    val VideoLibrary by lazy { filled("VideoLibrary", VIDEO_LIBRARY) }
    val VideoLibraryOutlined by lazy { outlined("VideoLibraryOutlined", VIDEO_LIBRARY) }
    val Settings by lazy { filled("Settings", SETTINGS, evenOdd = true) }
    val SettingsOutlined by lazy { outlined("SettingsOutlined", SETTINGS) }

    val ArrowBack by lazy { filled("ArrowBack", "M20,11H7.83l5.59,-5.59L12,4l-8,8 8,8 1.41,-1.41L7.83,13H20v-2z") }
    val Close by lazy {
        filled("Close", "M19,6.41L17.59,5 12,10.59 6.41,5 5,6.41 10.59,12 5,17.59 6.41,19 12,13.41 17.59,19 19,17.59 13.41,12z")
    }
    val Check by lazy { filled("Check", "M9,16.17L4.83,12l-1.42,1.41L9,19 21,7l-1.41,-1.41z") }
    val CheckCircle by lazy {
        filled(
            "CheckCircle",
            "M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM10,17l-5,-5 1.41,-1.41L10,14.17l7.59,-7.59L19,8l-9,9z",
        )
    }
    val Circle by lazy {
        filled(
            "Circle",
            "M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM12,20c-4.42,0 -8,-3.58 -8,-8s3.58,-8 8,-8 8,3.58 8,8 -3.58,8 -8,8z",
        )
    }
    val PlayArrow by lazy { filled("PlayArrow", "M8,5v14l11,-7z") }
    val Pause by lazy { filled("Pause", "M6,19h4V5H6v14zm8,-14v14h4V5h-4z") }
    val Share by lazy {
        filled(
            "Share",
            "M18,16.08c-0.76,0 -1.44,0.3 -1.96,0.77L8.91,12.7c0.05,-0.23 0.09,-0.46 0.09,-0.7s-0.04,-0.47 " +
                "-0.09,-0.7l7.05,-4.11c0.54,0.5 1.25,0.81 2.04,0.81 1.66,0 3,-1.34 3,-3s-1.34,-3 -3,-3 -3,1.34 -3,3c0,0.24 " +
                "0.04,0.47 0.09,0.7L8.04,9.81C7.5,9.31 6.79,9 6,9c-1.66,0 -3,1.34 -3,3s1.34,3 3,3c0.79,0 1.5,-0.31 " +
                "2.04,-0.81l7.12,4.16c-0.05,0.21 -0.08,0.43 -0.08,0.65 0,1.61 1.31,2.92 2.92,2.92 1.61,0 2.92,-1.31 " +
                "2.92,-2.92s-1.31,-2.92 -2.92,-2.92z",
        )
    }
    val Delete by lazy {
        filled(
            "Delete",
            "M16,9v10H8V9h8m-1.5,-6h-5l-1,1H5v2h14V4h-3.5l-1,-1zM18,7H6v12c0,1.1 0.9,2 2,2h8c1.1,0 2,-0.9 2,-2V7z",
        )
    }
    val MoreVert by lazy {
        filled(
            "MoreVert",
            "M12,8c1.1,0 2,-0.9 2,-2s-0.9,-2 -2,-2 -2,0.9 -2,2 0.9,2 2,2zM12,10c-1.1,0 -2,0.9 -2,2s0.9,2 2,2 " +
                "2,-0.9 2,-2 -0.9,-2 -2,-2zM12,16c-1.1,0 -2,0.9 -2,2s0.9,2 2,2 2,-0.9 2,-2 -0.9,-2 -2,-2z",
        )
    }
    val Mic by lazy {
        filled(
            "Mic",
            "M12,14c1.66,0 2.99,-1.34 2.99,-3L15,5c0,-1.66 -1.34,-3 -3,-3S9,3.34 9,5v6c0,1.66 1.34,3 3,3zM17.3,11c0,3 " +
                "-2.54,5.1 -5.3,5.1S6.7,14 6.7,11H5c0,3.41 2.72,6.23 6,6.72V21h2v-3.28c3.28,-0.48 6,-3.3 6,-6.72h-1.7z",
        )
    }
    val MicOff by lazy {
        filled(
            "MicOff",
            "M19,11h-1.7c0,0.74 -0.16,1.43 -0.43,2.05l1.23,1.23c0.56,-0.98 0.9,-2.09 0.9,-3.28zM14.98,11.17c0,-0.06 " +
                "0.02,-0.11 0.02,-0.17V5c0,-1.66 -1.34,-3 -3,-3S9,3.34 9,5v0.18l5.98,5.99zM4.27,3L3,4.27l6.01,6.01V11c0,1.66 " +
                "1.33,3 2.99,3 0.22,0 0.44,-0.03 0.65,-0.08l1.66,1.66c-0.71,0.33 -1.5,0.52 -2.31,0.52 -2.76,0 -5.3,-2.1 " +
                "-5.3,-5.1H5c0,3.41 2.72,6.23 6,6.72V21h2v-3.28c0.91,-0.13 1.77,-0.45 2.54,-0.9L19.73,21 21,19.73 4.27,3z",
        )
    }
    val CameraSwitch by lazy {
        filled(
            "CameraSwitch",
            "M16,7h-1l-1,-1h-4L9,7H8C6.9,7 6,7.9 6,9v6c0,1.1 0.9,2 2,2h8c1.1,0 2,-0.9 2,-2V9C18,7.9 17.1,7 16,7z" +
                "M12,14c-1.1,0 -2,-0.9 -2,-2c0,-1.1 0.9,-2 2,-2s2,0.9 2,2C14,13.1 13.1,14 12,14z" +
                "M8.57,0.51l4.48,4.48V2.04c4.72,0.47 8.48,4.23 8.95,8.95c0.05,0.52 0.48,0.91 1,0.91c0.59,0 1.05,-0.51 " +
                "1,-1.09C22.94,4.53 17.65,0 12.05,0C10.8,0 9.64,0.2 8.57,0.51z" +
                "M15.43,23.49l-4.48,-4.48v2.95c-4.72,-0.47 -8.48,-4.23 -8.95,-8.95C1.95,12.49 1.52,12.1 1,12.1c-0.59,0 " +
                "-1.05,0.51 -1,1.09C0.56,19.47 5.85,24 11.95,24C13.2,24 14.36,23.8 15.43,23.49z",
        )
    }
    val Timer by lazy {
        filled(
            "Timer",
            "M15,1H9v2h6V1zM11,14h2V8h-2v6zM19.03,7.39l1.42,-1.42c-0.43,-0.51 -0.9,-0.99 -1.41,-1.41l-1.42,1.42C16.07,4.74 " +
                "14.12,4 12,4c-4.97,0 -9,4.03 -9,9s4.02,9 9,9 9,-4.03 9,-9c0,-2.12 -0.74,-4.07 -1.97,-5.61zM12,20c-3.87,0 " +
                "-7,-3.13 -7,-7s3.13,-7 7,-7 7,3.13 7,7 -3.13,7 -7,7z",
        )
    }
    val HighQuality by lazy {
        filled(
            "HighQuality",
            "M19,3H5c-1.11,0 -2,0.9 -2,2v14c0,1.1 0.89,2 2,2h14c1.1,0 2,-0.9 2,-2V5c0,-1.1 -0.9,-2 -2,-2zM11,15H9.5v-2h-2v2H6" +
                "V9h1.5v2.5h2V9H11v6zM13,9h4c0.55,0 1,0.45 1,1v4c0,0.55 -0.45,1 -1,1h-4V9zM14.5,13.5h2v-3h-2v3z",
            evenOdd = true,
        )
    }
    val Palette by lazy {
        filled(
            "Palette",
            "M12,3c-4.97,0 -9,4.03 -9,9s4.03,9 9,9c0.83,0 1.5,-0.67 1.5,-1.5 0,-0.39 -0.15,-0.74 -0.39,-1.01 -0.23,-0.26 " +
                "-0.38,-0.61 -0.38,-0.99 0,-0.83 0.67,-1.5 1.5,-1.5H16c2.76,0 5,-2.24 5,-5 0,-4.42 -4.03,-8 -9,-8z" +
                "M6.5,12c-0.83,0 -1.5,-0.67 -1.5,-1.5S5.67,9 6.5,9 8,9.67 8,10.5 7.33,12 6.5,12z" +
                "M9.5,8C8.67,8 8,7.33 8,6.5S8.67,5 9.5,5s1.5,0.67 1.5,1.5S10.33,8 9.5,8z" +
                "M14.5,8c-0.83,0 -1.5,-0.67 -1.5,-1.5S13.67,5 14.5,5s1.5,0.67 1.5,1.5S15.33,8 14.5,8z" +
                "M17.5,12c-0.83,0 -1.5,-0.67 -1.5,-1.5S16.67,9 17.5,9s1.5,0.67 1.5,1.5 -0.67,1.5 -1.5,1.5z",
            evenOdd = true,
        )
    }
    val DarkMode by lazy {
        filled(
            "DarkMode",
            "M12.34,2.02C6.59,1.82 2,6.42 2,12c0,5.52 4.48,10 10,10c3.71,0 6.93,-2.02 8.66,-5.02C13.15,16.73 8.57,8.55 12.34,2.02z",
        )
    }
    val Info by lazy {
        filled(
            "Info",
            "M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM13,17h-2v-6h2v6zM13,9h-2V7h2v2z",
            evenOdd = true,
        )
    }
    val Warning by lazy { filled("Warning", "M1,21h22L12,2 1,21zM13,18h-2v-2h2v2zM13,14h-2v-4h2v4z", evenOdd = true) }
    val Shield by lazy { filled("Shield", "M12,1L3,5v6c0,5.55 3.84,10.74 9,12 5.16,-1.26 9,-6.45 9,-12V5l-9,-4z") }
    val Notifications by lazy {
        filled(
            "Notifications",
            "M12,22c1.1,0 2,-0.9 2,-2h-4c0,1.1 0.89,2 2,2zM18,16v-5c0,-3.07 -1.64,-5.64 -4.5,-6.32V4c0,-0.83 -0.67,-1.5 " +
                "-1.5,-1.5s-1.5,0.67 -1.5,1.5v0.68C7.63,5.36 6,7.92 6,11v5l-2,2v1h16v-1l-2,-2z",
        )
    }
    val PhotoCamera by lazy {
        filled(
            "PhotoCamera",
            "M9,2L7.17,4H4c-1.1,0 -2,0.9 -2,2v12c0,1.1 0.9,2 2,2h16c1.1,0 2,-0.9 2,-2V6c0,-1.1 -0.9,-2 -2,-2h-3.17L15,2H9z" +
                "M12,17c-2.76,0 -5,-2.24 -5,-5s2.24,-5 5,-5 5,2.24 5,5 -2.24,5 -5,5z",
            evenOdd = true,
        )
    }
    val ChevronRight by lazy { filled("ChevronRight", "M10,6L8.59,7.41 13.17,12l-4.58,4.59L10,18l6,-6z") }
    val Refresh by lazy {
        filled(
            "Refresh",
            "M17.65,6.35C16.2,4.9 14.21,4 12,4c-4.42,0 -7.99,3.58 -7.99,8s3.57,8 7.99,8c3.73,0 6.84,-2.55 7.73,-6h-2.08" +
                "c-0.82,2.33 -3.04,4 -5.65,4 -3.31,0 -6,-2.69 -6,-6s2.69,-6 6,-6c1.66,0 3.14,0.69 4.22,1.78L13,11h7V4l-2.35,2.35z",
        )
    }

    // --- builders -------------------------------------------------------------------------------

    private fun filled(
        name: String,
        pathData: String,
        evenOdd: Boolean = false,
    ): ImageVector =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
            .addPath(
                pathData = PathParser().parsePathString(pathData).toNodes(),
                pathFillType = if (evenOdd) PathFillType.EvenOdd else PathFillType.NonZero,
                fill = SolidColor(Color.Black),
            ).build()

    /** The same glyph as a 1.6-unit line drawing (see the class doc). */
    private fun outlined(
        name: String,
        pathData: String,
    ): ImageVector =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
            .addPath(
                pathData = PathParser().parsePathString(pathData).toNodes(),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineJoin = StrokeJoin.Round,
            ).build()

    private const val VIDEOCAM =
        "M17,10.5V7c0,-0.55 -0.45,-1 -1,-1H4c-0.55,0 -1,0.45 -1,1v10c0,0.55 0.45,1 1,1h12c0.55,0 1,-0.45 1,-1v-3.5l4,4v-11l-4,4z"
    private const val VIDEO_LIBRARY =
        "M4,6H2v14c0,1.1 0.9,2 2,2h14v-2H4V6zM20,2H8c-1.1,0 -2,0.9 -2,2v12c0,1.1 0.9,2 2,2h12c1.1,0 2,-0.9 2,-2V4" +
            "c0,-1.1 -0.9,-2 -2,-2zM12,14.5v-9l6,4.5 -6,4.5z"
    private const val SETTINGS =
        "M19.14,12.94c0.04,-0.3 0.06,-0.61 0.06,-0.94c0,-0.32 -0.02,-0.64 -0.07,-0.94l2.03,-1.58c0.18,-0.14 0.23,-0.41 " +
            "0.12,-0.61l-1.92,-3.32c-0.12,-0.22 -0.37,-0.29 -0.59,-0.22l-2.39,0.96c-0.5,-0.38 -1.03,-0.7 -1.62,-0.94L14.4,2.81" +
            "c-0.04,-0.24 -0.24,-0.41 -0.48,-0.41h-3.84c-0.24,0 -0.43,0.17 -0.47,0.41L9.25,5.35C8.66,5.59 8.12,5.92 7.63,6.29" +
            "L5.24,5.33c-0.22,-0.08 -0.47,0 -0.59,0.22L2.74,8.87C2.62,9.08 2.66,9.34 2.86,9.48l2.03,1.58C4.84,11.36 4.8,11.69 " +
            "4.8,12s0.02,0.64 0.07,0.94l-2.03,1.58c-0.18,0.14 -0.23,0.41 -0.12,0.61l1.92,3.32c0.12,0.22 0.37,0.29 0.59,0.22" +
            "l2.39,-0.96c0.5,0.38 1.03,0.7 1.62,0.94l0.36,2.54c0.05,0.24 0.24,0.41 0.48,0.41h3.84c0.24,0 0.44,-0.17 0.47,-0.41" +
            "l0.36,-2.54c0.59,-0.24 1.13,-0.56 1.62,-0.94l2.39,0.96c0.22,0.08 0.47,0 0.59,-0.22l1.92,-3.32c0.12,-0.22 " +
            "0.07,-0.47 -0.12,-0.61L19.14,12.94zM12,15.6c-1.98,0 -3.6,-1.62 -3.6,-3.6s1.62,-3.6 3.6,-3.6s3.6,1.62 3.6,3.6" +
            "S13.98,15.6 12,15.6z"
}
