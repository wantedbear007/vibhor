package com.pratap.vibhor.feature.music

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pratap.vibhor.core.platform.media.AndroidMediaControls
import com.pratap.vibhor.core.platform.media.MediaControls
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Bridges the platform media sessions into the Music screen. The view model
 * never invents playback state: when the session layer reports nothing, the
 * screen shows its idle state.
 */
class MusicViewModel(private val mediaControls: MediaControls) : ViewModel() {

    private val access = MutableStateFlow(mediaControls.hasAccess())

    val uiState: StateFlow<MusicUiState> = combine(
        mediaControls.nowPlaying,
        access,
    ) { track, hasAccess ->
        MusicUiState(hasAccess = hasAccess, track = track)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = MusicUiState(
            hasAccess = access.value,
            track = mediaControls.nowPlaying.value,
        ),
    )

    init {
        refreshAccess()
    }

    /** Called when the screen resumes, e.g. back from notification settings. */
    fun refreshAccess() {
        access.value = mediaControls.hasAccess()
        mediaControls.refresh()
    }

    fun playPause() {
        mediaControls.playPause()
    }

    fun next() {
        mediaControls.next()
    }

    fun previous() {
        mediaControls.previous()
    }

    override fun onCleared() {
        mediaControls.release()
    }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L

        val Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                MusicViewModel(AndroidMediaControls(application))
            }
        }
    }
}
