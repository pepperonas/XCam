package io.celox.xcam.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.celox.xcam.ui.icons.*
import io.celox.xcam.ui.theme.*
import kotlinx.coroutines.launch

data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val buttonText: String? = null
)

@Composable
fun OnboardingScreen(
    onRequestCameraPermission: () -> Unit,
    onRequestAudioPermission: () -> Unit,
    onComplete: () -> Unit,
    hasCameraPermission: Boolean,
    hasAudioPermission: Boolean
) {
    val pages = listOf(
        OnboardingPage(
            icon = Icons.Filled.VideocamCustom,
            title = "Welcome to XCam",
            description = "Background Video Recorder.\nRecord discreetly with the screen off."
        ),
        OnboardingPage(
            icon = Icons.Filled.CameraLensCustom,
            title = "Camera Access",
            description = "XCam needs camera access to record video in the background.",
            buttonText = if (hasCameraPermission) null else "Grant Camera Permission"
        ),
        OnboardingPage(
            icon = Icons.Filled.MicrophoneCustom,
            title = "Audio & Notifications",
            description = "Enable audio recording and notifications to stay informed while recording.",
            buttonText = if (hasAudioPermission) null else "Grant Permissions"
        ),
        OnboardingPage(
            icon = Icons.Filled.ShieldCustom,
            title = "You're All Set",
            description = "XCam is ready. Record responsibly and always respect privacy laws.",
            buttonText = "Get Started"
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    // Auto-advance when permission is granted
    LaunchedEffect(hasCameraPermission) {
        if (hasCameraPermission && pagerState.currentPage == 1) {
            kotlinx.coroutines.delay(500)
            pagerState.animateScrollToPage(2)
        }
    }

    LaunchedEffect(hasAudioPermission) {
        if (hasAudioPermission && pagerState.currentPage == 2) {
            kotlinx.coroutines.delay(500)
            pagerState.animateScrollToPage(3)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DarkBackground,
                        DarkSurface
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.1f))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(0.7f)
            ) { page ->
                OnboardingPageContent(
                    page = pages[page],
                    onButtonClick = when (page) {
                        1 -> onRequestCameraPermission
                        2 -> onRequestAudioPermission
                        3 -> onComplete
                        else -> ({})
                    },
                    isCurrentPage = pagerState.currentPage == page,
                    permissionGranted = when (page) {
                        1 -> hasCameraPermission
                        2 -> hasAudioPermission
                        else -> false
                    }
                )
            }

            // Page indicators
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (isSelected) 24.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Amber40
                                else Gray60.copy(alpha = 0.5f)
                            )
                    )
                }
            }

            // Skip / Next buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (pagerState.currentPage < pages.size - 1) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pages.size - 1)
                            }
                        }
                    ) {
                        Text(
                            "Skip",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    ) {
                        Text(
                            "Next",
                            color = Amber40,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    onButtonClick: () -> Unit,
    isCurrentPage: Boolean,
    permissionGranted: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (isCurrentPage) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "page_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .scale(scale)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Large icon with glow
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(160.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Amber40.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Amber40
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Action button
        if (page.buttonText != null && !permissionGranted) {
            Button(
                onClick = onButtonClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Amber40,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp
                )
            ) {
                Text(
                    text = page.buttonText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else if (permissionGranted) {
            // Permission granted indicator
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SuccessGreen.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "Permission Granted",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = SuccessGreen,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
            }
        }
    }
}
