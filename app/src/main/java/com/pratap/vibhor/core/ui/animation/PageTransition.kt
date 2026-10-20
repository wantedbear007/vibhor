package com.pratap.vibhor.core.ui.animation

import com.pratap.vibhor.core.platform.AnimationStyle
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

/** Pager snap duration in milliseconds, shared by swipes and programmatic jumps. */
const val PageTransitionDurationMillis = 260

/**
 * Visual transform for one page of the launcher pager.
 *
 * The pager already slides pages at the gesture rate; these values only add
 * the per-style effect on top of that slide. [alpha] is the page opacity,
 * [translationX] is an extra horizontal translation as a fraction of the page
 * width (positive moves the page right), [scale] is a uniform scale, and
 * [originX] is the horizontal transform origin used by the scale.
 */
data class PageTransitionValues(
    val alpha: Float,
    val translationX: Float,
    val scale: Float,
    val originX: Float = 0.5f,
    val rotationY: Float = 0f,
    val rotationZ: Float = 0f,
    val cornerPercent: Int = 0,
    val shadowFraction: Float = 0f,
) {
    companion object {
        val None = PageTransitionValues(alpha = 1f, translationX = 0f, scale = 1f)
    }
}

/**
 * Maps a page offset to the visual values of the selected [AnimationStyle].
 * [offset] is the distance from the centred page in pages: 0 is centred,
 * ±1 is one full page away, and the sign follows the pager's own
 * forward/backward convention.
 */
fun pageTransitionValues(style: AnimationStyle, offset: Float): PageTransitionValues {
    val distance = abs(offset).coerceIn(0f, 1f)
    return when (style) {
        // The incoming page slides in with a subtle fade.
        AnimationStyle.SMOOTH_SLIDE -> PageTransitionValues(
            alpha = 1f - 0.28f * distance,
            translationX = 0f,
            scale = 1f,
        )
        // Pages gain depth as they approach the centre and recede when they
        // leave. Scaling around the edge that faces the centre keeps adjacent
        // pages touching mid-swipe, so the wallpaper never shows through a seam.
        AnimationStyle.DEPTH -> PageTransitionValues(
            alpha = 1f - 0.32f * distance,
            translationX = 0f,
            scale = 1f - 0.08f * distance,
            originX = if (offset < 0f) 1f else 0f,
        )
        // Crossfade with damped movement: the pager slide is cancelled by a
        // sine curve that peaks mid-transition and returns to zero at the
        // edges, so settled pages still rest fully off-screen.
        AnimationStyle.SOFT_FADE -> PageTransitionValues(
            alpha = (1f - distance).coerceIn(0f, 1f),
            translationX = -offset * 0.72f * sin(distance * PI.toFloat()),
            scale = 1f,
        )
        // The offset sign alone separates the two rates: whichever page is
        // leaving slows down while the page moving towards the centre speeds
        // up, in both swipe directions.
        AnimationStyle.PARALLAX -> PageTransitionValues(
            alpha = 1f - 0.24f * distance,
            translationX = -offset * 0.16f,
            scale = 1f,
        )
        // Card-like pages that separate slightly with rounded corners and a
        // subtle tilt. The extra translation damps the pager slide mid-swipe
        // so the pages feel like they are stacked rather than on rails.
        AnimationStyle.CARD_STACK -> PageTransitionValues(
            alpha = 1f - 0.18f * distance,
            translationX = -offset * 0.18f * sin(distance * PI.toFloat()),
            scale = 1f - 0.10f * distance,
            originX = 0.5f,
            rotationZ = -offset * 2.2f * sin(distance * PI.toFloat()),
            cornerPercent = (10f + 10f * distance).roundToInt().coerceIn(8, 22),
            shadowFraction = 0.0042f * (1f - distance),
        )
        // Like CARD_STACK, but more playful: a stronger rotate + bigger corner
        // radius, keeping the page readable with a gentle fade.
        AnimationStyle.TILT_CARDS -> PageTransitionValues(
            alpha = 1f - 0.22f * distance,
            translationX = -offset * 0.08f,
            scale = 1f - 0.07f * distance,
            originX = 0.5f,
            rotationZ = -offset * 7.0f * sin(distance * PI.toFloat()),
            cornerPercent = (12f + 14f * distance).roundToInt().coerceIn(10, 28),
            shadowFraction = 0.0036f * (1f - distance),
        )
        // 3D cube rotation around the page edge that faces the centre.
        AnimationStyle.CUBE -> PageTransitionValues(
            alpha = 1f - 0.28f * distance,
            translationX = 0f,
            scale = 1f,
            originX = if (offset < 0f) 1f else 0f,
            rotationY = -offset * 58f,
            cornerPercent = 6,
            shadowFraction = 0.0024f * (1f - distance),
        )
    }
}
