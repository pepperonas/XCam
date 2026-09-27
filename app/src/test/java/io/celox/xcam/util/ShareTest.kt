package io.celox.xcam.util

import android.app.Activity
import android.content.Intent
import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ShareTest {
    private val activity = Robolectric.buildActivity(Activity::class.java).setup().get()

    private fun uri(id: Int) = Uri.parse("content://media/external/video/media/$id")

    private fun shared(uris: List<Uri>): Intent {
        shareVideos(activity, uris)
        val chooser = shadowOf(activity).nextStartedActivity
        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        return chooser.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)!!
    }

    @Test
    fun `nothing selected opens nothing`() {
        shareVideos(activity, emptyList())
        assertNull(shadowOf(activity).nextStartedActivity)
    }

    @Test
    fun `one video is sent as a single stream`() {
        val intent = shared(listOf(uri(1)))
        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals(Constants.VIDEO_MIME_TYPE, intent.type)
        assertEquals(uri(1), intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java))
    }

    @Test
    fun `several videos are sent as a list, in order`() {
        val uris = listOf(uri(1), uri(2), uri(3))
        val intent = shared(uris)
        assertEquals(Intent.ACTION_SEND_MULTIPLE, intent.action)
        assertEquals(uris, intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java))
    }

    @Test
    fun `the receiver may read every shared uri, not just the first`() {
        val uris = listOf(uri(1), uri(2), uri(3))
        val intent = shared(uris)
        assertTrue(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
        val clip = assertNotNull(intent.clipData).let { intent.clipData!! }
        assertEquals(uris, (0 until clip.itemCount).map { clip.getItemAt(it).uri })
    }
}
