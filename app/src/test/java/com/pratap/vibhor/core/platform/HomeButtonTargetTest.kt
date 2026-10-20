package com.pratap.vibhor.core.platform

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeButtonTargetTest {

    @Test
    fun `clock is the default target`() {
        assertEquals(HomeButtonTarget.Clock, HomeButtonTarget.DEFAULT)
    }

    @Test
    fun `every target id round trips`() {
        HomeButtonTarget.entries.forEach { target ->
            assertEquals(target, HomeButtonTarget.fromId(target.id))
        }
    }

    @Test
    fun `unknown or missing ids fall back to the default`() {
        assertEquals(HomeButtonTarget.DEFAULT, HomeButtonTarget.fromId(null))
        assertEquals(HomeButtonTarget.DEFAULT, HomeButtonTarget.fromId("music"))
    }
}
