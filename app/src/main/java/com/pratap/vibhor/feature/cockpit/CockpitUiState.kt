package com.pratap.vibhor.feature.cockpit

import com.pratap.vibhor.core.platform.NavigationApp

data class CockpitUiState(
    val timeText: String = "",
    val dateText: String = "",
    val navigationApp: NavigationApp? = null,
)
