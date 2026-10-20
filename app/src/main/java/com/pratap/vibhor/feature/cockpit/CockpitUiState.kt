package com.pratap.vibhor.feature.cockpit

import com.pratap.vibhor.core.platform.LauncherBackground
import com.pratap.vibhor.core.platform.LauncherSettings

data class CockpitUiState(
    val timeText: String = "",
    val dateText: String = "",
    val volumeLevel: Float = 0.5f,
    val volumeMuted: Boolean = false,
    val brightnessLevel: Float = 0.5f,
    val brightnessPermissionGranted: Boolean = false,
    val wifiSupported: Boolean = false,
    val wifiEnabled: Boolean = false,
    val bluetoothSupported: Boolean = false,
    val bluetoothEnabled: Boolean = false,
    val nightMode: Boolean = false,
    val background: LauncherBackground = LauncherBackground.DEFAULT,
    val backgroundOpacity: Float = LauncherSettings.DEFAULT_BACKGROUND_OPACITY,
)
