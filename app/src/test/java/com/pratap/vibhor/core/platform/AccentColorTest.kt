package com.pratap.vibhor.core.platform

import org.junit.Assert.assertEquals
import org.junit.Test

class AccentColorTest {

    @Test
    fun `every accent id round trips`() {
        AccentColor.entries.forEach { accent ->
            assertEquals(accent, AccentColor.fromId(accent.id))
        }
    }

    @Test
    fun `unknown or missing ids fall back to the default`() {
        assertEquals(AccentColor.DEFAULT, AccentColor.fromId(null))
        assertEquals(AccentColor.DEFAULT, AccentColor.fromId("chartreuse"))
    }
}
