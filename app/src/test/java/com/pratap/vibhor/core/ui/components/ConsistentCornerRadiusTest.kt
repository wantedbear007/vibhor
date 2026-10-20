package com.pratap.vibhor.core.ui.components

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class ConsistentCornerRadiusTest {

    @Test
    fun `large components keep the design radius`() {
        assertEquals(
            28.dp,
            consistentCornerRadius(width = 320.dp, height = 180.dp, maxRadius = 28.dp),
        )
        assertEquals(
            24.dp,
            consistentCornerRadius(width = 900.dp, height = 400.dp, maxRadius = 24.dp),
        )
    }

    @Test
    fun `radius is capped at half the smaller side`() {
        assertEquals(
            20.dp,
            consistentCornerRadius(width = 40.dp, height = 120.dp, maxRadius = 28.dp),
        )
        assertEquals(
            15.dp,
            consistentCornerRadius(width = 120.dp, height = 30.dp, maxRadius = 28.dp),
        )
        assertEquals(
            10.dp,
            consistentCornerRadius(width = 20.dp, height = 20.dp, maxRadius = 28.dp),
        )
    }

    @Test
    fun `unmeasured components start at the design radius`() {
        assertEquals(
            28.dp,
            consistentCornerRadius(width = 0.dp, height = 0.dp, maxRadius = 28.dp),
        )
    }
}
