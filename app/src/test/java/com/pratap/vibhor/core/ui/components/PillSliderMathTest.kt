package com.pratap.vibhor.core.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class PillSliderMathTest {

    @Test
    fun `scale range snaps to twenty-five percent stops`() {
        val range = 0.5f..2f
        assertEquals(0.5f, snapToSteps(0.55f, range, steps = 5), 0.0001f)
        assertEquals(1f, snapToSteps(1.1f, range, steps = 5), 0.0001f)
        assertEquals(1.75f, snapToSteps(1.64f, range, steps = 5), 0.0001f)
        assertEquals(2f, snapToSteps(1.99f, range, steps = 5), 0.0001f)
    }

    @Test
    fun `darkness snaps to twenty percent stops`() {
        val range = 0f..1f
        assertEquals(0.2f, snapToSteps(0.29f, range, steps = 4), 0.0001f)
        assertEquals(0.4f, snapToSteps(0.31f, range, steps = 4), 0.0001f)
        assertEquals(1f, snapToSteps(0.9f, range, steps = 4), 0.0001f)
    }

    @Test
    fun `blur snaps to whole indices`() {
        val range = 1f..20f
        assertEquals(14f, snapToSteps(14.4f, range, steps = 18), 0.0001f)
        assertEquals(15f, snapToSteps(14.6f, range, steps = 18), 0.0001f)
    }

    @Test
    fun `values are clamped to the range`() {
        val range = 1f..20f
        assertEquals(1f, snapToSteps(-5f, range, steps = 18), 0.0001f)
        assertEquals(20f, snapToSteps(99f, range, steps = 18), 0.0001f)
    }

    @Test
    fun `zero steps leaves the value continuous`() {
        assertEquals(0.234f, snapToSteps(0.234f, 0f..1f, steps = 0), 0.0001f)
    }
}
