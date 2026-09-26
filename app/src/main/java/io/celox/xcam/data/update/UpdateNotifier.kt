package io.celox.xcam.data.update

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import io.celox.xcam.R

/**
 * "XCam 3.1.0 is available" — on its own channel, so release news can be silenced without touching
 * the recording notification. A tap opens the product page (download button, changelog, checksum);
 * *What's new* opens the release notes.
 */
object UpdateNotifier {
    const val CHANNEL_ID = "app_updates"
    private const val NOTIFICATION_ID = 2001
    private const val REQUEST_WEBSITE = 1
    private const val REQUEST_NOTES = 2

    fun ensureChannel(context: Context) {
        val channel =
            NotificationChannel(CHANNEL_ID, context.getString(R.string.update_channel_name), NotificationManager.IMPORTANCE_DEFAULT)
                .apply { description = context.getString(R.string.update_channel_description) }
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    /** False when it could not be shown (no permission, notifications off) — then it is not recorded as sent. */
    fun show(
        context: Context,
        update: AppUpdate,
    ): Boolean {
        val manager = NotificationManagerCompat.from(context)
        val permitted =
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (!permitted || !manager.areNotificationsEnabled()) return false
        ensureChannel(context)
        val version = update.version.removePrefix("v")
        val text = context.getString(R.string.update_notification_text)
        val notification =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(ContextCompat.getColor(context, R.color.brand_primary))
                .setContentTitle(context.getString(R.string.update_notification_title, version))
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(viewIntent(context, UpdateChecker.WEBSITE_URL, REQUEST_WEBSITE))
                .addAction(0, context.getString(R.string.update_whats_new), viewIntent(context, update.notesUrl, REQUEST_NOTES))
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
                .build()
        return runCatching { manager.notify(NOTIFICATION_ID, notification) }.isSuccess
    }

    private fun viewIntent(
        context: Context,
        url: String,
        requestCode: Int,
    ): PendingIntent =
        PendingIntent.getActivity(
            context,
            requestCode,
            Intent(Intent.ACTION_VIEW, url.toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
}
