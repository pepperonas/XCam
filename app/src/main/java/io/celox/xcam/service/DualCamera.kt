package io.celox.xcam.service

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.media.ImageReader
import android.os.Handler
import android.os.Looper
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.CompositionSettings
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import kotlinx.coroutines.guava.await

/**
 * Front and back camera at once (CameraX concurrent camera, 1.5+). CameraX composes both into ONE
 * video only when the shared use-case group is exactly Preview + VideoCapture — without a Preview it
 * binds the cameras separately. The service records with the screen off and has no preview, so it
 * gets [DiscardingPreview]: a surface that takes the frames and drops them.
 */
object DualCamera {
    /** Back camera full frame. */
    val PRIMARY: CompositionSettings =
        CompositionSettings.Builder().setAlpha(1f).setOffset(0f, 0f).setScale(1f, 1f).build()

    /** Front camera as an inset at a third of the size, in a corner (values from Google's Jetpack Camera App). */
    val INSET: CompositionSettings =
        CompositionSettings.Builder().setAlpha(1f).setOffset(2 / 3f - 0.1f, -2 / 3f + 0.1f).setScale(1 / 3f, 1 / 3f).build()

    /**
     * The (back, front) pair from the device's concurrent camera combinations, or null. Pure, so the
     * choice is unit-tested; [facing] maps an entry to its CameraSelector lens constant.
     */
    fun <T> backFrontPair(
        combinations: List<Collection<T>>,
        facing: (T) -> Int,
    ): Pair<T, T>? =
        combinations.firstNotNullOfOrNull { combo ->
            val back = combo.firstOrNull { facing(it) == CameraSelector.LENS_FACING_BACK }
            val front = combo.firstOrNull { facing(it) == CameraSelector.LENS_FACING_FRONT }
            if (back != null && front != null) back to front else null
        }

    fun pairOf(provider: ProcessCameraProvider): Pair<CameraInfo, CameraInfo>? =
        backFrontPair(provider.availableConcurrentCameraInfos) { it.lensFacing }

    /** Whether this device lets apps run front and back camera at the same time. */
    suspend fun isSupported(context: Context): Boolean {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_CONCURRENT)) return false
        return pairOf(ProcessCameraProvider.getInstance(context).await()) != null
    }
}

/** A Preview whose frames go to an [ImageReader] that closes each one at once, so the pipeline never stalls. */
class DiscardingPreview(context: Context) {
    private var reader: ImageReader? = null
    private val handler = Handler(Looper.getMainLooper())

    val preview: Preview =
        Preview.Builder().build().also { preview ->
            preview.setSurfaceProvider(ContextCompat.getMainExecutor(context)) { request ->
                val size = request.resolution
                val sink =
                    ImageReader.newInstance(size.width, size.height, PixelFormat.RGBA_8888, 2).apply {
                        setOnImageAvailableListener({ it.acquireLatestImage()?.close() }, handler)
                    }
                reader?.close()
                reader = sink
                request.provideSurface(sink.surface, ContextCompat.getMainExecutor(context)) {
                    sink.close()
                    if (reader === sink) reader = null
                }
            }
        }

    fun release() {
        reader?.close()
        reader = null
    }
}
