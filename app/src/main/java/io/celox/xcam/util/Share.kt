package io.celox.xcam.util

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import io.celox.xcam.R

/**
 * Shares recordings through the system sheet. MediaStore content URIs can be handed out directly —
 * no FileProvider — as long as the receiving app is granted read access, which the ClipData +
 * FLAG_GRANT_READ_URI_PERMISSION pair does for every URI (EXTRA_STREAM alone only covers the first).
 */
fun shareVideos(
    context: Context,
    uris: List<Uri>,
) {
    if (uris.isEmpty()) return
    val intent =
        if (uris.size == 1) {
            Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_STREAM, uris.first())
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
        }.apply {
            type = Constants.VIDEO_MIME_TYPE
            clipData =
                ClipData.newRawUri(null, uris.first()).also { clip ->
                    uris.drop(1).forEach { clip.addItem(ClipData.Item(it)) }
                }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_chooser_title)))
}
