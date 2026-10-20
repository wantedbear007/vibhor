package com.pratap.vibhor.feature.apps

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pratap.vibhor.R
import com.pratap.vibhor.core.platform.InstalledApp
import com.pratap.vibhor.core.ui.theme.DeepSlate
import com.pratap.vibhor.core.ui.theme.VibhorThemeColors
import com.pratap.vibhor.core.ui.theme.Midnight
import com.pratap.vibhor.core.ui.theme.PrimaryText
import com.pratap.vibhor.core.ui.theme.SecondaryText
import com.pratap.vibhor.core.ui.theme.VibhorSpacing

object AppDrawerTestTags {
    const val Page = "page_apps"
    const val Grid = "app_grid"
    const val Item = "app_item"
    const val Loading = "apps_loading"
    const val Empty = "apps_empty"
    const val TabFavourites = "app_tab_favourites"
    const val TabAll = "app_tab_all"
}

@Composable
fun AppDrawerScreen(
    state: AppDrawerUiState,
    onAppClick: (InstalledApp) -> Unit,
    onTabSelected: (AppDrawerTab) -> Unit,
    onAppLongPress: (InstalledApp) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag(AppDrawerTestTags.Page),
        color = Midnight,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
        ) {
            AppDrawerTabs(
                selectedTab = state.selectedTab,
                onTabSelected = onTabSelected,
            )
            when {
                state.isLoading && state.apps.isEmpty() && state.favouriteApps.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.apps_loading),
                        style = MaterialTheme.typography.bodyLarge,
                        color = SecondaryText,
                        modifier = Modifier.testTag(AppDrawerTestTags.Loading),
                    )
                }

                else -> {
                    val displayedApps = when (state.selectedTab) {
                        AppDrawerTab.Favourites -> state.favouriteApps
                        AppDrawerTab.All -> state.apps
                    }
                    if (displayedApps.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (state.selectedTab == AppDrawerTab.Favourites) {
                                    stringResource(R.string.apps_favourites_empty)
                                } else {
                                    stringResource(R.string.apps_empty)
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = SecondaryText,
                                modifier = Modifier.testTag(AppDrawerTestTags.Empty),
                            )
                        }
                    } else {
                        val iconScale = state.iconScale
                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(VibhorSpacing.lg),
                        ) {
                            val columns = (maxWidth / (280.dp * iconScale))
                                .toInt()
                                .coerceIn(2, 6)
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(columns),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag(AppDrawerTestTags.Grid),
                                horizontalArrangement = Arrangement.spacedBy(VibhorSpacing.md),
                                verticalArrangement = Arrangement.spacedBy(VibhorSpacing.lg),
                            ) {
                                items(
                                    displayedApps,
                                    key = { "${it.packageName}/${it.activityName}" },
                                ) { app ->
                                    AppGridItem(
                                        app = app,
                                        iconScale = iconScale,
                                        isFavourite = app.packageName in state.favouritePackageNames,
                                        onClick = { onAppClick(app) },
                                        onLongPress = { onAppLongPress(app) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppDrawerTabs(
    selectedTab: AppDrawerTab,
    onTabSelected: (AppDrawerTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    TabRow(
        selectedTabIndex = selectedTab.ordinal,
        modifier = modifier.fillMaxWidth(),
        containerColor = Midnight,
        contentColor = PrimaryText,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                color = VibhorThemeColors.accent,
            )
        },
    ) {
        Tab(
            selected = selectedTab == AppDrawerTab.Favourites,
            onClick = { onTabSelected(AppDrawerTab.Favourites) },
            text = {
                Text(
                    text = stringResource(R.string.apps_tab_favourites),
                    color = if (selectedTab == AppDrawerTab.Favourites) PrimaryText else SecondaryText,
                )
            },
            modifier = Modifier.testTag(AppDrawerTestTags.TabFavourites),
        )
        Tab(
            selected = selectedTab == AppDrawerTab.All,
            onClick = { onTabSelected(AppDrawerTab.All) },
            text = {
                Text(
                    text = stringResource(R.string.apps_tab_all),
                    color = if (selectedTab == AppDrawerTab.All) PrimaryText else SecondaryText,
                )
            },
            modifier = Modifier.testTag(AppDrawerTestTags.TabAll),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppGridItem(
    app: InstalledApp,
    iconScale: Float,
    isFavourite: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(VibhorSpacing.lg))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress,
            )
            .padding(VibhorSpacing.sm)
            .testTag(AppDrawerTestTags.Item),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(VibhorSpacing.xs),
    ) {
        val icon = remember(app.packageName, app.activityName) {
            app.icon?.asImageBitmap()
        }
        if (icon != null) {
            Image(
                bitmap = icon,
                contentDescription = null,
                modifier = Modifier.size(140.dp * iconScale),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(140.dp * iconScale)
                    .background(DeepSlate, CircleShape),
            )
        }
        Text(
            text = app.label,
            style = MaterialTheme.typography.labelMedium,
            fontSize = (48 * iconScale).sp,
            color = PrimaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}
