package io.celox.xcam.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** The M3 Expressive corner scale. Cards use extraLarge (28), thumbnails large (16). */
val XCamShapes =
    Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(16.dp),
        extraLarge = RoundedCornerShape(28.dp),
    )

/** "Large increased" (20 dp) — banners and inline notices. */
val BannerShape = RoundedCornerShape(20.dp)
