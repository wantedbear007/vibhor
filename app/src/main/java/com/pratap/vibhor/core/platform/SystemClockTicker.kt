package com.pratap.vibhor.core.platform

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import com.pratap.vibhor.core.time.ClockTicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Duration
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

/**
 * Minute-aligned clock backed by the system clock. A broadcast receiver for
 * time and time-zone changes wakes the ticker early so the display does not
 * stay stale for up to a minute after a manual change.
 */
class SystemClockTicker(
    private val context: Context,
    private val now: () -> ZonedDateTime = { ZonedDateTime.now() },
) : ClockTicker {

    override fun ticks(): Flow<ZonedDateTime> = channelFlow {
        val timeChanged = Channel<Unit>(capacity = Channel.CONFLATED)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                timeChanged.trySend(Unit)
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }

        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )

        try {
            while (currentCoroutineContext().isActive) {
                val current = now()
                send(current)

                val nextMinute = current.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1)
                val waitMillis = Duration.between(current, nextMinute).toMillis().coerceAtLeast(1L)
                withTimeoutOrNull(waitMillis) { timeChanged.receive() }
            }
        } finally {
            context.unregisterReceiver(receiver)
        }
    }.flowOn(Dispatchers.Default)
}
