package io.celox.xcam.res

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/** Every English string has a German translation with the same format arguments, and vice versa. */
class StringsParityTest {
    private data class Res(val name: String, val values: List<String>)

    private fun load(dir: String): Map<String, Res> {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(File("src/main/res/$dir/strings.xml"))
        val out = mutableMapOf<String, Res>()
        val strings = doc.getElementsByTagName("string")
        for (i in 0 until strings.length) {
            val e = strings.item(i) as Element
            if (e.getAttribute("translatable") == "false") continue
            out[e.getAttribute("name")] = Res(e.getAttribute("name"), listOf(e.textContent))
        }
        val plurals = doc.getElementsByTagName("plurals")
        for (i in 0 until plurals.length) {
            val e = plurals.item(i) as Element
            val items = e.getElementsByTagName("item")
            out[e.getAttribute("name")] = Res(e.getAttribute("name"), (0 until items.length).map { items.item(it).textContent })
        }
        return out
    }

    private val en = load("values")
    private val de = load("values-de")

    @Test
    fun `german covers exactly the translatable english keys`() {
        assertEquals("missing in values-de", emptySet<String>(), en.keys - de.keys)
        assertEquals("only in values-de", emptySet<String>(), de.keys - en.keys)
    }

    @Test
    fun `format arguments match between languages`() {
        val arg = Regex("""%(\d+\$)?[-#+ 0,(]*\d*(\.\d+)?[sdf]""")
        en.forEach { (key, res) ->
            val enArgs = res.values.flatMap { v -> arg.findAll(v).map { it.value } }.toSet()
            val deArgs = de.getValue(key).values.flatMap { v -> arg.findAll(v).map { it.value } }.toSet()
            assertEquals("format args of $key", enArgs, deArgs)
        }
    }

    @Test
    fun `no untranslated english slipped into german`() {
        // Identical text is allowed only where the word is the same in both languages.
        val sameOnPurpose = setOf("nav_videos", "videos_meta", "videos_size_mb", "theme_system", "player_pause",
            "quality_720_long", "quality_1080_long", "quality_4k_long", "setting_version", "setting_website")
        val suspicious = en.keys.filter { it !in sameOnPurpose && en[it] == de[it] }
        assertTrue("untranslated: $suspicious", suspicious.isEmpty())
    }
}
