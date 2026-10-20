package com.pratap.vibhor.core.platform

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Actual platform state for the shortcuts shown on the unified apps screen. */
data class QuickControlsState(
    val wifiSupported: Boolean = false,
    val wifiEnabled: Boolean = false,
    val bluetoothSupported: Boolean = false,
    val bluetoothEnabled: Boolean = false,
)

/**
 * Small, Android-only surface around the two connectivity toggles. Kept
 * behind an interface so ViewModel behavior can be tested without system
 * services and so unavailable hardware is reported instead of faked.
 */
interface QuickControls {
    fun state(): QuickControlsState

    /** Requests a Wi-Fi state change; the platform may reject it. */
    fun setWifiEnabled(enabled: Boolean)

    /** Requests a Bluetooth state change; the platform may reject it. */
    fun setBluetoothEnabled(enabled: Boolean)

    /** Emits when either radio reports a state change. */
    fun observeChanges(): Flow<Unit>
}

/**
 * Android 9-compatible implementation.
 *
 * Wi-Fi and Bluetooth toggles use the legacy normal permissions, which are
 * usable on API 28 (the target head unit). Android 10+ restricts Wi-Fi
 * toggling and Android 12+ requires runtime Bluetooth permissions, so calls
 * are wrapped in [runCatching]: the UI keeps showing the real state and never
 * claims a toggle succeeded when the platform refused it.
 */
class AndroidQuickControls(
    private val application: Application,
) : QuickControls {

    private val wifiManager: WifiManager? =
        application.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    private val bluetoothAdapter: BluetoothAdapter? =
        (application.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)
            ?.adapter
            ?: @Suppress("DEPRECATION") BluetoothAdapter.getDefaultAdapter()

    @SuppressLint("MissingPermission")
    override fun state(): QuickControlsState = QuickControlsState(
        wifiSupported = wifiManager != null,
        wifiEnabled = runCatching { wifiManager?.isWifiEnabled == true }
            .getOrDefault(false),
        bluetoothSupported = bluetoothAdapter != null,
        bluetoothEnabled = runCatching { bluetoothAdapter?.isEnabled == true }
            .getOrDefault(false),
    )

    @Suppress("DEPRECATION")
    override fun setWifiEnabled(enabled: Boolean) {
        val manager = wifiManager ?: return
        runCatching { manager.isWifiEnabled = enabled }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    override fun setBluetoothEnabled(enabled: Boolean) {
        val adapter = bluetoothAdapter ?: return
        runCatching {
            if (enabled) adapter.enable() else adapter.disable()
        }
    }

    override fun observeChanges(): Flow<Unit> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                trySend(Unit)
            }
        }
        val filter = IntentFilter().apply {
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        }
        val registered = runCatching {
            ContextCompat.registerReceiver(
                application,
                receiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED,
            )
        }.isSuccess
        if (!registered) {
            close()
            return@callbackFlow
        }
        awaitClose {
            runCatching { application.unregisterReceiver(receiver) }
        }
    }
}
