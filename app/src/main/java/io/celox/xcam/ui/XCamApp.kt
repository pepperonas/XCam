package io.celox.xcam.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.celox.xcam.data.AppSettings
import io.celox.xcam.ui.motion.ScreenMotion
import io.celox.xcam.ui.motion.ScreenTransitions
import io.celox.xcam.ui.motion.rememberReduceMotion
import io.celox.xcam.ui.navigation.Destination
import io.celox.xcam.ui.navigation.Routes
import io.celox.xcam.ui.onboarding.OnboardingScreen
import io.celox.xcam.ui.player.PlayerScreen
import io.celox.xcam.ui.record.RecordScreen
import io.celox.xcam.ui.settings.SettingsScreen
import io.celox.xcam.ui.videos.VideosScreen
import io.celox.xcam.viewmodel.RecordingViewModel

/**
 * Navigate to a top-level tab. Every route into a tab goes through here, so a tab is never pushed
 * twice and each tab keeps its own saved state (scroll position, selection).
 */
private fun NavController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun XCamApp(
    viewModel: RecordingViewModel,
    settings: AppSettings,
    permissions: Permissions,
    permissionActions: PermissionActions,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val reduceMotion = rememberReduceMotion()
    val motion = MaterialTheme.motionScheme
    val transitions = remember(motion, reduceMotion) { ScreenTransitions(motion, reduceMotion) }

    val state by viewModel.recordingState.collectAsStateWithLifecycle()
    val config by viewModel.recordingConfig.collectAsStateWithLifecycle()
    val videos by viewModel.videoFiles.collectAsStateWithLifecycle()
    val videosLoading by viewModel.videosLoading.collectAsStateWithLifecycle()

    // Decided once: onboarding runs until it is completed, then the app starts on Record.
    val startRoute = remember { if (settings.onboardingCompleted) Destination.RECORD.route else Routes.ONBOARDING }
    val showBar = Destination.tabIndexOf(currentRoute) >= 0

    Scaffold(
        // Each screen draws its own top bar and handles the status bar; the outer scaffold only
        // reserves the navigation bar, so the full-screen player can go edge to edge.
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            AnimatedVisibility(
                visible = showBar,
                enter = slideInVertically(motion.defaultSpatialSpec()) { it },
                exit = slideOutVertically(motion.fastSpatialSpec()) { it },
            ) {
                // M3 Expressive *short* navigation bar (64 dp instead of 80).
                ShortNavigationBar {
                    Destination.entries.forEach { destination ->
                        val selected = currentRoute == destination.route
                        ShortNavigationBarItem(
                            selected = selected,
                            onClick = { navController.navigateToTab(destination.route) },
                            icon = {
                                Icon(if (selected) destination.selectedIcon() else destination.icon(), contentDescription = null)
                            },
                            label = { Text(stringResource(destination.labelRes)) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startRoute,
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),
            enterTransition = { transitions.tabEnter(forward = isForward()) },
            exitTransition = {
                if (targetState.isChild()) transitions.hierarchyExit() else transitions.tabExit(forward = isForward())
            },
            popEnterTransition = {
                if (initialState.isChild()) transitions.hierarchyPopEnter() else transitions.tabEnter(forward = isForward())
            },
            popExitTransition = { transitions.tabExit(forward = isForward()) },
        ) {
            composable(
                Routes.ONBOARDING,
                exitTransition = { transitions.hierarchyPopExit() },
            ) {
                OnboardingScreen(
                    permissions = permissions,
                    onRequestCamera = permissionActions.requestCamera,
                    onRequestAudio = permissionActions.requestAudioAndNotifications,
                    onComplete = {
                        viewModel.completeOnboarding()
                        navController.navigate(Destination.RECORD.route) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    },
                )
            }
            composable(Destination.RECORD.route) {
                RecordScreen(
                    state = state,
                    config = config,
                    latest = videos.firstOrNull(),
                    permissions = permissions,
                    permissionActions = permissionActions,
                    onStart = { viewModel.startRecording(permissions.audio) },
                    onStop = viewModel::stopRecording,
                    onDismissError = viewModel::dismissError,
                    onLensChange = viewModel::updateCameraLens,
                    onQualityChange = viewModel::updateVideoQuality,
                    onAudioChange = viewModel::updateEnableAudio,
                    onOpenVideo = { navController.navigate(Routes.player(it.id)) },
                )
            }
            composable(Destination.VIDEOS.route) {
                VideosScreen(
                    videos = videos,
                    loading = videosLoading,
                    events = viewModel.events,
                    onOpen = { navController.navigate(Routes.player(it.id)) },
                    onDelete = { list -> viewModel.deleteVideos(list.map { it.uri }) },
                    onReload = viewModel::loadVideoFiles,
                    mediaAccess = permissions.media,
                    onRequestMedia = permissionActions.requestMedia,
                    onStartRecording = { navController.navigateToTab(Destination.RECORD.route) },
                )
            }
            composable(Destination.SETTINGS.route) {
                SettingsScreen(
                    settings = settings,
                    locked = state.isBusy,
                    onLensChange = viewModel::updateCameraLens,
                    onQualityChange = viewModel::updateVideoQuality,
                    onAudioChange = viewModel::updateEnableAudio,
                    onMaxDurationChange = viewModel::updateMaxDuration,
                    onThemeChange = viewModel::updateThemeMode,
                    onDynamicColorChange = viewModel::updateDynamicColor,
                )
            }
            composable(
                Routes.PLAYER,
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
                enterTransition = { transitions.hierarchyEnter() },
                popExitTransition = { transitions.hierarchyPopExit() },
            ) { entry ->
                val id = entry.arguments?.getLong("id") ?: -1L
                PlayerScreen(
                    video = videos.firstOrNull { it.id == id },
                    onBack = { navController.popBackStack() },
                    onDelete = { viewModel.deleteVideos(listOf(it.uri)) },
                )
            }
        }
    }
}

private fun NavBackStackEntry.isChild(): Boolean = destination.route == Routes.PLAYER

/** Left-to-right in the bar = forward. */
private fun AnimatedContentTransitionScope<NavBackStackEntry>.isForward(): Boolean =
    ScreenMotion.isForward(
        fromIndex = Destination.tabIndexOf(initialState.destination.route),
        toIndex = Destination.tabIndexOf(targetState.destination.route),
    )
