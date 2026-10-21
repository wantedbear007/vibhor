package com.pratap.vibhor

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.pratap.vibhor.core.ui.theme.VibhorTheme
import com.pratap.vibhor.feature.settings.SettingsScreen
import com.pratap.vibhor.feature.settings.SettingsTestTags
import com.pratap.vibhor.feature.settings.SettingsUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsWallpaperTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun wallpaperActionInvokesTheSystemWallpaperHook() {
        var requested = false
        composeRule.setContent {
            VibhorTheme {
                SettingsScreen(
                    state = SettingsUiState(),
                    onClockScaleChange = {},
                    onClockFaceChange = {},
                    onClockFormatChange = {},
                    onClockBlurChange = {},
                    onBackgroundDarknessChange = {},
                    onShowStatusBarChange = {},
                    onChangeWallpaper = { requested = true },
                    onQuickButtonScaleChange = {},
                    onAppIconScaleChange = {},
                    onAccentColorChange = {},
                    onBackgroundChange = {},
                    onBackgroundOpacityChange = {},
                    onAnimationStyleChange = {},
                    onHomeButtonTargetChange = {},
                    onBlackoutBrightnessChange = {},
                    onResetToDefaults = {},
                    onClose = {},
                )
            }
        }

        composeRule.onNodeWithTag(SettingsTestTags.WallpaperButton)
            .performScrollTo()
            .performClick()

        assertTrue(requested)
    }
}
