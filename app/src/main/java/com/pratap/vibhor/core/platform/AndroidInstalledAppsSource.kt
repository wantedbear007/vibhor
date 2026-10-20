package com.pratap.vibhor.core.platform

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import java.text.Collator
import kotlin.math.roundToInt

class AndroidInstalledAppsSource(
    private val context: Context,
    private val ownPackageName: String = context.packageName,
) : InstalledAppsSource {

    override fun apps(): Flow<List<InstalledApp>> = channelFlow {
        val reload = Channel<Unit>(capacity = Channel.CONFLATED)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                reload.trySend(Unit)
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }

        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )

        try {
            send(loadInstalledApps())
            while (currentCoroutineContext().isActive) {
                reload.receive()
                send(loadInstalledApps())
            }
        } finally {
            context.unregisterReceiver(receiver)
        }
    }.flowOn(Dispatchers.IO)

    private fun loadInstalledApps(): List<InstalledApp> {
        val packageManager = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        // Do not require CATEGORY_DEFAULT here: many launcher activities only
        // declare MAIN + LAUNCHER, and the system launcher lists them anyway.
        val resolveInfos = packageManager.queryIntentActivities(mainIntent, 0)
        val collator = Collator.getInstance()
        return resolveInfos
            .asSequence()
            .filter { it.activityInfo?.packageName != ownPackageName }
            .mapNotNull { info ->
                val packageName = info.activityInfo?.packageName ?: return@mapNotNull null
                val activityName = info.activityInfo?.name ?: return@mapNotNull null
                InstalledApp(
                    packageName = packageName,
                    activityName = activityName,
                    label = info.loadLabel(packageManager).toString(),
                    icon = runCatching {
                        info.loadIcon(packageManager)?.toScaledBitmap()
                    }.getOrNull(),
                )
            }
            .sortedWith(compareBy(collator) { it.label })
            .toList()
    }

    private companion object {
        const val ICON_SIZE_PX = 96

        fun Drawable.toScaledBitmap(targetPx: Int = ICON_SIZE_PX): Bitmap? {
            val width = intrinsicWidth.takeIf { it > 0 } ?: return null
            val height = intrinsicHeight.takeIf { it > 0 } ?: return null
            val scale = targetPx.toFloat() / maxOf(width, height)
            val outWidth = (width * scale).roundToInt().coerceIn(1, targetPx)
            val outHeight = (height * scale).roundToInt().coerceIn(1, targetPx)
            val bitmap = createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888)
            setBounds(0, 0, outWidth, outHeight)
            draw(Canvas(bitmap))
            return bitmap
        }
    }
}
