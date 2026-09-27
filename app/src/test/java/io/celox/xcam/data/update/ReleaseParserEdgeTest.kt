package io.celox.xcam.data.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ReleaseParserEdgeTest {
    private val notes = "https://github.com/pepperonas/XCam/releases/tag/v3.2.0"

    @Test
    fun `unknown extra fields in the site manifest are ignored`() {
        val body = """{"version":"v3.2.0","notes":"$notes","sha256":"ab","size":123,"assets":[{"name":"x"}]}"""
        assertEquals(AppUpdate("v3.2.0", notes), ReleaseParser.parseSite(body))
    }

    @Test
    fun `a blank or non-string version in the site manifest is rejected`() {
        assertNull(ReleaseParser.parseSite("""{"version":"   ","notes":"$notes"}"""))
        assertNull(ReleaseParser.parseSite("""{"version":null,"notes":"$notes"}"""))
    }

    @Test
    fun `plain http and other schemes are rejected in both sources`() {
        assertNull(ReleaseParser.parseSite("""{"version":"v3.2.0","notes":"http://example.com"}"""))
        assertNull(ReleaseParser.parseSite("""{"version":"v3.2.0","notes":"intent://x"}"""))
        assertNull(ReleaseParser.parseGitHub("""{"tag_name":"v3.2.0","html_url":"http://github.com/x"}"""))
        assertNull(ReleaseParser.parseGitHub("""{"tag_name":"v3.2.0","html_url":"file:///sdcard/x"}"""))
    }

    @Test
    fun `a GitHub response without tag or url is rejected`() {
        assertNull(ReleaseParser.parseGitHub("""{"html_url":"$notes"}"""))
        assertNull(ReleaseParser.parseGitHub("""{"tag_name":"v3.2.0"}"""))
        assertNull(ReleaseParser.parseGitHub("""{"tag_name":"","html_url":"$notes"}"""))
    }

    @Test
    fun `a GitHub error body is not a release`() {
        assertNull(ReleaseParser.parseGitHub("""{"message":"API rate limit exceeded","documentation_url":"https://docs.github.com"}"""))
        assertNull(ReleaseParser.parseGitHub("""{"message":"Not Found"}"""))
    }

    @Test
    fun `a json array or truncated body is not a release`() {
        assertNull(ReleaseParser.parseSite("""[{"version":"v3.2.0"}]"""))
        assertNull(ReleaseParser.parseGitHub("""{"tag_name":"v3.2.0","html_url":"$notes""""))
        assertNull(ReleaseParser.parseGitHub("<html>502 Bad Gateway</html>"))
    }

    @Test
    fun `explicit false draft and prerelease flags are accepted`() {
        val body = """{"tag_name":"v3.2.0","html_url":"$notes","draft":false,"prerelease":false,"name":"XCam 3.2.0"}"""
        assertEquals(AppUpdate("v3.2.0", notes), ReleaseParser.parseGitHub(body))
    }

    @Test
    fun `the version string is passed on unchanged`() {
        assertEquals("3.2.0", ReleaseParser.parseSite("""{"version":"3.2.0","notes":"$notes"}""")?.version)
    }
}
