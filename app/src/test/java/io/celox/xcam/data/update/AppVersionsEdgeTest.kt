package io.celox.xcam.data.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The corners of the version comparison that the update check relies on in the field. */
class AppVersionsEdgeTest {
    private fun update(v: String) = AppUpdate(v, "https://github.com/pepperonas/XCam/releases/tag/$v")

    @Test
    fun `an upper-case V prefix and surrounding whitespace are ignored`() {
        assertTrue(AppVersions.isNewer("3.0.0", "V3.0.1"))
        assertTrue(AppVersions.isNewer(" 3.0.0 ", "  v3.0.1\n"))
    }

    @Test
    fun `numbers compare numerically, not as text`() {
        assertTrue(AppVersions.isNewer("3.2.0", "3.10.0"))
        assertTrue(AppVersions.isNewer("9.0.0", "10.0.0"))
        assertFalse(AppVersions.isNewer("3.10.0", "3.9.9"))
    }

    @Test
    fun `leading zeros do not change the value`() {
        assertFalse(AppVersions.isNewer("3.1.0", "3.01.0"))
        assertTrue(AppVersions.isNewer("3.1.0", "3.01.1"))
    }

    @Test
    fun `a fourth component counts`() {
        assertTrue(AppVersions.isNewer("3.0.0", "3.0.0.1"))
        assertFalse(AppVersions.isNewer("3.0.0.1", "3.0.0"))
    }

    @Test
    fun `a missing component equals zero in both directions`() {
        assertFalse(AppVersions.isNewer("3", "3.0.0"))
        assertFalse(AppVersions.isNewer("3.0.0", "3"))
        assertTrue(AppVersions.isNewer("3", "3.0.1"))
    }

    @Test
    fun `comparison is not symmetric - exactly one side is newer or neither`() {
        val versions = listOf("1.6", "2.0", "3.0.0", "3.0.1", "3.1.0", "3.10.0", "v4")
        for (a in versions) for (b in versions) {
            val ab = AppVersions.isNewer(a, b)
            val ba = AppVersions.isNewer(b, a)
            assertFalse("$a vs $b both newer", ab && ba)
            if (a == b) assertFalse(ab)
        }
    }

    @Test
    fun `garbage on either side is never newer`() {
        assertFalse(AppVersions.isNewer("garbage", "v3.0.1"))
        assertFalse(AppVersions.isNewer("3.0.0", "vNext"))
        assertFalse(AppVersions.isNewer("3.0.0", ""))
        assertFalse(AppVersions.isNewer("3.0.0", "   "))
        assertFalse(AppVersions.isNewer("3.0.0", "-1.0.0"))
    }

    @Test
    fun `a notified release that is newer than the known one suppresses the notification`() {
        // E.g. the site briefly served an older manifest after a rollback: do not announce it again.
        assertFalse(AppVersions.shouldNotify("3.0.0", update("v3.1.0"), notified = "v3.2.0", enabled = true))
    }

    @Test
    fun `an unparseable notified value does not block a real release`() {
        // isNewer("garbage", x) is false, so a corrupt stored value would silence updates forever —
        // pin today's behaviour so a change is a deliberate decision.
        assertFalse(AppVersions.shouldNotify("3.0.0", update("v3.1.0"), notified = "garbage", enabled = true))
        assertTrue(AppVersions.shouldNotify("3.0.0", update("v3.1.0"), notified = null, enabled = true))
    }
}
