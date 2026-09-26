package io.celox.xcam.res

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.math.hypot

/**
 * Pins the launcher icon's contract: an adaptive icon with a separate monochrome layer (themed icons
 * on Android 13+), and a foreground that is one evenOdd path — the camera is a *hole* — which stays
 * inside the 66 dp safe zone so no launcher mask can clip it.
 */
class LauncherIconTest {
    private val res = File("src/main/res")

    private fun read(path: String) = File(res, path).readText()

    private fun pathData(xml: String) = Regex("""android:pathData="([^"]+)"""").find(xml)!!.groupValues[1]

    @Test
    fun `adaptive icons declare background, foreground and monochrome`() {
        listOf("mipmap-anydpi-v26/ic_launcher.xml", "mipmap-anydpi-v26/ic_launcher_round.xml").forEach {
            val xml = read(it)
            assertTrue(it, "@drawable/ic_launcher_background" in xml)
            assertTrue(it, "@drawable/ic_launcher_foreground" in xml)
            assertTrue(it, "<monochrome android:drawable=\"@drawable/ic_launcher_monochrome\"" in xml)
        }
    }

    @Test
    fun `foreground is one evenOdd path on the 108 viewport`() {
        val xml = read("drawable/ic_launcher_foreground.xml")
        assertTrue("android:viewportWidth=\"108\"" in xml)
        assertTrue("android:fillType=\"evenOdd\"" in xml)
        assertEquals(1, Regex("<path").findAll(xml).count())
        // Body + camera body + lens wedge + record dot = at least four subpaths.
        assertTrue(Regex("[Mm]").findAll(pathData(xml)).count() >= 4)
    }

    @Test
    fun `monochrome layer draws exactly the foreground geometry`() {
        assertEquals(
            pathData(read("drawable/ic_launcher_foreground.xml")),
            pathData(read("drawable/ic_launcher_monochrome.xml")),
        )
    }

    @Test
    fun `every point of the mark stays inside the safe zone`() {
        val points = absolutePoints(pathData(read("drawable/ic_launcher_foreground.xml")))
        assertTrue(points.size > 20)
        val worst = points.maxOf { (x, y) -> hypot(x - 54.0, y - 54.0) }
        assertTrue("mark reaches radius $worst", worst <= 33.0)
    }

    /**
     * End and control points of an absolute-command path (the generator only emits M, L, H, V, C, Q,
     * A and Z in upper case). Arc radii and flags are skipped; only the arc end point counts.
     */
    private fun absolutePoints(d: String): List<Pair<Double, Double>> {
        val tokens = Regex("""[A-Za-z]|-?\d*\.?\d+""").findAll(d).map { it.value }.toList()
        val out = mutableListOf<Pair<Double, Double>>()
        var i = 0
        var cmd = 'M'
        var x = 0.0
        var y = 0.0
        while (i < tokens.size) {
            val t = tokens[i]
            if (t[0].isLetter()) {
                cmd = t[0]
                assertTrue("relative command $cmd", cmd.isUpperCase())
                i++
                if (cmd == 'Z') continue
            }
            when (cmd) {
                'M', 'L' -> {
                    x = tokens[i].toDouble()
                    y = tokens[i + 1].toDouble()
                    out += x to y
                    i += 2
                }
                'H' -> {
                    x = tokens[i++].toDouble()
                    out += x to y
                }
                'V' -> {
                    y = tokens[i++].toDouble()
                    out += x to y
                }
                'C' -> {
                    for (k in 0 until 3) out += tokens[i + 2 * k].toDouble() to tokens[i + 2 * k + 1].toDouble()
                    x = tokens[i + 4].toDouble()
                    y = tokens[i + 5].toDouble()
                    i += 6
                }
                'Q' -> {
                    for (k in 0 until 2) out += tokens[i + 2 * k].toDouble() to tokens[i + 2 * k + 1].toDouble()
                    x = tokens[i + 2].toDouble()
                    y = tokens[i + 3].toDouble()
                    i += 4
                }
                'A' -> {
                    // rx ry rotation large-arc sweep x y — the flags may be written as "1,1" or fused.
                    x = tokens[i + 5].toDouble()
                    y = tokens[i + 6].toDouble()
                    out += x to y
                    i += 7
                }
                else -> error("unexpected command $cmd")
            }
        }
        return out
    }
}
