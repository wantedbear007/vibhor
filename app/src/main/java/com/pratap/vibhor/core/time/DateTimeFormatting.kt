package com.pratap.vibhor.core.time

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Formats the clock time. When [is24Hour] is non-null it overrides the locale
 * default and follows the system 12/24-hour setting; when null the locale's
 * localized SHORT time style is used.
 */
fun formatClockTime(
    dateTime: ZonedDateTime,
    locale: Locale = Locale.getDefault(),
    is24Hour: Boolean? = null,
): String = when (is24Hour) {
    true -> DateTimeFormatter.ofPattern("HH:mm", locale).format(dateTime)
    false -> DateTimeFormatter.ofPattern("h:mm a", locale).format(dateTime)
    null -> DateTimeFormatter
        .ofLocalizedTime(FormatStyle.SHORT)
        .withLocale(locale)
        .format(dateTime)
}

fun formatClockDate(
    dateTime: ZonedDateTime,
    locale: Locale = Locale.getDefault(),
): String = DateTimeFormatter
    .ofPattern("EEEE, d MMMM", locale)
    .format(dateTime)

/**
 * Broken-out time pieces for the split clock face: hour and minute as
 * separate display strings plus the localized meridiem (empty in 24-hour
 * mode). Callers pass the system 12/24-hour setting.
 */
fun splitClockTime(
    dateTime: ZonedDateTime,
    locale: Locale = Locale.getDefault(),
    is24Hour: Boolean = false,
): SplitClockTime {
    val hour = dateTime.hour
    return if (is24Hour) {
        SplitClockTime(
            hourText = String.format(locale, "%02d", hour),
            minuteText = String.format(locale, "%02d", dateTime.minute),
            meridiemText = "",
        )
    } else {
        val hour12 = when (val h = hour % 12) {
            0 -> 12
            else -> h
        }
        SplitClockTime(
            hourText = hour12.toString(),
            minuteText = String.format(locale, "%02d", dateTime.minute),
            meridiemText = dateTime.format(DateTimeFormatter.ofPattern("a", locale)),
        )
    }
}

data class SplitClockTime(
    val hourText: String,
    val minuteText: String,
    val meridiemText: String,
)
