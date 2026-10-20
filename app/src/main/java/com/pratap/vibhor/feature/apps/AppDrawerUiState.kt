package com.pratap.vibhor.feature.apps

import com.pratap.vibhor.core.platform.InstalledApp

enum class AppDrawerTab {
    Favourites,
    All,
}

data class AppDrawerUiState(
    val apps: List<InstalledApp> = emptyList(),
    val favouriteApps: List<InstalledApp> = emptyList(),
    val favouritePackageNames: Set<String> = emptySet(),
    val selectedTab: AppDrawerTab = AppDrawerTab.All,
    val isLoading: Boolean = true,
    val iconScale: Float = 1f,
    val searchQuery: String = "",
    val displayedApps: List<InstalledApp> = emptyList(),
)
