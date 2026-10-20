package com.pratap.vibhor

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pratap.vibhor.core.platform.AndroidAppLauncher
import com.pratap.vibhor.core.platform.AppLauncher
import com.pratap.vibhor.core.platform.NavigationApp
import com.pratap.vibhor.core.ui.navigation.LauncherPage
import com.pratap.vibhor.feature.apps.AppDrawerScreen
import com.pratap.vibhor.feature.apps.AppDrawerViewModel
import com.pratap.vibhor.feature.cockpit.CockpitScreen
import com.pratap.vibhor.feature.cockpit.CockpitViewModel
import com.pratap.vibhor.feature.home.HomeScreen
import com.pratap.vibhor.feature.home.HomeViewModel
import com.pratap.vibhor.feature.settings.SettingsScreen
import com.pratap.vibhor.feature.settings.SettingsViewModel
import kotlinx.coroutines.launch

object LauncherTestTags {
    const val Pager = "launcher_pager"
}

/**
 * Three-page launcher. Page order is [Apps, Home, Cockpit]; the pager starts
 * on Home, never wraps around the ends, and snap transitions target roughly
 * 180 ms. The existing Home-intent reset flow (homeRequestToken) scrolls the
 * pager back to Home. Settings open as a full-screen overlay above the pager.
 */
@Composable
fun VibhorApp(homeRequestToken: Int = 0) {
    val pagerState = rememberPagerState(initialPage = LauncherPage.Home.ordinal) {
        LauncherPage.entries.size
    }
    val context = LocalContext.current
    val appLauncher = remember(context) {
        AndroidAppLauncher(context.applicationContext)
    }
    val scope = rememberCoroutineScope()
    var settingsVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(homeRequestToken) {
        if (homeRequestToken > 0 &&
            pagerState.settledPage != LauncherPage.Home.ordinal
        ) {
            pagerState.animateScrollToPage(LauncherPage.Home.ordinal)
        }
    }

    // Pager back: from Cockpit/Apps return to Home. Registered before the
    // settings back handler so settings takes precedence while open.
    BackHandler(enabled = pagerState.settledPage != LauncherPage.Home.ordinal) {
        scope.launch {
            pagerState.animateScrollToPage(LauncherPage.Home.ordinal)
        }
    }

    // Settings back: close the overlay first.
    BackHandler(enabled = settingsVisible) {
        settingsVisible = false
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .testTag(LauncherTestTags.Pager),
        flingBehavior = PagerDefaults.flingBehavior(
            state = pagerState,
            snapAnimationSpec = tween(durationMillis = 180),
        ),
    ) { page ->
        when (LauncherPage.entries[page]) {
            LauncherPage.Apps -> AppsRoute(appLauncher = appLauncher)
            LauncherPage.Home -> HomeRoute()
            LauncherPage.Cockpit -> CockpitRoute(
                onOpenNavigation = { app ->
                    appLauncher.launch(app.packageName, app.activityName)
                },
                onOpenSettings = { settingsVisible = true },
            )
        }
    }

    if (settingsVisible) {
        SettingsRoute(onClose = { settingsVisible = false })
    }
}

@Composable
private fun HomeRoute(
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(state = state)
}

@Composable
private fun AppsRoute(
    appLauncher: AppLauncher,
    viewModel: AppDrawerViewModel = viewModel(factory = AppDrawerViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AppDrawerScreen(
        state = state,
        onAppClick = { app ->
            appLauncher.launch(app.packageName, app.activityName)
        },
    )
}

@Composable
private fun CockpitRoute(
    onOpenNavigation: (NavigationApp) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: CockpitViewModel = viewModel(factory = CockpitViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CockpitScreen(
        state = state,
        onOpenNavigation = onOpenNavigation,
        onOpenSettings = onOpenSettings,
    )
}

@Composable
private fun SettingsRoute(
    onClose: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreen(
        state = state,
        onClockScaleChange = viewModel::setClockScale,
        onClockFaceChange = viewModel::setClockFace,
        onAppIconScaleChange = viewModel::setAppIconScale,
        onResetToDefaults = viewModel::resetToDefaults,
        onClose = onClose,
    )
}
