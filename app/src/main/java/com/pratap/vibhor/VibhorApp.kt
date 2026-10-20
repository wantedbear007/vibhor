package com.pratap.vibhor

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pratap.vibhor.core.platform.AndroidAppLauncher
import com.pratap.vibhor.core.platform.AppLauncher
import com.pratap.vibhor.core.platform.SharedPreferencesSettingsStore
import com.pratap.vibhor.core.ui.navigation.LauncherPage
import com.pratap.vibhor.feature.apps.AppDrawerViewModel
import com.pratap.vibhor.feature.apps.UnifiedAppsScreen
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

    // Status bar visibility follows the persisted setting and is re-applied
    // whenever the composition (re)enters, covering process recreation and
    // return-from-background. The system navigation bar is always left
    // visible so the user can never lock themselves out of system controls;
    // when hidden, a swipe still reveals the status bar temporarily.
    val statusBarVisible = LocalContext.current
        .let { remember(it) { SharedPreferencesSettingsStore.get(it) } }
        .settings
        .collectAsStateWithLifecycle()
        .value
        .showStatusBar
    val view = LocalView.current
    LaunchedEffect(statusBarVisible) {
        val controller = WindowCompat.getInsetsController(
            (view.context as android.app.Activity).window,
            view,
        )
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (statusBarVisible) {
            controller.show(WindowInsetsCompat.Type.statusBars())
        } else {
            controller.hide(WindowInsetsCompat.Type.statusBars())
        }
    }

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
            // Hide the pager while Settings is open so a translucent
            // launcher background reveals the wallpaper rather than the
            // current launcher page underneath the overlay.
            .alpha(if (settingsVisible) 0f else 1f)
            .testTag(LauncherTestTags.Pager),
        flingBehavior = PagerDefaults.flingBehavior(
            state = pagerState,
            snapAnimationSpec = tween(durationMillis = 180),
        ),
    ) { page ->
        when (LauncherPage.entries[page]) {
            LauncherPage.Apps -> AppsRoute(
                appLauncher = appLauncher,
                onOpenSettings = { settingsVisible = true },
            )
            LauncherPage.Home -> HomeRoute()
            LauncherPage.Cockpit -> CockpitRoute(
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
    val context = LocalContext.current
    val appLauncher = remember(context) { AndroidAppLauncher(context.applicationContext) }
    HomeScreen(
        state = state,
        onLaunchApp = { app -> appLauncher.launch(app.packageName, app.activityName) },
        onOpenAppDrawer = { /* no-op for now; can be extended */ },
    )
}

@Composable
private fun AppsRoute(
    appLauncher: AppLauncher,
    onOpenSettings: () -> Unit,
    appViewModel: AppDrawerViewModel = viewModel(factory = AppDrawerViewModel.Factory),
    controlViewModel: CockpitViewModel = viewModel(factory = CockpitViewModel.Factory),
) {
    val appState by appViewModel.uiState.collectAsStateWithLifecycle()
    val controlState by controlViewModel.uiState.collectAsStateWithLifecycle()
    UnifiedAppsScreen(
        appState = appState,
        controlState = controlState,
        onVolumeChange = controlViewModel::setVolume,
        onBrightnessChange = controlViewModel::setBrightness,
        onToggleWifi = controlViewModel::toggleWifi,
        onToggleBluetooth = controlViewModel::toggleBluetooth,
        onToggleNightMode = controlViewModel::toggleNightMode,
        onOpenSettings = onOpenSettings,
        onAppClick = { app ->
            appLauncher.launch(app.packageName, app.activityName)
        },
        onTabSelected = appViewModel::selectTab,
        onAppLongPress = { app -> appViewModel.toggleFavourite(app.packageName) },
        onSearchQueryChange = appViewModel::setSearchQuery,
    )
}

@Composable
private fun CockpitRoute(
    onOpenSettings: () -> Unit,
    viewModel: CockpitViewModel = viewModel(factory = CockpitViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CockpitScreen(
        state = state,
        onVolumeChange = viewModel::setVolume,
        onBrightnessChange = viewModel::setBrightness,
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
        onClockFormatChange = viewModel::setClockFormat,
        onClockBlurChange = viewModel::setClockBlurRadius,
        onBackgroundDarknessChange = viewModel::setBackgroundDarkness,
        onShowStatusBarChange = viewModel::setShowStatusBar,
        onQuickButtonScaleChange = viewModel::setQuickButtonScale,
        onAppIconScaleChange = viewModel::setAppIconScale,
        onAccentColorChange = viewModel::setAccentColor,
        onBackgroundChange = viewModel::setBackground,
        onBackgroundOpacityChange = viewModel::setBackgroundOpacity,
        onResetToDefaults = viewModel::resetToDefaults,
        onClose = onClose,
    )
}
