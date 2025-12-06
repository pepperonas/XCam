package io.celox.xcam

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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.celox.xcam.ui.screens.MainScreen
import io.celox.xcam.ui.screens.SettingsScreen
import io.celox.xcam.ui.screens.VideosScreen
import io.celox.xcam.ui.theme.XCamTheme
import io.celox.xcam.util.PermissionUtils
import io.celox.xcam.viewmodel.RecordingViewModel

class MainActivity : ComponentActivity() {

    private var hasPermissions by mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        hasPermissions = allGranted

        if (allGranted) {
            Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permissions required to use this app", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check permissions on start
        hasPermissions = PermissionUtils.hasAllPermissions(this)

        setContent {
            XCamTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    XCamApp(
                        hasPermissions = hasPermissions,
                        onRequestPermissions = { requestPermissions() }
                    )
                }
            }
        }
    }

    private fun requestPermissions() {
        val permissions = PermissionUtils.getRequiredPermissions()
        permissionLauncher.launch(permissions.toTypedArray())
    }

    override fun onResume() {
        super.onResume()
        // Recheck permissions when app comes back to foreground
        hasPermissions = PermissionUtils.hasAllPermissions(this)
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
    onRequestPermissions: () -> Unit
) {
    val navController = rememberNavController()
    val viewModel: RecordingViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "main",
        enterTransition = { enterTransition },
        exitTransition = { exitTransition },
        popEnterTransition = { popEnterTransition },
        popExitTransition = { popExitTransition }
    ) {
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
                viewModel = viewModel
            )
        }
    }
}