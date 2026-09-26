package io.celox.xcam.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import io.celox.xcam.R
import io.celox.xcam.ui.icons.XIcons

/**
 * A tab of the navigation bar. [icon] is the resting (line) glyph, [selectedIcon] the solid one the
 * bar shows for the active tab — the Material navigation-bar convention.
 */
enum class Destination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: () -> ImageVector,
    val selectedIcon: () -> ImageVector,
) {
    RECORD("record", R.string.nav_record, { XIcons.VideocamOutlined }, { XIcons.Videocam }),
    VIDEOS("videos", R.string.nav_videos, { XIcons.VideoLibraryOutlined }, { XIcons.VideoLibrary }),
    SETTINGS("settings", R.string.nav_settings, { XIcons.SettingsOutlined }, { XIcons.Settings }),
    ;

    companion object {
        /** Position of [route] in the bar, or -1 for a route outside it (drives the slide direction). */
        fun tabIndexOf(route: String?): Int = entries.indexOfFirst { it.route == route }
    }
}

object Routes {
    const val ONBOARDING = "onboarding"
    const val PLAYER = "player/{id}"

    fun player(id: Long) = "player/$id"
}
