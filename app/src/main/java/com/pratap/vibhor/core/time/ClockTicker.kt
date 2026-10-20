package com.pratap.vibhor.core.time

import kotlinx.coroutines.flow.Flow
import java.time.ZonedDateTime

/**
 * Emits the current time at minute boundaries and whenever the system time or
 * time zone changes. Implementations must stop emitting when collection stops.
 */
interface ClockTicker {
    fun ticks(): Flow<ZonedDateTime>
}
