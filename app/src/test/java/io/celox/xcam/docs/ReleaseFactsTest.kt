package io.celox.xcam.docs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The signing certificate and the release notes are stated in several places — release workflow,
 * README, CHANGELOG, website. They must say the same thing, or a user checking a download is told to
 * compare against the wrong value.
 */
class ReleaseFactsTest {
    private val buildFile = File("build.gradle.kts").readText()
    private val version = Regex("""versionName = "([^"]+)"""").find(buildFile)!!.groupValues[1]

    private fun certsIn(text: String) =
        Regex("""\b[0-9a-f]{64}\b""").findAll(text.lowercase().replace(":", "")).map { it.value }.toSet()

    @Test
    fun `workflow, README, website and CHANGELOG name the same signing certificate`() {
        val workflow = Regex("""EXPECTED=([0-9a-f]{64})""").find(File("../.github/workflows/release.yml").readText())!!.groupValues[1]
        assertTrue("README", workflow in certsIn(File("../README.md").readText()))
        assertTrue("website", workflow in certsIn(File("../website/site.json").readText()))
        assertTrue("CHANGELOG", workflow in certsIn(File("../CHANGELOG.md").readText()))
    }

    @Test
    fun `the CHANGELOG has a dated section for the version being built`() {
        val changelog = File("../CHANGELOG.md").readText()
        assertTrue("no '## [$version] - YYYY-MM-DD' section", Regex("""(?m)^## \[${Regex.escape(version)}] - \d{4}-\d{2}-\d{2}$""").containsMatchIn(changelog))
    }

    @Test
    fun `the CHANGELOG lists versions newest first`() {
        val versions =
            Regex("""(?m)^## \[(\d+(?:\.\d+)+)]""").findAll(File("../CHANGELOG.md").readText())
                .map { m -> m.groupValues[1].split('.').map(String::toInt) }.toList()
        val sorted =
            versions.sortedWith { a, b ->
                (0 until maxOf(a.size, b.size)).map { (a.getOrElse(it) { 0 }).compareTo(b.getOrElse(it) { 0 }) }
                    .firstOrNull { it != 0 } ?: 0
            }.reversed()
        assertEquals(sorted, versions)
    }

    @Test
    fun `the release workflow checks the tag against versionName and ships xcam-vX_Y_Z_apk`() {
        val workflow = File("../.github/workflows/release.yml").readText()
        assertTrue(workflow.contains("does not match versionName"))
        assertTrue(workflow.contains("xcam-\${TAG}.apk"))
    }

    @Test
    fun `the website offers the APK the workflow publishes`() {
        val asset = Regex(""""asset": "([^"]+)"""").find(File("../website/site.json").readText())!!.groupValues[1]
        assertTrue("site.json asset regex '$asset' does not match the release file name", Regex(asset.replace("\\\\", "\\")).matches("xcam-v$version.apk"))
    }
}
