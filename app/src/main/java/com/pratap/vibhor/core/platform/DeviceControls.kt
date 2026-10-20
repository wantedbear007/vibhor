package com.pratap.vibhor.core.platform

import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.WindowManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
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

    /** Maps a 0..1 slider fraction to the platform's 0..255 brightness scale. */
    fun fractionToSystemBrightness(fraction: Float): Int =
        (fraction.coerceIn(0f, 1f) * 255f)
            .roundToInt()
            .coerceIn(0, 255)

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

    /** True when the platform has muted the media stream. */
    fun isMediaMuted(): Boolean

    /** Mutes or unmutes the media stream without discarding its volume. */
    fun setMediaMuted(muted: Boolean)

    /** Effective screen brightness as a 0..1 fraction. */
    fun screenBrightness(): Float

    /**
     * Applies brightness immediately. When system settings access is
     * available, the global system brightness is updated as well.
     */
    fun setScreenBrightness(fraction: Float)

    /** True when the user granted the special system-settings app access. */
    fun canWriteSystemBrightness(): Boolean

    /**
     * Emits whenever the platform reports a volume or brightness change, or
     * periodically as a fallback for values changed by another app.
     */
    fun observeChanges(): Flow<Unit>
}

/**
 * Android-backed [DeviceControls]. Volume uses [AudioManager.STREAM_MUSIC].
 * Brightness writes the global system value when the user has granted
 * `WRITE_SETTINGS`; otherwise it applies a window-level override fallback.
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

    override fun isMediaMuted(): Boolean = runCatching {
        audioManager.isStreamMute(AudioManager.STREAM_MUSIC)
    }.getOrDefault(false)

    override fun setMediaMuted(muted: Boolean) {
        runCatching {
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                if (muted) AudioManager.ADJUST_MUTE else AudioManager.ADJUST_UNMUTE,
                0,
            )
        }
    }

    override fun screenBrightness(): Float {
        val systemBrightness = DeviceLevels.systemBrightnessToFraction(
            readSystemBrightness(),
        )
        // Once system access is granted, the system value is authoritative so
        // external brightness changes are reflected by the sliders. Clear any
        // window override left over from the pre-permission fallback so those
        // external changes also affect the actual panel.
        if (canWriteSystemBrightness()) {
            clearWindowOverride()
            return systemBrightness
        }
        val activity = currentActivity() ?: return systemBrightness
        return DeviceLevels.windowBrightnessToFraction(
            activity.window.attributes.screenBrightness,
        ) ?: systemBrightness
    }

    override fun setScreenBrightness(fraction: Float) {
        val clamped = fraction.coerceIn(0.01f, 1f)
        val systemApplied = if (canWriteSystemBrightness()) {
            runCatching {
                // Manual mode makes the requested brightness effective even
                // when adaptive brightness was enabled.
                Settings.System.putInt(
                    application.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL,
                )
            }
            runCatching {
                Settings.System.putInt(
                    application.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    DeviceLevels.fractionToSystemBrightness(clamped),
                )
            }.getOrDefault(false)
        } else {
            false
        }

        // When the global write succeeded, leave brightness under system
        // control so hardware/system changes remain visible. If it did not,
        // preserve the window-scoped fallback.
        currentActivity()?.let { activity ->
            val params = activity.window.attributes
            params.screenBrightness = if (systemApplied) {
                WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            } else {
                clamped
            }
            activity.window.attributes = params
        }
    }

    override fun canWriteSystemBrightness(): Boolean =
        runCatching { Settings.System.canWrite(application) }.getOrDefault(false)

    private fun clearWindowOverride() {
        currentActivity()?.let { activity ->
            val params = activity.window.attributes
            if (params.screenBrightness != WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE) {
                params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                activity.window.attributes = params
            }
        }
    }

    override fun observeChanges(): Flow<Unit> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                trySend(Unit)
            }
        }
        val receiverRegistered = runCatching {
            ContextCompat.registerReceiver(
                application,
                receiver,
                IntentFilter("android.media.VOLUME_CHANGED_ACTION"),
                ContextCompat.RECEIVER_NOT_EXPORTED,
            )
        }.isSuccess

        val brightnessObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }
        }
        val resolver = application.contentResolver
        runCatching {
            resolver.registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS),
                false,
                brightnessObserver,
            )
            resolver.registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE),
                false,
                brightnessObserver,
            )
        }

        // System providers do not always broadcast external brightness writes,
        // and app-op grants have no change callback. A light poll keeps the
        // visible sliders aligned while this flow is collected.
        launch {
            while (true) {
                delay(1_000)
                trySend(Unit)
            }
        }

        awaitClose {
            if (receiverRegistered) {
                runCatching { application.unregisterReceiver(receiver) }
            }
            runCatching { resolver.unregisterContentObserver(brightnessObserver) }
        }
    }

    private fun readSystemBrightness(): Int = runCatching {
        Settings.System.getInt(
            application.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS,
        )
    }.getOrDefault(128)

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
