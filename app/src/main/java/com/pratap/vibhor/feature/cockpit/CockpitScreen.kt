package com.pratap.vibhor.feature.cockpit

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pratap.vibhor.R
import com.pratap.vibhor.core.platform.NavigationApp
import com.pratap.vibhor.core.ui.theme.DeepSlate
import com.pratap.vibhor.core.ui.theme.ElectricBlue
import com.pratap.vibhor.core.ui.theme.Midnight
import com.pratap.vibhor.core.ui.theme.PrimaryText
import com.pratap.vibhor.core.ui.theme.SecondaryText
import com.pratap.vibhor.core.ui.theme.VibhorSpacing
import com.pratap.vibhor.core.ui.theme.VibhorTheme

object CockpitTestTags {
    const val Page = "page_cockpit"
    const val NavCard = "cockpit_nav_card"
    const val MediaCard = "cockpit_media_card"
    const val RailClock = "cockpit_rail_clock"
    const val RailDate = "cockpit_rail_date"
    const val SettingsButton = "cockpit_settings_button"
}

@Composable
fun CockpitScreen(
    state: CockpitUiState,
    onOpenNavigation: (NavigationApp) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag(CockpitTestTags.Page),
        color = Midnight,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(VibhorSpacing.xl),
        ) {
            if (maxWidth > maxHeight) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(VibhorSpacing.xl),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CockpitSideRail(
                        state = state,
                        onOpenSettings = onOpenSettings,
                        modifier = Modifier
                            .width(148.dp)
                            .fillMaxHeight(),
                    )
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        horizontalArrangement = Arrangement.spacedBy(VibhorSpacing.xl),
                    ) {
                        NavigationCard(
                            state = state,
                            onOpenNavigation = onOpenNavigation,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                        MediaCard(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(VibhorSpacing.xl),
                ) {
                    CockpitSideRail(
                        state = state,
                        onOpenSettings = onOpenSettings,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = VibhorSpacing.sm),
                    )
                    NavigationCard(
                        state = state,
                        onOpenNavigation = onOpenNavigation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )
                    MediaCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun CockpitSideRail(
    state: CockpitUiState,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = state.timeText,
            style = MaterialTheme.typography.headlineLarge,
            color = PrimaryText,
            modifier = Modifier.testTag(CockpitTestTags.RailClock),
        )
        Text(
            text = state.dateText,
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText,
            modifier = Modifier.testTag(CockpitTestTags.RailDate),
        )
        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .padding(top = VibhorSpacing.md)
                .testTag(CockpitTestTags.SettingsButton),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_settings),
                contentDescription = stringResource(R.string.settings_title),
                tint = SecondaryText,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun NavigationCard(
    state: CockpitUiState,
    onOpenNavigation: (NavigationApp) -> Unit,
    modifier: Modifier = Modifier,
) {
    val navigationApp = state.navigationApp
    Card(
        modifier = modifier
            .testTag(CockpitTestTags.NavCard)
            .then(
                if (navigationApp != null) {
                    Modifier.clickable { onOpenNavigation(navigationApp) }
                } else {
                    Modifier
                },
            ),
        shape = RoundedCornerShape(VibhorSpacing.xl),
        colors = CardDefaults.cardColors(containerColor = DeepSlate),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(VibhorSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(VibhorSpacing.md),
        ) {
            CardHeader(
                iconRes = R.drawable.ic_navigation,
                title = stringResource(R.string.navigation_title),
            )
            if (navigationApp != null) {
                Text(
                    text = navigationApp.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = ElectricBlue,
                )
                Text(
                    text = stringResource(R.string.navigation_open_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText,
                )
            } else {
                Text(
                    text = stringResource(R.string.navigation_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText,
                )
            }
        }
    }
}

@Composable
private fun MediaCard(
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.testTag(CockpitTestTags.MediaCard),
        shape = RoundedCornerShape(VibhorSpacing.xl),
        colors = CardDefaults.cardColors(containerColor = DeepSlate),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(VibhorSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(VibhorSpacing.md),
        ) {
            CardHeader(
                iconRes = R.drawable.ic_media,
                title = stringResource(R.string.media_title),
            )
            Text(
                text = stringResource(R.string.media_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryText,
            )
        }
    }
}

@Composable
private fun CardHeader(
    @DrawableRes iconRes: Int,
    title: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(VibhorSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = ElectricBlue,
            modifier = Modifier.size(32.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = PrimaryText,
        )
    }
}

@Preview(name = "Cockpit landscape", widthDp = 1024, heightDp = 600)
@Composable
private fun CockpitLandscapePreview() {
    VibhorTheme {
        CockpitScreen(
            state = CockpitUiState(
                timeText = "10:42",
                dateText = "Monday, 19 October",
                navigationApp = NavigationApp(
                    label = "Maps",
                    packageName = "com.example.maps",
                    activityName = "com.example.maps.MainActivity",
                ),
            ),
            onOpenNavigation = {},
            onOpenSettings = {},
        )
    }
}

@Preview(name = "Cockpit empty", widthDp = 412, heightDp = 915)
@Composable
private fun CockpitPortraitPreview() {
    VibhorTheme {
        CockpitScreen(
            state = CockpitUiState(
                timeText = "10:42",
                dateText = "Monday, 19 October",
            ),
            onOpenNavigation = {},
            onOpenSettings = {},
        )
    }
}
