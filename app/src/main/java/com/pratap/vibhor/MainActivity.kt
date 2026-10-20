package com.pratap.vibhor

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pratap.vibhor.core.platform.SharedPreferencesSettingsStore
import com.pratap.vibhor.core.ui.theme.VibhorTheme

class MainActivity : ComponentActivity() {

    private var homeRequestToken by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )

        setContent {
            // The persisted accent drives the entire launcher theme, so every
            // screen (and the full-screen settings overlay) shares one accent.
            val settingsStore = remember {
                SharedPreferencesSettingsStore.get(applicationContext)
            }
            val settings by settingsStore.settings.collectAsStateWithLifecycle()
            VibhorTheme(accentColor = settings.accentColor) {
                VibhorApp(
                    homeRequestToken = homeRequestToken,
                    startOnHomeTarget = intent.hasCategory(Intent.CATEGORY_HOME),
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.hasCategory(Intent.CATEGORY_HOME)) {
            homeRequestToken++
        }
    }
}
