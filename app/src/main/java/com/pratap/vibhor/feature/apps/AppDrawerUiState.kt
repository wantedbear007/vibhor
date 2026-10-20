package com.pratap.vibhor.feature.apps

import com.pratap.vibhor.core.platform.InstalledApp

data class AppDrawerUiState(
    val apps: List<InstalledApp> = emptyList(),
    val isLoading: Boolean = true,
    val iconScale: Float = 1f,
)
