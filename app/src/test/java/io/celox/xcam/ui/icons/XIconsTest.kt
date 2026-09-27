package io.celox.xcam.ui.icons

import androidx.compose.ui.graphics.vector.ImageVector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * XIcons parses Material path data at runtime (to avoid the 10 MB icons-extended artifact). A typo in
 * one path string would only surface as a crash when that screen opens — so every icon is built here.
 */
class XIconsTest {
    private val icons: Map<String, ImageVector> =
        // Every `val X by lazy { … }` compiles to a public `getX()` — Java reflection, no kotlin-reflect needed.
        XIcons::class.java.declaredMethods
            .filter { it.returnType == ImageVector::class.java && it.name.startsWith("get") && it.parameterCount == 0 }
            .associate { it.name.removePrefix("get") to it.invoke(XIcons) as ImageVector }

    @Test
    fun `every icon builds on the 24 dp grid`() {
        assertTrue("found only ${icons.size} icons", icons.size >= 25)
        icons.forEach { (name, icon) ->
            assertEquals(name, 24f, icon.viewportWidth)
            assertEquals(name, 24f, icon.viewportHeight)
            assertTrue("$name has no path", icon.root.size > 0)
        }
    }

    @Test
    fun `navigation icons come in a resting and a selected form`() {
        listOf("Videocam", "VideoLibrary", "Settings").forEach { base ->
            assertTrue("$base missing", icons.containsKey(base))
            assertTrue("${base}Outlined missing", icons.containsKey("${base}Outlined"))
            assertTrue("$base forms are identical", icons.getValue(base) != icons.getValue("${base}Outlined"))
        }
    }

    @Test
    fun `icon names are unique`() {
        assertEquals(icons.size, icons.values.map { it.name }.toSet().size)
    }
}
