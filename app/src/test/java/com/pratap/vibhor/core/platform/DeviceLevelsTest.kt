package com.pratap.vibhor.core.platform

import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceLevelsTest {

    @Test
    fun `volume fraction maps onto the supported index range`() {
        assertEquals(0, DeviceLevels.volumeFractionToIndex(0f, maxIndex = 15))
        assertEquals(8, DeviceLevels.volumeFractionToIndex(0.5f, maxIndex = 15))
        assertEquals(15, DeviceLevels.volumeFractionToIndex(1f, maxIndex = 15))
        assertEquals(15, DeviceLevels.volumeFractionToIndex(2f, maxIndex = 15))
        assertEquals(0, DeviceLevels.volumeFractionToIndex(-1f, maxIndex = 15))
    }

    @Test
    fun `device index maps back to a fraction`() {
        assertEquals(0f, DeviceLevels.indexToVolumeFraction(0, maxIndex = 15))
        assertEquals(1f, DeviceLevels.indexToVolumeFraction(15, maxIndex = 15))
        assertEquals(0f, DeviceLevels.indexToVolumeFraction(5, maxIndex = 0))
    }

    @Test
    fun `system brightness is normalized from the 0-255 scale`() {
        assertEquals(0f, DeviceLevels.systemBrightnessToFraction(0), 0.001f)
        assertEquals(0.502f, DeviceLevels.systemBrightnessToFraction(128), 0.001f)
        assertEquals(1f, DeviceLevels.systemBrightnessToFraction(255), 0.001f)
    }

    @Test
    fun `negative window brightness means follow the system`() {
        assertEquals(null, DeviceLevels.windowBrightnessToFraction(-1f))
        assertEquals(0.4f, DeviceLevels.windowBrightnessToFraction(0.4f) ?: 0f, 0.001f)
        assertEquals(1f, DeviceLevels.windowBrightnessToFraction(1.4f) ?: 0f, 0.001f)
    }
}
