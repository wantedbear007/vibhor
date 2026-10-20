package com.pratap.vibhor.core.ui.components

import kotlin.math.roundToInt

/**
 * Snaps [raw] into [valueRange] using Material's step convention: [steps] is
 * the number of discrete stops *between* the two endpoints. A [steps] value of
 * zero leaves the value continuous. Values outside the range are clamped
 * before snapping.
 */
internal fun snapToSteps(
    raw: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
): Float {
    val clamped = raw.coerceIn(valueRange.start, valueRange.endInclusive)
    if (steps <= 0) return clamped
    val interval = (valueRange.endInclusive - valueRange.start) / (steps + 1).toFloat()
    if (interval <= 0f) return clamped
    val index = ((clamped - valueRange.start) / interval).roundToInt()
    return (valueRange.start + index * interval)
        .coerceIn(valueRange.start, valueRange.endInclusive)
}
