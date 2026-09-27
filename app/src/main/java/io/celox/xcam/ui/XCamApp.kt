package io.celox.xcam.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.celox.xcam.BuildConfig
import io.celox.xcam.data.AppSettings
import io.celox.xcam.data.update.AppVersions
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

/**
 * Onboarding is deliberately **not** a destination in the navigation graph: it runs once, before the
 * app, and must never sit on the back stack. As a graph destination it was the graph's start, so after
 * it was popped every tab switch `popUpTo(start)` popped nothing — the tabs piled up and back walked
 * through the whole tab history. Now Record is the one fixed root: back goes player → tab → Record → out.
 */
/**
 * Opens the player once. A fast double tap on a recording would otherwise push the player twice, and
 * back would then land on the same video again. Only the resumed screen may navigate.
 */
private fun NavController.openPlayer(id: Long) {
    if (currentBackStackEntry?.lifecycle?.currentState != Lifecycle.State.RESUMED) return
    navigate(Routes.player(id)) { launchSingleTop = true }
}

@Composable
fun XCamApp(
    viewModel: RecordingViewModel,
    settings: AppSettings,
    permissions: Permissions,
    permissionActions: PermissionActions,
) {
    val motion = MaterialTheme.motionScheme
    AnimatedContent(
        targetState = settings.onboardingCompleted,
        transitionSpec = { fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec()) },
        label = "onboarding",
    ) { completed ->
        if (completed) {
            MainShell(viewModel, settings, permissions, permissionActions)
        } else {
            OnboardingScreen(
                permissions = permissions,
                onRequestCamera = permissionActions.requestCamera,
                onRequestAudio = permissionActions.requestAudioAndNotifications,
                onComplete = viewModel::completeOnboarding,
            )
        }
    }
}

@Composable
private fun MainShell(
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
    val dualCameraSupported by viewModel.dualCameraSupported.collectAsStateWithLifecycle()

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
            startDestination = Destination.RECORD.route,
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
            composable(Destination.RECORD.route) {
                RecordScreen(
                    state = state,
                    config = config,
                    latest = videos.firstOrNull(),
                    availableUpdate =
                    settings.knownUpdate?.takeIf {
                        settings.updateChecks && AppVersions.isNewer(BuildConfig.VERSION_NAME, it.version)
                    },
                    permissions = permissions,
                    permissionActions = permissionActions,
                    onStart = { viewModel.startRecording(permissions.audio) },
                    onStop = viewModel::stopRecording,
                    onDismissError = viewModel::dismissError,
                    onLensChange = viewModel::updateCameraLens,
                    onQualityChange = viewModel::updateVideoQuality,
                    onAudioChange = viewModel::updateEnableAudio,
                    onOpenVideo = { navController.openPlayer(it.id) },
                    dualCameraSupported = dualCameraSupported,
                )
            }
            composable(Destination.VIDEOS.route) {
                VideosScreen(
                    videos = videos,
                    loading = videosLoading,
                    events = viewModel.events,
                    onOpen = { navController.openPlayer(it.id) },
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
                    onUpdateChecksChange = viewModel::updateUpdateChecks,
                    dualCameraSupported = dualCameraSupported,
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
                    // Only while the player is the resumed screen: a double tap on the arrow, or the
                    // "video is gone" effect firing after a delete, must not pop the tab underneath too.
                    onBack = dropUnlessResumed { navController.popBackStack() },
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
