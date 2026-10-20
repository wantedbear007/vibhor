package com.pratap.vibhor.core.time

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale

class DateTimeFormattingTest {

    private val instant = ZonedDateTime.of(2026, 1, 5, 7, 5, 0, 0, ZoneId.of("UTC"))

    @Test
    fun `formats time in the requested locale`() {
        assertEquals(
            "7:05 AM",
            formatClockTime(instant, Locale.US).replace('\u202F', ' '),
        )
    }

    @Test
    fun `formats date in the requested locale`() {
        assertEquals("Monday, 5 January", formatClockDate(instant, Locale.UK))
    }
}
