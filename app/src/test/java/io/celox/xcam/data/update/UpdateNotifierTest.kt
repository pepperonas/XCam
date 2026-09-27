package io.celox.xcam.data.update

import android.Manifest
import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UpdateNotifierTest {
    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val manager = app.getSystemService(NotificationManager::class.java)
    private val update = AppUpdate("v3.2.0", "https://github.com/pepperonas/XCam/releases/tag/v3.2.0")

    @Test
    fun `the update channel is created with its own id`() {
        UpdateNotifier.ensureChannel(app)
        val channel = manager.getNotificationChannel(UpdateNotifier.CHANNEL_ID)
        assertNotNull(channel)
        assertEquals(NotificationManager.IMPORTANCE_DEFAULT, channel.importance)
    }

    @Test
    fun `without the notification permission nothing is posted and it reports false`() {
        shadowOf(app).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)
        assertFalse(UpdateNotifier.show(app, update))
        assertTrue(shadowOf(manager).allNotifications.isEmpty())
    }

    @Test
    fun `with permission it posts one notification naming the version without the v`() {
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        assertTrue(UpdateNotifier.show(app, update))
        val n = shadowOf(manager).allNotifications.single()
        assertEquals(UpdateNotifier.CHANNEL_ID, n.channelId)
        val title = n.extras.getCharSequence(Notification.EXTRA_TITLE).toString()
        assertTrue(title, title.contains("3.2.0") && !title.contains("v3.2.0"))
        assertTrue(n.flags and Notification.FLAG_AUTO_CANCEL != 0)
    }

    @Test
    fun `a tap opens the product page, the action opens the release notes`() {
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        UpdateNotifier.show(app, update)
        val n = shadowOf(manager).allNotifications.single()
        val tap = shadowOf(n.contentIntent).savedIntent
        assertEquals(Intent.ACTION_VIEW, tap.action)
        assertEquals(Uri.parse(UpdateChecker.WEBSITE_URL), tap.data)
        val action = n.actions.single()
        assertEquals(Uri.parse(update.notesUrl), shadowOf(action.actionIntent).savedIntent.data)
    }

    @Test
    fun `showing the same release twice replaces the notification instead of stacking`() {
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        UpdateNotifier.show(app, update)
        UpdateNotifier.show(app, update)
        assertEquals(1, shadowOf(manager).allNotifications.size)
    }
}
