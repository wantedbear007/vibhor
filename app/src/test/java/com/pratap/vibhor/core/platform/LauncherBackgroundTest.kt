package com.pratap.vibhor.core.platform

import org.junit.Assert.assertEquals
import org.junit.Test

class LauncherBackgroundTest {

    @Test
    fun `every background id round trips`() {
        LauncherBackground.entries.forEach { background ->
            assertEquals(background, LauncherBackground.fromId(background.id))
        }
    }

    @Test
    fun `unknown or missing ids fall back to the default`() {
        assertEquals(LauncherBackground.DEFAULT, LauncherBackground.fromId(null))
        assertEquals(LauncherBackground.DEFAULT, LauncherBackground.fromId("wallpaper"))
    }
}
