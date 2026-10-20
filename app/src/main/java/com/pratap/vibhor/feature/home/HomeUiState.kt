package com.pratap.vibhor.feature.home

import com.pratap.vibhor.core.platform.ClockFace

data class HomeUiState(
    val timeText: String = "",
    val dateText: String = "",
    val clockScale: Float = 1f,
    val clockFace: ClockFace = ClockFace.DEFAULT,
    val clockBlurRadius: Int = 0,
    val backgroundDarkness: Float = 0f,
    val hour: Int = 0,
    val minute: Int = 0,
    val second: Int = 0,
    val hourText: String = "",
    val minuteText: String = "",
    val meridiemText: String = "",
    val quickButtonScale: Float = 1f,
    val favourites: List<com.pratap.vibhor.core.platform.InstalledApp> = emptyList(),
)
