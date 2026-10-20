package com.pratap.vibhor

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import com.pratap.vibhor.core.platform.AccentColor
import com.pratap.vibhor.core.platform.InstalledApp
import com.pratap.vibhor.core.ui.theme.VibhorTheme
import com.pratap.vibhor.feature.apps.AppDrawerScreen
import com.pratap.vibhor.feature.apps.AppDrawerTab
import com.pratap.vibhor.feature.apps.AppDrawerTestTags
import com.pratap.vibhor.feature.apps.AppDrawerUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AppDrawerScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val apps = listOf(
        InstalledApp(
            packageName = "com.example.dialer",
            activityName = "com.example.dialer.MainActivity",
            label = "Dialer",
        ),
        InstalledApp(
            packageName = "com.example.maps",
            activityName = "com.example.maps.MainActivity",
            label = "Maps",
        ),
    )

    @Test
    fun searchTabsAndTilesDriveCallbacks() {
        val query = mutableStateOf("")
        val tab = mutableStateOf(AppDrawerTab.All)
        var launched: InstalledApp? = null
        var longPressed: InstalledApp? = null

        composeRule.setContent {
            VibhorTheme(accentColor = AccentColor.BLUE) {
                val visible = apps.filter {
                    query.value.isBlank() ||
                        it.label.contains(query.value, ignoreCase = true)
                }
                AppDrawerScreen(
                    state = AppDrawerUiState(
                        apps = apps,
                        favouriteApps = apps,
                        favouritePackageNames = setOf(apps.first().packageName),
                        selectedTab = tab.value,
                        isLoading = false,
                        iconScale = 1f,
                        searchQuery = query.value,
                        displayedApps = visible,
                    ),
                    onAppClick = { launched = it },
                    onTabSelected = { tab.value = it },
                    onAppLongPress = { longPressed = it },
                    onSearchQueryChange = { query.value = it },
                )
            }
        }

        composeRule.onNodeWithTag(AppDrawerTestTags.Search)
            .assertIsDisplayed()
            .performTextInput("map")
        composeRule.onNodeWithTag(AppDrawerTestTags.SearchClear)
            .assertIsDisplayed()
            .performClick()
        composeRule.runOnIdle { assertEquals("", query.value) }

        composeRule.onNodeWithTag(AppDrawerTestTags.TabAll)
            .assertIsSelected()
            .assertHasClickAction()
        composeRule.onNodeWithTag(AppDrawerTestTags.TabFavourites)
            .assertHasClickAction()
            .performClick()
        composeRule.runOnIdle { assertEquals(AppDrawerTab.Favourites, tab.value) }
        composeRule.onNodeWithTag(AppDrawerTestTags.TabFavourites).assertIsSelected()
        composeRule.onNodeWithTag(AppDrawerTestTags.TabAll).assertIsNotSelected()
        composeRule.onNodeWithTag(AppDrawerTestTags.TabAll).performClick()
        composeRule.runOnIdle { assertEquals(AppDrawerTab.All, tab.value) }
        composeRule.onNodeWithTag(AppDrawerTestTags.TabAll).assertIsSelected()

        composeRule.onAllNodesWithTag(AppDrawerTestTags.Item)
            .onFirst()
            .performTouchInput { longClick() }
        composeRule.runOnIdle { assertEquals(apps.first(), longPressed) }

        composeRule.onNodeWithText("Dialer").performClick()
        composeRule.runOnIdle { assertEquals(apps.first(), launched) }
    }
}
