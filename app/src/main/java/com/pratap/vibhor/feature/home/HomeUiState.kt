package com.pratap.vibhor.feature.home

import com.pratap.vibhor.core.platform.ClockFace

data class HomeUiState(
    val timeText: String = "",
    val dateText: String = "",
    val clockScale: Float = 1f,
    val clockFace: ClockFace = ClockFace.DEFAULT,
    val hour: Int = 0,
    val minute: Int = 0,
    val second: Int = 0,
    val hourText: String = "",
    val minuteText: String = "",
    val meridiemText: String = "",
)
