package io.celox.xcam.res

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class ResourcesTest {
    private fun parse(path: String) = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(File("src/main/res/$path"))

    private fun elements(path: String, tag: String) =
        parse(path).getElementsByTagName(tag).let { l -> (0 until l.length).map { l.item(it) as Element } }

    private val languages = listOf("values", "values-de")

    @Test
    fun `the per-app language list matches the translations that exist`() {
        val declared = elements("xml/locales_config.xml", "locale").map { it.getAttribute("android:name") }.toSet()
        assertEquals(setOf("en", "de"), declared)
        declared.filter { it != "en" }.forEach { assertTrue("values-$it", File("src/main/res/values-$it/strings.xml").exists()) }
    }

    @Test
    fun `every plural has one and other in every language`() {
        languages.forEach { dir ->
            elements("$dir/strings.xml", "plurals").forEach { p ->
                val items = p.getElementsByTagName("item").let { l -> (0 until l.length).map { (l.item(it) as Element).getAttribute("quantity") } }
                assertTrue("$dir ${p.getAttribute("name")}", "one" in items && "other" in items)
            }
        }
    }

    @Test
    fun `the plural other form carries the count wherever it shows a number`() {
        languages.forEach { dir ->
            elements("$dir/strings.xml", "plurals").forEach { p ->
                val items = p.getElementsByTagName("item")
                val other = (0 until items.length).map { items.item(it) as Element }.first { it.getAttribute("quantity") == "other" }
                assertTrue("$dir ${p.getAttribute("name")}: '${other.textContent}'", other.textContent.contains("%d"))
            }
        }
    }

    @Test
    fun `no string is empty`() {
        languages.forEach { dir ->
            elements("$dir/strings.xml", "string").forEach {
                assertTrue("$dir ${it.getAttribute("name")} is empty", it.textContent.isNotBlank())
            }
        }
    }

    @Test
    fun `string names are unique within a file`() {
        languages.forEach { dir ->
            val names = elements("$dir/strings.xml", "string").map { it.getAttribute("name") }
            assertEquals(dir, names.size, names.toSet().size)
        }
    }

    @Test
    fun `the app is called XCam in every language`() {
        val name = elements("values/strings.xml", "string").single { it.getAttribute("name") == "app_name" }
        assertEquals("XCam", name.textContent)
        val de = elements("values-de/strings.xml", "string").firstOrNull { it.getAttribute("name") == "app_name" }
        assertTrue(de == null || de.textContent == "XCam")
    }

    @Test
    fun `backup rules exist for both backup mechanisms referenced by the manifest`() {
        assertTrue(File("src/main/res/xml/backup_rules.xml").exists())
        assertTrue(File("src/main/res/xml/data_extraction_rules.xml").exists())
    }
}
