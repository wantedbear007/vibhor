package com.pratap.vibhor.core.ui.components

import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.pratap.vibhor.core.ui.animation.rememberMotionEnabled

/**
 * Single-line text that scrolls only when it is wider than [modifier]'s
 * viewport. Short text stays still and centered. The edge fade is an alpha
 * mask, so the composable never paints a background and can sit directly on
 * the wallpaper.
 */
@Composable
fun MarqueeText(
    text: String,
    fontSize: TextUnit,
    color: Color,
    modifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
    velocity: Dp = 22.dp,
) {
    val motionEnabled = rememberMotionEnabled()
    Box(
        modifier = modifier
            .graphicsLayer {
                clip = true
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.horizontalGradient(
                        0f to Color.Transparent,
                        0.035f to Color.Black,
                        0.965f to Color.Black,
                        1f to Color.Transparent,
                    ),
                    blendMode = BlendMode.DstIn,
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        if (motionEnabled) {
            Text(
                text = text,
                modifier = textModifier.basicMarquee(
                    iterations = Int.MAX_VALUE,
                    animationMode = MarqueeAnimationMode.Immediately,
                    repeatDelayMillis = 1_800,
                    initialDelayMillis = 900,
                    spacing = MarqueeSpacing(48.dp),
                    velocity = velocity,
                ),
                color = color,
                fontSize = fontSize,
                lineHeight = fontSize * 1.25f,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
            )
        } else {
            Text(
                text = text,
                modifier = textModifier.fillMaxWidth(),
                color = color,
                fontSize = fontSize,
                lineHeight = fontSize * 1.25f,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}
