package com.pratap.vibhor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.pratap.vibhor.core.ui.theme.VibhorTheme
import com.pratap.vibhor.feature.apps.AppDrawerUiState
import com.pratap.vibhor.feature.apps.UnifiedAppsScreen
import com.pratap.vibhor.feature.apps.UnifiedAppsTestTags
import com.pratap.vibhor.feature.cockpit.CockpitUiState
import com.pratap.vibhor.feature.music.MusicScreen
import com.pratap.vibhor.feature.music.MusicTestTags
import com.pratap.vibhor.feature.music.MusicUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MuteControlTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun muteTileSitsBesideNightModeAndDisablesOnlyTheVolumeSlider() {
        var muted by mutableStateOf(false)
        composeRule.setContent {
            VibhorTheme {
                UnifiedAppsScreen(
                    appState = AppDrawerUiState(isLoading = false),
                    controlState = CockpitUiState(
                        volumeLevel = 0.4f,
                        volumeMuted = muted,
                    ),
                    onVolumeChange = {},
                    onBrightnessChange = {},
                    onToggleWifi = {},
                    onToggleBluetooth = {},
                    onOpenBluetoothSettings = {},
                    onOpenWifiSettings = {},
                    onToggleNightMode = {},
                    onToggleMute = { muted = !muted },
                    onOpenBlackout = {},
                    onOpenSettings = {},
                    onRequestSystemBrightnessPermission = {},
                    onAppClick = {},
                    onTabSelected = {},
                    onAppLongPress = {},
                    onSearchQueryChange = {},
                )
            }
        }

        val night = composeRule.onNodeWithTag(UnifiedAppsTestTags.NightTile)
            .fetchSemanticsNode().boundsInRoot
        val mute = composeRule.onNodeWithTag(UnifiedAppsTestTags.MuteTile)
            .fetchSemanticsNode().boundsInRoot

        assertEquals(night.top, mute.top, 0.5f)
        assertTrue("Mute should sit directly beside Night mode", mute.left >= night.right)

        composeRule.onNodeWithTag(UnifiedAppsTestTags.VolumeSlider).assertIsEnabled()
        composeRule.onNodeWithTag(UnifiedAppsTestTags.BrightnessSlider).assertIsEnabled()

        composeRule.onNodeWithTag(UnifiedAppsTestTags.MuteTile).performClick()

        composeRule.onNodeWithTag(UnifiedAppsTestTags.VolumeSlider).assertIsNotEnabled()
        composeRule.onNodeWithTag(UnifiedAppsTestTags.BrightnessSlider).assertIsEnabled()

        composeRule.onNodeWithTag(UnifiedAppsTestTags.MuteTile).performClick()

        composeRule.onNodeWithTag(UnifiedAppsTestTags.VolumeSlider).assertIsEnabled()
    }

    @Test
    fun musicVolumePillIsDisabledWhileMuted() {
        composeRule.setContent {
            VibhorTheme {
                MusicScreen(
                    musicState = MusicUiState(hasAccess = false),
                    controlState = CockpitUiState(volumeMuted = true),
                    onPlayPause = {},
                    onNext = {},
                    onPrevious = {},
                    onSeek = {},
                    onVolumeChange = {},
                    onEnableMediaAccess = {},
                )
            }
        }

        composeRule.onNodeWithTag(MusicTestTags.VolumeSlider)
            .assertIsDisplayed()
            .assertIsNotEnabled()
    }
}
