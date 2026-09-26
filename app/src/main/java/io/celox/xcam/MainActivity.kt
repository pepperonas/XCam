package io.celox.xcam

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.celox.xcam.ui.PermissionActions
import io.celox.xcam.ui.Permissions
import io.celox.xcam.ui.XCamApp
import io.celox.xcam.ui.theme.XCamTheme
import io.celox.xcam.ui.theme.isDarkTheme
import io.celox.xcam.viewmodel.RecordingViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: RecordingViewModel by viewModels()

    private var permissions by mutableStateOf(Permissions())

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { refreshPermissions() }

    private val audioLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { refreshPermissions() }

    private val mediaLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { refreshPermissions() }

    private val permissionActions =
        PermissionActions(
            requestCamera = { cameraLauncher.launch(Manifest.permission.CAMERA) },
            requestAudioAndNotifications = {
                audioLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.POST_NOTIFICATIONS))
            },
            requestMedia = { mediaLauncher.launch(mediaPermissions()) },
            openAppSettings = {
                startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            },
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        // Hold the splash until the settings are read: otherwise the first frame would be drawn with
        // default theme and start destination, and onboarding would flash for returning users.
        splash.setKeepOnScreenCondition { viewModel.settings.value == null }
        refreshPermissions()

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val current = settings ?: return@setContent
            val dark = isDarkTheme(current.themeMode)
            // System-bar icons follow the app's theme choice, not the OS setting.
            LaunchedEffect(dark) {
                val style =
                    if (dark) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }
            XCamTheme(themeMode = current.themeMode, dynamicColor = current.dynamicColor) {
                XCamApp(
                    viewModel = viewModel,
                    settings = current,
                    permissions = permissions,
                    permissionActions = permissionActions,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Permissions can change in the system settings while the app is in the background.
        refreshPermissions()
    }

    private fun refreshPermissions() {
        permissions =
            Permissions(
                camera = granted(Manifest.permission.CAMERA),
                audio = granted(Manifest.permission.RECORD_AUDIO),
                notifications = granted(Manifest.permission.POST_NOTIFICATIONS),
                media =
                granted(Manifest.permission.READ_MEDIA_VIDEO) ||
                    (
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                            granted(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                        ),
            )
    }

    /** On Android 14+ asking for both lets the user pick "all" or "selected" videos. */
    private fun mediaPermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            arrayOf(Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
        } else {
            arrayOf(Manifest.permission.READ_MEDIA_VIDEO)
        }

    private fun granted(permission: String) =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}
