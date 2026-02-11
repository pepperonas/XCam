package io.celox.xcam

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.celox.xcam.ui.screens.MainScreen
import io.celox.xcam.ui.screens.OnboardingScreen
import io.celox.xcam.ui.screens.SettingsScreen
import io.celox.xcam.ui.screens.VideoPlayerScreen
import io.celox.xcam.ui.screens.VideosScreen
import io.celox.xcam.ui.theme.XCamTheme
import io.celox.xcam.util.PermissionUtils
import io.celox.xcam.viewmodel.RecordingViewModel

class MainActivity : ComponentActivity() {

    private var hasPermissions by mutableStateOf(false)
    private var hasCameraPermission by mutableStateOf(false)
    private var hasAudioPermission by mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        hasPermissions = allGranted
        hasCameraPermission = permissions[Manifest.permission.CAMERA] ?: hasCameraPermission
        hasAudioPermission = permissions[Manifest.permission.RECORD_AUDIO] ?: hasAudioPermission

        if (allGranted) {
            Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        checkAllPermissions()
    }

    private val audioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasAudioPermission = permissions[Manifest.permission.RECORD_AUDIO] ?: hasAudioPermission
        checkAllPermissions()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check permissions on start
        checkAllPermissions()

        setContent {
            XCamTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    XCamApp(
                        hasPermissions = hasPermissions,
                        hasCameraPermission = hasCameraPermission,
                        hasAudioPermission = hasAudioPermission,
                        onRequestPermissions = { requestPermissions() },
                        onRequestCameraPermission = { requestCameraPermission() },
                        onRequestAudioPermission = { requestAudioPermission() }
                    )
                }
            }
        }
    }

    private fun checkAllPermissions() {
        hasPermissions = PermissionUtils.hasAllPermissions(this)
        hasCameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        hasAudioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        val permissions = PermissionUtils.getRequiredPermissions()
        permissionLauncher.launch(permissions.toTypedArray())
    }

    private fun requestCameraPermission() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun requestAudioPermission() {
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        audioPermissionLauncher.launch(permissions.toTypedArray())
    }

    override fun onResume() {
        super.onResume()
        checkAllPermissions()
    }
}

// Animation specs for navigation transitions
private const val TRANSITION_DURATION = 300

private val enterTransition: EnterTransition = fadeIn(
    animationSpec = tween(TRANSITION_DURATION)
) + slideInHorizontally(
    animationSpec = tween(TRANSITION_DURATION),
    initialOffsetX = { fullWidth -> fullWidth / 4 }
)

private val exitTransition: ExitTransition = fadeOut(
    animationSpec = tween(TRANSITION_DURATION)
) + slideOutHorizontally(
    animationSpec = tween(TRANSITION_DURATION),
    targetOffsetX = { fullWidth -> -fullWidth / 4 }
)

private val popEnterTransition: EnterTransition = fadeIn(
    animationSpec = tween(TRANSITION_DURATION)
) + slideInHorizontally(
    animationSpec = tween(TRANSITION_DURATION),
    initialOffsetX = { fullWidth -> -fullWidth / 4 }
)

private val popExitTransition: ExitTransition = fadeOut(
    animationSpec = tween(TRANSITION_DURATION)
) + slideOutHorizontally(
    animationSpec = tween(TRANSITION_DURATION),
    targetOffsetX = { fullWidth -> fullWidth / 4 }
)

@Composable
fun XCamApp(
    hasPermissions: Boolean,
    hasCameraPermission: Boolean,
    hasAudioPermission: Boolean,
    onRequestPermissions: () -> Unit,
    onRequestCameraPermission: () -> Unit,
    onRequestAudioPermission: () -> Unit
) {
    val navController = rememberNavController()
    val viewModel: RecordingViewModel = viewModel()
    val hasCompletedOnboarding by viewModel.hasCompletedOnboarding.collectAsState()

    val startDestination = if (hasCompletedOnboarding) "main" else "onboarding"

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { enterTransition },
        exitTransition = { exitTransition },
        popEnterTransition = { popEnterTransition },
        popExitTransition = { popExitTransition }
    ) {
        composable(
            route = "onboarding",
            enterTransition = { fadeIn(animationSpec = tween(TRANSITION_DURATION)) },
            exitTransition = { fadeOut(animationSpec = tween(TRANSITION_DURATION)) }
        ) {
            OnboardingScreen(
                onRequestCameraPermission = onRequestCameraPermission,
                onRequestAudioPermission = onRequestAudioPermission,
                onComplete = {
                    viewModel.completeOnboarding()
                    navController.navigate("main") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                },
                hasCameraPermission = hasCameraPermission,
                hasAudioPermission = hasAudioPermission
            )
        }
        composable(
            route = "main",
            enterTransition = { fadeIn(animationSpec = tween(TRANSITION_DURATION)) },
            exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition },
            popExitTransition = { fadeOut(animationSpec = tween(TRANSITION_DURATION)) }
        ) {
            MainScreen(
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToVideos = { navController.navigate("videos") },
                onRequestPermissions = onRequestPermissions,
                hasPermissions = hasPermissions,
                viewModel = viewModel
            )
        }
        composable(
            route = "settings",
            enterTransition = { enterTransition },
            exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition },
            popExitTransition = { popExitTransition }
        ) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }
        composable(
            route = "videos",
            enterTransition = { enterTransition },
            exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition },
            popExitTransition = { popExitTransition }
        ) {
            VideosScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlayer = { index ->
                    navController.navigate("player/$index")
                },
                viewModel = viewModel
            )
        }
        composable(
            route = "player/{videoIndex}",
            arguments = listOf(navArgument("videoIndex") { type = NavType.IntType }),
            enterTransition = { fadeIn(animationSpec = tween(TRANSITION_DURATION)) },
            exitTransition = { fadeOut(animationSpec = tween(TRANSITION_DURATION)) },
            popEnterTransition = { fadeIn(animationSpec = tween(TRANSITION_DURATION)) },
            popExitTransition = { fadeOut(animationSpec = tween(TRANSITION_DURATION)) }
        ) { backStackEntry ->
            val videoIndex = backStackEntry.arguments?.getInt("videoIndex") ?: 0
            val videos by viewModel.videoFiles.collectAsState()
            val videoFile = videos.getOrNull(videoIndex)

            VideoPlayerScreen(
                videoFile = videoFile,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
