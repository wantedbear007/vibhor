package com.pratap.vibhor.core.ui.animation

import androidx.compose.foundation.pager.PagerState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.shape.RoundedCornerShape
import com.pratap.vibhor.core.platform.AnimationStyle
import kotlin.math.min

/**
 * Applies the selected [AnimationStyle] to a single pager page.
 *
 * The page offset is read inside the graphics layer block, so scrolling only
 * invalidates the layer instead of recomposing the page. When motion is
 * disabled the modifier is a no-op and pages use the pager's plain slide.
 */
fun Modifier.pageTransition(
    style: AnimationStyle,
    pagerState: PagerState,
    page: Int,
    motionEnabled: Boolean,
): Modifier {
    if (!motionEnabled) return this
    return graphicsLayer {
        val offset = pagerState.getOffsetDistanceInPages(page)
        val values = pageTransitionValues(style, offset)
        val minDimension = min(size.width, size.height)
        translationX = values.translationX * size.width
        alpha = values.alpha
        scaleX = values.scale
        scaleY = values.scale
        transformOrigin = TransformOrigin(values.originX, 0.5f)
        rotationY = values.rotationY
        rotationZ = values.rotationZ
        if (values.rotationY != 0f) {
            cameraDistance = size.width * 12f
        }
        shadowElevation = values.shadowFraction * minDimension
        if (values.cornerPercent > 0) {
            shape = RoundedCornerShape(percent = values.cornerPercent)
            clip = true
        } else {
            shape = RectangleShape
            clip = false
        }
    }
}
