package com.pratap.vibhor.feature.blackout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pratap.vibhor.R

object BlackoutTestTags {
    const val Page = "blackout_page"
    const val Time = "blackout_time"
    const val Track = "blackout_track"
    const val Dismiss = "blackout_dismiss"
}

/**
 * Immersive, pitch-black page with only a small clock, the active media title,
 * and an exit action. It deliberately ignores the launcher theme so the page
 * stays pure black regardless of accent or background settings.
 */
@Composable
fun BlackoutScreen(
    timeText: String,
    trackTitle: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            // The overlay owns the whole pointer stream so gestures cannot
            // reach the pager or app grid underneath. Child controls still
            // receive events before this Final-pass observer.
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(PointerEventPass.Final)
                    }
                }
            }
            .safeDrawingPadding()
            .testTag(BlackoutTestTags.Page),
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = timeText,
                color = Color.White.copy(alpha = 0.64f),
                fontSize = 22.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag(BlackoutTestTags.Time),
            )
            Text(
                text = trackTitle
                    ?.takeIf { it.isNotBlank() }
                    ?: stringResource(R.string.blackout_no_music),
                color = Color.White.copy(alpha = 0.88f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .testTag(BlackoutTestTags.Track),
            )
        }

        Surface(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(64.dp)
                .testTag(BlackoutTestTags.Dismiss),
            shape = RoundedCornerShape(999.dp),
            color = Color.White.copy(alpha = 0.08f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
            contentColor = Color.White,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.blackout_turn_off),
                    modifier = Modifier.size(30.dp),
                )
            }
        }
    }
}
