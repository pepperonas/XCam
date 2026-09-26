package io.celox.xcam.data

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.IntentSender
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import io.celox.xcam.data.model.VideoFile
import io.celox.xcam.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.withContext

/**
 * The recordings, read from MediaStore — not from the file system. CameraX writes through MediaStore,
 * so this is where the files are indexed with size and duration (no per-file MediaMetadataRetriever),
 * and the content [Uri]s it returns are what playing, sharing and deleting need.
 *
 * Without READ_MEDIA_VIDEO, MediaStore returns only the items this app created — exactly XCam's own
 * recordings, which is all the app needs.
 */
class VideoRepository(context: Context) {
    private val resolver: ContentResolver = context.applicationContext.contentResolver

    suspend fun load(): List<VideoFile> =
        withContext(Dispatchers.IO) {
            val projection =
                arrayOf(
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.SIZE,
                    MediaStore.Video.Media.DURATION,
                    MediaStore.Video.Media.DATE_TAKEN,
                    MediaStore.Video.Media.DATE_ADDED,
                )
            val selection = "${MediaStore.Video.Media.RELATIVE_PATH} LIKE ?"
            val args = arrayOf("${Constants.RELATIVE_VIDEO_PATH}%")
            val sort = "${MediaStore.Video.Media.DATE_ADDED} DESC"
            buildList {
                resolver.query(COLLECTION, projection, selection, args, sort)?.use { c ->
                    val id = c.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                    val name = c.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                    val size = c.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                    val duration = c.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                    val taken = c.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN)
                    val added = c.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                    while (c.moveToNext()) {
                        val videoId = c.getLong(id)
                        add(
                            VideoFile(
                                id = videoId,
                                uri = ContentUris.withAppendedId(COLLECTION, videoId),
                                name = c.getString(name) ?: "",
                                size = c.getLong(size),
                                duration = c.getLong(duration),
                                // DATE_TAKEN (ms) can be 0 for freshly written files; DATE_ADDED is seconds.
                                timestamp = c.getLong(taken).takeIf { it > 0 } ?: (c.getLong(added) * 1000L),
                            ),
                        )
                    }
                }
            }.sortedByDescending { it.timestamp }
        }

    /** Emits once whenever the video collection changes (a recording landed, something was deleted). */
    fun changes(): Flow<Unit> =
        callbackFlow {
            val observer =
                object : ContentObserver(Handler(Looper.getMainLooper())) {
                    override fun onChange(selfChange: Boolean) {
                        trySend(Unit)
                    }
                }
            resolver.registerContentObserver(COLLECTION, true, observer)
            awaitClose { resolver.unregisterContentObserver(observer) }
        }.conflate()

    sealed interface DeleteResult {
        data class Deleted(val count: Int) : DeleteResult

        /** Some items are not ours (e.g. after a reinstall): the system has to ask the user. */
        data class NeedsConsent(val intentSender: IntentSender) : DeleteResult
    }

    suspend fun delete(uris: List<Uri>): DeleteResult =
        withContext(Dispatchers.IO) {
            var deleted = 0
            val foreign = mutableListOf<Uri>()
            for (uri in uris) {
                try {
                    deleted += resolver.delete(uri, null, null)
                } catch (_: SecurityException) {
                    foreign += uri
                }
            }
            if (foreign.isEmpty()) {
                DeleteResult.Deleted(deleted)
            } else {
                DeleteResult.NeedsConsent(MediaStore.createDeleteRequest(resolver, foreign).intentSender)
            }
        }

    private companion object {
        val COLLECTION: Uri = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    }
}
