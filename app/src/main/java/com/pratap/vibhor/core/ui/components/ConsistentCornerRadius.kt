package com.pratap.vibhor.core.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

/**
 * Vibhor's consistent corner rule: use the component's design radius until it
 * would exceed half of its smaller side.
 *
 * R = min(maxRadius, min(width, height) / 2)
 */
fun consistentCornerRadius(
    width: Dp,
    height: Dp,
    maxRadius: Dp,
): Dp {
    // Before the first measurement there is no meaningful size. Start at the
    // design radius so the first frame is not square; onSizeChanged will apply
    // the cap immediately if the component is small.
    if (width <= 0.dp || height <= 0.dp) return maxRadius
    return minOf(maxRadius, minOf(width, height) / 2f)
}

@Composable
fun rememberConsistentCornerShape(
    size: IntSize,
    maxRadius: Dp,
): RoundedCornerShape {
    val density = LocalDensity.current
    val radius = remember(size, maxRadius, density) {
        with(density) {
            consistentCornerRadius(
                width = size.width.toDp(),
                height = size.height.toDp(),
                maxRadius = maxRadius,
            )
        }
    }
    return remember(radius) { RoundedCornerShape(radius) }
}
