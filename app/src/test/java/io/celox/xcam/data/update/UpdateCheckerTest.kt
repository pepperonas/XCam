package io.celox.xcam.data.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URI

class UpdateCheckerTest {
    @Test
    fun `all endpoints are https`() {
        listOf(UpdateChecker.SITE_LATEST_URL, UpdateChecker.GITHUB_LATEST_URL, UpdateChecker.WEBSITE_URL)
            .forEach { assertEquals("https", URI(it).scheme) }
    }

    @Test
    fun `the manifest is served by the product page the notification opens`() {
        assertEquals(URI(UpdateChecker.WEBSITE_URL).host, URI(UpdateChecker.SITE_LATEST_URL).host)
        assertEquals("/latest.json", URI(UpdateChecker.SITE_LATEST_URL).path)
    }

    @Test
    fun `the fallback asks GitHub for this repository's latest release`() {
        val uri = URI(UpdateChecker.GITHUB_LATEST_URL)
        assertEquals("api.github.com", uri.host)
        assertTrue(uri.path.endsWith("/repos/pepperonas/XCam/releases/latest"))
    }

    @Test
    fun `the update channel is separate from the recording channel`() {
        assertTrue(UpdateNotifier.CHANNEL_ID != io.celox.xcam.util.Constants.NOTIFICATION_CHANNEL_ID)
    }
}
