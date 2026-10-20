package com.pratap.vibhor

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pratap.vibhor.core.platform.AndroidAppLauncher
import com.pratap.vibhor.core.platform.AppLauncher
import com.pratap.vibhor.core.platform.HomeButtonTarget
import com.pratap.vibhor.core.platform.SharedPreferencesSettingsStore
import com.pratap.vibhor.core.ui.animation.PageTransitionDurationMillis
import com.pratap.vibhor.core.ui.animation.pageTransition
import com.pratap.vibhor.core.ui.animation.rememberMotionEnabled
import com.pratap.vibhor.core.ui.navigation.LauncherPage
import com.pratap.vibhor.feature.apps.AppDrawerViewModel
import com.pratap.vibhor.feature.apps.UnifiedAppsScreen
import com.pratap.vibhor.feature.cockpit.CockpitViewModel
import com.pratap.vibhor.feature.home.HomeScreen
import com.pratap.vibhor.feature.home.HomeViewModel
import com.pratap.vibhor.feature.music.MusicScreen
import com.pratap.vibhor.feature.music.MusicViewModel
import com.pratap.vibhor.feature.settings.SettingsScreen
import com.pratap.vibhor.feature.settings.SettingsViewModel
import kotlinx.coroutines.launch

object LauncherTestTags {
    const val Pager = "launcher_pager"
}

/**
 * Three-page launcher. Page order is [Music, Home, Apps]; the persisted
 * Home-button target seeds the pager when launched from the system Home intent,
 * and owns the existing Home-intent reset flow (homeRequestToken). Never wraps
 * around the ends, and snap transitions target roughly 260 ms with the
 * per-page effect of the persisted animation style. Settings open as a
 * full-screen overlay above the pager.
 */
@Composable
fun VibhorApp(
    homeRequestToken: Int = 0,
    startOnHomeTarget: Boolean = false,
) {
    val context = LocalContext.current
    val settingsStore = remember(context) { SharedPreferencesSettingsStore.get(context) }
    val launcherSettings by settingsStore.settings.collectAsStateWithLifecycle()
    val homeButtonPage = launcherSettings.homeButtonTarget.toLauncherPage()
    val initialPage = if (startOnHomeTarget) homeButtonPage else LauncherPage.Home
    val pagerState = rememberPagerState(initialPage = initialPage.ordinal) {
        LauncherPage.entries.size
    }
    val appLauncher = remember(context) {
        AndroidAppLauncher(context.applicationContext)
    }
    val scope = rememberCoroutineScope()
    var settingsVisible by rememberSaveable { mutableStateOf(false) }

    // Status bar visibility follows the persisted setting and is re-applied
    // whenever the composition (re)enters, covering process recreation and
    // return-from-background. The system navigation bar is always left
    // visible so the user can never lock themselves out of system controls;
    // when hidden, a swipe still reveals the status bar temporarily. The
    // swipe animation style comes from the same settings flow and applies
    // immediately without recreating the pager.
    val statusBarVisible = launcherSettings.showStatusBar
    val animationStyle = launcherSettings.animationStyle
    val motionEnabled = rememberMotionEnabled()
    val pageAnimationSpec: AnimationSpec<Float> = remember(motionEnabled) {
        if (motionEnabled) {
            tween(
                durationMillis = PageTransitionDurationMillis,
                easing = FastOutSlowInEasing,
            )
        } else {
            snap()
        }
    }

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
        if (homeRequestToken > 0) {
            // A Home press while Settings is open should complete the reset,
            // not scroll the pager behind an invisible overlay.
            settingsVisible = false
            if (pagerState.settledPage != homeButtonPage.ordinal) {
                pagerState.animateScrollToPage(
                    page = homeButtonPage.ordinal,
                    animationSpec = pageAnimationSpec,
                )
            }
        }
    }

    // Pager back: from Music/Apps return to Home. Registered before the
    // settings back handler so settings takes precedence while open.
    BackHandler(enabled = pagerState.settledPage != LauncherPage.Home.ordinal) {
        scope.launch {
            pagerState.animateScrollToPage(
                page = LauncherPage.Home.ordinal,
                animationSpec = pageAnimationSpec,
            )
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
            snapAnimationSpec = pageAnimationSpec,
        ),
    ) { page ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pageTransition(
                    style = animationStyle,
                    pagerState = pagerState,
                    page = page,
                    motionEnabled = motionEnabled,
                ),
        ) {
            when (LauncherPage.entries[page]) {
                LauncherPage.Apps -> AppsRoute(
                    appLauncher = appLauncher,
                    onOpenSettings = { settingsVisible = true },
                )
                LauncherPage.Home -> HomeRoute()
                LauncherPage.Music -> MusicRoute()
            }
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
    val context = LocalContext.current
    UnifiedAppsScreen(
        appState = appState,
        controlState = controlState,
        onVolumeChange = controlViewModel::setVolume,
        onBrightnessChange = controlViewModel::setBrightness,
        onToggleWifi = controlViewModel::toggleWifi,
        onToggleBluetooth = controlViewModel::toggleBluetooth,
        onOpenBluetoothSettings = {
            runCatching {
                context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
            }
        },
        onOpenWifiSettings = {
            runCatching {
                context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            }
        },
        onToggleNightMode = controlViewModel::toggleNightMode,
        onOpenSettings = onOpenSettings,
        onRequestSystemBrightnessPermission = {
            runCatching {
                context.startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:${context.packageName}"),
                    ),
                )
            }
        },
        onAppClick = { app ->
            appLauncher.launch(app.packageName, app.activityName)
        },
        onTabSelected = appViewModel::selectTab,
        onAppLongPress = { app -> appViewModel.toggleFavourite(app.packageName) },
        onSearchQueryChange = appViewModel::setSearchQuery,
    )
}

@Composable
private fun MusicRoute(
    controlViewModel: CockpitViewModel = viewModel(factory = CockpitViewModel.Factory),
    musicViewModel: MusicViewModel = viewModel(factory = MusicViewModel.Factory),
) {
    val controlState by controlViewModel.uiState.collectAsStateWithLifecycle()
    val musicState by musicViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Notification access is granted in system settings; re-check on return.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                musicViewModel.refreshAccess()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    MusicScreen(
        musicState = musicState,
        controlState = controlState,
        onPlayPause = musicViewModel::playPause,
        onNext = musicViewModel::next,
        onPrevious = musicViewModel::previous,
        onSeek = musicViewModel::seekTo,
        onVolumeChange = controlViewModel::setVolume,
        onEnableMediaAccess = {
            runCatching {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        },
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
        onAnimationStyleChange = viewModel::setAnimationStyle,
        onHomeButtonTargetChange = viewModel::setHomeButtonTarget,
        onResetToDefaults = viewModel::resetToDefaults,
        onClose = onClose,
    )
}

private fun HomeButtonTarget.toLauncherPage(): LauncherPage = when (this) {
    HomeButtonTarget.Clock -> LauncherPage.Home
    HomeButtonTarget.Player -> LauncherPage.Music
}
