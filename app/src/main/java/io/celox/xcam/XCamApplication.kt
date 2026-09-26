package io.celox.xcam

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder
import io.celox.xcam.data.PreferencesManager
import io.celox.xcam.data.update.UpdateNotifier
import io.celox.xcam.data.update.UpdateWatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class XCamApplication : Application(), ImageLoaderFactory {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        runCatching { UpdateNotifier.ensureChannel(this) }
        // Keep the background update check in line with the setting; never let it break startup.
        appScope.launch {
            runCatching {
                if (PreferencesManager(this@XCamApplication).settings.first().updateChecks) {
                    UpdateWatcher.schedule(this@XCamApplication)
                    UpdateWatcher.checkIfDue(this@XCamApplication)
                } else {
                    UpdateWatcher.cancel(this@XCamApplication)
                }
            }
        }
    }

    /** A Coil loader that can decode video frames, for the recording thumbnails. */
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .components { add(VideoFrameDecoder.Factory()) }
            .crossfade(true)
            .build()
}
