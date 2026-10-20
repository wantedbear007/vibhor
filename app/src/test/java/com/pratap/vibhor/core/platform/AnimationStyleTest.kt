package com.pratap.vibhor.core.platform

import org.junit.Assert.assertEquals
import org.junit.Test

class AnimationStyleTest {

    @Test
    fun `stored ids round-trip`() {
        AnimationStyle.entries.forEach { style ->
            assertEquals(style, AnimationStyle.fromId(style.id))
        }
    }

    @Test
    fun `unknown or missing ids fall back to the default`() {
        assertEquals(AnimationStyle.DEFAULT, AnimationStyle.fromId("unknown"))
        assertEquals(AnimationStyle.DEFAULT, AnimationStyle.fromId(null))
    }

    @Test
    fun `default is smooth slide`() {
        assertEquals(AnimationStyle.SMOOTH_SLIDE, AnimationStyle.DEFAULT)
    }
}
