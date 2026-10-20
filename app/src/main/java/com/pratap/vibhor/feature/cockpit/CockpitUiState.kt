package com.pratap.vibhor.feature.cockpit

data class CockpitUiState(
    val timeText: String = "",
    val dateText: String = "",
    val volumeLevel: Float = 0.5f,
    val brightnessLevel: Float = 0.5f,
)
