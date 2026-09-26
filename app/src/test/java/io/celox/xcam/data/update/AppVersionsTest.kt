package io.celox.xcam.data.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppVersionsTest {
    private fun update(v: String) = AppUpdate(v, "https://github.com/pepperonas/XCam/releases/tag/$v")

    @Test
    fun `a higher version is newer, with or without the v prefix`() {
        assertTrue(AppVersions.isNewer("3.0.0", "v3.0.1"))
        assertTrue(AppVersions.isNewer("3.0.0", "3.1"))
        assertTrue(AppVersions.isNewer("2.9.9", "v3.0.0"))
        assertTrue(AppVersions.isNewer("3.9.0", "3.10.0"))
    }

    @Test
    fun `same or older is not newer`() {
        assertFalse(AppVersions.isNewer("3.0.0", "v3.0.0"))
        assertFalse(AppVersions.isNewer("3.0", "3.0.0"))
        assertFalse(AppVersions.isNewer("3.1.0", "v3.0.9"))
    }

    @Test
    fun `suffixes are ignored and garbage is never newer`() {
        assertFalse(AppVersions.isNewer("3.0.0-debug", "v3.0.0"))
        assertTrue(AppVersions.isNewer("3.0.0 (10)", "v3.0.1"))
        assertFalse(AppVersions.isNewer("3.0.0", "latest"))
        assertFalse(AppVersions.isNewer("", "v3.0.1"))
    }

    @Test
    fun `notify only when enabled, newer, and not notified for this release yet`() {
        assertTrue(AppVersions.shouldNotify("3.0.0", update("v3.1.0"), notified = null, enabled = true))
        assertFalse(AppVersions.shouldNotify("3.0.0", update("v3.1.0"), notified = null, enabled = false))
        assertFalse(AppVersions.shouldNotify("3.1.0", update("v3.1.0"), notified = null, enabled = true))
        assertFalse(AppVersions.shouldNotify("3.0.0", update("v3.1.0"), notified = "v3.1.0", enabled = true))
        assertFalse(AppVersions.shouldNotify("3.0.0", null, notified = null, enabled = true))
    }

    @Test
    fun `a later release is announced even after an earlier one was`() {
        assertTrue(AppVersions.shouldNotify("3.0.0", update("v3.2.0"), notified = "v3.1.0", enabled = true))
    }
}
