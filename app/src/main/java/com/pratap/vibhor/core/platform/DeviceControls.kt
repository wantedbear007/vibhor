package com.pratap.vibhor.core.platform

import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.roundToInt

/**
 * Pure conversions between a 0..1 slider fraction and device levels. Kept
 * separate from Android APIs so the mapping can be unit tested.
 */
object DeviceLevels {

    /** Maps a 0..1 fraction to a platform volume index in 0..[maxIndex]. */
    fun volumeFractionToIndex(fraction: Float, maxIndex: Int): Int {
        if (maxIndex <= 0) return 0
        return (fraction.coerceIn(0f, 1f) * maxIndex)
            .roundToInt()
            .coerceIn(0, maxIndex)
    }

    /** Maps a platform volume index back to a 0..1 fraction. */
    fun indexToVolumeFraction(index: Int, maxIndex: Int): Float {
        if (maxIndex <= 0) return 0f
        return (index.toFloat() / maxIndex.toFloat()).coerceIn(0f, 1f)
    }

    /** System brightness is reported on a 0..255 scale. */
    fun systemBrightnessToFraction(value: Int): Float =
        (value.toFloat() / 255f).coerceIn(0f, 1f)

    /**
     * Window brightness uses -1 for "follow system" and 0..1 for an override.
     * Anything negative is not a usable override.
     */
    fun windowBrightnessToFraction(value: Float): Float? =
        if (value < 0f) null else value.coerceIn(0f, 1f)
}

/**
 * Device controls the Cockpit screen drives. Kept as an interface so the
 * ViewModel can be exercised in unit tests without Android services.
 */
interface DeviceControls {

    /** Current media-stream volume as a 0..1 fraction. */
    fun mediaVolume(): Float

    /** Sets the media-stream volume. Never changes ring or alarm volume. */
    fun setMediaVolume(fraction: Float)

    /** Effective screen brightness as a 0..1 fraction. */
    fun screenBrightness(): Float

    /** Applies a window-level brightness override for the current activity. */
    fun setScreenBrightness(fraction: Float)

    /** Emits whenever the platform reports a volume change (e.g. hard keys). */
    fun observeChanges(): Flow<Unit>
}

/**
 * Android-backed [DeviceControls]. Volume uses [AudioManager.STREAM_MUSIC];
 * brightness uses a window-level override so no WRITE_SETTINGS permission is
 * required and global system brightness is left untouched.
 */
class AndroidDeviceControls(
    private val application: Application,
) : DeviceControls {

    private val audioManager: AudioManager =
        application.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    override fun mediaVolume(): Float = DeviceLevels.indexToVolumeFraction(
        audioManager.getStreamVolume(AudioManager.STREAM_MUSIC),
        audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
    )

    override fun setMediaVolume(fraction: Float) {
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val index = DeviceLevels.volumeFractionToIndex(fraction, max)
        runCatching {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0)
        }
    }

    override fun screenBrightness(): Float {
        val activity = currentActivity() ?: return DeviceLevels.systemBrightnessToFraction(
            runCatching {
                Settings.System.getInt(
                    application.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                )
            }.getOrDefault(128),
        )
        val windowBrightness = DeviceLevels.windowBrightnessToFraction(
            activity.window.attributes.screenBrightness,
        )
        if (windowBrightness != null) return windowBrightness
        val systemBrightness = runCatching {
            Settings.System.getInt(
                application.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
            )
        }.getOrDefault(128)
        return DeviceLevels.systemBrightnessToFraction(systemBrightness)
    }

    override fun setScreenBrightness(fraction: Float) {
        val activity = currentActivity() ?: return
        val params = activity.window.attributes
        // A tiny floor keeps the panel readable at the bottom of the range.
        params.screenBrightness = fraction.coerceIn(0.01f, 1f)
        activity.window.attributes = params
    }

    override fun observeChanges(): Flow<Unit> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                trySend(Unit)
            }
        }
        ContextCompat.registerReceiver(
            application,
            receiver,
            IntentFilter("android.media.VOLUME_CHANGED_ACTION"),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        awaitClose { application.unregisterReceiver(receiver) }
    }

    /**
     * Finds the current resumed activity. Window brightness only exists on an
     * Activity, so this is required for the brightness override. Returns null
     * instead of throwing if the platform internals are unavailable.
     */
    private fun currentActivity(): Activity? = runCatching {
        val activityThreadClass = Class.forName("android.app.ActivityThread")
        val activityThread = activityThreadClass
            .getMethod("currentActivityThread")
            .invoke(null)
        val activitiesField = activityThreadClass.getDeclaredField("mActivities")
        activitiesField.isAccessible = true
        val activities = activitiesField.get(activityThread) as? Map<*, *> ?: return null
        for (record in activities.values) {
            val recordClass = record?.javaClass ?: continue
            val pausedField = recordClass.getDeclaredField("paused")
            pausedField.isAccessible = true
            if (!pausedField.getBoolean(record)) {
                val activityField = recordClass.getDeclaredField("activity")
                activityField.isAccessible = true
                return activityField.get(record) as? Activity
            }
        }
        null
    }.getOrNull()
}
