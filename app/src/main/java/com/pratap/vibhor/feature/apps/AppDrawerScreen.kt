package com.pratap.vibhor.feature.apps

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
}

@Composable
fun AppDrawerScreen(
    state: AppDrawerUiState,
    onAppClick: (InstalledApp) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag(AppDrawerTestTags.Page),
        color = Midnight,
    ) {
        when {
            state.isLoading && state.apps.isEmpty() -> Box(
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

            state.apps.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.apps_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = SecondaryText,
                    modifier = Modifier.testTag(AppDrawerTestTags.Empty),
                )
            }

            else -> {
                val iconScale = state.iconScale
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                        .padding(VibhorSpacing.lg),
                ) {
                    val columns = (maxWidth / (560.dp * iconScale))
                        .toInt()
                        .coerceIn(1, 4)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag(AppDrawerTestTags.Grid),
                        horizontalArrangement = Arrangement.spacedBy(VibhorSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(VibhorSpacing.lg),
                    ) {
                        items(
                            state.apps,
                            key = { "${it.packageName}/${it.activityName}" },
                        ) { app ->
                            AppGridItem(
                                app = app,
                                iconScale = iconScale,
                                onClick = { onAppClick(app) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppGridItem(
    app: InstalledApp,
    iconScale: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(VibhorSpacing.lg))
            .clickable(onClick = onClick)
            .padding(VibhorSpacing.md)
            .testTag(AppDrawerTestTags.Item),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(VibhorSpacing.sm),
    ) {
        val icon = remember(app.packageName, app.activityName) {
            app.icon?.asImageBitmap()
        }
        if (icon != null) {
            Image(
                bitmap = icon,
                contentDescription = null,
                modifier = Modifier.size(280.dp * iconScale),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(280.dp * iconScale)
                    .background(DeepSlate, CircleShape),
            )
        }
        Text(
            text = app.label,
            style = MaterialTheme.typography.labelLarge,
            fontSize = (70 * iconScale).sp,
            color = PrimaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}
