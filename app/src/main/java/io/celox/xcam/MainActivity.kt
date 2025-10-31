package io.celox.xcam

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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

@Composable
fun XCamApp(
    hasPermissions: Boolean,
    onRequestPermissions: () -> Unit
) {
    val navController = rememberNavController()
    val viewModel: RecordingViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen(
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToVideos = { navController.navigate("videos") },
                onRequestPermissions = onRequestPermissions,
                hasPermissions = hasPermissions,
                viewModel = viewModel
            )
        }
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }
        composable("videos") {
            VideosScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }
    }
}