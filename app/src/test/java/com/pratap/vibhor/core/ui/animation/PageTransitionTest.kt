package com.pratap.vibhor.core.ui.animation

import com.pratap.vibhor.core.platform.AnimationStyle
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PageTransitionTest {

    @Test
    fun `centered page is unchanged for every style`() {
        AnimationStyle.entries.forEach { style ->
            val values = pageTransitionValues(style, offset = 0f)
            assertEquals(style.name, 1f, values.alpha, 0.0001f)
            assertEquals(style.name, 0f, values.translationX, 0.0001f)
            assertEquals(style.name, 1f, values.scale, 0.0001f)
        }
    }

    @Test
    fun `alpha and scale stay inside their valid ranges`() {
        AnimationStyle.entries.forEach { style ->
            var offset = -3f
            while (offset <= 3f) {
                val values = pageTransitionValues(style, offset)
                assertTrue("${style.name} alpha at $offset", values.alpha in 0f..1f)
                assertTrue("${style.name} scale at $offset", values.scale in 0.5f..1f)
                offset += 0.1f
            }
        }
    }

    @Test
    fun `depth scales pages down as they move away`() {
        val center = pageTransitionValues(AnimationStyle.DEPTH, offset = 0f)
        val edge = pageTransitionValues(AnimationStyle.DEPTH, offset = 1f)
        assertEquals(1f, center.scale, 0.0001f)
        assertTrue(edge.scale < center.scale)
        assertTrue(edge.alpha < center.alpha)
        // Scale is anchored on the edge facing the centre to avoid a seam.
        assertEquals(0f, pageTransitionValues(AnimationStyle.DEPTH, 0.5f).originX, 0.0001f)
        assertEquals(1f, pageTransitionValues(AnimationStyle.DEPTH, -0.5f).originX, 0.0001f)
    }

    @Test
    fun `soft fade crossfades and damps movement`() {
        val mid = pageTransitionValues(AnimationStyle.SOFT_FADE, offset = -0.5f)
        assertEquals(0.5f, mid.alpha, 0.0001f)
        assertTrue("expected damped translation", abs(mid.translationX) < 0.5f)

        val edge = pageTransitionValues(AnimationStyle.SOFT_FADE, offset = -1f)
        assertEquals(0f, edge.translationX, 0.0001f)
        assertEquals(0f, edge.alpha, 0.0001f)
    }

    @Test
    fun `parallax translates pages against their offset in both directions`() {
        val forward = pageTransitionValues(AnimationStyle.PARALLAX, offset = 0.75f)
        val backward = pageTransitionValues(AnimationStyle.PARALLAX, offset = -0.75f)
        assertTrue(forward.translationX < 0f)
        assertTrue(backward.translationX > 0f)
        assertEquals(-forward.translationX, backward.translationX, 0.0001f)
        assertEquals(forward.alpha, backward.alpha, 0.0001f)
    }

    @Test
    fun `smooth slide only fades`() {
        val values = pageTransitionValues(AnimationStyle.SMOOTH_SLIDE, offset = 0.5f)
        assertEquals(1f, values.scale, 0.0001f)
        assertEquals(0f, values.translationX, 0.0001f)
        assertTrue(values.alpha < 1f)
    }

    @Test
    fun `centered pages are never rotated`() {
        AnimationStyle.entries.forEach { style ->
            val values = pageTransitionValues(style, offset = 0f)
            assertEquals(style.name, 0f, values.rotationY, 0.0001f)
            assertEquals(style.name, 0f, values.rotationZ, 0.0001f)
        }
    }

    @Test
    fun `card styles round and tilt pages in both directions`() {
        val stackRest = pageTransitionValues(AnimationStyle.CARD_STACK, offset = 0f)
        val tiltRest = pageTransitionValues(AnimationStyle.TILT_CARDS, offset = 0f)
        assertTrue("card corners at rest", stackRest.cornerPercent >= 8)
        assertTrue("tilt corners at rest", tiltRest.cornerPercent >= 10)
        assertTrue("card shadow at rest", stackRest.shadowFraction > 0f)

        val stackMid = pageTransitionValues(AnimationStyle.CARD_STACK, offset = 0.5f)
        val tiltMid = pageTransitionValues(AnimationStyle.TILT_CARDS, offset = 0.5f)
        assertTrue("card tilt direction", stackMid.rotationZ < 0f)
        assertTrue("tilt direction", tiltMid.rotationZ < 0f)
        assertTrue(
            "tilt cards rotate further than the card stack",
            abs(tiltMid.rotationZ) > abs(stackMid.rotationZ),
        )
        assertTrue(
            "shadow softens with distance",
            stackMid.shadowFraction < stackRest.shadowFraction,
        )

        val backward = pageTransitionValues(AnimationStyle.CARD_STACK, offset = -0.5f)
        assertTrue("card tilt mirrors direction", backward.rotationZ > 0f)
        assertEquals(-stackMid.rotationZ, backward.rotationZ, 0.0001f)
    }

    @Test
    fun `cube rotates around the edge that faces the centre`() {
        val forward = pageTransitionValues(AnimationStyle.CUBE, offset = 0.5f)
        val backward = pageTransitionValues(AnimationStyle.CUBE, offset = -0.5f)
        assertEquals(0f, forward.originX, 0.0001f)
        assertEquals(1f, backward.originX, 0.0001f)
        assertTrue(forward.rotationY < 0f)
        assertTrue(backward.rotationY > 0f)
        assertEquals(-forward.rotationY, backward.rotationY, 0.0001f)
        assertTrue(forward.cornerPercent > 0)
    }

    @Test
    fun `rotation, corner, and shadow values stay inside their bounds`() {
        AnimationStyle.entries.forEach { style ->
            repeat(41) { step ->
                val offset = -1f + step * 0.05f
                val values = pageTransitionValues(style, offset)
                assertTrue(
                    "${style.name} rotationY at $offset",
                    abs(values.rotationY) <= 58.01f,
                )
                assertTrue(
                    "${style.name} rotationZ at $offset",
                    abs(values.rotationZ) <= 7.01f,
                )
                assertTrue("${style.name} corner at $offset", values.cornerPercent in 0..28)
                assertTrue(
                    "${style.name} shadow at $offset",
                    values.shadowFraction in 0f..0.01f,
                )
                assertTrue("${style.name} origin at $offset", values.originX in 0f..1f)
            }
        }
    }
}
