package com.pratap.vibhor.feature.apps

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pratap.vibhor.R
import com.pratap.vibhor.core.platform.InstalledApp
import com.pratap.vibhor.core.ui.theme.VibhorThemeColors
import com.pratap.vibhor.core.ui.theme.toColor
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object AppDrawerTestTags {
    const val Page = "page_apps"
    const val Search = "app_search"
    const val SearchClear = "app_search_clear"
    const val Grid = "app_grid"
    const val Item = "app_item"
    const val Loading = "apps_loading"
    const val Empty = "apps_empty"
    const val TabFavourites = "app_tab_favourites"
    const val TabAll = "app_tab_all"
}

private enum class DrawerContent {
    Loading,
    Empty,
    Grid,
}

@Composable
fun AppDrawerScreen(
    state: AppDrawerUiState,
    onAppClick: (InstalledApp) -> Unit,
    onTabSelected: (AppDrawerTab) -> Unit,
    onAppLongPress: (InstalledApp) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag(AppDrawerTestTags.Page),
        color = state.background.toColor().copy(alpha = state.backgroundOpacity),
    ) {
        AppDrawerContent(
            state = state,
            onAppClick = onAppClick,
            onTabSelected = onTabSelected,
            onAppLongPress = onAppLongPress,
            onSearchQueryChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
        )
    }
}

/**
 * The app launcher without its own background or window insets. The unified
 * app page embeds this in the right-hand panel beside the quick controls; the
 * standalone [AppDrawerScreen] keeps the original full-screen behavior.
 */
@Composable
fun AppDrawerContent(
    state: AppDrawerUiState,
    onAppClick: (InstalledApp) -> Unit,
    onTabSelected: (AppDrawerTab) -> Unit,
    onAppLongPress: (InstalledApp) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    headerTopPadding: Dp = 20.dp,
    horizontalPadding: Dp = 24.dp,
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        AppDrawerHeader(
            state = state,
            onSearchQueryChange = onSearchQueryChange,
            onTabSelected = onTabSelected,
            topPadding = headerTopPadding,
            horizontalPadding = horizontalPadding,
        )

        val content = when {
            state.isLoading &&
                state.apps.isEmpty() &&
                state.favouriteApps.isEmpty() -> DrawerContent.Loading
            state.displayedApps.isEmpty() -> DrawerContent.Empty
            else -> DrawerContent.Grid
        }

        Crossfade(
            targetState = content,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            animationSpec = tween(durationMillis = 180),
            label = "app-drawer-content",
        ) { target ->
            when (target) {
                DrawerContent.Loading -> LoadingContent(Modifier.fillMaxSize())
                DrawerContent.Empty -> EmptyContent(
                    state = state,
                    modifier = Modifier.fillMaxSize(),
                )
                DrawerContent.Grid -> AppGrid(
                    apps = state.displayedApps,
                    iconScale = state.iconScale,
                    favouritePackageNames = state.favouritePackageNames,
                    onAppClick = onAppClick,
                    onAppLongPress = onAppLongPress,
                    horizontalPadding = horizontalPadding,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun AppDrawerHeader(
    state: AppDrawerUiState,
    onSearchQueryChange: (String) -> Unit,
    onTabSelected: (AppDrawerTab) -> Unit,
    topPadding: Dp,
    horizontalPadding: Dp,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = horizontalPadding,
                end = horizontalPadding,
                top = topPadding,
                bottom = 16.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.apps_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.weight(1f))
            if (!state.isLoading && state.apps.isNotEmpty()) {
                AppCountPill(state = state)
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            if (maxWidth >= 600.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppSearchField(
                        query = state.searchQuery,
                        onQueryChange = onSearchQueryChange,
                        modifier = Modifier.weight(1f),
                    )
                    AppDrawerTabs(
                        selectedTab = state.selectedTab,
                        onTabSelected = onTabSelected,
                        modifier = Modifier.width(300.dp),
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AppSearchField(
                        query = state.searchQuery,
                        onQueryChange = onSearchQueryChange,
                    )
                    AppDrawerTabs(
                        selectedTab = state.selectedTab,
                        onTabSelected = onTabSelected,
                    )
                }
            }
        }
    }
}

@Composable
private fun AppCountPill(
    state: AppDrawerUiState,
    modifier: Modifier = Modifier,
) {
    val searching = state.searchQuery.isNotBlank()
    val tabCount = when (state.selectedTab) {
        AppDrawerTab.Favourites -> state.favouriteApps.size
        AppDrawerTab.All -> state.apps.size
    }
    val count = if (searching) state.displayedApps.size else tabCount
    val text = pluralStringResource(
        id = if (searching) R.plurals.apps_results else R.plurals.apps_count,
        count = count,
        count,
    )
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.68f),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun AppSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val clearLabel = stringResource(R.string.apps_search_clear)
    val placeholder = stringResource(R.string.apps_search_hint)
    val colorScheme = MaterialTheme.colorScheme

    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .testTag(AppDrawerTestTags.Search),
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = colorScheme.onSurface),
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyLarge,
                color = colorScheme.onSurfaceVariant,
            )
        },
        leadingIcon = {
            SearchGlyph(
                tint = if (focused) colorScheme.primary else colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(
                    onClick = {
                        onQueryChange("")
                        focusManager.clearFocus()
                    },
                    modifier = Modifier.testTag(AppDrawerTestTags.SearchClear),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = clearLabel,
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        } else {
            null
        },
        singleLine = true,
        shape = CircleShape,
        interactionSource = interactionSource,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = { focusManager.clearFocus() },
        ),
        colors = TextFieldDefaults.colors(
            focusedTextColor = colorScheme.onSurface,
            unfocusedTextColor = colorScheme.onSurface,
            focusedContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.88f),
            unfocusedContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.72f),
            cursorColor = colorScheme.primary,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            focusedPlaceholderColor = colorScheme.onSurfaceVariant,
            unfocusedPlaceholderColor = colorScheme.onSurfaceVariant,
            focusedLeadingIconColor = colorScheme.primary,
            unfocusedLeadingIconColor = colorScheme.onSurfaceVariant,
            focusedTrailingIconColor = colorScheme.onSurfaceVariant,
            unfocusedTrailingIconColor = colorScheme.onSurfaceVariant,
        ),
    )
}

@Composable
private fun AppDrawerTabs(
    selectedTab: AppDrawerTab,
    onTabSelected: (AppDrawerTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val pillShape = RoundedCornerShape(percent = 50)
    val leftSegmentShape = RoundedCornerShape(
        topStartPercent = 50,
        bottomStartPercent = 50,
    )
    val rightSegmentShape = RoundedCornerShape(
        topEndPercent = 50,
        bottomEndPercent = 50,
    )

    Surface(
        // Match the filled search field beside it (and the stacked one on
        // narrow layouts) exactly, instead of a shorter fixed height.
        modifier = modifier.height(TextFieldDefaults.MinHeight),
        shape = pillShape,
        color = colorScheme.surfaceVariant.copy(alpha = 0.68f),
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.28f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .selectableGroup(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppDrawerTab.entries.forEachIndexed { index, tab ->
                val selected = tab == selectedTab
                val background by animateColorAsState(
                    targetValue = if (selected) {
                        colorScheme.primary.copy(alpha = 0.22f)
                    } else {
                        Color.Transparent
                    },
                    animationSpec = tween(durationMillis = 160),
                    label = "drawer-tab-background",
                )
                val contentColor by animateColorAsState(
                    targetValue = if (selected) {
                        colorScheme.primary
                    } else {
                        colorScheme.onSurfaceVariant
                    },
                    animationSpec = tween(durationMillis = 160),
                    label = "drawer-tab-content",
                )
                val segmentShape = if (index == 0) leftSegmentShape else rightSegmentShape

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(segmentShape)
                        .background(background)
                        .selectable(
                            selected = selected,
                            role = Role.Tab,
                            onClick = { onTabSelected(tab) },
                        )
                        .testTag(tab.testTag),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(tab.labelRes),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor,
                        maxLines = 1,
                    )
                }

                if (index != AppDrawerTab.entries.lastIndex) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(colorScheme.outline.copy(alpha = 0.30f)),
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.testTag(AppDrawerTestTags.Loading),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(36.dp),
                color = VibhorThemeColors.accent,
                strokeWidth = 3.dp,
            )
            Text(
                text = stringResource(R.string.apps_loading),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyContent(
    state: AppDrawerUiState,
    modifier: Modifier = Modifier,
) {
    val searching = state.searchQuery.isNotBlank()
    val favouritesTab = state.selectedTab == AppDrawerTab.Favourites
    val title: String
    val body: String
    val icon: @Composable (Color, Modifier) -> Unit
    when {
        searching -> {
            title = stringResource(R.string.apps_search_empty_title)
            body = stringResource(R.string.apps_search_empty, state.searchQuery.trim())
            icon = { color, iconModifier -> SearchGlyph(color, iconModifier) }
        }
        favouritesTab -> {
            title = stringResource(R.string.apps_favourites_empty_title)
            body = stringResource(R.string.apps_favourites_empty)
            icon = { color, iconModifier -> StarGlyph(color, iconModifier) }
        }
        else -> {
            title = stringResource(R.string.apps_empty_title)
            body = stringResource(R.string.apps_empty)
            icon = { color, iconModifier -> GridGlyph(color, iconModifier) }
        }
    }
    val accent = VibhorThemeColors.accent
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .testTag(AppDrawerTestTags.Empty),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surfaceVariant.copy(alpha = 0.58f),
            ),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(accent.copy(alpha = 0.14f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    icon(accent, Modifier.size(24.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 420.dp),
                )
            }
        }
    }
}

@Composable
private fun AppGrid(
    apps: List<InstalledApp>,
    iconScale: Float,
    favouritePackageNames: Set<String>,
    onAppClick: (InstalledApp) -> Unit,
    onAppLongPress: (InstalledApp) -> Unit,
    horizontalPadding: Dp,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier.padding(horizontal = horizontalPadding),
    ) {
        val spacing = 16.dp
        val minTileWidth = 136.dp * iconScale
        val columns = ((maxWidth + spacing) / (minTileWidth + spacing))
            .toInt()
            .coerceIn(2, 8)
        val tileWidth = (maxWidth - spacing * (columns - 1)) / columns
        val iconSize = (76.dp * iconScale)
            .coerceAtMost(tileWidth - 28.dp)
            .coerceAtLeast(36.dp)
        val labelSize = (16f * iconScale).coerceAtLeast(12f).sp
        val labelLineHeight = (20f * iconScale).coerceAtLeast(16f).sp

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier
                .fillMaxSize()
                .testTag(AppDrawerTestTags.Grid),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalArrangement = Arrangement.spacedBy(spacing),
        ) {
            items(
                items = apps,
                key = { "${it.packageName}/${it.activityName}" },
            ) { app ->
                AppTile(
                    app = app,
                    iconSize = iconSize,
                    labelSize = labelSize,
                    labelLineHeight = labelLineHeight,
                    isFavourite = app.packageName in favouritePackageNames,
                    onClick = { onAppClick(app) },
                    onLongPress = { onAppLongPress(app) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppTile(
    app: InstalledApp,
    iconSize: Dp,
    labelSize: TextUnit,
    labelLineHeight: TextUnit,
    isFavourite: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = VibhorThemeColors.accent
    val colorScheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val shape = MaterialTheme.shapes.medium
    val favouriteLabel = stringResource(R.string.apps_favourite)
    val favouriteAction = stringResource(R.string.apps_favourite_action)
    val indication = LocalIndication.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = indication,
                role = Role.Button,
                onLongClickLabel = favouriteAction,
                onLongClick = onLongPress,
                onClick = onClick,
            )
            .testTag(AppDrawerTestTags.Item),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = if (isFavourite) {
                colorScheme.primary.copy(alpha = 0.16f)
            } else {
                colorScheme.surfaceVariant.copy(alpha = 0.68f)
            },
        ),
        border = if (isFavourite) {
            BorderStroke(1.dp, accent.copy(alpha = 0.45f))
        } else {
            null
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val icon = remember(app.packageName, app.activityName) {
                app.icon?.asImageBitmap()
            }
            if (icon != null) {
                Image(
                    bitmap = icon,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                )
            } else {
                val fallbackShape = RoundedCornerShape(iconSize * 0.24f)
                Box(
                    modifier = Modifier
                        .size(iconSize)
                        .clip(fallbackShape)
                        .background(colorScheme.surface)
                        .border(
                            width = 1.dp,
                            color = colorScheme.outlineVariant.copy(alpha = 0.8f),
                            shape = fallbackShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = app.label.take(1).uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontSize = (iconSize.value * 0.34f).sp,
                        color = accent,
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = labelSize,
                    lineHeight = labelLineHeight,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (isFavourite) {
                    Spacer(modifier = Modifier.width(6.dp))
                    StarGlyph(
                        tint = accent,
                        modifier = Modifier
                            .size(14.dp)
                            .semantics { contentDescription = favouriteLabel },
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchGlyph(
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.10f
        val radius = size.minDimension * 0.30f
        drawCircle(
            color = tint,
            radius = radius,
            center = Offset(size.width * 0.42f, size.height * 0.42f),
            style = Stroke(width = strokeWidth),
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.64f, size.height * 0.64f),
            end = Offset(size.width * 0.88f, size.height * 0.88f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun StarGlyph(
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val outer = size.minDimension * 0.48f
        val inner = outer * 0.45f
        val center = Offset(size.width / 2f, size.height / 2f)
        val path = Path()
        repeat(10) { index ->
            val radius = if (index % 2 == 0) outer else inner
            val angle = (-PI / 2.0 + index * PI / 5.0).toFloat()
            val point = Offset(
                x = center.x + radius * cos(angle),
                y = center.y + radius * sin(angle),
            )
            if (index == 0) {
                path.moveTo(point.x, point.y)
            } else {
                path.lineTo(point.x, point.y)
            }
        }
        path.close()
        drawPath(path = path, color = tint)
    }
}

@Composable
private fun GridGlyph(
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val cell = size.minDimension * 0.34f
        val gap = size.minDimension * 0.16f
        val start = (size.minDimension - cell * 2f - gap) / 2f
        val corner = CornerRadius(cell * 0.28f)
        repeat(2) { row ->
            repeat(2) { column ->
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(
                        x = start + column * (cell + gap),
                        y = start + row * (cell + gap),
                    ),
                    size = Size(cell, cell),
                    cornerRadius = corner,
                )
            }
        }
    }
}

private val AppDrawerTab.labelRes: Int
    get() = when (this) {
        AppDrawerTab.Favourites -> R.string.apps_tab_favourites
        AppDrawerTab.All -> R.string.apps_tab_all
    }

private val AppDrawerTab.testTag: String
    get() = when (this) {
        AppDrawerTab.Favourites -> AppDrawerTestTags.TabFavourites
        AppDrawerTab.All -> AppDrawerTestTags.TabAll
    }
