package io.celox.xcam.data.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// org.json is part of Android, not of the JVM stub jar: Robolectric provides the real one.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ReleaseParserTest {
    @Test
    fun `parses the product page latest json`() {
        val body =
            """{"version":"v3.1.0","published":"2026-10-01T10:00:00Z",
               "notes":"https://github.com/pepperonas/XCam/releases/tag/v3.1.0","assets":[]}"""
        assertEquals(
            AppUpdate("v3.1.0", "https://github.com/pepperonas/XCam/releases/tag/v3.1.0"),
            ReleaseParser.parseSite(body),
        )
    }

    @Test
    fun `site json without version or with a non-https notes link is rejected`() {
        assertNull(ReleaseParser.parseSite("""{"notes":"https://x"}"""))
        assertNull(ReleaseParser.parseSite("""{"version":"v3.1.0","notes":"javascript:alert(1)"}"""))
        assertNull(ReleaseParser.parseSite("not json"))
        assertNull(ReleaseParser.parseSite(""))
    }

    @Test
    fun `parses the GitHub releases latest response`() {
        val body =
            """{"tag_name":"v3.1.0","html_url":"https://github.com/pepperonas/XCam/releases/tag/v3.1.0",
               "draft":false,"prerelease":false}"""
        assertEquals(
            AppUpdate("v3.1.0", "https://github.com/pepperonas/XCam/releases/tag/v3.1.0"),
            ReleaseParser.parseGitHub(body),
        )
    }

    @Test
    fun `drafts and prereleases are never offered`() {
        assertNull(ReleaseParser.parseGitHub("""{"tag_name":"v4.0.0","html_url":"https://x","draft":true}"""))
        assertNull(ReleaseParser.parseGitHub("""{"tag_name":"v4.0.0","html_url":"https://x","prerelease":true}"""))
    }
}
