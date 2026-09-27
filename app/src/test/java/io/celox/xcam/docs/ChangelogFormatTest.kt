package io.celox.xcam.docs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.LocalDate

/** The CHANGELOG is the release notes: its shape is what `scripts/release-notes.sh` cuts on. */
class ChangelogFormatTest {
    private val lines = File("../CHANGELOG.md").readLines()
    private val headings = lines.filter { it.startsWith("## ") }
    private val released = Regex("""^## \[(\d+(?:\.\d+)+)] - (\d{4}-\d{2}-\d{2})$""")

    @Test
    fun `it starts with Unreleased, then released versions`() {
        assertEquals("# Changelog", lines.first())
        assertEquals("## [Unreleased]", headings.first())
    }

    @Test
    fun `every release heading is a version in brackets and an ISO date`() {
        headings.drop(1).forEach { assertTrue("bad heading: $it", released.matches(it)) }
    }

    @Test
    fun `release dates are real dates and never go forward down the file`() {
        val dates = headings.drop(1).map { LocalDate.parse(released.find(it)!!.groupValues[2]) }
        assertEquals(dates.sortedDescending(), dates)
    }

    @Test
    fun `versions are unique`() {
        val versions = headings.drop(1).map { released.find(it)!!.groupValues[1] }
        assertEquals(versions.size, versions.toSet().size)
    }

    @Test
    fun `every release section has content`() {
        val idx = lines.indices.filter { lines[it].startsWith("## [") && !lines[it].contains("Unreleased") }
        idx.forEach { start ->
            val end = lines.indices.firstOrNull { it > start && lines[it].startsWith("## ") } ?: lines.size
            assertTrue("empty: ${lines[start]}", lines.subList(start + 1, end).any { it.isNotBlank() })
        }
    }

    @Test
    fun `semantic versioning from 3 on - three numeric parts`() {
        headings.drop(1).map { released.find(it)!!.groupValues[1] }
            .filter { it.substringBefore('.').toInt() >= 3 }
            .forEach { assertEquals(it, 3, it.split('.').size) }
    }
}
