package com.pratap.vibhor.core.platform

import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow

data class InstalledApp(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: Bitmap? = null,
)

/**
 * Provides the list of launcher-visible installed apps and re-emits the list
 * when relevant package changes occur. The full list is never loaded from the
 * main thread and is never re-scanned on recomposition.
 */
interface InstalledAppsSource {
    fun apps(): Flow<List<InstalledApp>>
}
